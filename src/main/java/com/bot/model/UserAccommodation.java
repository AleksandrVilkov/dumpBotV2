package com.bot.model;

import lombok.*;

import java.util.Date;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAccommodation implements ModelObject {
    private int id;
    private AccommodationType type;
    private List<String> carsId;
    private Date createdDate;
    private String clientLogin;
    private int clientId;
    private int price;
    private boolean approved;
    private boolean rejected;
    private boolean topical;
    private String description;
    private List<String> photos;
}
