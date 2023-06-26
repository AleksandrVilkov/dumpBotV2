package com.bot.storage;

import com.bot.model.Region;
import com.bot.processor.IRegionStorage;
import com.bot.storage.entity.RegionEntity;
import com.bot.storage.repository.RegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class RegionStorage implements IRegionStorage {
    @Autowired
    RegionRepository regionRepository;

    @Override
    public List<Region> getAllCities() {
        List<Object[]> result = regionRepository.getAllCities();
        List<Region> cities = new ArrayList<>();
        for (Object[] o : result) {
            Region region = new Region();
            region.setId((Integer) o[0]);
            region.setName((String) o[1]);
            region.setCountryCode((String) o[2]);
            region.setRegionId((String) o[3]);
            cities.add(region);
        }

        return cities;
    }

    @Override
    public List<String> getCountries() {
        List<Object[]> searchResult = regionRepository.getCountries();
        List<String> result = new ArrayList<>();
        for (Object[] o : searchResult) {
            result.add((String) o[0]);
        }
        return result;
    }

    @Override
    public Region getCityById(int id) {
        RegionEntity re = regionRepository.findById(id).get();
        Region city = new Region();
        city.setCountryCode(re.getCountryCode());
        city.setId(re.getId());
        city.setName(re.getName());
        city.setRegionId(re.getRegionId());
        return city;
    }

    @Override
    public List<Region> getRegionPage(int pageNumber, int count, boolean sorted) {
        Pageable pageable = sorted
                ? PageRequest.of(pageNumber, count, Sort.by("name").descending())
                : PageRequest.of(pageNumber, count);
        Page<RegionEntity> searchResult = regionRepository.findAll(pageable);
        return searchResult.get()
                .map(regionEntity -> (Region) regionEntity.toModelObject())
                .collect(Collectors.toList());
    }

    @Override
    public int countAllByCountryCode(String countryCode) {
        return regionRepository.countAllByCountryCode(countryCode);
    }

    @Override
    public List<Region> getRegionPageByCountryCode(String countryCode, int pageNumber, int count, boolean sorted) {
        Pageable pageable;
        if (sorted) {
            pageable = PageRequest.of(pageNumber, count, Sort.by("name").ascending());
        } else {
            pageable = PageRequest.of(pageNumber, count);
        }
        Page<RegionEntity> searchResult = regionRepository.findAllByCountryCode(countryCode, pageable);
        List<RegionEntity> regionEntityList = searchResult.get().collect(Collectors.toList());
        List<Region> result = new ArrayList<>();
        regionEntityList.forEach(regionEntity -> {
            result.add((Region) regionEntity.toModelObject());
        });
        return result;
    }
}
