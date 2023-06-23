package com.bot.storage;

import com.bot.model.UserAccommodation;
import com.bot.processor.IAccommodationStorage;
import com.bot.storage.entity.CarAccommodationEntity;
import com.bot.storage.entity.PhotoEntity;
import com.bot.storage.entity.UserAccommodationEntity;
import com.bot.storage.repository.UserAccommodationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AccommodationStorage implements IAccommodationStorage {

    @Autowired
    UserAccommodationRepository accommodationRepository;


    @Override
    public boolean saveAccommodation(UserAccommodation accommodation) {
        try {
            accommodationRepository.save(convertToEntity(accommodation));
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public List<UserAccommodation> getAll() {
        Iterable<UserAccommodationEntity> data = accommodationRepository.findAll();
        List<UserAccommodation> result = new ArrayList<>();
        for (UserAccommodationEntity o : data) {
            result.add((UserAccommodation) o.toModelObject());
        }
        return result;
    }

    @Override
    public UserAccommodation getById(int id) {
        UserAccommodationEntity res = accommodationRepository.findById(id).get();
        return (UserAccommodation) res.toModelObject();
    }

    @Override
    public List<UserAccommodation> getAllInconsistent() {
        List<UserAccommodationEntity> data = accommodationRepository.findAllByTopical(true);
        List<UserAccommodation> result = new ArrayList<>();
        for (UserAccommodationEntity userAccommodationEntity : data) {
            result.add((UserAccommodation) userAccommodationEntity.toModelObject());
        }
        return result;
    }
    private UserAccommodationEntity convertToEntity(UserAccommodation accommodation) {
        UserAccommodationEntity result = new UserAccommodationEntity();
        result.setId(accommodation.getId());
        result.setCreatedDate(accommodation.getCreatedDate());
        result.setClientLogin(accommodation.getClientLogin());
        result.setClientId(accommodation.getClientId());
        result.setMinPrice(accommodation.getPrice());
        result.setMaxPrice(accommodation.getPrice());
        result.setApproved(accommodation.isApproved());
        result.setRejected(accommodation.isRejected());
        result.setTopical(accommodation.isTopical());
        result.setDescription(accommodation.getDescription());
        result.setType(accommodation.getType().toString());
        Set<PhotoEntity> set = new HashSet<>();
        if (accommodation.getPhotos() != null) {
            for (String photo : accommodation.getPhotos()) {
                PhotoEntity photoEntity = new PhotoEntity();
                photoEntity.setUserAccommodationEntity(result);
                photoEntity.setTelegramId(photo);
                set.add(photoEntity);
            }
        }

        Set<CarAccommodationEntity> cae = new HashSet<>();
        if (accommodation.getCarsId() != null) {
            for (String carId: accommodation.getCarsId()) {
                CarAccommodationEntity carAccommodationEntity = new CarAccommodationEntity();
                carAccommodationEntity.setUserAccommodationEntity(result);
                carAccommodationEntity.setCarId(carId);
                cae.add(carAccommodationEntity);
            }
        }

        result.setCars(cae);
        result.setPhoto(set);
        return result;
    }
}