package com.developer.drodriguez;

import com.developer.drodriguez.model.Album;
import com.developer.drodriguez.model.Artist;
import com.developer.drodriguez.model.Song;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Daniel on 3/31/17.
 */
public class SongServiceTest {

    @Test
    public void removeFilesTest() {
        String filePath = "/Users/Daniel/Music/library/Foo Fighters/Greatest Hits/Times Like These.mp3";
        File songFile = new File(filePath);
        File albumFolder = songFile.getParentFile();
        File artistFolder = albumFolder.getParentFile();
        if (songFile.isFile())
            if (songFile.exists()) {
                System.out.println("Deleting Song...");
                //songFile.delete();
            }
        if (albumFolder.isDirectory())
            if (albumFolder.list().length == 0)
                System.out.println("Delete Album");
        //albumFolder.delete();
        if (artistFolder.isDirectory())
            if (artistFolder.list().length == 0)
                System.out.println("Delete Artist");
        //artistFolder.delete();
    }

    @Test
    public void testSong() {
        Map<Integer, Song> songMap = new TreeMap<>();
        songMap.put(1, new Song(1, 1,"I Believe", "2007", "/Users/Daniel/Music/library/Dustin Kensrue/Please Come Home/I Believe.mp3") );
        songMap.put(2, new Song(2, 1,"Consider the Ravens", "2007", "/Users/Daniel/Music/library/Dustin Kensrue/Please Come Home/Consider the Ravens.mp3") );
        System.out.println(songMap.get(2));
        assertTrue(songMap.containsValue(songMap.get(2)));

    }

}
