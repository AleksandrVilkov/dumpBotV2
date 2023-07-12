package com.bot.processor.registration.subActions;

import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.IRegionStorage;
import com.bot.processor.SubAction;
import com.bot.processor.common.ProcessorUtil;
import com.bot.model.Operations;
import com.bot.processor.registration.RegistrationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RegisterCitySelection implements SubAction {
    @Autowired
    private IRegionStorage regionStorage;

    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {
        confirmCountrySelection(tempObject);
    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        String text = "Выбери город:";
        //Запоминаем выбо страны
        String countryCode = tempObject.getSelectedData().getCountryCode();

        int commonRegionCount = regionStorage.countAllByCountryCode(countryCode);
        int countRegionsOnPage = 8;

        Navigation navigation = tempObject.getNavigation();

        int currentPage = RegistrationHelper.defineCurrentPage(navigation);
        int nextPage = RegistrationHelper.defineNextPage(navigation, currentPage);

        List<Region> regions = regionStorage.getRegionPageByCountryCode(countryCode,
                nextPage == 0 ? 0 : nextPage - 1, countRegionsOnPage, true);

        List<ButtonWrapper> buttons = new ArrayList<>();

        for (Region region : regions) {
            TempObject newTemp = tempObject.clone();
            newTemp.getOption().setRegion(region);
            newTemp.setOperation(Operations.CONCERN_SELECTION);
            String key = Util.generateToken(newTemp);
            buttons.add(new ButtonWrapper(region.getName(), key, newTemp));
        }

        if (currentPage != 0) {
            buttons.add(RegistrationHelper.createBackBntData(tempObject, nextPage));
        }
        if ((currentPage + 1) * countRegionsOnPage < commonRegionCount || (navigation != null && navigation.isBack())) {
            buttons.add(RegistrationHelper.createNextBntData(tempObject, nextPage));
        }


        return ProcessorUtil.createMessages(text, update, buttons);
    }

    private void confirmCountrySelection(TempObject tempObject) {
        if (tempObject.getSelectedData() == null) {
            SelectedData selectedData = new SelectedData();
            tempObject.setSelectedData(selectedData);
        }
        tempObject.getSelectedData().setCountryCode(tempObject.getOption().getCountryCode());
    }

}
