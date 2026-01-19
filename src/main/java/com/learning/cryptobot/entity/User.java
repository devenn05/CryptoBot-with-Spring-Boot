package com.learning.cryptobot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "Assets")
@NoArgsConstructor
public class User {

    @Id
    private Long chatId;
    private String name;

    @Column(precision = 18, scale = 2)
    private BigDecimal balance;

}
