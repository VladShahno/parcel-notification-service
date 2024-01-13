package ua.com.hookahcat.service;

import java.io.File;

public interface TelegramService {

    void sendTelegramMessage(Long chatId, String textToSend);

    void sendTelegramFile(Long chatId, File file);
}
