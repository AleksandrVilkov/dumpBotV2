package com.bot.processor.registration;

import com.bot.common.Util;
import com.bot.model.Navigation;
import com.bot.model.TempObject;
import com.bot.processor.ITempStorage;

import java.util.Collections;
import java.util.Map;

public class RegistrationHelper {
    public static Map<String, String> createBackBntData(TempObject tempObject, int nextPage, ITempStorage tempStorage) {
        TempObject newTemp = tempObject.clone();
        Navigation navigation = new Navigation();
        navigation.setBack(true);
        navigation.setNext(false);
        navigation.setCurrentPage(nextPage);
        newTemp.getData().setNavigation(navigation);
        String key = Util.generateToken(newTemp);
        tempStorage.set(key, newTemp.toString());
        //"&#9654
        return Collections.singletonMap("Назад", key);
    }

    public static Map<String, String> createNextBntData(TempObject tempObject, int nextPage, ITempStorage tempStorage) {
        TempObject newTemp = tempObject.clone();
        Navigation navigation = new Navigation();
        navigation.setBack(false);
        navigation.setNext(true);
        navigation.setCurrentPage(nextPage);
        newTemp.getData().setNavigation(navigation);
        String key = Util.generateToken(newTemp);
        tempStorage.set(key, newTemp.toString());
        //"&#9654
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
