package com.bot.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OptionData implements Cloneable {
    private String countryCode;
    private Region region;
    private List<Car> carList = new ArrayList<>();
    private String carValue;

    @Override
    public OptionData clone() {
        try {
            OptionData clone = (OptionData) super.clone();
            clone.setCarList(new ArrayList<>(this.carList));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
