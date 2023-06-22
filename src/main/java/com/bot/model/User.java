package com.bot.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User  implements ModelObject {
    private int id;
    private String userName;
    private Date createDate;
    private Role role;
    private String login;
    private int regionId;
    private int carId;
    private boolean waitingMessages;
    private String clientAction;
    private String lastCallback;

    public User(Date createDate, Role role, String login, int regionId, int carId) {
        this.createDate = createDate;
        this.role = role;
        this.login = login;
        this.regionId = regionId;
        this.carId = carId;
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}