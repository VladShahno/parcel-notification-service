package ua.com.hookahcat.telegram.bot.configuration;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static ua.com.hookahcat.telegram.bot.common.Constants.BOT_USERNAME;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ua.com.hookahcat.telegram.bot.service.HookahCatTelegramBotService;

@Component
@Slf4j
public class TelegramBotInitializer {

    private final HookahCatTelegramBotService hookahCatTelegramBotService;

    public TelegramBotInitializer(HookahCatTelegramBotService hookahCatTelegramBotService) {
        this.hookahCatTelegramBotService = hookahCatTelegramBotService;
    }

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

        try {
            log.info("Registering bot with {}",
                kv("botUsername", hookahCatTelegramBotService.getBotUsername()));
            telegramBotsApi.registerBot(hookahCatTelegramBotService);
        } catch (TelegramApiRequestException e) {
            log.error(
                "Failed to register bot with {} (check internet connection / bot token or make sure only one instance of bot is running).",
                kv(BOT_USERNAME, hookahCatTelegramBotService.getBotUsername()), e);
        }
        log.info("Telegram bot with {} is registered and ready!", kv(BOT_USERNAME, hookahCatTelegramBotService.getBotUsername()));
    }
}

