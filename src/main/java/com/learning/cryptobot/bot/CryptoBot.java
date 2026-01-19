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

@Component // Register this class as a Spring Bean so it can start automatically
public class CryptoBot extends TelegramLongPollingBot {

    // Dependencies to fetch prices and handle trades (Database interactions)
    private final BinancePriceService binancePriceService;
    private final TradingService tradingService;

    // Inject the bot name from application.properties
    @Value("${bot.name}")
    private String botUsername;

    public CryptoBot(@Value("${bot.token}") String botToken ,
                     BinancePriceService binancePriceService,
                     TradingService tradingService){
        super(botToken);
        this.binancePriceService = binancePriceService;
        this.tradingService = tradingService;
    }

    // Helper method to make prices look nice (e.g., 50000.12345 instead of 50000.123456789)
    private String formatCurrency(BigDecimal amount) {
        return amount.setScale(5, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // checks and tell that only act if there is a message and it contains text
        if (update.hasMessage() && update.getMessage().hasText()) {

            // .trim() removes accidental spaces at the start/end
            String messageText = update.getMessage().getText().trim();
            // Chat ID is unique to the user - used to reply back to the specific person
            long chatId = update.getMessage().getChatId();

            // Get user's first name for a personal greeting. Default to "Trader" if hidden.
            String username = update.getMessage().getFrom().getFirstName();
            if (username == null) username = "Trader";

            String responseText;

            // 2. Parse Command
            // Split by space: "buy BTC 0.1" -> parts["buy", "BTC", "0.1"]
            String[] parts = messageText.split(" ");
            // Normalize command to lowercase ("Buy" -> "buy") for comparison
            String command = parts[0].toLowerCase();

            try {
                // ---START COMMAND (/start) ---
                if (command.equals("/start")) {
                    responseText = "Welcome " + username + "!\n" +
                            "🔹 Check Price: Type symbol (e.g., BTC/BTCUSDT)\n" +
                            "🔹 Buy Asset ->  Type 'buy <symbol> <qty>' (e.g., buy BTC 0.05)\n" +
                            "🔹 Sell Asset ->  Type 'sell <symbol> <qty>' (e.g., sell BTC 0.05)\n" +
                            "🔹 Check Wallet -> wallet";
                }

                // --- BUYING ---
                else if (command.equals("buy")) {
                    // Validatate that Did the user type 3 parts? (buy + symbol + qty)
                    if (parts.length < 3) {
                        responseText = "Usage: buy <SYMBOL> <QUANTITY>\nExample: buy BTC 0.01";
                    } else {
                        String symbol = parts[1]; // e.g."BTC"
                        BigDecimal quantity = new BigDecimal(parts[2]); // e.g."0.01"

                        // Call Service to update Database
                        responseText = tradingService.buyCrypto(chatId, username, symbol, quantity);
                    }
                }

                // --- SELLING ---
                else if (command.equals("sell")) {
                    if (parts.length < 3) {
                        responseText = "Usage: sell <SYMBOL> <QUANTITY>\nExample: sell BTC 0.01";
                    } else {
                        String symbol = parts[1];
                        BigDecimal quantity = new BigDecimal(parts[2]);

                        // Call Service to sell assets from Database
                        responseText = tradingService.sellCrypto(chatId, username, symbol, quantity);
                    }
                }

                // --- SCENARIO 4: WALLET CHECK ---
                else if (command.equals("wallet") || command.equals("balance")) {
                    // Calls service to generate the text report of wallet
                    responseText = tradingService.getWallet(chatId);
                }

                // --- SCENARIO 5: DEFAULT (PRICE CHECK) ---
                // If the user typed "BTC" or "ETH"
                else {
                    try {
                        // Attempt to fetch price data from Binance
                        BinanceTickerDto stat = binancePriceService.getData(messageText);

                        responseText = "📊 *Market Report for " + stat.getSymbol() + "*\n\n" +
                                "💰 Price: $" + formatCurrency(stat.getLastPrice())  + "\n" +
                                "📈 24h High: $" + formatCurrency(stat.getHighPrice()) + "\n" +
                                "📉 24h Low: $" + formatCurrency(stat.getLowPrice()) + "\n" +
                                "Change (24h): " + stat.getPriceChangePercent() + "%\n"+
                                "Volume: " + formatCurrency(stat.getVolume()) + "\n" +
                                "ATR(Average True Range: " + formatCurrency(stat.getWeightedAvgPrice());

                    } catch (Exception e) {
                        // If Binance API throws error, it means the symbol (e.g., "XYZ") doesn't exist
                        responseText = "Error: Could not find market data for '" + messageText + "'. check spelling.";
                    }
                }

            } catch (NumberFormatException e) {
                // Catches if user types "buy BTC hello" instead of "buy BTC 0.05"
                responseText = "Invalid Quantity. Use 0.05 not words.";
            } catch (Exception e) {
                // Catch all other unexpected errors
                e.printStackTrace();
                responseText = "Transaction Failed: " + e.getMessage();
            }

            // sends the constructed text back to the user
            sendMessage(chatId, responseText);
        }
    }

    // This method help send message to Telegram
    public void sendMessage(long charId, String responseText){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(charId)); // Where to send it
        message.setText(responseText);            // What to send
        try {
            execute(message); // sends request to Telegram servers
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    // Required method: Tells Telegram what the name of this bot is
    @Override
    public String getBotUsername() {
        return botUsername;
    }
}