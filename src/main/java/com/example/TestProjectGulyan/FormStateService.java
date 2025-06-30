package com.example.TestProjectGulyan;

import com.example.TestProjectGulyan.model.State;
import com.example.TestProjectGulyan.model.UserResponse;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class FormStateService {
    private final UserResponseRepository userResponseRepository;
    private final BotMessageSender botMessageSender;


    private final Map<Long, State> userStates = new HashMap<>();
    private final Map<Long, UserResponse> userResponses = new HashMap<>();

    public FormStateService(UserResponseRepository userResponseRepository, BotMessageSender botMessageSender) {
        this.userResponseRepository = userResponseRepository;
        this.botMessageSender = botMessageSender;
    }
    private void sendMessage(Long chatId, String text) {
        botMessageSender.sendMessage(chatId, text); // Делегируем отправку боту
    }

    private void saveResponse(Long chatId, UserResponse response) {
        userResponseRepository.save(response);
    }

    public void resetState(Long chatId) {
        userStates.remove(chatId);
        userResponses.remove(chatId);
    }

    public void startForm(Long chatId) {
        userStates.put(chatId, State.NAME);
        sendMessage(chatId, "Введите ваше имя:");
    }

    public void processInput(Long chatId, String text) {
        State currentState = userStates.get(chatId);
        if (currentState == null) return;

        UserResponse response = userResponses.computeIfAbsent(chatId, k -> new UserResponse());

        switch (currentState) {
            case NAME:
                response.setName(text);
                userStates.put(chatId, State.EMAIL);
                sendMessage(chatId, "Введите email:");
                break;
            case EMAIL:
                if (isValidEmail(text)) {
                    response.setEmail(text);
                    userStates.put(chatId, State.RATING);
                    sendMessage(chatId, "Оцените бота от 1 до 10:");
                } else {
                    sendMessage(chatId, "Неверный email. Попробуйте ещё раз:");
                }
                break;
            case RATING:
                try {
                    int rating = Integer.parseInt(text);
                    if (rating >= 1 && rating <= 10) {
                        response.setRating(rating);
                        saveResponse(chatId, response);
                        sendMessage(chatId, "Спасибо! Данные сохранены.");
                        resetState(chatId);
                    } else {
                        sendMessage(chatId, "Оценка должна быть от 1 до 10:");
                    }
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Введите число от 1 до 10:");
                }
                break;
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
