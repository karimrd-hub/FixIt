package com.example.fixit.module.product.airfilter.entity;

import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "air_filters")
@DiscriminatorValue("AIR_FILTER")
public class AirFilter extends Product {

    @Column(name = "filter_type", nullable = false, length = 20)
    private String filterType; // PANEL, ROUND, CONICAL

    @Column(name = "length_mm", nullable = false)
    private Integer lengthMm;

    @Column(name = "width_mm", nullable = false)
    private Integer widthMm;

    @Column(name = "height_mm", nullable = false)
    private Integer heightMm;

    @Column(name = "compatible_models", length = 500)
    private String compatibleModels;
}
