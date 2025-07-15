package com.example.TestProjectGulyan;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot implements BotMessageSender {
    private final FormStateService formStateService;
    private final ReportService reportService;
    private final String botToken;

    public TelegramBot(
            @Value("${bot.token}") String botToken,
            @Lazy FormStateService formStateService,
            ReportService reportService
    ) {
        super(new DefaultBotOptions(), botToken);
        this.botToken = botToken;
        this.formStateService = formStateService;
        this.reportService = reportService;
    }

    @Override
    public String getBotUsername() {
        return "MyTestBot"; // замените на актуальное имя бота
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            switch (text) {
                case "/start" -> {
                    sendMessage(chatId, "Привет! Для прохождения опроса используй /form");
                    formStateService.resetState(chatId);
                }
                case "/form" -> formStateService.startForm(chatId);
                case "/report" -> reportService.generateReport(chatId);
                default -> formStateService.processInput(chatId, text);
            }
        }
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendDocument(Long chatId, File file) {
        SendDocument document = new SendDocument();
        document.setChatId(chatId.toString());
        document.setDocument(new InputFile(file, "report.docx"));
        try {
            execute(document);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
