package com.learning.cryptobot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "Asset")
@NoArgsConstructor
@Data
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private String symbol;

    @Column(precision = 18, scale = 8)
    private BigDecimal quantity;

    public Asset(Long chatId, String symbol, BigDecimal quantity){
        this.chatId = chatId;
        this.symbol = symbol;
        this.quantity = quantity;
    }

}
