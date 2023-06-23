package com.bot.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OptionData {
    private String countryCode;
    private Region region;
    private List<Car> carList = new ArrayList<>();
    private String carValue;
}
