package com.example.TestProjectGulyan;

import org.telegram.telegrambots.meta.api.objects.File;

public interface BotMessageSender {
    void sendMessage(Long chatId, String text);
    void sendDocument(Long chatId, java.io.File file);
}
