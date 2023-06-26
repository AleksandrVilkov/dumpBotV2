package com.bot.processor;

import com.bot.model.Car;

import java.util.List;

public interface ICarStorage {
    List<Car> getCars();
    Car getCarById(int id);
}
