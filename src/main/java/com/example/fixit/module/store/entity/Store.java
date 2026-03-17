package com.example.fixit.module.store.entity;

import com.example.fixit.common.entity.AuditableEntity;
import com.example.fixit.module.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "stores")
public class Store extends AuditableEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "governorate", nullable = false, length = 100)
    private String governorate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StoreStatus status = StoreStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 25)
    private StoreServiceType serviceType = StoreServiceType.AUTOPARTS_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_mode", nullable = false, length = 20)
    private FullfillmentMode fulfillmentMode;


    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @Column(name = "rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal rating = BigDecimal.ZERO;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
}
