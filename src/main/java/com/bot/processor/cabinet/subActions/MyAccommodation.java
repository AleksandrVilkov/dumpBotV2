package com.bot.processor.cabinet.subActions;

import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.model.UserAccommodation;
import com.bot.processor.IAccommodationStorage;
import com.bot.processor.SubAction;
import com.bot.processor.common.ProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@Slf4j
public class MyAccommodation implements SubAction {
    @Autowired
    private IAccommodationStorage accommodationStorage;

    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        List<UserAccommodation> accommodations = accommodationStorage.getAllByUserId(user.getId());
        int allCount = accommodations.size();
        StringBuilder text = new StringBuilder();
        text.append("Всего подано ").append(allCount).append(" заявок. \n");
        long rejected = accommodations.stream().filter(UserAccommodation::isRejected).count();
        long topical = accommodations.stream().filter(UserAccommodation::isTopical).count();
        long approved = accommodations.stream().filter(UserAccommodation::isApproved).count();
        text.append("Из них еще не рассмотренно: ").append(topical).append("\n");
        text.append("Одобрено: ").append(approved).append("\n");
        text.append("Отклонено: ").append(rejected).append("\n");
        return ProcessorUtil.createMessages(text.toString(), update);
    }
}
