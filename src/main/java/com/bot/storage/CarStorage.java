package com.bot.storage;

import com.bot.model.Car;
import com.bot.processor.ICarStorage;
import com.bot.storage.entity.CarEntity;
import com.bot.storage.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CarStorage implements ICarStorage {
    @Autowired
    CarRepository carRepository;

    @Override
    public List<Car> getCars() {
        List<CarEntity> carEntityList = carRepository.findAll();
        List<Car> result = new ArrayList<>();
        carEntityList.forEach(carEntity -> {
            result.add((Car) carEntity.toModelObject());
        });
        return result;
    }

    @Override
    public Car getCarById(int id) {
        CarEntity car = carRepository.findById(id).orElseGet(CarEntity::new);
        return (Car) car.toModelObject();
    }
}
