export interface VendeurStatDto {
    vendeurName: string;
    invoicesCount: number;
    totalRevenue: number;
}

export interface DashboardStatsDto {
    totalProducts: number;
    totalStock: number;
    sellerStats: VendeurStatDto[];
}
