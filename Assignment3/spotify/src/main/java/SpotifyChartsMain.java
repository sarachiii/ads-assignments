import spotifycharts.ChartsCalculator;
import spotifycharts.Song;
import spotifycharts.SongSorter;
import spotifycharts.SorterImpl;

import java.util.List;

public class SpotifyChartsMain {
    public static void main(String[] args) {
        System.out.println("Welcome to the HvA Spotify Charts Calculator\n");

        ChartsCalculator chartsCalculator = new ChartsCalculator(19670427L);
        chartsCalculator.registerStreamedSongs(257);
        chartsCalculator.showResults();
        List<Song> songs100 = chartsCalculator.registerStreamedSongs(100);
        double start = System.nanoTime();
//        songs100.sort(SongSorter);
    }
}
