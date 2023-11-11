package com.bot.enums;

import lombok.Getter;

@Getter
public enum ChatMemberStatus {
    MEMBER("member"),      //пользователь является подписчиком;
    LEFT("left"),          //пользователь не подписан;
    KICKED("kicked"),      // пользователь заблокирован;
    ADMIN("admin"),        //админ
    CREATOR("admin");      //создатель

    final String name;

    ChatMemberStatus(String name) {
        this.name = name;
    }
}
