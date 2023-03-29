import bot.Bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        try {
            Bot bot = new Bot();
            bot.startApi();
            System.out.println("Application start. Bot is ready.");
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            System.err.println("Application not start. Telegram lib have a problem.");
        }
    }
}
