package com.bot.processor.registration;

import com.bot.common.Util;
import com.bot.model.*;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Date;

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

    public static ButtonWrapper createBackBntData(TempObject tempObject, int nextPage) {
        TempObject newTemp = tempObject.clone();
        Navigation navigation = new Navigation();
        navigation.setBack(true);
        navigation.setNext(false);
        navigation.setCurrentPage(nextPage);
        newTemp.setNavigation(navigation);
        return new ButtonWrapper("Назад", Util.generateToken(newTemp), newTemp);
    }

    public static ButtonWrapper createNextBntData(TempObject tempObject, int nextPage) {
        TempObject newTemp = tempObject.clone();
        Navigation navigation = new Navigation();
        navigation.setBack(false);
        navigation.setNext(true);
        navigation.setCurrentPage(nextPage);
        newTemp.setNavigation(navigation);
        return new ButtonWrapper("Еще..", Util.generateToken(newTemp), newTemp);
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
