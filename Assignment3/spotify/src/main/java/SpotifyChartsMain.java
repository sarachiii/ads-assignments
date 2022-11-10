import spotifycharts.ChartsCalculator;
import spotifycharts.Song;
import spotifycharts.SongSorter;
import spotifycharts.SorterImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

public class SpotifyChartsMain {
    public static void main(String[] args) {
        System.out.println("Welcome to the HvA Spotify Charts Calculator\n");

        ChartsCalculator chartsCalculator = new ChartsCalculator(19670427L);
        chartsCalculator.registerStreamedSongs(257);
        chartsCalculator.showResults();
        SongSorter songSorter = new SongSorter();

        for (int n = 100; n <= 5000000; n *= 2) {
            ChartsCalculator testCalculator = new ChartsCalculator(0);
            List<Song> songsTest =  new ArrayList(testCalculator.registerStreamedSongs(n));
            songTester(songsTest);
        }
    }

    private static void songTester(List<Song> songs) {
        long started, finished;
        SongSorter sorter = new SongSorter();

        System.out.printf("\n\nTesting with a list of %d songs:\n", songs.size());
        Collections.shuffle(songs);
        System.gc();

        // Measure time of selInsBubSort sort algorithm
        started = System.nanoTime();
        sorter.selInsBubSort(songs,Comparator.comparing(Song::getTitle));
        finished = System.nanoTime();

        if ((finished-started)/1E9 >= 20) {
            System.out.println("\nIt takes longer than 20 sec");
        } else {
            System.out.printf("\nFinished selInsBubSort sort in %.3f sec", (finished-started)/1E9);

        }

        // Measure time of quickSort sort algorithm
        started = System.nanoTime();
        sorter.quickSort(songs,Comparator.comparing(Song::getTitle));
        finished = System.nanoTime();

        if ((finished-started)/1E9 >= 20) {
            System.out.println("\nIt takes longer than 20 sec");
        } else {
            System.out.printf("\nFinished quickSort sort in %.3f sec", (finished-started)/1E9);
        }

        // Measure time of topHeapSort sort algorithm
        started = System.nanoTime();
        sorter.topsHeapSort(songs.size(), songs, Comparator.comparing(Song::getTitle));
        finished = System.nanoTime();

        if ((finished-started)/1E9 >= 20) {
            System.out.println("\nIt takes longer than 20 sec");
        } else {
            System.out.printf("\nFinished topHeapSort sort in %.3f sec", (finished-started)/1E9);
        }
    }
}
