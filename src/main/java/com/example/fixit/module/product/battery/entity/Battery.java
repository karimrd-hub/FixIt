package com.example.fixit.module.product.battery.entity;

import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "batteries")
@DiscriminatorValue("BATTERY")
public class Battery extends Product {

    @Column(name = "capacity_ah", nullable = false)
    private Integer capacityAh; // Ampere-hours

    @Column(name = "cold_cranking_amps", nullable = false)
    private Integer coldCrankingAmps; // CCA

    @Column(name = "voltage", nullable = false)
    private Integer voltage; // typically 12V

    @Column(name = "terminal_layout", nullable = false, length = 20)
    private String terminalLayout; // e.g. TOP_LEFT, TOP_RIGHT

    @Column(name = "length_mm", nullable = false)
    private Integer lengthMm;

    @Column(name = "width_mm", nullable = false)
    private Integer widthMm;

    @Column(name = "height_mm", nullable = false)
    private Integer heightMm;
}
