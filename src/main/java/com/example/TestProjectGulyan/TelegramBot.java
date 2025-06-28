package com.example.TestProjectGulyan;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final FormStateService formStateService;
    private final ReportService reportService;

    @Override
    public String getBotUsername() {
        return "MyTestBot";
    }

    /*    public TelegramBot(FormStateService formStateService, ReportService reportService) {
            super(new DefaultBotOptions(), "8053565336:AAHwnWZCmcsYzV-OCts752nCTLJ1ZNkqivU");
            this.formStateService = formStateService;
            this.reportService = reportService;
        }*/
    public TelegramBot(
            @Value("${bot.token}") String botToken,
            FormStateService formStateService,
            ReportService reportService
    ) {
        super(new DefaultBotOptions(), botToken);
        this.formStateService = formStateService;
        this.reportService = reportService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            switch (text) {
                case "/start":
                    sendMessage(chatId, "Привет! Для прохождения опроса используй /form");
                    formStateService.resetState(chatId);
                    break;
                case "/form":
                    formStateService.startForm(chatId);
                    break;
                case "/report":
                    reportService.generateReport(chatId);
                    break;
                default:
                    formStateService.processInput(chatId, text);
            }
        }
    }

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

    public void sendDocument(Long chatId, java.io.File file) {  // Используем java.io.File
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId.toString());
        sendDocument.setDocument(new InputFile(file, "report.docx"));
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

