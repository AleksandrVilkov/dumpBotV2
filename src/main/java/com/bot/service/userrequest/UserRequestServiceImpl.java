package com.bot.service.userrequest;

import com.bot.config.ValidatorConfig;
import com.bot.enums.UserRequestType;
import com.bot.mappers.PhotoMapper;
import com.bot.mappers.UserRequestMapper;
import com.bot.model.domain.Message;
import com.bot.model.domain.UserRequest;
import com.bot.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserRequestServiceImpl implements UserRequestService {
    @Autowired
    UserRequestMapper userRequestMapper;

    @Autowired
    PhotoMapper photoMapper;
    @Autowired
    UserService userService;
    @Autowired
    ValidatorConfig validatorConfig;

    @Override
    public Message generatePreview(String telegramUserId) {
        var userRequest = findActiveRequest(telegramUserId);
        return generateRequestMessage(userRequest, telegramUserId);
    }

    private Message generateRequestMessage(UserRequest userRequest, String chatId) {
        var msgBuilder = Message.builder().chatId(chatId);
        if (photoMapper.existsPhotosByRequestId(userRequest.getId())) {
            var photos = photoMapper.findByRequestId(userRequest.getId());
            msgBuilder.photos(photos);
        }

        msgBuilder.text(userRequest.getText());
        return msgBuilder.build();
    }

    @Override
    public Message messageToChanel(UserRequest userRequest) {
        markNonActive(userRequest.getId());

        var message = generateRequestMessage(userRequest,String.valueOf(validatorConfig.getChannelID()));
        userService.resetState(userRequest.getTelegramUserId());
        return message;
    }

    @Override
    public UserRequest findActiveRequest(String telegramUserId) {
        return userRequestMapper.findActiveRequest(telegramUserId)
                .orElseThrow(() -> new RuntimeException("Я не смог найти твой запрос..."));
    }

    @Override
    public UUID findActiveRequestId(String telegramUserId) {
        return userRequestMapper.findActiveRequestId(telegramUserId)
                .orElseThrow(() -> new RuntimeException("Я не смог найти твой запрос..."));
    }

    @Override
    public void markNonActive(UUID userRequestId) {
        userRequestMapper.markNonActive(userRequestId);
    }

    @Override
    public void saveUserRequest(UserRequest userRequest) {
        userRequestMapper.saveUserRequest(userRequest);
    }
}
