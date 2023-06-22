package com.bot.storage.entity;

import com.bot.model.ModelObject;
import com.bot.model.Region;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "region")
@NoArgsConstructor
@Getter
@Setter
public class RegionEntity implements EntityObject{
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "country_code")
    private String countryCode;
    @Column(name = "region_id")
    private String regionId;
    @Override
    public ModelObject toModelObject() {
        Region region = new Region();
        region.setRegionId((String.valueOf(this.getId())));
        region.setName(this.getName());
        region.setCountryCode(this.countryCode);
        region.setId(this.id);
        return region;
    }
}