package ua.com.hookahcat.service;

import static ua.com.hookahcat.common.Constants.START;
import static ua.com.hookahcat.service.scheduler.ParcelSearchingJob.generateFile;

import java.io.File;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.com.hookahcat.configuration.TelegramBotProperties;
import ua.com.hookahcat.reststarter.exception.InternalErrorException;
import ua.com.hookahcat.service.scheduler.ParcelSearchingJob;

@Service
@AllArgsConstructor
public class HookahCatTelegramBot extends TelegramLongPollingBot implements TelegramService {

    private final TelegramBotProperties telegramBotProperties;
    private final ParcelSearchingJob parcelSearchingJob;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var text = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();
            if (START.equals(text)) {
                var parcelsCsv = generateFile(parcelSearchingJob.getUnReceivedParcelsCsv());
                if (parcelsCsv.length() > 0) {
                    sendTelegramMessage(chatId,
                        "The CSV file with unreceived parcels has been successfully generated");
                    sendTelegramFile(chatId, parcelsCsv);
                } else {
                    sendTelegramMessage(chatId, "Unreceived parcels not found, try tomorrow...");
                }
            }
        }
    }

    @Override
    public void sendTelegramMessage(Long chatId, String textToSend) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new InternalErrorException(e.getMessage());
        }

    }

    @Override
    public void sendTelegramFile(Long chatId, File file) {
        var sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(new InputFile(file));

        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new InternalErrorException(e.getMessage());
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
