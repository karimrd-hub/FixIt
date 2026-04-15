package com.example.fixit.common.seed;

import com.example.fixit.module.product.autoparts.airfilter.entity.AirFilter;
import com.example.fixit.module.product.autoparts.alternator.entity.Alternator;
import com.example.fixit.module.product.autoparts.battery.entity.Battery;
import com.example.fixit.module.product.autoparts.brake.entity.Brake;
import com.example.fixit.module.product.autoparts.brake.entity.BrakeType;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.entity.ProductCondition;
import com.example.fixit.module.product.entity.ProductStatus;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@Profile("seed")
public class DataSeeder implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    private final Random rng = new Random(42);

    // ── Lebanese geography ────────────────────────────────────────────────────

    /** governorate → district → cities */
    private static final Map<String, Map<String, List<String>>> LEBANON = new LinkedHashMap<>();

    static {
        // 1. Beirut
        Map<String, List<String>> beirut = new LinkedHashMap<>();
        beirut.put("Beirut", List.of("Achrafieh","Hamra","Ras Beirut","Mazraa","Mousaitbeh",
                "Gemmayzeh","Mar Mikhael","Raouche","Bachoura","Rmeil"));
        LEBANON.put("Beirut", beirut);

        // 2. Mount Lebanon
        Map<String, List<String>> mountLebanon = new LinkedHashMap<>();
        mountLebanon.put("Baabda", List.of("Baabda","Hadath","Hazmieh","Chiyah","Ghobeiry",
                "Hammana","Falougha","Furn el Chebbak","Haret Hreik","Ras el Metn"));
        mountLebanon.put("Aley", List.of("Aley","Bhamdoun","Souk El Gharb","Choueifat","Khalde",
                "Qabr Shmoun","Ain Dara","Mansourieh","Bayssour","Kahale"));
        mountLebanon.put("Matn", List.of("Jdeideh","Antelias","Bourj Hammoud","Bikfaya","Baskinta",
                "Dbayeh","Mansourieh","Sin el Fil","Baabdat","Brummana"));
        mountLebanon.put("Chouf", List.of("Beiteddine","Deir el Qamar","Baakline","Damour","Jiyeh",
                "Barouk","Mukhtara","Anout","Chehim","Naameh"));
        LEBANON.put("Mount Lebanon", mountLebanon);

        // 3. Keserwan-Jbeil
        Map<String, List<String>> keserwan = new LinkedHashMap<>();
        keserwan.put("Keserwan", List.of("Jounieh","Ajaltoun","Faraya","Ghazir","Harissa",
                "Zouk Mikael","Zouk Mosbeh","Kfardebian","Ballouneh","Ashqout"));
        keserwan.put("Jbeil", List.of("Byblos","Amchit","Halat","Nahr Ibrahim","Kartaba",
                "Aqoura","Ehmej","Tannourine","Lassa","Jaj"));
        LEBANON.put("Keserwan-Jbeil", keserwan);

        // 4. North
        Map<String, List<String>> north = new LinkedHashMap<>();
        north.put("Tripoli", List.of("Tripoli","Mina","Qalamoun","Beddawi","Abu Samra",
                "Bab al-Tabbaneh","Jabal Mohsen","Zahrieh","Bahsas","Basateen"));
        north.put("Zgharta", List.of("Zgharta","Ehden","Ardeh","Miziara","Majdlaya",
                "Kfarhata","Rachiine","Sebhel","Aitou","Bane"));
        north.put("Koura", List.of("Amioun","Kousba","Anfeh","Chekka","Deddeh",
                "Bterram","Afsdik","Bishmizzine","Enfeh","Kaftoun"));
        north.put("Batroun", List.of("Batroun","Douma","Hamat","Tannourine","Chekka",
                "Kfarabida","Ibrine","Hardine","Bchealeh","Zan"));
        north.put("Bsharri", List.of("Bsharri","Hadath El Jebbeh","Hasroun","Dimane","Qannoubine",
                "Bekaa Kafra","Tourza","Ban","Blouza","Qnat"));
        north.put("Miniyeh-Danniyeh", List.of("Minieh","Sir El Danniyeh","Bakhoun","Deir Ammar","Beddawi",
                "Nabi Youchaa","Asoun","Izal","Kfarhabou","Karm El Mohr"));
        LEBANON.put("North", north);

        // 5. Akkar
        Map<String, List<String>> akkar = new LinkedHashMap<>();
        akkar.put("Akkar", List.of("Halba","Kobayat","Akkar el-Atika","Berqayel","Bebnine",
                "Rahbe","Fneydeq","Arqa","Andaket","Wadi Khaled"));
        LEBANON.put("Akkar", akkar);

        // 6. Beqaa
        Map<String, List<String>> beqaa = new LinkedHashMap<>();
        beqaa.put("Zahlé", List.of("Zahlé","Chtaura","Barelias","Saadnayel","Rayak",
                "Qab Elias","Majdel Anjar","Anjar","Jdita","Ferzol"));
        beqaa.put("Western Beqaa", List.of("Joub Jannine","Saghbine","Qaraoun","Machghara","Kefraya",
                "Kherbet Qanafar","Mansoura","Sohmor","Yohmor","Baaloul"));
        beqaa.put("Rashaya", List.of("Rashaya","Kfar Mechki","Mdoukha","Ain Ata","Aiha",
                "Bakka","Deir El Ahmar","Kawkaba","Tannoura","Yanta"));
        LEBANON.put("Beqaa", beqaa);

        // 7. Baalbek-Hermel
        Map<String, List<String>> baalbek = new LinkedHashMap<>();
        baalbek.put("Baalbek", List.of("Baalbek","Arsal","Brital","Chmistar","Deir el Ahmar",
                "Labweh","Nabi Chit","Taraya","Younine","Bodai"));
        baalbek.put("Hermel", List.of("Hermel","Al-Qaa","Ras Baalbek","Chouaghir","Jouar el Hachich",
                "Kouakh","Fisane","Brissa","Sahlat el May","Zighrine"));
        LEBANON.put("Baalbek-Hermel", baalbek);

        // 8. South
        Map<String, List<String>> south = new LinkedHashMap<>();
        south.put("Sidon", List.of("Sidon","Maghdouche","Ghazieh","Sarafand","Abra",
                "Haret Saida","Bramieh","Mieh Mieh","Kfar Hatta","Qanarit"));
        south.put("Tyre", List.of("Tyre","Qana","Jouaiyya","Abbassieh","Borj el-Chimali",
                "Bazourieh","Maarakeh","Mansouri","Naqoura","Al-Samaaiyeh"));
        south.put("Jezzine", List.of("Jezzine","Bkassine","Kfarhouna","Roum","Azour",
                "Benouati","Aramta","Lebaa","Snaya","Machmouche"));
        LEBANON.put("South", south);

        // 9. Nabatieh
        Map<String, List<String>> nabatieh = new LinkedHashMap<>();
        nabatieh.put("Nabatieh", List.of("Nabatieh","Doueir","Jebchit","Kfar Remmane","Ansar",
                "Arabsalim","Houmine El Fawqa","Jbaa","Mayfadoun","Zefta"));
        nabatieh.put("Bint Jbeil", List.of("Bint Jbeil","Tibnin","Ayta ash-Shab","Rmeich","Ain Ebel",
                "Yaroun","Debel","Chakra","Ghandourieh","Hariss"));
        nabatieh.put("Marjeyoun", List.of("Marjeyoun","Khiam","Ebel el-Saqi","Kleiaa","Meiss el-Jabal",
                "Houla","Taybeh","Odaisseh","Markaba","Blida"));
        nabatieh.put("Hasbaya", List.of("Hasbaya","Chebaa","Kfar Chouba","Mari","El-Fardis",
                "Kawkaba","Mimis","Rachaya El Foukhar","Ein Qiniya","Chwaya"));
        LEBANON.put("Nabatieh", nabatieh);
    }

    // ── Brands per category ───────────────────────────────────────────────────

    private static final String[] TIRE_BRANDS    = {"Michelin","Bridgestone","Continental","Pirelli","Goodyear","Yokohama","Hankook","Falken"};
    private static final String[] BRAKE_BRANDS   = {"Brembo","Bosch","ATE","TRW","Ferodo","Akebono","EBC","Textar"};
    private static final String[] BATTERY_BRANDS = {"Varta","Bosch","Exide","Optima","Yuasa","Banner","Hella","Delphi"};
    private static final String[] FILTER_BRANDS  = {"Mann","Bosch","Mahle","Fram","K&N","Wix","Hengst","Purflux"};
    private static final String[] SHOCK_BRANDS   = {"Monroe","Bilstein","KYB","Sachs","Gabriel","Rancho","Koni","Tokico"};
    private static final String[] SPARK_BRANDS   = {"NGK","Bosch","Denso","Champion","Autolite","ACDelco","Motorcraft","Brisk"};
    private static final String[] ALT_BRANDS     = {"Bosch","Valeo","Denso","Hitachi","Mitsubishi","Delphi","Lucas","Hella"};
    private static final String[] SPRING_BRANDS  = {"Eibach","H&R","Bilstein","KW","Tein","Moog","Monroe","Sachs"};
    private static final String[] FLUID_BRANDS   = {"Castrol","Mobil","Shell","Valvoline","Fuchs","Liqui-Moly","Total","Motul"};

    private static final Set<String> HIGH_DENSITY = Set.of("Beirut", "North", "Nabatieh");
    private static final String[] STORE_SUFFIXES = {
            "Auto Parts", "Car Parts", "Motor Supplies", "Auto Center", "Parts Hub",
            "Auto Shop", "Car Care", "Auto Depot", "Parts Express", "Auto World",
            "Motor Parts", "Auto Zone", "Parts Plus", "Auto Pro", "Car Fix",
            "Auto Mart", "Parts City", "Auto Link", "Motor Hub", "Auto Spot"
    };

    // ── Entry point ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void run(String... args) {
        LEBANON.forEach((governorate, districts) ->
                districts.forEach((district, cities) -> {
                    if (HIGH_DENSITY.contains(governorate)) {
                        // Use ALL cities and create multiple stores per city
                        int storesPerCity = Math.max(1, 300 / (districts.size() * cities.size()));
                        cities.forEach(city -> {
                            for (int i = 0; i < storesPerCity; i++) {
                                createStore(governorate, district, city, STORE_SUFFIXES[i % STORE_SUFFIXES.length]);
                            }
                        });
                    } else {
                        pickRandom(cities, 5).forEach(city ->
                                createStore(governorate, district, city, "Auto Parts"));
                    }
                }));
        em.flush();
        System.out.println("[DataSeeder] Seed complete.");
    }

    // ── Store creation ────────────────────────────────────────────────────────

    private void createStore(String governorate, String district, String city, String suffix) {
        Store store = new Store();
        store.setName(city + " " + suffix);
        store.setDescription("Quality auto parts in " + city + ", " + district);
        store.setPhone("+961 " + (70 + rng.nextInt(10)) + " " + (100000 + rng.nextInt(900000)));
        store.setEmail("info@" + city.toLowerCase().replaceAll("[^a-z0-9]", "") + "autoparts.lb");
        store.setAddress(rng.nextInt(200) + 1 + " Main Street, " + city);
        store.setCity(city);
        store.setDistrict(district);
        store.setGovernorate(governorate);
        store.setStatus(StoreStatus.ACTIVE);
        store.setServiceType(pick(StoreServiceType.values()));
        store.setFulfillmentMode(pick(FullfillmentMode.values()));
        store.setRating(BigDecimal.valueOf(3.0 + rng.nextInt(20) / 10.0).setScale(1, RoundingMode.HALF_UP));
        em.persist(store);

        int productCount = 1 + rng.nextInt(10); // 1–10
        for (int i = 0; i < productCount; i++) {
            Product p = randomProduct(store);
            em.persist(p);
        }
    }

    // ── Random product dispatcher ─────────────────────────────────────────────

    private Product randomProduct(Store store) {
        return switch (rng.nextInt(10)) {
            case 0 -> buildTire(store);
            case 1 -> buildBrake(store);
            case 2 -> buildBattery(store);
            case 3 -> buildOilFilter(store);
            case 4 -> buildShockAbsorber(store);
            case 5 -> buildAirFilter(store);
            case 6 -> buildSparkPlug(store);
            case 7 -> buildAlternator(store);
            case 8 -> buildSuspensionSpring(store);
            default -> buildTransmissionFluid(store);
        };
    }

    // ── Product builders ──────────────────────────────────────────────────────

    private Tire buildTire(Store store) {
        Tire t = new Tire();
        base(t, store, pick(TIRE_BRANDS), "Tire", bd(60, 350));
        t.setWidthMm(pick(new Integer[]{175, 185, 195, 205, 215, 225, 235, 245, 255}));
        t.setAspectRatio(pick(new Integer[]{35, 40, 45, 50, 55, 60, 65, 70}));
        t.setRimDiameterInches(pick(new Integer[]{15, 16, 17, 18, 19, 20}));
        t.setSeason(pick(Season.values()));
        t.setLoadIndex(75 + rng.nextInt(40));
        t.setSpeedRating(pick(new String[]{"H","V","W","Y","T","S"}));
        t.setRunFlat(rng.nextBoolean());
        t.setFuelEfficiencyRating(pick(new String[]{"A","B","C","D","E","F","G"}));
        return t;
    }

    private Brake buildBrake(Store store) {
        Brake b = new Brake();
        base(b, store, pick(BRAKE_BRANDS), "Brake", bd(20, 150));
        b.setBrakeType(pick(BrakeType.values()));
        b.setAxlePosition(pick(new String[]{"FRONT","REAR"}));
        b.setDiameterMm(240 + rng.nextInt(120));
        b.setThicknessMm(10 + rng.nextInt(20));
        b.setCompatibleModels(randomCompatible());
        return b;
    }

    private Battery buildBattery(Store store) {
        Battery b = new Battery();
        base(b, store, pick(BATTERY_BRANDS), "Battery", bd(50, 250));
        b.setCapacityAh(pick(new Integer[]{44, 55, 60, 70, 74, 80, 88, 95, 100, 110}));
        b.setColdCrankingAmps(300 + rng.nextInt(500));
        b.setVoltage(12);
        b.setTerminalLayout(pick(new String[]{"TOP_LEFT","TOP_RIGHT"}));
        b.setLengthMm(230 + rng.nextInt(60));
        b.setWidthMm(165 + rng.nextInt(20));
        b.setHeightMm(175 + rng.nextInt(30));
        return b;
    }

    private OilFilter buildOilFilter(Store store) {
        OilFilter f = new OilFilter();
        base(f, store, pick(FILTER_BRANDS), "Oil Filter", bd(5, 30));
        f.setThreadSize(pick(new String[]{"M20x1.5","M22x1.5","M18x1.5","3/4-16 UNF"}));
        f.setOuterDiameterMm(65 + rng.nextInt(30));
        f.setHeightMm(70 + rng.nextInt(50));
        f.setBypassValvePressureKpa(70 + rng.nextInt(50));
        f.setCompatibleModels(randomCompatible());
        return f;
    }

    private ShockAbsorber buildShockAbsorber(Store store) {
        ShockAbsorber s = new ShockAbsorber();
        base(s, store, pick(SHOCK_BRANDS), "Shock Absorber", bd(40, 200));
        s.setAbsorberType(pick(ShockAbsorberType.values()));
        s.setAxlePosition(pick(new String[]{"FRONT","REAR"}));
        s.setExtendedLengthMm(400 + rng.nextInt(200));
        s.setCompressedLengthMm(250 + rng.nextInt(100));
        s.setCompatibleModels(randomCompatible());
        return s;
    }

    private AirFilter buildAirFilter(Store store) {
        AirFilter f = new AirFilter();
        base(f, store, pick(FILTER_BRANDS), "Air Filter", bd(8, 50));
        f.setFilterType(pick(new String[]{"PANEL","ROUND","CONICAL"}));
        f.setLengthMm(200 + rng.nextInt(150));
        f.setWidthMm(150 + rng.nextInt(100));
        f.setHeightMm(30 + rng.nextInt(50));
        f.setCompatibleModels(randomCompatible());
        return f;
    }

    private SparkPlug buildSparkPlug(Store store) {
        SparkPlug p = new SparkPlug();
        base(p, store, pick(SPARK_BRANDS), "Spark Plug", bd(3, 25));
        p.setPlugType(pick(SparkPlugType.values()));
        p.setThreadDiameterMm(pick(new Integer[]{10, 12, 14}));
        p.setThreadReachMm(pick(new Integer[]{12, 19, 26}));
        p.setGapMm(0.6 + rng.nextInt(7) * 0.1);
        p.setHeatRange(4 + rng.nextInt(6));
        p.setCompatibleModels(randomCompatible());
        return p;
    }

    private Alternator buildAlternator(Store store) {
        Alternator a = new Alternator();
        base(a, store, pick(ALT_BRANDS), "Alternator", bd(80, 350));
        a.setVoltage(12);
        a.setAmperage(pick(new Integer[]{70, 80, 90, 100, 110, 120, 140, 150, 180}));
        a.setPulleyType(pick(new String[]{"FIXED","DECOUPLER","OVERRUNNING"}));
        a.setCompatibleModels(randomCompatible());
        return a;
    }

    private SuspensionSpring buildSuspensionSpring(Store store) {
        SuspensionSpring s = new SuspensionSpring();
        base(s, store, pick(SPRING_BRANDS), "Suspension Spring", bd(30, 180));
        s.setAxlePosition(pick(new String[]{"FRONT","REAR"}));
        s.setSpringRateNPerMm(15 + rng.nextInt(35));
        s.setFreeLengthMm(300 + rng.nextInt(150));
        s.setCoilDiameterMm(10 + rng.nextInt(8));
        s.setCompatibleModels(randomCompatible());
        return s;
    }

    private TransmissionFluid buildTransmissionFluid(Store store) {
        TransmissionFluid f = new TransmissionFluid();
        base(f, store, pick(FLUID_BRANDS), "Transmission Fluid", bd(10, 60));
        f.setFluidType(pick(new String[]{"ATF","MTF","CVT","DCT"}));
        f.setViscosityGrade(pick(new String[]{"ATF+4","Dexron VI","Mercon V","CVT NS-3","MTF 75W-90"}));
        f.setVolumeLiters(pick(new Double[]{1.0, 2.0, 4.0, 5.0}));
        f.setCompatibleModels(randomCompatible());
        return f;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void base(Product p, Store store, String brand, String type, BigDecimal price) {
        p.setName(brand + " " + type);
        p.setBrand(brand);
        p.setPrice(price);
        p.setStock((long) (1 + rng.nextInt(50)));
        p.setStatus(ProductStatus.ACTIVE);
        p.setCondition(rng.nextInt(10) < 8 ? ProductCondition.NEW : ProductCondition.USED);
        p.setDescription(brand + " " + type + " — high quality auto part.");
        p.setStore(store);
    }

    private BigDecimal bd(int min, int max) {
        double val = min + rng.nextInt(max - min) + rng.nextDouble();
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP);
    }

    private <T> T pick(T[] arr) {
        return arr[rng.nextInt(arr.length)];
    }

    private List<String> pickRandom(List<String> list, int n) {
        List<String> copy = new ArrayList<>(list);
        Collections.shuffle(copy, rng);
        return copy.subList(0, Math.min(n, copy.size()));
    }

    private String randomCompatible() {
        String[] makes  = {"Toyota","Honda","Nissan","Hyundai","Kia","BMW","Mercedes","Volkswagen","Ford","Renault"};
        String[] models = {"Corolla","Civic","Sentra","Elantra","Sportage","3 Series","C-Class","Golf","Focus","Clio"};
        return pick(makes) + " " + pick(models) + ", " + pick(makes) + " " + pick(models);
    }
}
