package com.learning.cryptobot.bot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration // Runs during application startup
public class BotInitializer {

    @Bean
    public TelegramBotsApi telegramBotsApi(CryptoBot cryptoBot) throws TelegramApiException {

        // Create the API instance (Handles the connection sessions)
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);

        // Register your specific bot instance!
        // This links your logic (onUpdateReceived) to the incoming web requests.
        api.registerBot(cryptoBot);

        return api;
    }
}