package com.developer.drodriguez;

import com.developer.drodriguez.model.Album;
import com.developer.drodriguez.model.Artist;
import com.developer.drodriguez.model.Song;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    public void testMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "Test Value");
        System.out.println(map.get(2));
    }

}
