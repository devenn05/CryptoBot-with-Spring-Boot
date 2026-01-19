package com.learning.cryptobot.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BinanceTickerDto {
    private String symbol;
    private BigDecimal lastPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private String priceChangePercent;
    private BigDecimal weightedAvgPrice;
    private BigDecimal volume;
}