package com.example.fixit.module.product.autoparts.transmissionfluid.entity;

import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "transmission_fluids")
@DiscriminatorValue("TRANSMISSION_FLUID")
public class TransmissionFluid extends Product {

    @Column(name = "fluid_type", nullable = false, length = 20)
    private String fluidType; // ATF, MTF, CVT, DCT

    @Column(name = "viscosity_grade", nullable = false, length = 20)
    private String viscosityGrade; // e.g. ATF+4, Dexron VI

    @Column(name = "volume_liters", nullable = false)
    private Double volumeLiters;

    @Column(name = "compatible_models", length = 500)
    private String compatibleModels;
}
