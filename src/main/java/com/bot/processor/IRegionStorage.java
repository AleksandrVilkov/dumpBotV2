package com.bot.processor;

import com.bot.model.Region;

import java.util.List;

public interface IRegionStorage {
    List<Region> getAllCities();
    List<String> getCountries();
    Region getCityById(int id);
    List<Region> getRegionPage(int pageNumber, int count, boolean sorted);
    int countAllByCountryCode(String countryCode);
    List<Region> getRegionPageByCountryCode(String countryCode, int pageNumber, int count, boolean sorted);
}
