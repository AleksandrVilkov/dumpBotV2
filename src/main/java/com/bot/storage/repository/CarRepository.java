package com.bot.storage.repository;

import com.bot.storage.entity.CarEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends CrudRepository<CarEntity, Integer> {

    public List<CarEntity> findAll();
}