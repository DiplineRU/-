package com.example.TestProjectGulyan;

import com.example.TestProjectGulyan.model.UserResponse;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@Service
public class ReportService {
    private final UserResponseRepository userResponseRepository;
    private final TelegramBot telegramBot;

    @Autowired
    public ReportService(UserResponseRepository userResponseRepository, TelegramBot telegramBot) {
        this.userResponseRepository = userResponseRepository;
        this.telegramBot = telegramBot;
    }

    private void sendDocument(Long chatId, File file) {
        telegramBot.sendDocument(chatId, file); // делегируем отправку боту
    }

 /*   @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(telegramBot);
        return api;
    }*/

    public void generateReport(Long chatId) {
        List<UserResponse> responses = userResponseRepository.findAll();

        try (XWPFDocument document = new XWPFDocument()) {
            XWPFTable table = document.createTable();

            // заголовки таблицы
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.getCell(0).setText("Имя");
            headerRow.addNewTableCell().setText("Email");
            headerRow.addNewTableCell().setText("Оценка");

            // данные
            for (UserResponse response : responses) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(response.getName());
                row.getCell(1).setText(response.getEmail());
                row.getCell(2).setText(String.valueOf(response.getRating()));
            }

            File file = File.createTempFile("report", ".docx");
            try (FileOutputStream out = new FileOutputStream(file)) {
                document.write(out);
                telegramBot.sendDocument(chatId, file);
                file.deleteOnExit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
