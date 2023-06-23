package com.bot.storage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_accommodation_photo")
@NoArgsConstructor
@Getter
@Setter
public class PhotoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int photoId;
    @Column(name = "telegram_id")
    private String telegramId;
    @ManyToOne
    @JoinColumn(name = "user_accommodation_id", nullable = false)
    private UserAccommodationEntity userAccommodationEntity;
}