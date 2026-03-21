package com.example.fixit.module.product.autoparts.alternator.entity;

import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "alternators")
@DiscriminatorValue("ALTERNATOR")
public class Alternator extends Product {

    @Column(name = "voltage", nullable = false)
    private Integer voltage; // typically 12V or 24V

    @Column(name = "amperage", nullable = false)
    private Integer amperage; // output in amps

    @Column(name = "pulley_type", nullable = false, length = 20)
    private String pulleyType; // FIXED, DECOUPLER, OVERRUNNING

    @Column(name = "compatible_models", length = 500)
    private String compatibleModels;
}
