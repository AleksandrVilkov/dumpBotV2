package com.bot.processor.statistic;

import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.Action;
import com.bot.processor.IAccommodationStorage;
import com.bot.processor.ICarStorage;
import com.bot.processor.IUserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StatisticAction implements Action {
    @Autowired
    IUserStorage userStorage;
    @Autowired
    ICarStorage carStorage;

    @Autowired
    IAccommodationStorage accommodationStorage;

    @Override
    public MessageWrapper execute(Update update, TempObject tempObject, User user) {
        List<User> users = userStorage.getAllUsers();
        List<SendMessage> result = new ArrayList<>();
        MessageWrapper messageWrapper = new MessageWrapper();
        result.add(calcUserStat(update, users));
        result.add(calcCarsStat(update, users));
        result.add(calcAccommodationStat(update));
        messageWrapper.setSendMessage(result);
        return messageWrapper;
    }

    private SendMessage calcUserStat(Update update, List<User> users) {
        String userStatistic = "на текущий момент зарегистрировано пользователей: " + users.size();
        return new SendMessage(Util.getUserId(update), userStatistic);
    }

    private SendMessage calcCarsStat(Update update, List<User> users) {
        Map<Integer, Integer> carCount = new HashMap<>(); //id автомобиля и его количество
        for (User user : users) {
            int carId = user.getCarId();
            if (carCount.containsKey(carId)) {
                int count = carCount.get(carId);
                count++;
                carCount.put(carId, count);
            } else {
                carCount.put(carId, 1);
            }
        }
        StringBuilder carStatisticStringBuilder = new StringBuilder();
        carStatisticStringBuilder.append("Присутствуют следующие автомобили").append(":\n");
        for (Map.Entry entry : carCount.entrySet()) {
            Car car = carStorage.getCarById((Integer) entry.getKey());
            carStatisticStringBuilder.append(car.getBrand().getName())
                    .append(" ")
                    .append(car.getModel().getName())
                    .append(" ")
                    .append("в количестве")
                    .append(" ")
                    .append(entry.getValue())
                    .append("шт")
                    .append(";\n");
        }
        String carStatistic = carStatisticStringBuilder.toString();
        return new SendMessage(Util.getUserId(update), carStatistic);
    }

    private SendMessage calcAccommodationStat(Update update) {
        List<UserAccommodation> userAccommodations = accommodationStorage.getAll();

        int countSearch = 0;
        int approvedSearch = 0;
        int rejectedSearch = 0;

        int countSale = 0;
        int approvedSale = 0;
        int rejectedSale = 0;

        int topical = 0;

        for (UserAccommodation userAccommodation : userAccommodations) {
            if (userAccommodation.getType().equals(AccommodationType.SEARCH)) {
                countSearch++;
                if (userAccommodation.isApproved()) {
                    approvedSearch++;
                }
                if (userAccommodation.isRejected()) {
                    rejectedSearch++;
                }
            }
            if (userAccommodation.getType().equals(AccommodationType.SALE)) {
                countSale++;
                if (userAccommodation.isApproved()) {
                    approvedSale++;
                }
                if (userAccommodation.isRejected()) {
                    rejectedSale++;
                }
            }
            if (userAccommodation.isTopical()) {
                topical++;
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("СТАТИСТИКА\nВсего запросов: ")
                .append(userAccommodations.size())
                .append(" шт.\n")

                .append("Из ниx: \n - запросов на поиск запчастей - ")
                .append(countSearch)
                .append(" шт.\n")
                .append("- на продажу - ")
                .append(countSale)
                .append("\n\n")
                .append("Одобрено и размещено в канале :\n- ")
                .append(approvedSearch)
                .append(" запросов на поиск\n- ")
                .append(approvedSale)
                .append(" объявлений о продаже.\n\n")
                .append("Отклонено: \n- ")
                .append(rejectedSearch)
                .append(" запросов на поиск;\n- ")
                .append(rejectedSale)
                .append(" объявлений.\n")
                .append("На текущий момент не рассмотрено: ")
                .append(topical)
                .append(" заявкок.");
        return new SendMessage(Util.getUserId(update), stringBuilder.toString());
    }
}
