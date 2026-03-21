package com.example.fixit.module.product.autoparts.tire.entity;

import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="tires")
@DiscriminatorValue("TIRE")
public class Tire extends Product {

    @Column(name = "width_mm", nullable = false)
    private Integer widthMm;

    @Column(name = "aspect_ratio", nullable = false)
    private Integer aspectRatio;

    @Column(name = "rim_diameter_inches", nullable = false)
    private Integer rimDiameterInches;

    @Enumerated(EnumType.STRING)
    @Column(name = "season", nullable = false, length = 20)
    private Season season;

    @Column(name = "load_index", nullable = false)
    private Integer loadIndex;

    @Column(name = "speed_rating", nullable = false, length = 5)
    private String speedRating;

    @Column(name = "run_flat", nullable = false)
    private Boolean runFlat = false;

    @Column(name = "fuel_efficiency_rating", length = 2)
    private String fuelEfficiencyRating; // EU label A-G
}
