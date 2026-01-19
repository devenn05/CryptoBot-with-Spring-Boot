package com.learning.cryptobot.bot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotInitializer {

    @Bean
    public TelegramBotsApi telegramBotsApi(CryptoBot cryptoBot) throws TelegramApiException {

        // 1. Create the API instance
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);

        // 2. Register your bot! (This is the "Turning the key" moment)
        api.registerBot(cryptoBot);

        return api;
    }
}
