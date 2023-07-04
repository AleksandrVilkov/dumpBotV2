package com.bot.processor;

import com.bot.model.UserAccommodation;

import java.util.List;

public interface IAccommodationStorage {
    boolean saveAccommodation(UserAccommodation accommodation);

    List<UserAccommodation> getAll();

    UserAccommodation getById(int id);
    List<UserAccommodation> getAllByUserId(int userId);

    List<UserAccommodation> getAllInconsistent();

    UserAccommodation getFirstNotAgreed();

    int countNotAgreed();
}
