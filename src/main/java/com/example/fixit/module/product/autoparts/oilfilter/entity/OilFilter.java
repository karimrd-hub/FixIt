package com.example.fixit.module.product.autoparts.oilfilter.entity;

import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "oil_filters")
@DiscriminatorValue("OIL_FILTER")
public class OilFilter extends Product {

    @Column(name = "thread_size", nullable = false, length = 20)
    private String threadSize; // e.g. M20x1.5

    @Column(name = "outer_diameter_mm", nullable = false)
    private Integer outerDiameterMm;

    @Column(name = "height_mm", nullable = false)
    private Integer heightMm;

    @Column(name = "bypass_valve_pressure_kpa")
    private Integer bypassValvePressureKpa;

    @Column(name = "compatible_models", length = 500)
    private String compatibleModels;
}
