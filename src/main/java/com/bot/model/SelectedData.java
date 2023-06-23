package com.bot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class SelectedData {
    List<Car> cars;
    Region region;
    String CountryCode;
    List<String> photos;
    public SelectedData() {
        this.photos = new ArrayList<>();
    }
}
