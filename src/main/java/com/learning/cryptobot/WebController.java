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

/**
 * WebController
 * This class handles all traffic between the User's Browser (Chrome/Edge)
 * and the Backend Logic.
 */
@Controller //
public class WebController {

    // Dependencies (The Services that do the actual work)
    private final TradingService tradingService;
    private final BinancePriceService binancePriceService;

    // Constructor Injection
    public WebController(TradingService tradingService, BinancePriceService binancePriceService) {
        this.tradingService = tradingService;
        this.binancePriceService = binancePriceService;
    }

    // --- LOGIN SECTION ---

    @GetMapping("/login")
    public String showLogin(){
        return "login"; // Looks for login.html
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam(required = false) Long userId, @RequestParam String userName, HttpSession session) {

        // If the user left ID blank (Web User), create a random fake ID
        if (userId == null) userId = ThreadLocalRandom.current().nextLong(1000000L, 999999999L);

        // Create or fetch the User from Database
        User user = tradingService.getOrCreateUser(userId, userName);

        // This is how we know who they are on the next page.
        session.setAttribute("userId", user.getChatId());
        session.setAttribute("userName", user.getName());

        // Redirect moves the user to a new URL, effectively reloading the page
        return "redirect:/dashboard";
    }

    // --- DASHBOARD SECTION ---

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model){

        // Check if ID exists in Session
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login"; // Kick them out if not logged in

        String name = (String) session.getAttribute("userName");

        // Add data to the "Box" (Model) so Thymeleaf can use ${name}
        model.addAttribute("name", name);

        return "dashboard";
    }

    // Home Page
    @GetMapping("/")
    public String home(){
        return "redirect:/login";
    }


    // --- PRICE CHECKER SECTION ---

    @GetMapping("/price")
    public String getPrice(HttpSession session){
        // Checks if user already exists
        if (session.getAttribute("userId") == null) return "redirect:/login";
        return "price"; // Loads price.html
    }

    @PostMapping("/price")
    public String processPrice(HttpSession session, Model model, @RequestParam String symbol, Principal principal) {

        if (session.getAttribute("userId") == null) return "redirect:/login";

        try {
            // Call external API -> binancePriceService and sade data into a DTO
            BinanceTickerDto ticker = binancePriceService.getData(symbol);

            // Put success object in Model
            model.addAttribute("ticker", ticker);
        } catch (Exception e){
            // Put error message in Model
            model.addAttribute("error", "Invalid Symbol : Error -> " + e.getMessage());
        }

        // Reloads the same page, but now the 'model' has data, so the result box appears
        return "price";
    }

    // --- TRADING SECTION ---

    @GetMapping("/trade")
    public String showTrade(HttpSession session){
        if (session.getAttribute("userId") == null) return "redirect:/login";
        return "trade";
    }

    @PostMapping("/trade")
    public String processTrade(HttpSession session, Model model, @RequestParam String symbol, @RequestParam BigDecimal quantity, String action){

        // 1. Get User ID from Session
        Long userId = (Long) session.getAttribute("userId");
        String userName = (String)session.getAttribute("userName");

        if (userId == null) return "redirect:/login";

        try {
            String result;

            // 2. Decide logic based on button clicked
            if ("buy".equalsIgnoreCase(action)){
                result = tradingService.buyCrypto(userId, userName, symbol, quantity);
            } else {
                result = tradingService.sellCrypto(userId, userName, symbol, quantity);
            }

            // 3. Send success message to UI
            model.addAttribute("message", result);
        } catch (Exception e) {
            // 4. Send failure message to UI
            model.addAttribute("error", "Trade Failed: " + e.getMessage());
        }

        return "trade";
    }

    // --- WALLET SECTION ---

    @GetMapping("/wallet")
    public String showWallet(HttpSession session, Model model){
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        // Fetches the string formatted with \n newlines
        String result = tradingService.getWallet(userId);

        // 'wallettxt' is just a name we invented for the HTML to look for
        model.addAttribute("wallettxt", result);

        return "wallet";
    }
}