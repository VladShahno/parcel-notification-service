package ua.com.hookahcat.service.impl;

import static ua.com.hookahcat.util.Constants.FileNames.NOT_RECEIVED_PARCELS;
import static ua.com.hookahcat.util.NewPostUtils.createCsvFile;

import java.io.File;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ua.com.hookahcat.service.NewPostServiceProxy;
import ua.com.hookahcat.telegram.bot.service.TelegramService;

@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    private final NewPostServiceProxy newPostServiceProxy;

    @Value("${scheduled.max-storage-days-before-notification}")
    private String maxStorageDaysBeforeNotification;


    @Override
    public SendMessage getTelegramMessageToSend(Long chatId, String textToSend) {
        return SendMessage.builder()
            .chatId(chatId)
            .text(textToSend)
            .build();
    }

    @Override
    public SendDocument getTelegramFileToSend(Long chatId, File file) {
        return SendDocument.builder()
            .chatId(chatId)
            .document(new InputFile(file))
            .build();
    }

    @Override
    public File getFileToSend() {
        return createCsvFile(
            newPostServiceProxy.getUnReceivedParcelsCsv(maxStorageDaysBeforeNotification),
            NOT_RECEIVED_PARCELS);
    }
}
