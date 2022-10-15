package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrafficTrackerTest2 {
    private final static String VAULT_NAME = "/test1";

    TrafficTracker trafficTracker;

    @BeforeEach
    private void setup() {
        Locale.setDefault(Locale.ENGLISH);
        trafficTracker = new TrafficTracker();

        trafficTracker.importCarsFromVault(VAULT_NAME + "/cars.txt");

        trafficTracker.importDetectionsFromVault(VAULT_NAME + "/detections");
    }

    @Test
    public void calculateTotalFines(){
        assertEquals(7, trafficTracker.getViolations().stream().mapToInt(Violation::getOffencesCount).sum(),
                "Total number of offences across all Violation instances did not match.");
        assertEquals(175.0, trafficTracker.calculateTotalFines(),
                "Total revenue of fines from all offences did not match");
    }

    @Test
    public void topViolationsByCar(){

    }

    @Test
    public void topViolationsByCity(){

    }
}
