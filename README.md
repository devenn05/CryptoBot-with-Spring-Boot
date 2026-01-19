# 🤖 Spring Boot CryptoBot & Dashboard

A hybrid trading simulator that allows users to check real-time crypto prices and simulate trades using **fake money** via both a **Telegram Bot** and a **Web Dashboard**.

## 🚀 Features

### 1. Telegram Bot
- **Real-time Prices:** Type a symbol (e.g., `BTC`) to get live market data from Binance.
- **Commands:**
  - `/start` - Welcome message.
  - `buy BTC 0.5` - Simulates buying 0.5 Bitcoin.
  - `sell BTC 0.5` - Simulates selling.
  - `wallet` - View your balance and holdings.

### 2. Web Dashboard (Thymeleaf)
- **Login System:** Enter your specific Chat ID to sync with Telegram, or auto-generate a new ID.
- **Price Checker:** Clean UI to fetch live API data.
- **Trade Interface:** GUI forms to execute Buy/Sell orders using button logic.
- **Wallet View:** Visual display of current portfolio.

## 🛠️ Tech Stack
- **Framework:** Spring Boot 3.5.9 (Java 17)
- **Database:** H2 Database (In-Memory JPA)
- **UI:** Thymeleaf (Server-side rendering)
- **APIs:** 
  - Telegram Bots Api (`telegrambots-spring-boot-starter`)
  - Binance Public API (via `RestTemplate`)

---

## 👨‍💻 How I Made It

I started this project to learn how **Spring Boot** handles real-world logic by connecting external APIs with local persistence.

I began by setting up the **Backend Logic**. I created a `BinancePriceService` using `RestTemplate` to fetch JSON data from the Binance API. Once I had real-time data, I implemented the **Database Layer** using **Spring Data JPA** and **H2**. I designed `User` and `Asset` entities to track balances and holdings, ensuring that money wasn't just "floating" but persisted in a structured SQL database.

Next, I built the **Telegram Bot interface**. I extended `TelegramLongPollingBot` to parse incoming text messages. I learned how to process commands (using string splitting) to trigger my `TradingService`, which handles the business logic: checking if a user has enough funds, calculating costs, and updating the database transactionally.

Finally, I expanded the project to the **Web**. This was a major shift from a "Chat" mindset to a "View" mindset. I used **Thymeleaf** to create HTML templates (`login.html`, `dashboard.html`). I learned about `HttpSession` to maintain user state across different pages. The best part was re-using the exact same `TradingService` for both the Telegram Bot and the Web Controller—proving that a solid backend structure allows you to plug in any frontend interface you want.
