import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DependencyService } from '../../services/dependency.service';
import { DependencyReport, Dependency, Vulnerability } from '../../models/dependency';

@Component({
    selector: 'app-dependency-dashboard',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './dependency-dashboard.component.html',
    styleUrls: ['./dependency-dashboard.component.css']
})
export class DependencyDashboardComponent implements OnInit {
    report: DependencyReport | null = null;
    loading = false;
    error: string | null = null;

    // Filtres
    selectedCategory: string = 'all';
    selectedRiskLevel: string = 'all';
    searchTerm: string = '';

    // Track which dependencies have details shown
    private expandedDependencies = new Set<string>();

    constructor(private dependencyService: DependencyService) { }

    ngOnInit(): void {
        this.loadDependencyReport();
    }

    loadDependencyReport(): void {
        this.loading = true;
        this.error = null;

        this.dependencyService.getDependencyReport().subscribe({
            next: (data) => {
                this.report = data;
                this.loading = false;
                console.log('✅ Dependency report loaded:', data);
            },
            error: (err) => {
                this.error = 'Erreur lors du chargement du rapport de dépendances';
                this.loading = false;
                console.error('❌ Error loading dependency report:', err);
            }
        });
    }

    getFilteredDependencies(): Dependency[] {
        if (!this.report) return [];

        let filtered = this.report.dependencies;

        // Filtre par catégorie
        if (this.selectedCategory !== 'all') {
            filtered = filtered.filter(d => d.category === this.selectedCategory);
        }

        // Filtre par niveau de risque
        if (this.selectedRiskLevel !== 'all') {
            filtered = filtered.filter(d => d.riskLevel === this.selectedRiskLevel);
        }

        // Filtre par recherche
        if (this.searchTerm) {
            const term = this.searchTerm.toLowerCase();
            filtered = filtered.filter(d =>
                d.artifactId.toLowerCase().includes(term) ||
                d.groupId.toLowerCase().includes(term)
            );
        }

        return filtered;
    }

    getRiskClass(riskLevel: string): string {
        const classes: { [key: string]: string } = {
            'CRITICAL': 'risk-critical',
            'HIGH': 'risk-high',
            'MEDIUM': 'risk-medium',
            'LOW': 'risk-low',
            'NONE': 'risk-none'
        };
        return classes[riskLevel] || 'risk-none';
    }

    getSeverityClass(severity: string): string {
        const classes: { [key: string]: string } = {
            'CRITICAL': 'severity-critical',
            'HIGH': 'severity-high',
            'MEDIUM': 'severity-medium',
            'LOW': 'severity-low'
        };
        return classes[severity] || 'severity-low';
    }

    getOverallRiskClass(): string {
        if (!this.report) return 'risk-none';
        return this.getRiskClass(this.report.overallRiskLevel);
    }

    refresh(): void {
        this.loadDependencyReport();
    }

    toggleDetails(dep: Dependency): void {
        const key = `${dep.groupId}:${dep.artifactId}:${dep.version}`;
        if (this.expandedDependencies.has(key)) {
            this.expandedDependencies.delete(key);
        } else {
            this.expandedDependencies.add(key);
        }
    }

    isDetailsShown(dep: Dependency): boolean {
        const key = `${dep.groupId}:${dep.artifactId}:${dep.version}`;
        return this.expandedDependencies.has(key);
    }
}
