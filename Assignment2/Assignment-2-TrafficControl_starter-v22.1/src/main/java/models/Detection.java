package models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import static models.Car.CarType;
import static models.Car.FuelType;

public class Detection {
    private final Car car;                  // the car that was detected
    private final String city;              // the name of the city where the detector was located
    private final LocalDateTime dateTime;   // date and time of the detection event

    /* Representation Invariant:
     *      every Detection shall be associated with a valid Car
     */

    public Detection(Car car, String city, LocalDateTime dateTime) {
        this.car = car;
        this.city = city;
        this.dateTime = dateTime;
    }

    /**
     * Parses detection information from a line of text about a car that has entered an environmentally controlled zone
     * of a specified city.
     * the format of the text line is: licensePlate, city, dateTime
     * The licensePlate shall be matched with a car from the provided list.
     * If no matching car can be found, a new Car shall be instantiated with the given licensePlate and added to the list
     * (besides the license plate number there will be no other information available about this car)
     *
     * @param textLine
     * @param cars     a list of known cars, ordered and searchable by licensePlate
     *                 (i.e. the indexOf method of the list shall only consider the licensePlate when comparing cars)
     * @return a new Detection instance with the provided information
     * or null if the textLine is corrupt or incomplete
     */
    public static Detection fromLine(String textLine, List<Car> cars) {

        Detection newDetection = null;

        // extract the comma-separated fields from the textLine
        String[] fields = textLine.split(",");
        if (fields.length < 3) return null; // If the textLine is incomplete return null
        else {
            try {
                String licensePlate = fields[0].trim(); // Get the licensePlate number from the textLine

                Car newCar = new Car(licensePlate);

                // If car is matched with the licensePlate it will be the new car,
                // otherwise the newCar with only the licensePlate will be added to the cars list
                if (cars.indexOf(newCar) != -1) {
                    newCar = cars.get(cars.indexOf(newCar));
                } else {
                    cars.add(newCar);
                }

                // parse the fields and instantiate a new Detection
                newDetection = new Detection(
                        newCar,
                        fields[1].trim(),
                        LocalDateTime.parse(fields[2].trim())
                );
            } catch (Exception e) {
                // any of the parse and valueOf methods could throw an exception on a format mismatch
                System.out.printf("Could not parse Detection specification in text line '%s'\n", textLine);
                System.out.println(e.getMessage());
            }
        }
        return newDetection;
    }

    /**
     * Validates a detection against the purple conditions for entering an environmentally restricted zone
     * I.e.:
     * Diesel trucks and diesel coaches with an emission category of below 6 may not enter a purple zone
     *
     * @return a Violation instance if the detection saw an offence against the purple zone rule/
     * null if no offence was found.
     */
    public Violation validatePurple() {

        CarType cartype = this.car.getCarType();
        FuelType fuelType = this.car.getFuelType();
        int emissionCategory = this.car.getEmissionCategory();

        if (fuelType.equals(FuelType.valueOf("Diesel"))) {
            if ((cartype.equals(CarType.valueOf("Truck")) && emissionCategory < 6) ||
                    (cartype.equals(CarType.valueOf("Coach")) && emissionCategory < 6)) {
                return new Violation(car, city);
            }
        }
        return null;
    }

    public Car getCar() {
        return car;
    }

    public String getCity() {
        return city;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return car.getLicensePlate() + "/" + city + "/" + dateTime;
    }

}
