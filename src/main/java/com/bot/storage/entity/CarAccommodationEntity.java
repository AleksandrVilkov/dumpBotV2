package com.bot.storage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_accommodation_car")
@NoArgsConstructor
@Getter
@Setter
public class CarAccommodationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int carAccommodationId;
    @Column(name = "carId", nullable = false)
    private String carId;
    @ManyToOne
    @JoinColumn(name="user_accommodation_id", nullable = false)
    private UserAccommodationEntity userAccommodationEntity;
}