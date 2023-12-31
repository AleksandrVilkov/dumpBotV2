package com.bot.storage.repository;

import com.bot.storage.entity.UserAccommodationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAccommodationRepository extends CrudRepository<UserAccommodationEntity, Integer> {
    List<UserAccommodationEntity> findAllByTopical(boolean topical);
    List<UserAccommodationEntity> findAllByClientId(int clientId);
    UserAccommodationEntity findFirstByTopical(boolean topical);
    int countAllByTopical(boolean topical);
}
