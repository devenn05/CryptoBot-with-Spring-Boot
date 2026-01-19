package com.learning.cryptobot.service;

import com.learning.cryptobot.dto.BinanceTickerDto;
import com.learning.cryptobot.entity.Asset;
import com.learning.cryptobot.entity.User;
import com.learning.cryptobot.repository.AssetRepository;
import com.learning.cryptobot.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TradingService {
    private final BinancePriceService binancePriceService;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    public TradingService(BinancePriceService binancePriceService, UserRepository userRepository, AssetRepository assetRepository) {
        this.binancePriceService = binancePriceService;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
    }

    // For Webpage
    public User getUser(Long id) {
        return getOrCreateUser(id, "WebUser");
    }
    public List<Asset> getUserAssets(Long id) {
        return assetRepository.findByChatId(id);
    }


    private String formatCurrency(BigDecimal amount) {
        return "$" + amount.setScale(5, RoundingMode.HALF_UP).toPlainString();
    }

    private String symbolFormat(String cleanSymbol) {
        cleanSymbol = cleanSymbol.toUpperCase();
        if (!cleanSymbol.endsWith("USDT")) {
            return cleanSymbol + "USDT";
        } else return cleanSymbol;
    }

    public User getOrCreateUser(Long userId, String userName) {
        return userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User();
            newUser.setChatId(userId);
            newUser.setName(userName);
            newUser.setBalance(new BigDecimal("1000.00"));
            return userRepository.save(newUser);
        });
    }

    private Asset getOrCreateAsset(Long userId, String symbol) {
        return assetRepository.findByChatIdAndSymbol(userId, symbol).orElse(new Asset(userId, symbol, BigDecimal.ZERO));
    }

    private BigDecimal getMarketPrice(String symbol) {
        BinanceTickerDto ticker = binancePriceService.getData(symbol);
        return ticker.getLastPrice();
    }

    private String formatTradeReport(String action, String symbol, BigDecimal qty, BigDecimal price, BigDecimal totalValue, BigDecimal newBalance) {
        return "✅ **" + action + " " + symbol + "**\n" +
                "Qty: " + qty + "\n" +
                "At Price: " + formatCurrency(price) + "\n" +
                "Total Value: " + formatCurrency(totalValue) + "\n" +
                "New Balance: " + formatCurrency(newBalance);
    }


    @Transactional
    public String buyCrypto(Long id, String username, String symbol, BigDecimal quantity){
        symbol = symbolFormat(symbol);
        User user = getOrCreateUser(id,username);

        BigDecimal currPrice = getMarketPrice(symbol);
        BigDecimal cost = currPrice.multiply(quantity);

        if (user.getBalance().compareTo(cost) < 0 ) return "Insufficent Funds";

        user.setBalance(user.getBalance().subtract(cost));
        userRepository.save(user);

        Asset asset = getOrCreateAsset(id, symbol);
        asset.setQuantity(asset.getQuantity().add(quantity));
        assetRepository.save(asset);

        return formatTradeReport("BOUGHT", symbol, quantity, currPrice, cost, user.getBalance());
    }

    @Transactional
    public String sellCrypto(Long id, String username, String symbol, BigDecimal quantity){
        symbol = symbolFormat(symbol);

        User user = getOrCreateUser(id, username);
        Asset asset = assetRepository.findByChatIdAndSymbol(id, symbol).orElse(null);

        if (asset == null || asset.getQuantity().compareTo(quantity) < 0) return "You dont have enough Asset.";

        BigDecimal currPrice = getMarketPrice(symbol);
        BigDecimal cost = currPrice.multiply(quantity);

        user.setBalance(user.getBalance().add(cost));
        userRepository.save(user);

        BigDecimal newQuantity = asset.getQuantity().subtract(quantity);
        if (newQuantity.equals(BigDecimal.ZERO)){
            assetRepository.delete(asset);
        } else {
            asset.setQuantity(newQuantity);
            assetRepository.save(asset);
        }
        return formatTradeReport("SOLD", symbol, quantity, currPrice, cost, user.getBalance());
    }

    public String getWallet(Long id){
        User user = userRepository.findById(id).orElse(null);
        if (user ==null) return "No user found, no Assets.";
        StringBuilder sb = new StringBuilder();
        sb.append("👛 **YOUR WALLET**\n\n");
        sb.append("💵 **USD Balance:** $").append(user.getBalance().setScale(2, RoundingMode.HALF_UP)).append("\n");

        List<Asset> assets = assetRepository.findByChatId(id);
        if (assets.isEmpty()) {
            sb.append("\n🚫 No Crypto Assets held.");
        } else {
            sb.append("\n🏆 **Crypto Holdings:**\n");
            for (Asset a : assets) {
                sb.append("- ").append(a.getSymbol()).append(": ")
                        .append(a.getQuantity().stripTrailingZeros().toPlainString()).append("\n");
            }
        }
        return sb.toString();
    }


}