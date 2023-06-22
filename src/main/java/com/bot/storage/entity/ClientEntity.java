package com.bot.storage.entity;

import com.bot.common.Util;
import com.bot.model.ModelObject;
import com.bot.model.Role;
import com.bot.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "CLIENT")
@NoArgsConstructor
@Getter
@Setter
public class ClientEntity implements EntityObject {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "createdDate")
    private Date createdDate;
    @Column(name = "role")
    private String role;
    @Column(name = "login")
    private String login;
    @Column(name = "regionId")
    private int regionId;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "carid")
    private int carid;
    @Column(name = "waiting_messages")
    private boolean waitingMessages;
    @Column(name = "clientAction")
    private String clientAction;
    @Column(name = "lastCallback")
    private String lastCallback;


    @Override
    public ModelObject toModelObject() {
        return new User(
                this.getId(),
                this.getUserName(),
                this.getCreatedDate(),
                Util.findEnumConstant(Role.class, this.getRole()),
                this.getLogin(),
                this.getRegionId(),
                this.getCarid(),
                this.isWaitingMessages(),
                this.getClientAction(),
                this.getLastCallback()
        );
    }
}