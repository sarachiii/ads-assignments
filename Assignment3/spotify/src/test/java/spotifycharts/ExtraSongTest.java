package spotifycharts;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtraSongTest {

    private static Comparator<Song> rankingSchemeTotal, rankingSchemeDutchNational;
    Song songBYC, songKKA, songTS, songJVT, songBB;

    @BeforeAll
    static void setupClass() {
        rankingSchemeTotal = Song::compareByHighestStreamsCountTotal;
        rankingSchemeDutchNational = Song::compareForDutchNationalChart;
    }

    @BeforeEach
    void setup() {
        songBYC = new Song("Beyoncé", "CUFF IT", Song.Language.EN);
        songBYC.setStreamsCountOfCountry(Song.Country.UK,100);
        songBYC.setStreamsCountOfCountry(Song.Country.NL,40);
        songBYC.setStreamsCountOfCountry(Song.Country.BE,20);
        songTS = new Song("Taylor Swift", "Anti-Hero", Song.Language.EN);
        songTS.setStreamsCountOfCountry(Song.Country.UK,100);
        songTS.setStreamsCountOfCountry(Song.Country.DE,60);
        songKKA = new Song("Kris Kross Amsterdam", "Vluchtstrook", Song.Language.NL);
        songKKA.setStreamsCountOfCountry(Song.Country.NL,40);
        songKKA.setStreamsCountOfCountry(Song.Country.BE,30);
        songJVT = new Song("De Jeugd Van Tegenwoordig", "Sterrenstof", Song.Language.NL);
        songJVT.setStreamsCountOfCountry(Song.Country.NL,70);
        songBB = new Song("Bad Bunny", "La Coriente", Song.Language.SP);
    }

    @Test
    void comparesSongWithItselfCorrectlyForStreamCount() {
        assertEquals(0, songBYC.compareByHighestStreamsCountTotal(songBYC));
        assertEquals(0, songTS.compareByHighestStreamsCountTotal(songTS));
        assertEquals(0, songKKA.compareByHighestStreamsCountTotal(songKKA));
        assertEquals(0, songJVT.compareByHighestStreamsCountTotal(songJVT));
        assertEquals(0, songBB.compareByHighestStreamsCountTotal(songBB));
    }

    @Test
    void comparesSongWithItselfCorrectlyForDutchCharts() {
        assertEquals(0, songBYC.compareForDutchNationalChart(songBYC));
        assertEquals(0, songTS.compareForDutchNationalChart(songTS));
        assertEquals(0, songKKA.compareForDutchNationalChart(songKKA));
        assertEquals(0, songJVT.compareForDutchNationalChart(songJVT));
        assertEquals(0, songBB.compareForDutchNationalChart(songBB));
    }

    @Test
    void compareDifferentSongsCorrectly() {
        assertEquals(-songTS.compareForDutchNationalChart(songBYC), songBYC.compareForDutchNationalChart(songTS));
        assertEquals(-songKKA.compareForDutchNationalChart(songBYC), songBYC.compareForDutchNationalChart(songKKA));
        assertEquals(-songJVT.compareForDutchNationalChart(songBB), songBB.compareForDutchNationalChart(songJVT));
        assertEquals(-songBB.compareForDutchNationalChart(songTS), songTS.compareForDutchNationalChart(songBB));

        assertEquals(-songTS.compareByHighestStreamsCountTotal(songBYC), songBYC.compareByHighestStreamsCountTotal(songTS));
        assertEquals(-songKKA.compareByHighestStreamsCountTotal(songBYC), songBYC.compareByHighestStreamsCountTotal(songKKA));
        assertEquals(-songJVT.compareByHighestStreamsCountTotal(songBB), songBB.compareByHighestStreamsCountTotal(songJVT));
        assertEquals(-songBB.compareByHighestStreamsCountTotal(songTS), songTS.compareByHighestStreamsCountTotal(songBB));
    }

}
