package ua.com.hookahcat.telegram.bot.service;

import java.io.File;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface TelegramService {

    SendMessage getTelegramMessageToSend(Long chatId, String textToSend);

    SendDocument getTelegramFileToSend(Long chatId, File file);

    File getFileToSend();
}
