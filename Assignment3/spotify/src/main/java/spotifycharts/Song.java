package spotifycharts;

import java.util.HashMap;
import java.util.Map;

public class Song {

    public enum Language {
        NL, // Dutch
        EN, // English
        DE, // German
        FR, // French
        SP, // Spanish
        IT, // Italian
    }

    public enum Country {
        NL, // Netherlands
        UK, // United Kingdom
        DE, // Germany
        BE, // Belgium
        FR, // France
        SP, // Spain
        IT  // Italy
    }

    private final String artist;
    private final String title;
    private final Language language;

    private Map<Country, Integer> BY_STREAMS = new HashMap<>();

    /**
     * Constructs a new instance of Song based on given attribute values
     */
    public Song(String artist, String title, Language language) {
        this.artist = artist;
        this.title = title;
        this.language = language;

        for (Country c : Country.values()) {
            BY_STREAMS.put(c, 0);
        }

    }

    /**
     * Sets the given streams count for the given country on this song
     *
     * @param country
     * @param streamsCount
     */
    public void setStreamsCountOfCountry(Country country, int streamsCount) {
        BY_STREAMS.replace(country, streamsCount);
    }

    /**
     * retrieves the streams count of a given country from this song
     *
     * @param country
     * @return
     */
    public int getStreamsCountOfCountry(Country country) {
        return BY_STREAMS.get(country);
    }

    /**
     * Calculates/retrieves the total of all streams counts across all countries from this song
     *
     * @return
     */
    public int getStreamsCountTotal() {
        int sumOfCountries = 0;

        for (Country c : Country.values()) {
            sumOfCountries += getStreamsCountOfCountry(c);
        }

        return sumOfCountries;
    }


    /**
     * compares this song with the other song
     * ordening songs with the highest total number of streams upfront
     *
     * @param other the other song to compare against
     * @return negative number, zero or positive number according to Comparator convention
     */
    public int compareByHighestStreamsCountTotal(Song other) {
        int compareResult = 0;

        if (other.getStreamsCountTotal() > this.getStreamsCountTotal()) {
            compareResult = 1;
        } else if (other.getStreamsCountTotal() < this.getStreamsCountTotal()) {
            compareResult = -1;
        }
        return compareResult;    // replace by proper result
    }

    /**
     * compares this song with the other song
     * ordening all Dutch songs upfront and then by decreasing total number of streams
     *
     * @param other the other song to compare against
     * @return negative number, zero or positive number according to Comparator conventions
     */
    public int compareForDutchNationalChart(Song other) {
        if (other.language.compareTo(this.language) > 0){
            return -1;
        } else if (other.language.compareTo(this.language) < 0){
            return 1;
        } else return compareByHighestStreamsCountTotal(other);
    }


    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public Language getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return String.format("%s/%s{%s}(%d)", artist, title, language.name(), getStreamsCountTotal());
    }
}
