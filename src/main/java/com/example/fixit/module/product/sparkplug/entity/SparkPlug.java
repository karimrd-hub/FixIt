package com.example.fixit.module.product.sparkplug.entity;

import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "spark_plugs")
@DiscriminatorValue("SPARK_PLUG")
public class SparkPlug extends Product {

    @Enumerated(EnumType.STRING)
    @Column(name = "plug_type", nullable = false, length = 20)
    private SparkPlugType plugType;

    @Column(name = "thread_diameter_mm", nullable = false)
    private Integer threadDiameterMm;

    @Column(name = "thread_reach_mm", nullable = false)
    private Integer threadReachMm;

    @Column(name = "gap_mm", nullable = false)
    private Double gapMm;

    @Column(name = "heat_range", nullable = false)
    private Integer heatRange;

    @Column(name = "compatible_models", length = 500)
    private String compatibleModels;
}
