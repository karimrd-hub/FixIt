package com.example.fixit.module.product.autoparts.shockabsorber.entity;

import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "shock_absorbers")
@DiscriminatorValue("SHOCK_ABSORBER")
public class ShockAbsorber extends Product {

    @Enumerated(EnumType.STRING)
    @Column(name = "absorber_type", nullable = false, length = 20)
    private ShockAbsorberType absorberType; // GAS, OIL, ELECTRONIC

    @Column(name = "axle_position", nullable = false, length = 10)
    private String axlePosition; // FRONT, REAR

    @Column(name = "extended_length_mm", nullable = false)
    private Integer extendedLengthMm;

    @Column(name = "compressed_length_mm", nullable = false)
    private Integer compressedLengthMm;

    @Column(name = "compatible_models", length = 500)
    private String compatibleModels;
}
