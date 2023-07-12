package com.bot.processor.registration.subActions;

import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.IRegionStorage;
import com.bot.processor.SubAction;
import com.bot.processor.common.ProcessorUtil;
import com.bot.model.Operations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class StartRegistration implements SubAction {
    @Autowired
    private IRegionStorage regionStorage;

    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        String text = "Выбери страну:";
        List<String> countries = regionStorage.getCountries();
        List<ButtonWrapper> buttons = new ArrayList<>();
        for (String c : countries) {
            TempObject newTemp = tempObject.clone();
            OptionData optionData = new OptionData();
            optionData.setCountryCode(c);
            newTemp.setOption(optionData);
            newTemp.setOperation(Operations.CITY_SELECTION);
            buttons.add(new ButtonWrapper(c, Util.generateToken(newTemp), newTemp));
        }
        MessageWrapper messageWrapper = ProcessorUtil.createMessages(text, update, buttons);
        messageWrapper.setButtons(buttons);
        return messageWrapper;
    }
}
