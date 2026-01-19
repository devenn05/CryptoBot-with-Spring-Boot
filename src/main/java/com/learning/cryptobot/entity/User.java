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
@Table(name = "Assets") // **Note**: Sets the table name in DB to "Assets".
@NoArgsConstructor
public class User {

    @Id
    private Long chatId;

    private String name;

    // precision = 18 (total digits), scale = 2 (digits after decimal)
    // Example: 123456.99 fits.
    @Column(precision = 18, scale = 2)
    private BigDecimal balance;

}