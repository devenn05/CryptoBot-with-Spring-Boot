package com.learning.cryptobot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "Asset") // The name of the table in H2 Database
@NoArgsConstructor
@Data
public class Asset {

    @Id
    // @GeneratedValue means the DB automatically counts up: 1, 2, 3...
    // You never have to set this manually.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    private String symbol; // e.g., "BTC", "ETH"

    // precision = 18 (total digits), scale = 8 (digits after decimal)
    // Example: 123456.99456789 fits.
    @Column(precision = 18, scale = 8)
    private BigDecimal quantity;

    // Custom constructor to make creating new assets easy in your Service layer
    public Asset(Long chatId, String symbol, BigDecimal quantity){
        this.chatId = chatId;
        this.symbol = symbol;
        this.quantity = quantity;
    }

}