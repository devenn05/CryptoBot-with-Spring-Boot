package com.learning.cryptobot.repository;

import com.learning.cryptobot.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    //Doubt
    Optional<Asset> findByChatIdAndSymbol(Long chatId, String symbol);
    List<Asset> findByChatId(Long chatId);


}
