package com.bot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Region implements ModelObject {
    private int id;
    private String countryCode;
    private String regionId;
    private String name;
}