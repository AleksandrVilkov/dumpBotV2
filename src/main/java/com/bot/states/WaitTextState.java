package com.bot.states;

import com.bot.enums.BotCommand;
import com.bot.service.ai.AIService;
import com.bot.enums.EnumUtils;
import com.bot.enums.State;
import com.bot.enums.UserRequestType;
import com.bot.model.ai.AIResult;
import com.bot.model.domain.*;
import com.bot.service.user.UserService;
import com.bot.service.userrequest.UserRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class WaitTextState implements BaseState {
    @Autowired
    UserService userService;
    @Autowired
    UserRequestService userRequestService;
    @Autowired
    AIService aiService;

    @Override
    public List<Message> execute(BotUser botUser, IncomingData incomingData) {

        //TODO предусмотреть разные стратегии в зависисмости от типа объявления
        var aiResult = aiService.parseText(incomingData.getText());
        List<Message> errors = checkResultForError(aiResult, botUser.getId());
        if (!errors.isEmpty()) {
            errors.add(new Message(botUser.getId(),
                    "Запрос оформлен не точно, выше я перечислил недостаки. Попробуй отправить запрос заново исправив ошибки"));
            return errors;
        }

        String text = createTextFromAIResult(aiResult, incomingData);

        var userRequest = UserRequest.builder()
                .id(UUID.randomUUID())
                .telegramUserId(botUser.getId())
                .telegramLogin(incomingData.getUserName())
                .text(text)
                .type(EnumUtils.findEnumConstant(UserRequestType.class, aiResult.getType()))
                .userApprove(false)
                .adminApprove(false)
                .deleted(false)
                .posted(false)
                .hasPhoto(false)
                .inProgress(true)
                .createdAt(Instant.now().toEpochMilli())
                .tgAccountCreatedAt(0L)
                .build();

        userRequestService.saveUserRequest(userRequest);

        //TODO переделать мб на стратегию
        if (UserRequestType.SELL.equals(userRequest.getType())) {
            userService.saveUserState(botUser.getId(), State.WAIT_PHOTO);
            //TODO исправить на бот команды
            return List.of(
                    new Message(botUser.getId(),
                            "Теперь приложи фото."));
        } else if (UserRequestType.BUY.equals(userRequest.getType())) {
            userService.saveUserState(botUser.getId(), State.WAIT_USER_APPROVE);
            return List.of(new Message(botUser.getId(),
                            "Прекрасно. Посмотри как будет выглядеть пост:"),
                    new Message(botUser.getId(), text),
                    new Message(botUser.getId(),
                            "Если все хорошо - нажми " + BotCommand.APPROVE.getCommand() + "\n\n" +
                                    "Или начни заново нажав на " + BotCommand.CANCEL.getCommand()),
            new Message(botUser.getId(),
                    "Ты можешь добавить фото к своему запросу - это увеличит вероятность найти нужную деталь. Для этого нажми " + BotCommand.ADD_PHOTO.getCommand()));

        }
        return List.of(
                new Message(botUser.getId(),
                        "Произошла ошибка, я не понимаю что это за запрос")
        );
    }

    @Override
    public List<Message> handleCommands(BotUser botUser, List<BotCommand> commands) {
        return List.of(new Message(botUser.getId(), "Пришли мне текст, а не команду"));
    }

    private String createTextFromAIResult(AIResult aiResult, IncomingData incomingData) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(aiResult.getType().equalsIgnoreCase(UserRequestType.SELL.name()) ? "Продам" : "Куплю")
                .append(":\n")
                .append(aiResult.getDescription())
                .append("\n")
                .append("Подходит к автомобил").append(aiResult.getCars().size() == 1 ? "ю" : "ям")
                .append(":\n");
        aiResult.getCars().forEach(car -> {
            stringBuilder.append(car).append(";\n");
        });
        if (aiResult.getPrice() != null) {
            stringBuilder.append("\n")
                    .append("Цена: ").append(aiResult.getPrice()).append("\n");
        }

        stringBuilder.append("Контакт для связи: ").append(incomingData.getUserName());
        return stringBuilder.toString();
    }

    private List<Message> checkResultForError(AIResult result, String chatId) {
        List<Message> errors = new ArrayList<>();
        if (UserRequestType.SELL.name().equalsIgnoreCase(result.getType()) && result.getPrice() == null)
            errors.add(new Message(chatId, "Не указана либо не распознана цена"));
        if (result.getDescription() == null)
            errors.add(new Message(chatId, "Не указано либо не заполнено описание товара"));
        if (result.getCars() == null || result.getCars().isEmpty())
            errors.add(new Message(chatId, "Отсутвует или не распознана информация о применяемости к автомобилю(ям)"));

        if (result.getType() == null)
            errors.add(new Message(chatId, "Не указан либо отсутвует признак типа запроса: продажа или покупка"));
        return errors;
    }
}
