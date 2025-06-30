package com.example.TestProjectGulyan;

import com.example.TestProjectGulyan.model.UserResponse;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@Service
public class ReportService {
    private final UserResponseRepository userResponseRepository;

    @Autowired
    public ReportService(UserResponseRepository userResponseRepository) {
        this.userResponseRepository = userResponseRepository;
    }


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
/*
                telegramBot.sendDocument(chatId, file);
*/
                file.deleteOnExit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
