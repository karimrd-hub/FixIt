package com.example.fixit.common;

import com.example.fixit.module.product.autoparts.airfilter.entity.AirFilter;
import com.example.fixit.module.product.autoparts.alternator.entity.Alternator;
import com.example.fixit.module.product.autoparts.battery.entity.Battery;
import com.example.fixit.module.product.autoparts.brake.entity.Brake;
import com.example.fixit.module.product.autoparts.brake.entity.BrakeType;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.entity.ProductCondition;
import com.example.fixit.module.product.entity.ProductStatus;
import com.example.fixit.module.product.image.entity.ProductImage;
import com.example.fixit.module.product.autoparts.oilfilter.entity.OilFilter;
import com.example.fixit.module.product.autoparts.shockabsorber.entity.ShockAbsorber;
import com.example.fixit.module.product.autoparts.shockabsorber.entity.ShockAbsorberType;
import com.example.fixit.module.product.autoparts.sparkplug.entity.SparkPlug;
import com.example.fixit.module.product.autoparts.sparkplug.entity.SparkPlugType;
import com.example.fixit.module.product.autoparts.suspensionspring.entity.SuspensionSpring;
import com.example.fixit.module.product.autoparts.tire.entity.Season;
import com.example.fixit.module.product.autoparts.tire.entity.Tire;
import com.example.fixit.module.product.autoparts.transmissionfluid.entity.TransmissionFluid;
import com.example.fixit.module.store.entity.FullfillmentMode;
import com.example.fixit.module.store.entity.Store;
import com.example.fixit.module.store.entity.StoreServiceType;
import com.example.fixit.module.store.entity.StoreStatus;

import java.math.BigDecimal;

/**
 * Reusable factory for building test entities with sensible defaults.
 * Every method returns a detached entity (no ID set) ready to be persisted.
 * TestDataFactory is a plain utility class and provides static factory methods for building detached entities with fixed, predictable values.
 * It's designed for unit/integration tests where we need a known Brake with exactly 300mm diameter or a Battery at exactly $150.00, no randomness involved.
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    // ── Store ─────────────────────────────────────────────────────────────────

    public static Store store() {
        return store("Test Store", "Beirut", "Beirut", "Achrafieh");
    }

    public static Store store(String name, String governorate, String district, String city) {
        Store s = new Store();
        s.setName(name);
        s.setDescription("Test store in " + city);
        s.setPhone("+961 70 123456");
        s.setEmail("test@store.lb");
        s.setAddress("1 Main Street, " + city);
        s.setCity(city);
        s.setDistrict(district);
        s.setGovernorate(governorate);
        s.setStatus(StoreStatus.ACTIVE);
        s.setServiceType(StoreServiceType.AUTOPARTS_ONLY);
        s.setFulfillmentMode(FullfillmentMode.IN_SHOP);
        s.setRating(BigDecimal.valueOf(4.5));
        return s;
    }

    // ── Base product helper ───────────────────────────────────────────────────

    private static void base(Product p, Store store, String brand, String name, BigDecimal price) {
        p.setName(name);
        p.setBrand(brand);
        p.setPrice(price);
        p.setStock(10L);
        p.setStatus(ProductStatus.ACTIVE);
        p.setCondition(ProductCondition.NEW);
        p.setDescription(brand + " " + name + " — test product");
        p.setStore(store);
    }

    // ── Product subtypes ──────────────────────────────────────────────────────

    public static Brake brake(Store store) {
        Brake b = new Brake();
        base(b, store, "Brembo", "Brembo Brake", BigDecimal.valueOf(85.00));
        b.setBrakeType(BrakeType.DISC);
        b.setAxlePosition("FRONT");
        b.setDiameterMm(300);
        b.setThicknessMm(25);
        b.setCompatibleModels("Toyota Corolla, Honda Civic");
        return b;
    }

    public static Tire tire(Store store) {
        Tire t = new Tire();
        base(t, store, "Michelin", "Michelin Tire", BigDecimal.valueOf(120.00));
        t.setWidthMm(205);
        t.setAspectRatio(55);
        t.setRimDiameterInches(16);
        t.setSeason(Season.ALL_SEASON);
        t.setLoadIndex(91);
        t.setSpeedRating("H");
        t.setRunFlat(false);
        t.setFuelEfficiencyRating("B");
        return t;
    }

    public static Battery battery(Store store) {
        Battery b = new Battery();
        base(b, store, "Varta", "Varta Battery", BigDecimal.valueOf(150.00));
        b.setCapacityAh(74);
        b.setColdCrankingAmps(680);
        b.setVoltage(12);
        b.setTerminalLayout("TOP_LEFT");
        b.setLengthMm(278);
        b.setWidthMm(175);
        b.setHeightMm(190);
        return b;
    }

    public static OilFilter oilFilter(Store store) {
        OilFilter f = new OilFilter();
        base(f, store, "Mann", "Mann Oil Filter", BigDecimal.valueOf(12.50));
        f.setThreadSize("M20x1.5");
        f.setOuterDiameterMm(76);
        f.setHeightMm(100);
        f.setBypassValvePressureKpa(90);
        f.setCompatibleModels("Nissan Sentra, Hyundai Elantra");
        return f;
    }

    public static AirFilter airFilter(Store store) {
        AirFilter f = new AirFilter();
        base(f, store, "K&N", "K&N Air Filter", BigDecimal.valueOf(35.00));
        f.setFilterType("PANEL");
        f.setLengthMm(280);
        f.setWidthMm(200);
        f.setHeightMm(40);
        f.setCompatibleModels("BMW 3 Series, Mercedes C-Class");
        return f;
    }

    public static ShockAbsorber shockAbsorber(Store store) {
        ShockAbsorber s = new ShockAbsorber();
        base(s, store, "Monroe", "Monroe Shock Absorber", BigDecimal.valueOf(95.00));
        s.setAbsorberType(ShockAbsorberType.GAS);
        s.setAxlePosition("FRONT");
        s.setExtendedLengthMm(500);
        s.setCompressedLengthMm(310);
        s.setCompatibleModels("Ford Focus, Renault Clio");
        return s;
    }

    public static SparkPlug sparkPlug(Store store) {
        SparkPlug p = new SparkPlug();
        base(p, store, "NGK", "NGK Spark Plug", BigDecimal.valueOf(8.00));
        p.setPlugType(SparkPlugType.IRIDIUM);
        p.setThreadDiameterMm(14);
        p.setThreadReachMm(19);
        p.setGapMm(0.8);
        p.setHeatRange(6);
        p.setCompatibleModels("Kia Sportage, Volkswagen Golf");
        return p;
    }

    public static Alternator alternator(Store store) {
        Alternator a = new Alternator();
        base(a, store, "Bosch", "Bosch Alternator", BigDecimal.valueOf(220.00));
        a.setVoltage(12);
        a.setAmperage(120);
        a.setPulleyType("FIXED");
        a.setCompatibleModels("Toyota Corolla, Honda Civic");
        return a;
    }

    public static SuspensionSpring suspensionSpring(Store store) {
        SuspensionSpring s = new SuspensionSpring();
        base(s, store, "Eibach", "Eibach Suspension Spring", BigDecimal.valueOf(75.00));
        s.setAxlePosition("REAR");
        s.setSpringRateNPerMm(25);
        s.setFreeLengthMm(350);
        s.setCoilDiameterMm(14);
        s.setCompatibleModels("Hyundai Elantra, Kia Sportage");
        return s;
    }

    public static TransmissionFluid transmissionFluid(Store store) {
        TransmissionFluid f = new TransmissionFluid();
        base(f, store, "Castrol", "Castrol Transmission Fluid", BigDecimal.valueOf(28.00));
        f.setFluidType("ATF");
        f.setViscosityGrade("Dexron VI");
        f.setVolumeLiters(4.0);
        f.setCompatibleModels("Mercedes C-Class, BMW 3 Series");
        return f;
    }

    // ── ProductImage ──────────────────────────────────────────────────────────

    public static ProductImage productImage(Product product, boolean primary) {
        ProductImage img = new ProductImage();
        img.setProduct(product);
        img.setUrl("https://cdn.example.com/images/test.jpg");
        img.setS3Key("images/test.jpg");
        img.setPrimary(primary);
        img.setDisplayOrder(primary ? 0 : 1);
        img.setFileName("test.jpg");
        img.setFileSizeBytes(50000L);
        img.setContentType(ProductImage.ImageContentType.IMAGE_JPEG);
        return img;
    }
}
