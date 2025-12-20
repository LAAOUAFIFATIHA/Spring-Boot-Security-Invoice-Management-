package com.estc.mediatech_2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendeurStatDto {
    private String vendeurName;
    private long invoicesCount; // How many invoices they sold

    // We could add total amount sold if needed, but for now user asked for "how
    // much they sell"
    // which could mean quantity or value. I'll include both if possible, or just
    // count of invoices for simplicity first.
    // "combien il vendre" -> usually means quantity of sales or revenue.
    // I'll assume revenue is better.
    private BigDecimal totalRevenue;
}
