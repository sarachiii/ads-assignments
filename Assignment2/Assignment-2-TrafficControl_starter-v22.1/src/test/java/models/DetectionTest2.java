package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class DetectionTest2 {

    Car scoda, audi, mercedes, icova, daf1, daf2, kamaz;
    List<Car> cars;

    @BeforeEach
    private void setup() {
        Locale.setDefault(Locale.ENGLISH);
        scoda = new Car("1-AAA-02", 6, Car.CarType.Car, Car.FuelType.Gasoline, LocalDate.of(2014, 1, 31));
        audi = new Car("AA-11-BB", 4, Car.CarType.Car, Car.FuelType.Diesel, LocalDate.of(1998, 1, 31));
        mercedes = new Car("VV-11-BB", 3, Car.CarType.Truck, Car.FuelType.Diesel, LocalDate.of(1998, 1, 31));
        icova = new Car("1-TTT-99", 5, Car.CarType.Truck, Car.FuelType.Lpg, LocalDate.of(2011, 1, 31));
        daf1 = new Car("1-CCC-01", 1, Car.CarType.Coach, Car.FuelType.Diesel, LocalDate.of(2009, 1, 31));
        daf2 = new Car("1-CCC-02", 6, Car.CarType.Coach, Car.FuelType.Diesel, LocalDate.of(2011, 1, 31));
        kamaz = new Car("1-AAAA-0000");
        cars = new ArrayList<>(List.of(scoda, audi, mercedes, icova, daf1, daf2, kamaz));
    }

    @Test
    public void shouldNotReturnViolation() {
        Detection detection1 = Detection.fromLine("1-AAA-02,Amsterdam,2022-10-01T12:11:10", cars);
        Detection detection2 = Detection.fromLine("AA-11-BB, Rotterdam, 2022-10-01T12:11:10", cars);
        Detection detection3 = Detection.fromLine(" 1-TTT-99 , Den Haag , 2022-10-01T12:11:10", cars);
        Detection detection4 = Detection.fromLine(" 1-AAAA-0000 , Den Haag , 2022-10-01T12:11:10", cars);
        Detection detection6 = Detection.fromLine("1-CCC-02, Leiden , 2022-10-01T12:11:10", cars);

        assertNull(detection1.validatePurple());
        assertNull(detection2.validatePurple());
        assertNull(detection3.validatePurple());
        assertNull(detection4.validatePurple());
        assertNull(detection6.validatePurple());
    }

    @Test
    public void shouldReturnViolation() {
        Detection detection7 = Detection.fromLine(" VV-11-BB , Amsterdam , 2022-10-01T12:11:10", cars);

        assertNotNull(detection7.validatePurple());
        assertEquals(mercedes, detection7.validatePurple().getCar());
        assertEquals("Amsterdam", detection7.validatePurple().getCity());

        Detection detection8 = Detection.fromLine("1-CCC-01, Maastricht , 2022-10-01T12:11:10", cars);

        assertNotNull(detection8.validatePurple());
        assertEquals(daf1, detection8.validatePurple().getCar());
        assertEquals("Maastricht", detection8.validatePurple().getCity());
    }
}