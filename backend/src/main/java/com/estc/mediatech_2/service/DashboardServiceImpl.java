package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dao.FactureDao;
import com.estc.mediatech_2.dao.ProduitDao;
import com.estc.mediatech_2.dto.DashboardStatsDto;
import com.estc.mediatech_2.dto.VendeurStatDto;
import com.estc.mediatech_2.models.FactureEntity;
import com.estc.mediatech_2.models.ProduitEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProduitDao produitDao;
    private final FactureDao factureDao;
    private final SecurityAnalyticsService securityAnalyticsService;

    @Override
    public DashboardStatsDto getAdminStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        // 1. Product Stats
        List<ProduitEntity> allProducts = produitDao.findAll();
        stats.setTotalProducts(allProducts.size());

        long totalStock = allProducts.stream()
                .mapToLong(p -> p.getQte_stock() != null ? p.getQte_stock() : 0)
                .sum();
        stats.setTotalStock(totalStock);

        // 2. Seller Stats
        List<FactureEntity> allFactures = factureDao.findAll();

        Map<String, SellerAggregator> aggregatorMap = new HashMap<>();

        for (FactureEntity f : allFactures) {
            if (f.getVendeur() == null)
                continue; // Skip if no seller linked (e.g. old data)

            String sellerName = f.getVendeur().getUsername(); // Or full name
            aggregatorMap.putIfAbsent(sellerName, new SellerAggregator());

            SellerAggregator ag = aggregatorMap.get(sellerName);
            ag.invoiceCount++;

            // Calculate revenue for this invoice
            BigDecimal invoiceTotal = f.getLigneFactures().stream()
                    .map(l -> l.getProduit().getPrix_unitaire().multiply(BigDecimal.valueOf(l.getQuantite())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            ag.totalRevenue = ag.totalRevenue.add(invoiceTotal);
        }

        List<VendeurStatDto> sellerStats = aggregatorMap.entrySet().stream()
                .map(entry -> new VendeurStatDto(entry.getKey(), entry.getValue().invoiceCount,
                        entry.getValue().totalRevenue))
                .collect(Collectors.toList());

        stats.setSellerStats(sellerStats);

        // 3. Security Stats
        stats.setVulnerabilityCount(securityAnalyticsService.getVulnerabilityReport().size());

        return stats;
    }

    private static class SellerAggregator {
        long invoiceCount = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
    }
}
