package com.learning.cryptobot;

import com.learning.cryptobot.dto.BinanceTickerDto;
import com.learning.cryptobot.entity.Asset;
import com.learning.cryptobot.entity.User;
import com.learning.cryptobot.service.BinancePriceService;
import com.learning.cryptobot.service.TradingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Controller
public class WebController {

    private final TradingService tradingService;
    private final BinancePriceService binancePriceService;

    // Notice: UserRepository is GONE! simpler and cleaner.

    public WebController(TradingService tradingService, BinancePriceService binancePriceService) {
        this.tradingService = tradingService;
        this.binancePriceService = binancePriceService;
    }

    @GetMapping("/login")
    public String showLogin(){
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam (required = false) Long userId, @RequestParam String userName, HttpSession session){
        if (userId == null) userId = ThreadLocalRandom.current().nextLong(1000000L, 999999999L);
        User user = tradingService.getOrCreateUser(userId, userName);

        session.setAttribute("userID", user.getChatId());
        session.setAttribute("userName", user.getName());

        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model){
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        String name = (String) session.getAttribute("userName");
        model.addAttribute("name", name);

        return "dashboard";
    }

    @GetMapping("/")
    public String home(){
        return "redirect:/login";
    }

    @GetMapping("/price")
    public String getPrice(HttpSession session){
        if (session.getAttribute("userId") == null) return "redirect:/login";
        return "price";
    }

    @PostMapping("/price")
    public String processPrice(HttpSession session, Model model, @RequestParam String symbol, Principal principal){
        if (session.getAttribute("userId") == null) return "redirect:/login";
        try {
            BinanceTickerDto ticker = binancePriceService.getData(symbol);
            model.addAttribute("ticker", ticker);
        } catch (Exception e){
            model.addAttribute("error", "Invalid Symbol : Error -> " + e.getMessage());
        }
        return "price";
    }

    @GetMapping("/trade")
    public String showTrade(HttpSession session){
        if (session.getAttribute("userId") == null) return "redirect:/login";
        return "trade";
    }

    @PostMapping("/trade")
    public String processTrade(HttpSession session, Model model, @RequestParam String symbol, @RequestParam BigDecimal quantity, String action){
        Long userId = (Long) session.getAttribute("userId");
        String userName = (String)session.getAttribute("userName");

        if (userId == null) return "redirect:/login";

        try {
            String result;
            if ("buy".equalsIgnoreCase(action)){
                result = tradingService.buyCrypto(userId, userName, symbol, quantity);
            } else {
                result = tradingService.sellCrypto(userId, userName, symbol, quantity);
            }
            model.addAttribute("message", result);
        } catch (Exception e) {
            model.addAttribute("error", "Trade Failed: " + e.getMessage());
        }
        return "trade";
    }

    @GetMapping("/wallet")
    public String showWallet(HttpSession session, Model model){
        Long userId = (Long) session.getAttribute("userId");
        if (userId==null) return "redirect:/login";
        String result = tradingService.getWallet(userId);

        model.addAttribute("wallettxt", result);

        return "wallet";
    }


}