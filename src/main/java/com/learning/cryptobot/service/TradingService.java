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

    // --- WEB HELPERS ---

    // Simple wrappers used by WebController to get raw objects for HTML rendering
    public User getUser(Long id) {
        return getOrCreateUser(id, "WebUser");
    }
    public List<Asset> getUserAssets(Long id) {
        // Fetches all crypto holdings for a specific user ID
        return assetRepository.findByChatId(id);
    }

    // --- INTERNAL HELPERS ---

    // Formats money: $123.45678
    private String formatCurrency(BigDecimal amount) {
        return "$" + amount.setScale(5, RoundingMode.HALF_UP).toPlainString();
    }

    // If user types "BTC", this changes it to "BTCUSDT" for Binance API
    private String symbolFormat(String cleanSymbol) {
        cleanSymbol = cleanSymbol.toUpperCase();
        if (!cleanSymbol.endsWith("USDT")) {
            return cleanSymbol + "USDT";
        } else return cleanSymbol;
    }

    // To Auto-Register a new user upon their first interaction
    public User getOrCreateUser(Long userId, String userName) {
        return userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User();
            newUser.setChatId(userId);
            newUser.setName(userName);
            // Default "Simulated Money" for new users
            newUser.setBalance(new BigDecimal("1000.00"));
            return userRepository.save(newUser);
        });
    }

    // Checks if user owns a specific coin (e.g., BTC). If not, returns an empty Asset object with Qty 0.
    private Asset getOrCreateAsset(Long userId, String symbol) {
        return assetRepository.findByChatIdAndSymbol(userId, symbol)
                .orElse(new Asset(userId, symbol, BigDecimal.ZERO));
    }

    // Fetch live price
    private BigDecimal getMarketPrice(String symbol) {
        BinanceTickerDto ticker = binancePriceService.getData(symbol);
        return ticker.getLastPrice();
    }

    // Generates the success message string
    private String formatTradeReport(String action, String symbol, BigDecimal qty, BigDecimal price, BigDecimal totalValue, BigDecimal newBalance) {
        return "✅ **" + action + " " + symbol + "**\n" +
                "Qty: " + qty + "\n" +
                "At Price: " + formatCurrency(price) + "\n" +
                "Total Value: " + formatCurrency(totalValue) + "\n" +
                "New Balance: " + formatCurrency(newBalance);
    }


    @Transactional
    public String buyCrypto(Long id, String username, String symbol, BigDecimal quantity){
        symbol = symbolFormat(symbol); // "BTC" -> "BTCUSDT"

        // Get User and Calculate Cost
        User user = getOrCreateUser(id,username);
        BigDecimal currPrice = getMarketPrice(symbol);
        BigDecimal cost = currPrice.multiply(quantity);

        // Check if they have enough Money
        if (user.getBalance().compareTo(cost) < 0 ) return "Insufficent Funds";

        // Update Balance (User table)
        user.setBalance(user.getBalance().subtract(cost));
        userRepository.save(user);

        // Update Portfolio (Asset table)
        Asset asset = getOrCreateAsset(id, symbol);
        asset.setQuantity(asset.getQuantity().add(quantity));
        assetRepository.save(asset);

        return formatTradeReport("BOUGHT", symbol, quantity, currPrice, cost, user.getBalance());
    }

    @Transactional
    public String sellCrypto(Long id, String username, String symbol, BigDecimal quantity){
        symbol = symbolFormat(symbol);

        User user = getOrCreateUser(id, username);
        // Get the specific asset row
        Asset asset = assetRepository.findByChatIdAndSymbol(id, symbol).orElse(null);

        // Validate if they actually own enough of this coin?
        if (asset == null || asset.getQuantity().compareTo(quantity) < 0) return "You dont have enough Asset.";

        BigDecimal currPrice = getMarketPrice(symbol);
        BigDecimal cost = currPrice.multiply(quantity); // "Cost" here is actually "Profit/Revenue"

        // 1. Give money to user
        user.setBalance(user.getBalance().add(cost));
        userRepository.save(user);

        // 2. Reduce Asset Quantity
        BigDecimal newQuantity = asset.getQuantity().subtract(quantity);

        // If qty reaches 0, delete the row entirely so wallet doesn't show "BTC: 0.00000"
        if (newQuantity.equals(BigDecimal.ZERO)){
            assetRepository.delete(asset);
        } else {
            asset.setQuantity(newQuantity);
            assetRepository.save(asset);
        }
        return formatTradeReport("SOLD", symbol, quantity, currPrice, cost, user.getBalance());
    }

    // Generates the wallet report
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
                // stripTrailingZeros cleans up "0.50000000" to "0.5"
                sb.append("- ").append(a.getSymbol()).append(": ")
                        .append(a.getQuantity().stripTrailingZeros().toPlainString()).append("\n");
            }
        }
        return sb.toString();
    }
}