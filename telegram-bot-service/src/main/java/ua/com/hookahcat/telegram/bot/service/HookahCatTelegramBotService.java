package ua.com.hookahcat.telegram.bot.service;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static ua.com.hookahcat.telegram.bot.common.Constants.START;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.com.hookahcat.reststarter.exception.InternalErrorException;
import ua.com.hookahcat.telegram.bot.configuration.TelegramBotProperties;

@Service
@AllArgsConstructor
@Slf4j
public class HookahCatTelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotProperties telegramBotProperties;
    private final TelegramService telegramService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var text = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();
            if (START.equals(text)) {
                log.info("Start command received {}", kv("chatId", chatId));
                var fileToSend = telegramService.getFileToSend();
                if (fileToSend.length() > 0) {
                    try {
                        execute(telegramService.getTelegramMessageToSend(chatId,
                            "The CSV file with unreceived parcels has been successfully generated"));
                        execute(telegramService.getTelegramFileToSend(chatId, fileToSend));

                    } catch (TelegramApiException e) {
                        throw new InternalErrorException(e.getMessage());
                    }
                } else {
                    try {
                        execute(telegramService.getTelegramMessageToSend(chatId,
                            "Unreceived parcels not found, try tomorrow..."));
                    } catch (TelegramApiException e) {
                        throw new InternalErrorException(e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return telegramBotProperties.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return telegramBotProperties.getBotToken();
    }
}
