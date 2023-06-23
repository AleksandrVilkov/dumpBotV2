package com.bot.processor.registration;

import com.bot.common.Util;
import com.bot.model.Navigation;
import com.bot.model.Role;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.processor.ITempStorage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class RegistrationHelper {

    public static User createUser(TempObject tempObject, Update update) {
        return User.builder()
                .userName(Util.getUserName(update))
                .createDate(new Date())
                .role(Role.USER_ROLE)
                .login(Util.getUserId(update))
                .regionId(tempObject.getSelectedData().getRegion().getId())
                .carId(tempObject.getSelectedData().getCars().get(0).getId())
                .waitingMessages(false)
                .build();
    }

    public static Map<String, String> createBackBntData(TempObject tempObject, int nextPage, ITempStorage tempStorage) {
        TempObject newTemp = tempObject.clone();
        Navigation navigation = new Navigation();
        navigation.setBack(true);
        navigation.setNext(false);
        navigation.setCurrentPage(nextPage);
        newTemp.setNavigation(navigation);
        String key = Util.generateToken(newTemp);
        tempStorage.set(key, newTemp.toString());
        return Collections.singletonMap("Назад", key);
    }

    public static Map<String, String> backStepBtn(TempObject tempObject, int currentStep, ITempStorage tempStorage) {
        TempObject newTemp = tempObject.clone();
        newTemp.setStep(currentStep - 1);
        String key = Util.generateToken(newTemp);
        tempStorage.set(key, newTemp.toString());
        return Collections.singletonMap("Шаг назад", key);
    }

    public static Map<String, String> createNextBntData(TempObject tempObject, int nextPage, ITempStorage tempStorage) {
        TempObject newTemp = tempObject.clone();
        Navigation navigation = new Navigation();
        navigation.setBack(false);
        navigation.setNext(true);
        navigation.setCurrentPage(nextPage);
        newTemp.setNavigation(navigation);
        String key = Util.generateToken(newTemp);
        tempStorage.set(key, newTemp.toString());
        return Collections.singletonMap("Еще..", key);
    }

    public static int defineCurrentPage(Navigation navigation) {
        if (navigation == null) {
            return 0;
        } else {
            return navigation.getCurrentPage();
        }
    }

    public static int defineNextPage(Navigation navigation, int currentPage) {
        if (navigation != null) {
            if (navigation.isNext()) {
                return currentPage + 1;
            }
            if (navigation.isBack()) {
                return Math.max(currentPage - 1, 0);
            }
        }
        return 1;
    }

}
