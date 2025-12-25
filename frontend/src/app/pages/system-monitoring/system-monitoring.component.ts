import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SystemMonitoringService, SystemMetrics, PortStatus } from '../../services/system-monitoring.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-system-monitoring',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="monitoring-container">
      <div class="header">
        <div class="header-main">
          <h1><span class="prefix">#</span> SYSTEM_GUARDIAN_SOC</h1>
          <p>Real-time Intelligence & Infrastructure Control</p>
        </div>
        <div class="tabs">
          <button [class.active]="activeTab === 'metrics'" (click)="activeTab = 'metrics'">📊 CORE_METRICS</button>
          <button [class.active]="activeTab === 'config'" (click)="activeTab = 'config'">🌍 CONFIG_EXPLORER</button>
          <button [class.active]="activeTab === 'beans'" (click)="activeTab = 'beans'">🧩 BEAN_INSPECTOR</button>
        </div>
      </div>

      <!-- TAB: CORE METRICS -->
      <div class="tab-content" *ngIf="activeTab === 'metrics'">
        <div class="grid">
          <!-- CPU & RAM Metrics -->
          <div class="card metrics-card">
            <div class="card-header">
              <h3>🖥️ CPU_RAM_RESOURCES</h3>
              <div class="status-container">
                <span class="health-label" [class.up]="systemHealth?.status === 'UP'">
                  {{ systemHealth?.status || 'PENDING' }}
                </span>
                <div class="status-pulse" [class.red]="systemHealth?.status !== 'UP'"></div>
              </div>
            </div>
            <div class="metrics-grid" *ngIf="metrics">
              <div class="metric-item">
                <span class="label">JVM_MEMORY</span>
                <div class="progress-bar">
                  <div class="progress" [style.width.%]="(metrics.jvm_memory_used / metrics.jvm_memory_max) * 100"></div>
                </div>
                <span class="value">{{ formatBytes(metrics.jvm_memory_used) }} / {{ formatBytes(metrics.jvm_memory_max) }}</span>
              </div>
              <div class="metric-item">
                <span class="label">CPU_LOAD (PROCESS)</span>
                <div class="progress-bar">
                  <div class="progress yellow" [style.width.%]="metrics.process_cpu_usage * 100"></div>
                </div>
                <span class="value">{{ (metrics.process_cpu_usage * 100).toFixed(2) }}%</span>
              </div>
              <div class="metric-item">
                <span class="label">CPU_LOAD (SYSTEM)</span>
                <div class="progress-bar">
                  <div class="progress green" [style.width.%]="metrics.system_cpu_usage * 100"></div>
                </div>
                <span class="value">{{ (metrics.system_cpu_usage * 100).toFixed(2) }}%</span>
              </div>
              <div class="metric-item">
                <span class="label">LIVE_THREADS</span>
                <span class="value large">{{ metrics.jvm_threads_live }}</span>
              </div>
            </div>
          </div>

          <!-- Port Scanner -->
          <div class="card scanner-card">
            <div class="card-header">
              <h3>📡 NETWORK_SURFACE_SCAN</h3>
              <button class="btn-scan" (click)="startScan()" [disabled]="scanning">
                {{ scanning ? 'SCANNING...' : 'EXECUTE_SCAN' }}
              </button>
            </div>
            
            <div class="scan-controls">
              <div class="input-group">
                <label>START_PORT</label>
                <input type="number" [(ngModel)]="startPort" [disabled]="scanning">
              </div>
              <div class="input-group">
                <label>END_PORT</label>
                <input type="number" [(ngModel)]="endPort" [disabled]="scanning">
              </div>
            </div>

            <div class="scan-results">
              <div class="scan-line" *ngIf="scanning"></div>
              <div class="port-list">
                <div *ngFor="let p of openPorts" class="port-item open">
                  <span class="port-num">PORT {{ p.port }}</span>
                  <span class="port-service">{{ p.service }}</span>
                  <span class="port-status">OPEN</span>
                </div>
                <div *ngIf="openPorts.length === 0 && !scanning" class="no-results">
                  No open ports detected in this range.
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB: CONFIG EXPLORER -->
      <div class="tab-content" *ngIf="activeTab === 'config'">
        <div class="card env-explorer">
          <div class="card-header">
            <h3>🌍 CONFIGURATION_MAP</h3>
            <div class="search-box">
              <input type="text" [(ngModel)]="envSearchTerm" placeholder="Filter property keys...">
            </div>
          </div>
          
          <div class="env-sources-container">
            <div *ngFor="let source of filteredEnvSources" class="source-group">
              <div class="source-header" (click)="source.expanded = !source.expanded">
                <span class="source-name">{{ source.name }}</span>
                <span class="prop-count">{{ getPropertyCount(source) }} properties</span>
                <span class="toggle-icon">{{ source.expanded ? '▼' : '▶' }}</span>
              </div>
              
              <div class="source-properties" *ngIf="source.expanded">
                <table class="nested-table">
                  <tbody>
                    <tr *ngFor="let prop of getFilteredProperties(source)">
                      <td class="prop-key">{{ prop.key }}</td>
                      <td class="prop-value">
                        <span [class.masked]="prop.value === '******'">{{ prop.value }}</span>
                        <span *ngIf="prop.origin" class="origin-hint" [title]="prop.origin">ℹ️</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB: BEAN INSPECTOR -->
      <div class="tab-content" *ngIf="activeTab === 'beans'">
        <div class="card bean-explorer">
          <div class="card-header">
            <h3>🧩 SPRING_IOC_BEANS</h3>
            <div class="search-box">
              <input type="text" [(ngModel)]="beanSearchTerm" placeholder="Search bean name or type...">
            </div>
          </div>

          <div class="bean-list-container">
            <div class="bean-summary">
                Total Beans: {{ totalBeanCount }} | Displaying: {{ filteredBeans.length }}
            </div>
            <table class="actuator-table">
              <thead>
                <tr>
                  <th>Bean Name</th>
                  <th>Type & Scope</th>
                  <th>Source / Resource</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let bean of filteredBeans">
                  <td class="bean-name">
                    {{ bean.name }}
                  </td>
                  <td>
                    <div class="bean-type">{{ bean.type }}</div>
                    <div class="bean-scope" [class.singleton]="bean.scope === 'singleton'">{{ bean.scope }}</div>
                  </td>
                  <td class="bean-resource">
                    {{ bean.resource || 'Dynamic / Java Config' }}
                    <div class="bean-deps" *ngIf="bean.dependencies?.length">
                        <span class="dep-label">Deps:</span> {{ bean.dependencies.join(', ') }}
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      --neon-green: #00ff9d;
      --neon-yellow: #facc15;
      --glass-bg: rgba(15, 15, 15, 0.7);
      --glass-border: rgba(0, 255, 157, 0.15);
      --bg-color: #050505;
    }

    .monitoring-container {
      padding: 3rem;
      background: radial-gradient(circle at top right, #0a1a14, #050505 50%);
      min-height: 100vh;
      color: #eee;
      font-family: 'JetBrains Mono', 'Inter', monospace;
      position: relative;
      overflow-x: hidden;
    }

    /* Background Animation */
    .monitoring-container::before {
      content: '';
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      background-image: 
        linear-gradient(rgba(0, 255, 157, 0.05) 1px, transparent 1px),
        linear-gradient(90deg, rgba(0, 255, 157, 0.05) 1px, transparent 1px);
      background-size: 50px 50px;
      pointer-events: none;
      z-index: 0;
    }

    .header {
      margin-bottom: 3rem;
      padding-left: 2rem;
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      position: relative;
      z-index: 1;
      animation: fadeInDown 0.8s ease-out;
    }

    .header::after {
      content: '';
      position: absolute;
      left: 0; top: 0; bottom: 0;
      width: 4px;
      background: var(--neon-green);
      box-shadow: 0 0 15px var(--neon-green);
    }

    .tabs {
      display: flex;
      gap: 1rem;
    }

    .tabs button {
      background: rgba(255, 255, 255, 0.03);
      backdrop-filter: blur(5px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      color: #888;
      padding: 0.8rem 1.5rem;
      font-family: inherit;
      font-size: 0.85rem;
      cursor: pointer;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      letter-spacing: 1px;
    }

    .tabs button:hover {
      border-color: var(--neon-green);
      color: #ccc;
    }

    .tabs button.active {
      background: rgba(0, 255, 157, 0.15);
      color: var(--neon-green);
      border-color: var(--neon-green);
      box-shadow: 0 0 20px rgba(0, 255, 157, 0.2);
      font-weight: bold;
      transform: translateY(-2px);
    }

    .tab-content {
      position: relative;
      z-index: 1;
    }

    .grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 2.5rem;
    }

    /* Glassmorphism Cards */
    .card {
      background: var(--glass-bg);
      backdrop-filter: blur(12px);
      border: 1px solid var(--glass-border);
      padding: 2rem;
      position: relative;
      transition: all 0.4s ease;
      animation: fadeInUp 0.6s ease-out backwards;
      box-shadow: 0 10px 30px rgba(0,0,0,0.5);
    }

    .card:hover {
      border-color: rgba(0, 255, 157, 0.4);
      box-shadow: 0 0 40px rgba(0, 255, 157, 0.1);
      transform: translateY(-5px);
    }

    .card.full-width { grid-column: 1 / -1; }

    /* Staggered entry */
    .metrics-card { animation-delay: 0.1s; }
    .scanner-card { animation-delay: 0.2s; }
    .env-explorer { animation-delay: 0.1s; }
    .bean-explorer { animation-delay: 0.1s; }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2.5rem;
      border-bottom: 1px solid rgba(255,255,255,0.05);
      padding-bottom: 1rem;
    }

    .card-header h3 {
      margin: 0;
      font-size: 1.1rem;
      color: #aaa;
      letter-spacing: 2px;
      text-transform: uppercase;
    }

    .status-container { display: flex; align-items: center; gap: 1rem; }
    .health-label { font-size: 0.75rem; color: #ff5252; font-weight: bold; }
    .health-label.up { color: var(--neon-green); text-shadow: 0 0 10px var(--neon-green); }

    .metrics-grid { display: flex; flex-direction: column; gap: 2rem; }
    .metric-item { display: flex; flex-direction: column; gap: 0.8rem; }
    .metric-item .label { font-size: 0.7rem; color: #666; letter-spacing: 1px; }
    .metric-item .value { font-size: 1rem; color: var(--neon-green); }
    .metric-item .value.large { font-size: 2rem; font-weight: 800; text-shadow: 0 0 15px rgba(0, 255, 157, 0.3); }

    .progress-bar { height: 6px; background: rgba(255,255,255,0.05); width: 100%; border-radius: 3px; overflow: hidden; }
    .progress { height: 100%; background: var(--neon-green); box-shadow: 0 0 15px var(--neon-green); transition: width 1s cubic-bezier(0.1, 0, 0.1, 1); }
    .progress.yellow { background: var(--neon-yellow); box-shadow: 0 0 15px var(--neon-yellow); }

    .status-pulse {
      width: 12px; height: 12px;
      background: var(--neon-green);
      border-radius: 50%;
      box-shadow: 0 0 15px var(--neon-green);
      animation: pulse 2s infinite;
    }
    .status-pulse.red { background: #ff5252; box-shadow: 0 0 15px #ff5252; }

    @keyframes pulse {
      0% { transform: scale(1); opacity: 1; box-shadow: 0 0 0 0 rgba(0, 255, 157, 0.4); }
      70% { transform: scale(1.5); opacity: 0; box-shadow: 0 0 0 10px rgba(0, 255, 157, 0); }
      100% { transform: scale(1); opacity: 0; }
    }

    @keyframes fadeInUp {
      from { opacity: 0; transform: translateY(30px); }
      to { opacity: 1; transform: translateY(0); }
    }

    @keyframes fadeInDown {
      from { opacity: 0; transform: translateY(-30px); }
      to { opacity: 1; transform: translateY(0); }
    }

    /* Table Styles */
    .actuator-table { width: 100%; border-collapse: separate; border-spacing: 0 0.5rem; }
    .actuator-table th { text-align: left; color: #555; padding: 1.2rem; font-size: 0.75rem; text-transform: uppercase; }
    .actuator-table td { padding: 1.2rem; background: rgba(255,255,255,0.02); vertical-align: middle; border-top: 1px solid rgba(255,255,255,0.03); }
    .actuator-table tr:hover td { background: rgba(0, 255, 157, 0.05); }

    .ep-url { color: var(--neon-green); font-weight: bold; }
    .btn-scan { background: var(--neon-green); color: #000; border: none; padding: 0.8rem 2rem; font-family: inherit; font-weight: 800; cursor: pointer; transition: all 0.3s; clip-path: polygon(10% 0, 100% 0, 90% 100%, 0% 100%); }
    .btn-scan:hover:not(:disabled) { background: #fff; box-shadow: 0 0 20px #fff; transform: scale(1.05); }
    .btn-scan:disabled { opacity: 0.5; cursor: not-allowed; }

    .search-box input {
      background: rgba(0,0,0,0.3);
      border: 1px solid rgba(255,255,255,0.1);
      color: var(--neon-green);
      padding: 0.7rem 1.2rem;
      font-family: inherit;
      width: 350px;
      font-size: 0.85rem;
      border-radius: 4px;
      transition: all 0.3s;
    }
    .search-box input:focus { border-color: var(--neon-green); box-shadow: 0 0 15px rgba(0, 255, 157, 0.2); outline: none; }

    .env-sources-container, .bean-list-container { max-height: 550px; overflow-y: auto; padding-right: 1rem; }
    .env-sources-container::-webkit-scrollbar, .bean-list-container::-webkit-scrollbar { width: 4px; }
    .env-sources-container::-webkit-scrollbar-thumb, .bean-list-container::-webkit-scrollbar-thumb { background: rgba(0, 255, 157, 0.3); border-radius: 2px; }

    .source-group { margin-bottom: 1rem; border: 1px solid rgba(255,255,255,0.05); }
    .source-header { padding: 1rem; background: rgba(255,255,255,0.03); display: flex; justify-content: space-between; align-items: center; cursor: pointer; transition: background 0.3s; }
    .source-header:hover { background: rgba(255,255,255,0.07); }

    .bean-name { color: var(--neon-green); font-weight: bold; text-shadow: 0 0 10px rgba(0, 255, 157, 0.1); }
    .bean-scope { font-size: 0.65rem; color: #888; border: 1px solid #444; padding: 2px 6px; margin-top: 5px; display: inline-block; border-radius: 2px; }
    .bean-scope.singleton { color: var(--neon-yellow); border-color: var(--neon-yellow); }

    .scan-results { background: rgba(0,0,0,0.4); border: 1px solid rgba(255,255,255,0.05); height: 320px; position: relative; overflow: hidden; padding: 1.5rem; }
    .scan-line { position: absolute; width: 100%; height: 2px; background: linear-gradient(90deg, transparent, var(--neon-green), transparent); animation: scan 3s linear infinite; }
    @keyframes scan { 0% { top: 0; opacity: 1; } 50% { opacity: 0.5; } 100% { top: 100%; opacity: 1; } }
  `]
})
export class SystemMonitoringComponent implements OnInit, OnDestroy {
  activeTab: 'metrics' | 'config' | 'beans' = 'metrics';

  // Metrics Data
  metrics: SystemMetrics | null = null;
  systemHealth: any = null;
  openPorts: PortStatus[] = [];
  scanning = false;
  startPort = 8080;
  endPort = 8100;

  // Config Explorer Data
  envSearchTerm: string = '';
  envSources: any[] = [];

  // Bean Inspector Data
  beanSearchTerm: string = '';
  beans: any[] = [];
  totalBeanCount = 0;

  keyEndpoints = [
    { url: '/actuator/health', value: 'Loading...', description: 'État de santé général', usage: 'Crucial pour Load Balancers' },
    { url: '/actuator/info', value: 'Loading...', description: 'Informations App', usage: 'Version déployée' },
    { url: '/actuator/metrics', value: 'Loading...', description: 'Nombre de métriques', usage: 'Monitoring Prometheus' },
    { url: '/actuator/env', value: 'Loading...', description: 'Propriétés config', usage: 'Débogage configuration' },
    { url: '/actuator/beans', value: 'Loading...', description: 'Beans Spring', usage: 'Analyse conteneur IoC' }
  ];

  private timerSubscription: Subscription | null = null;

  constructor(private monitoringService: SystemMonitoringService) { }

  ngOnInit() {
    this.refreshData();
    this.loadEnvExplorer();
    this.loadBeanInspector();

    // Auto-refresh metrics and health every 5 seconds
    this.timerSubscription = interval(5000).subscribe(() => {
      this.loadMetrics();
      this.loadHealth();
    });
  }

  ngOnDestroy() {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }

  refreshData() {
    this.loadMetrics();
    this.loadHealth();
    this.loadExtraActuators();
  }

  loadMetrics() {
    this.monitoringService.getMetrics().subscribe({
      next: (data) => this.metrics = data,
      error: (err) => console.error('Failed to load metrics', err)
    });
  }

  loadHealth() {
    this.monitoringService.getHealth().subscribe({
      next: (data) => {
        this.systemHealth = data;
        this.updateEndpointValue('/actuator/health', data.status);
      },
      error: (err) => {
        this.systemHealth = { status: 'DOWN' };
        this.updateEndpointValue('/actuator/health', 'DOWN');
      }
    });
  }

  loadExtraActuators() {
    this.monitoringService.getInfo().subscribe(data => {
      const infoStr = data.app ? `${data.app.name} v${data.app.version}` : 'N/A';
      this.updateEndpointValue('/actuator/info', infoStr);
    });

    this.monitoringService.getMetricsList().subscribe(data => {
      this.updateEndpointValue('/actuator/metrics', `${data.names?.length || 0} metrics`);
    });

    this.monitoringService.getEnv().subscribe(data => {
      this.updateEndpointValue('/actuator/env', `${data.propertySources?.length || 0} sources`);
    });
  }

  loadEnvExplorer() {
    this.monitoringService.getEnv().subscribe(data => {
      this.envSources = (data.propertySources || []).map((s: any) => ({
        ...s,
        expanded: s.name.includes('application.properties')
      }));
    });
  }

  loadBeanInspector() {
    this.monitoringService.getBeans().subscribe(data => {
      if (data.contexts) {
        const mainContext = Object.values(data.contexts)[0] as any;
        if (mainContext && mainContext.beans) {
          this.beans = Object.keys(mainContext.beans).map(key => ({
            name: key,
            ...mainContext.beans[key]
          }));
          this.totalBeanCount = this.beans.length;
          this.updateEndpointValue('/actuator/beans', `${this.totalBeanCount} beans`);
        }
      }
    });
  }

  get filteredBeans() {
    if (!this.beanSearchTerm) return this.beans;
    const term = this.beanSearchTerm.toLowerCase();
    return this.beans.filter(b =>
      b.name.toLowerCase().includes(term) ||
      b.type.toLowerCase().includes(term)
    );
  }

  get filteredEnvSources() {
    if (!this.envSearchTerm) return this.envSources;
    const term = this.envSearchTerm.toLowerCase();
    return this.envSources.filter(source => {
      const keys = Object.keys(source.properties || {});
      return keys.some(k => k.toLowerCase().includes(term));
    });
  }

  getPropertyCount(source: any): number {
    return Object.keys(source.properties || {}).length;
  }

  getFilteredProperties(source: any) {
    const props = source.properties || {};
    const term = this.envSearchTerm.toLowerCase();
    return Object.keys(props)
      .filter(k => !term || k.toLowerCase().includes(term))
      .map(k => ({
        key: k,
        value: props[k].value,
        origin: props[k].origin
      }));
  }

  private updateEndpointValue(url: string, value: string) {
    const ep = this.keyEndpoints.find(e => e.url === url);
    if (ep) ep.value = value;
  }

  startScan() {
    this.scanning = true;
    this.openPorts = [];
    this.monitoringService.scanPorts(this.startPort, this.endPort).subscribe({
      next: (data) => {
        this.openPorts = data.filter(p => p.isOpen);
        this.scanning = false;
      },
      error: (err) => {
        console.error('Scan failed', err);
        this.scanning = false;
      }
    });
  }

  formatBytes(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
