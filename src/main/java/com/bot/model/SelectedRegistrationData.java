package com.bot.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectedRegistrationData {
    List<Car> cars;
    Region region;
    String CountryCode;
}