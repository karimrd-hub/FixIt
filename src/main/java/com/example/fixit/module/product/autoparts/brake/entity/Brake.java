package com.example.fixit.module.product.autoparts.brake.entity;

import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "brakes")
@DiscriminatorValue("BRAKE")
public class Brake extends Product {

    @Enumerated(EnumType.STRING)
    @Column(name = "brake_type", nullable = false, length = 20)
    private BrakeType brakeType; // DISC, DRUM

    @Column(name = "axle_position", nullable = false, length = 10)
    private String axlePosition; // FRONT, REAR

    @Column(name = "diameter_mm", nullable = false)
    private Integer diameterMm;

    @Column(name = "thickness_mm", nullable = false)
    private Integer thicknessMm;

    @Column(name = "compatible_models", length = 500)
    private String compatibleModels;
}
