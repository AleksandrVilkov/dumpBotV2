package com.bot.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ButtonWrapper {
    String name;
    String key;
    TempObject tempObject;
}
