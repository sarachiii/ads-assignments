package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class TrafficTrackerTest2 {
    private final static String VAULT_NAME = "/test1";

    TrafficTracker trafficTracker;

    @BeforeEach
    private void setup() {
        Locale.setDefault(Locale.ENGLISH);
        trafficTracker = new TrafficTracker();

        trafficTracker.importCarsFromVault(VAULT_NAME + "/cars.txt");

        trafficTracker.importDetectionsFromVault(VAULT_NAME + "/detections");

        violations = trafficTracker.getViolations();
    }

    @Test
    public void calculateTotalFines(){
        assertEquals(7, trafficTracker.getViolations().stream().mapToInt(Violation::getOffencesCount).sum(),
                "Total number of offences across all Violation instances did not match.");
        assertEquals(175.0, trafficTracker.calculateTotalFines(),
                "Total revenue of fines from all offences did not match");
    }

    @Test
    public void topViolationsByCar() {
        List<Violation> topViolationsByCar = trafficTracker.topViolationsByCar(4);

        assertTrue(topViolationsByCar.get(0).getOffencesCount() >= topViolationsByCar.get(1).getOffencesCount());
        assertTrue(topViolationsByCar.get(1).getOffencesCount() >= topViolationsByCar.get(2).getOffencesCount());
        assertTrue(topViolationsByCar.get(2).getOffencesCount() >= topViolationsByCar.get(3).getOffencesCount());
        assertEquals(4, trafficTracker.topViolationsByCar(5).size(),
                "Topnumber will be set to the list size if it exceeds the index");
    }

    @Test
    public void topViolationsByCity() {
        List<Violation> topViolationsByCity = trafficTracker.topViolationsByCity(3);

        assertTrue(topViolationsByCity.get(0).getOffencesCount() >= topViolationsByCity.get(1).getOffencesCount());
        assertTrue(topViolationsByCity.get(1).getOffencesCount() >= topViolationsByCity.get(2).getOffencesCount());
        assertEquals(5, trafficTracker.topViolationsByCity(7).size(),
                "Topnumber will be set to the list size if it exceeds the index");
    }
}
