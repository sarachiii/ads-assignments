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

    // TODO add instance variable(s) to track the streams counts per country
    //  choose a data structure that you deem to be most appropriate for this application.

    private static Map<Country,Integer> BY_STREAMS = new HashMap<>();

    /**
     * Constructs a new instance of Song based on given attribute values
     */
    public Song(String artist, String title, Language language) {
        this.artist = artist;
        this.title = title;
        this.language = language;

        for (Country c : Country.values()){
            BY_STREAMS.put(c,0);
        }

    }

    /**
     * Sets the given streams count for the given country on this song
     * @param country
     * @param streamsCount
     */
    public void setStreamsCountOfCountry(Country country, int streamsCount) {
        for (Country c : Country.values()) {
            if (c.name().equals(country.name())){
                BY_STREAMS.put(country,streamsCount);
            }
        }
    }

    /**
     * retrieves the streams count of a given country from this song
     * @param country
     * @return
     */
    public int getStreamsCountOfCountry(Country country) {
        for (Country c : Country.values()) {
            if (c.name().equals(country.name())){
                System.out.println(BY_STREAMS.get(country));
                return BY_STREAMS.get(country);

            }
        }
        return 0;
    }
    /**
     * Calculates/retrieves the total of all streams counts across all countries from this song
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
     * @param other     the other song to compare against
     * @return  negative number, zero or positive number according to Comparator convention
     */
    public int compareByHighestStreamsCountTotal(Song other) {
        int compareResult = 0;

        if (this.getStreamsCountTotal() > other.getStreamsCountTotal()){
            compareResult = -1;
        } else if (this.getStreamsCountTotal() < other.getStreamsCountTotal()){
            compareResult = 1;
        }

        return compareResult;    // replace by proper result
    }

    /**
     * compares this song with the other song
     * ordening all Dutch songs upfront and then by decreasing total number of streams
     * @param other     the other song to compare against
     * @return  negative number, zero or positive number according to Comparator conventions
     */
    public int compareForDutchNationalChart(Song other) {
        int compareResult = 0;

        if (this.getStreamsCountOfCountry(Country.NL) > other.getStreamsCountOfCountry(Country.NL)){
            compareResult = -1;
        } else if (this.getStreamsCountOfCountry(Country.NL) < other.getStreamsCountOfCountry(Country.NL)){
            compareResult = 1;
        }

        return compareResult;    // replace by proper result
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

    // TODO provide a toString implementation to format songs as in "artist/title{language}(total streamsCount)"


    @Override
    public String toString() {
        return String.format("%s/%s {%s}(%d)",artist,title,language.name(),getStreamsCountTotal());
    }
}
