package com.bot.storage.entity;

import com.bot.common.Util;
import com.bot.model.AccommodationType;
import com.bot.model.ModelObject;
import com.bot.model.UserAccommodation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_accommodation")
@NoArgsConstructor
@Getter
@Setter
public class UserAccommodationEntity implements EntityObject {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "client_login")
    private String clientLogin;

    @Column(name = "client_id")
    private int clientId;

    @Column(name = "min_price")
    private int minPrice;

    @Column(name = "max_price")
    private int maxPrice;

    @Column(name = "approved")
    private boolean approved;

    @Column(name = "rejected")
    private boolean rejected;

    @Column(name = "topical")
    private boolean topical;

    @Column(name = "description")
    private String description;
    @Column(name = "type")
    private String type;
    @OneToMany(mappedBy = "userAccommodationEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Set<PhotoEntity> photo;
    @OneToMany(mappedBy = "userAccommodationEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Set<CarAccommodationEntity> cars;

    @Override
    public ModelObject toModelObject() {
        UserAccommodation userAccommodation = new UserAccommodation();
        userAccommodation.setId(this.id);
        //TODO
        //userAccommodation.setCreatedDate((java.sql.Date) this.getCreatedDate());
        userAccommodation.setType(Util.findEnumConstant(AccommodationType.class, this.type));
        userAccommodation.setClientLogin(this.clientLogin);
        userAccommodation.setClientId(this.clientId);
        userAccommodation.setPrice(this.maxPrice);
        userAccommodation.setApproved(this.isApproved());
        userAccommodation.setRejected(this.isRejected());
        userAccommodation.setTopical(this.isTopical());
        userAccommodation.setDescription(this.getDescription());
        List<String> photos = new ArrayList<>();
        for (PhotoEntity photoEntity : this.photo) {
            photos.add(photoEntity.getTelegramId());
        }
        List<String> carsId = new ArrayList<>();
        for (CarAccommodationEntity cae : this.cars) {
            carsId.add(cae.getCarId());
        }
        userAccommodation.setCarsId(carsId);
        userAccommodation.setPhotos(photos);
        return userAccommodation;
    }
}
