import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SecurityService, UserRiskProfile, InvoiceStats, VulnerabilityReport } from '../../services/security.service';

@Component({
    selector: 'app-security-console',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './security-console.component.html',
    styleUrls: ['./security-console.component.css']
})
export class SecurityConsoleComponent implements OnInit {
    riskyUsers: UserRiskProfile[] = [];
    invoiceStats: InvoiceStats | null = null;
    vulnerabilities: VulnerabilityReport[] = [];
    loading = true;

    constructor(private securityService: SecurityService) { }

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.loading = true;

        // ForkJoin could be used here, but simple sequential/parallel calls are fine for now
        this.securityService.getRiskyUsers().subscribe({
            next: (data) => this.riskyUsers = data,
            error: (e) => console.error(e)
        });

        this.securityService.getInvoiceStats().subscribe({
            next: (data) => this.invoiceStats = data,
            error: (e) => console.error(e)
        });

        this.securityService.getVulnerabilities().subscribe({
            next: (data) => this.vulnerabilities = data,
            error: (e) => console.error(e),
            complete: () => this.loading = false
        });
    }

    getSeverityClass(level: string): string {
        switch (level) {
            case 'CRITICAL': return 'bg-red-600 text-white';
            case 'HIGH': return 'bg-orange-500 text-white';
            case 'MEDIUM': return 'bg-yellow-500 text-black';
            case 'LOW': return 'bg-blue-500 text-white';
            default: return 'bg-gray-500';
        }
    }
}
