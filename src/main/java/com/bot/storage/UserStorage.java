package com.bot.storage;

import com.bot.common.Util;
import com.bot.model.Role;
import com.bot.model.User;
import com.bot.processor.IUserStorage;
import com.bot.storage.entity.ClientEntity;
import com.bot.storage.repository.CarRepository;
import com.bot.storage.repository.ClientRepository;
import com.bot.storage.repository.RegionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class UserStorage implements IUserStorage {
    @Autowired
    CarRepository carRepository;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    RegionRepository regionRepository;
    @Override
    public boolean checkUser(String id) {
        try {
            List<ClientEntity> c = clientRepository.findByLogin(id);
            return c.size() != 0;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public User getUser(String id) {
        List<ClientEntity> c = clientRepository.findByLogin(id);
        ClientEntity clientEntity = c.get(0);
        return (User) clientEntity.toModelObject();
    }

    @Override
    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>();
        List<Object[]> datas = clientRepository.getAll();
        for (Object[] o : datas) {
            User user = createUser(o);
            result.add(user);
        }
        return result;
    }

    @Override
    public List<User> getAllUsersByCarId(int carId) {
        List<ClientEntity> clientEntities = clientRepository.findAllByCarid(carId);
        List<User> users = new ArrayList<>();
        for (ClientEntity clientEntity : clientEntities) {
            users.add((User) clientEntity.toModelObject());
        }
        return users;
    }


    @Override
    public boolean saveUser(User user) {
        ClientEntity clientEntity = convertUserToClient(user, Integer.valueOf(user.getCarId()));
        try {
            Object o1 = clientRepository.save(clientEntity);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public List<User> findAdmins() {
        List<Object[]> datas = clientRepository.findAdmins();
        List<User> admins = new ArrayList<>();
        for (Object[] o : datas) {
            User user = createUser(o);
            admins.add(user);
        }
        return admins;
    }

    private User createUser(Object[] o) {
        User user = new User();
        user.setId((Integer) o[0]);
        user.setCreateDate((Date) o[1]);
        user.setRole(Util.findEnumConstant(Role.class, (String) o[2]));
        user.setLogin((String) o[3]);
        user.setRegionId((Integer) o[4]);
        user.setUserName((String) o[5]);
        user.setClientAction((String) o[6]);
        user.setWaitingMessages((Boolean) o[7]);
        user.setCarId((Integer) o[8]);
        user.setLastCallback((String) o[9]);
        return user;
    }


    private ClientEntity convertUserToClient(User user, Integer carId) {
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setId(user.getId());
        clientEntity.setUserName(user.getUserName());
        clientEntity.setCreatedDate(new java.util.Date());
        clientEntity.setRole(user.getRole().name());
        clientEntity.setRegionId(user.getRegionId());
        if (!(user.getCarId() == 0)) {
            clientEntity.setCarid(user.getCarId());
        } else {
            clientEntity.setCarid(carId);
        }
        clientEntity.setLastCallback(user.getLastCallback());
        clientEntity.setLogin(user.getLogin());
        clientEntity.setClientAction(user.getClientAction());
        clientEntity.setWaitingMessages(user.isWaitingMessages());
        return clientEntity;
    }
}