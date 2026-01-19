package com.learning.cryptobot.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.learning.cryptobot.dto.BinanceTickerDto;

@Service
public class BinancePriceService {

    // API Endpoint for 24hr statistics
    private final String BINANCE_URL = "https://api.binance.com/api/v3/ticker/24hr?symbol=";

    // RestTemplate is Spring's tool for making HTTP Requests (GET, POST, etc.)
    RestTemplate restTemplate = new RestTemplate();

    public BinanceTickerDto getData(String symbol) {
        String cleanSymbol = symbol.trim().toUpperCase();

        try {
            // Try fetching exact match (e.g. "BTCUSDT")
            return fetchData(cleanSymbol);
        }
        catch (HttpClientErrorException e) {
            // If the user typed just "BTC", the API throws 400 Bad Request.
            // We catch it, append "USDT", and try one more time.
            if (!cleanSymbol.endsWith("USDT")) {
                try {
                    return fetchData(cleanSymbol + "USDT");
                } catch (HttpClientErrorException ex) {
                    // If it STILL fails, it means the symbol is actually garbage (e.g., "XYZ123")
                    throw e;
                }
            } else {
                throw e;
            }
        }
    }

    private BinanceTickerDto fetchData(String symbol){
        String url = BINANCE_URL + symbol;
        // getForObject automatically maps the JSON result to your Java Class
        BinanceTickerDto response = restTemplate.getForObject(url, BinanceTickerDto.class);
        return response;
    }
}