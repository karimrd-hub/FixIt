package com.example.fixit.module.catalog.category;

import com.example.fixit.common.exception.BadRequestException;
import com.example.fixit.module.product.autoparts.airfilter.entity.AirFilter;
import com.example.fixit.module.product.autoparts.alternator.entity.Alternator;
import com.example.fixit.module.product.autoparts.shockabsorber.entity.ShockAbsorber;
import com.example.fixit.module.product.autoparts.battery.entity.Battery;
import com.example.fixit.module.product.autoparts.brake.entity.Brake;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.autoparts.oilfilter.entity.OilFilter;
import com.example.fixit.module.product.autoparts.sparkplug.entity.SparkPlug;
import com.example.fixit.module.product.autoparts.suspensionspring.entity.SuspensionSpring;
import com.example.fixit.module.product.autoparts.tire.entity.Tire;
import com.example.fixit.module.product.autoparts.transmissionfluid.entity.TransmissionFluid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ProductCategory {

    TIRE("tire", Tire.class),
    BRAKE("brake", Brake.class),
    BATTERY("battery", Battery.class),
    OIL_FILTER("oil-filter", OilFilter.class),
    SHOCK_ABSORBER("shock-absorber", ShockAbsorber.class),
    AIR_FILTER("air-filter", AirFilter.class),
    SPARK_PLUG("spark-plug", SparkPlug.class),
    ALTERNATOR("alternator", Alternator.class),
    SUSPENSION_SPRING("suspension-spring", SuspensionSpring.class),
    TRANSMISSION_FLUID("transmission-fluid", TransmissionFluid.class);

    private final String slug;
    private final Class<? extends Product> entityType;

    public static ProductCategory fromSlug(String slug) {
        return Arrays.stream(values())
                .filter(c -> c.slug.equalsIgnoreCase(slug))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unknown category: " + slug));
    }
}
