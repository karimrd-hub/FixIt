package com.example.fixit.module.product.autoparts.suspensionspring.entity;

import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "suspension_springs")
@DiscriminatorValue("SUSPENSION_SPRING")
public class SuspensionSpring extends Product {

    @Column(name = "axle_position", nullable = false, length = 10)
    private String axlePosition; // FRONT, REAR

    @Column(name = "spring_rate_n_per_mm", nullable = false)
    private Integer springRateNPerMm;

    @Column(name = "free_length_mm", nullable = false)
    private Integer freeLengthMm;

    @Column(name = "coil_diameter_mm", nullable = false)
    private Integer coilDiameterMm;

    @Column(name = "compatible_models", length = 500)
    private String compatibleModels;
}
