package com.learning.cryptobot.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.learning.cryptobot.dto.BinanceTickerDto;

@Service
public class BinancePriceService {
    private final String BINANCE_URL = "https://api.binance.com/api/v3/ticker/24hr?symbol=";
    RestTemplate restTemplate = new RestTemplate();

    public BinanceTickerDto getData(String symbol) {
        String cleanSymbol = symbol.trim().toUpperCase();

        try {return fetchData(cleanSymbol);}
        catch (HttpClientErrorException e) {
            if (!cleanSymbol.endsWith("USDT")) {
                try {return fetchData(cleanSymbol + "USDT");} catch (HttpClientErrorException ex) {throw e;}
            } else throw e;

        }
    }

    private BinanceTickerDto fetchData(String symbol){
        String url = BINANCE_URL + symbol;
        BinanceTickerDto response  = restTemplate.getForObject(url,BinanceTickerDto.class);
        return response;
    }
}