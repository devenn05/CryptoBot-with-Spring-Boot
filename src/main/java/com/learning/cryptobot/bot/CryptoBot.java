package com.learning.cryptobot.bot;

import com.learning.cryptobot.service.BinancePriceService;
import com.learning.cryptobot.dto.BinanceTickerDto;

import com.learning.cryptobot.service.TradingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Component
public class CryptoBot extends TelegramLongPollingBot {

    private final BinancePriceService binancePriceService;
    private final TradingService tradingService;

    @Value("${bot.name}")
    private String botUsername;

    public CryptoBot(@Value("${bot.token}") String botToken , BinancePriceService binancePriceService, TradingService tradingService){
        super(botToken);
        this.binancePriceService = binancePriceService;
        this.tradingService = tradingService;
    }

    private String formatCurrency(BigDecimal amount) {
        return amount.setScale(5, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            // 1. Get Basic Data (Safe trim to remove accidental spaces)
            String messageText = update.getMessage().getText().trim();
            long chatId = update.getMessage().getChatId();

            // FIX: Use getFrom().getFirstName() instead of getForwardSenderName()
            String username = update.getMessage().getFrom().getFirstName();
            if (username == null) username = "Trader";

            String responseText;

            // 2. Split using Regex (Handling multiple spaces like "buy   BTC   1")
            String[] parts = messageText.split(" ");
            String command = parts[0].toLowerCase();

            try {
                // SCENARIO 1: START
                if (command.equals("/start")) {
                    responseText = "Welcome " + username + "!\n" +
                            "🔹 Check Price: Type symbol (e.g., BTC/BTCUSDT)\n" +
                            "🔹 Buy Asset ->  Type 'buy <symbol> <qty>' (e.g., buy BTC 0.05)\n" +
                            "🔹 Buy Asset ->  Type 'sell <symbol> <qty>' (e.g., sell BTC 0.05)\n" +
                            "🔹 Check Wallet -> wallet";
                }

                // SCENARIO 2: BUY
                else if (command.equals("buy")) {
                    if (parts.length < 3) {
                        responseText = "Usage: buy <SYMBOL> <QUANTITY>\nExample: buy BTC 0.01";
                    } else {
                        String symbol = parts[1];
                        BigDecimal quantity = new BigDecimal(parts[2]);

                        responseText = tradingService.buyCrypto(chatId, username, symbol, quantity);
                    }
                }

                else if (command.equals("sell")) {
                    if (parts.length < 3) {
                        responseText = "Usage: sell <SYMBOL> <QUANTITY>\nExample: buy BTC 0.01";
                    } else {
                        String symbol = parts[1];
                        BigDecimal quantity = new BigDecimal(parts[2]);

                        responseText = tradingService.sellCrypto(chatId, username, symbol, quantity);
                    }
                }

                else if (command.equals("wallet") || command.equals("balance")) {
                    responseText = tradingService.getWallet(chatId);
                }

                // SCENARIO 3: DEFAULT (Market Report)
                else {
                    // This logic specifically calls the price service
                    try {
                        BinanceTickerDto stat = binancePriceService.getData(messageText);

                        responseText = "📊 *Market Report for " + stat.getSymbol() + "*\n\n" +
                                "💰 Price: $" + formatCurrency(stat.getLastPrice())  + "\n" +
                                "📈 24h High: $" + formatCurrency(stat.getHighPrice()) + "\n" +
                                "📉 24h Low: $" + formatCurrency(stat.getLowPrice()) + "\n" +
                                "Change (24h): " + stat.getPriceChangePercent() + "%\n"+
                                "Volume: " + formatCurrency(stat.getVolume()) + "\n" +
                                "ATR(Average True Range: " + formatCurrency(stat.getWeightedAvgPrice());

                    } catch (Exception e) {
                        // Logic 3 failed: Must be an invalid symbol
                        responseText = "Error: Could not find market data for '" + messageText + "'. check spelling.";
                    }
                }

            } catch (NumberFormatException e) {
                responseText = "Invalid Quantity. Use 0.05 not 'xyz'.";
            } catch (Exception e) {
                // CATCH ALL FOR BUYING: This will now show the REAL error from TradingService
                e.printStackTrace(); // Look at your IntelliJ console too!
                responseText = "Transaction Failed: " + e.getMessage();
            }

            sendMessage(chatId, responseText);
        }
    }

    public void sendMessage(long charId, String responseText){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(charId));
        message.setText(responseText);
        try {execute(message); } catch (TelegramApiException e) {e.printStackTrace();}
    }


    @Override
    public String getBotUsername() {
        return botUsername;
    }
}

