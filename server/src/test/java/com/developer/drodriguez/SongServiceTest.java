package com.developer.drodriguez;

import com.developer.drodriguez.model.Album;
import com.developer.drodriguez.model.Artist;
import com.developer.drodriguez.model.Song;
import org.junit.Test;

import java.io.File;

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
    public void setBeansInit() {
        //Dustin Kensrue: I Believe
        Song song1 = new Song(1, "I Believe", 2007);
        Album album1 = new Album(1, "Please Come Home", song1);
        Artist artist1 = new Artist(1, "Dustin Kensrue", album1);

        //Dustin Kensrue: Consider the Ravens
        Song song2 = new Song(2, "Consider the Ravens", 2007);
        album1.addSong(song2);

        //Foo Fighters: Times Like These
        Song song3 = new Song(3, "Times Like These", 2009);
        Album album2 = new Album(2, "Greatest Hits", song3);
        Artist artist2 = new Artist(2, "Foo Fighters", album2);
    }

}
