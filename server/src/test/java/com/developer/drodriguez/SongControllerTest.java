package com.developer.drodriguez;

import com.mpatric.mp3agic.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Created by Daniel on 3/26/17.
 */
public class SongControllerTest {

    @Test
    public void getSongArtwork() throws IOException {
        File testFile = new File("/Users/Daniel/Music/library/Green Day/Dookie/Basket Case.mp3");
        System.out.println(testFile);
        assertEquals(true, testFile.canRead());
        assertEquals(true, testFile.exists());
    }

    @Test
    public void TestMp3Read() throws IOException, UnsupportedTagException, InvalidDataException {
        Mp3File songData = new Mp3File("/Users/Daniel/Music/library/Green Day/Dookie/Basket Case.mp3");
        songData.getLengthInSeconds();

        if (songData.hasId3v2Tag()) {
            ID3v2 songTags = songData.getId3v2Tag();
            System.out.println(songTags.getArtist());
            System.out.println(songTags.getTitle());
            System.out.println(songTags.getAlbum());
            System.out.println(songTags.getYear());

            byte[] imageData = songTags.getAlbumImage();
            if (imageData != null) {
                String mimeType = songTags.getAlbumImageMimeType();
                System.out.println("Mime type: " + mimeType);
            }
        }
    }
}
