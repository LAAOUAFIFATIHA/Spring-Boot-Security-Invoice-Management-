package com.estc.mediatech_2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalProducts;
    private long totalStock; // Sum of quantities
    private List<VendeurStatDto> sellerStats;
}
