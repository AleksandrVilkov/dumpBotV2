package com.bot.storage.repository;

import com.bot.storage.entity.ClientEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends CrudRepository<ClientEntity, Integer> {
    List<ClientEntity> findByLogin(String login);

    @Query(value = "select * from client c WHERE c.role = 'ADMIN_ROLE'", nativeQuery = true)
    List<Object[]> findAdmins();

    @Query(value = "select * from client", nativeQuery = true)
    List<Object[]> getAll();

    List<ClientEntity> findAllByCarid(int carId);
}