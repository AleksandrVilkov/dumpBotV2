package com.bot.storage.repository;

import com.bot.storage.entity.RegionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<RegionEntity, Integer> {
    @Query(value = "select * from region", nativeQuery = true)
    List<Object[]> getAllCities();

    Page<RegionEntity> findAll(Pageable pageable);

    Page<RegionEntity> findAllByCountryCode(String countryCode, Pageable pageable);

    @Query(value = "select DISTINCT country_code from region", nativeQuery = true)
    List<Object[]> getCountries();

    int countAllByCountryCode(String countryCode);


}