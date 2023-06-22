package com.bot.processor;

import com.bot.model.User;

import java.util.List;

public interface IUserStorage {
    boolean checkUser(String id);
    User getUser(String id);
    List<User> getAllUsers();
    List<User> getAllUsersByCarId(int carId);
    boolean saveUser(User user);
    List<User> findAdmins();
}
