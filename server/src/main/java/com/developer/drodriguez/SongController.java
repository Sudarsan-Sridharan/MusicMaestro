package com.developer.drodriguez;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.NoSuchTagException;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import jdk.nashorn.internal.objects.NativeJSON;
import org.omg.CORBA.DynAnyPackage.Invalid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Created by Daniel on 3/19/17.
 */

@RestController
public class SongController {

    @Autowired
    private SongService songService;

    @RequestMapping(method=RequestMethod.GET, value="/library")
    public Set<String> getArtists() {
        return songService.getArtists();
    }

    @RequestMapping(method=RequestMethod.GET, value="/library/{artist}")
    public Set<String> getAlbums(@PathVariable String artist) {
        return songService.getAlbums(artist);
    }

    @RequestMapping(method=RequestMethod.GET, value="/library/{artist}/{album}")
    public Set<String> getSongs(@PathVariable("artist") String artist, @PathVariable("album") String album) {
        return songService.getSongs(artist, album);
    }

    @RequestMapping(method=RequestMethod.GET, value="/library/{artist}/{album}/{title}")
    public Song getSong(@PathVariable("artist") String artist, @PathVariable("album") String album,
                              @PathVariable("title") String title) {
        return songService.getSong(artist, album, title);
    }

    @RequestMapping(method=RequestMethod.GET, value="/playback/{artist}/{album}/{songTitle}")
    public ResponseEntity<InputStreamResource> getSongFile(@PathVariable("artist") String artist, @PathVariable("album") String album,
                                                           @PathVariable("songTitle") String songTitle) throws IOException {
        return songService.getSongFile(artist, album, songTitle);
    }

    @RequestMapping(method=RequestMethod.GET, value="/artwork/{artist}/{album}/{songTitle}")
    public ResponseEntity<InputStreamResource> getSongArtwork(@PathVariable("artist") String artist,
                                                              @PathVariable("album") String album,
                                                              @PathVariable("songTitle") String songTitle)
            throws IOException, UnsupportedTagException, InvalidDataException {
        return songService.getSongArtwork(artist, album, songTitle);
    }

    @RequestMapping(method=RequestMethod.POST, value="/upload/file")
    public Song addSongFile(@RequestParam("file") MultipartFile file)
            throws IOException, UnsupportedTagException, InvalidDataException, NoSuchTagException {
        return songService.addSongFile(file);
    }

    @RequestMapping(method=RequestMethod.PUT, value="/library")
    public void updateSongInfo(@RequestBody Song song)
            throws IOException, InvalidDataException, NotSupportedException, UnsupportedTagException {
        songService.updateSongInfo(song);
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/library/{artist}/{album}/{songTitle}")
    public void deleteSong(@PathVariable("artist") String artist, @PathVariable("album") String album,
                                 @PathVariable("songTitle") String songTitle) {
        songService.deleteSong(artist, album, songTitle);
    }

    /*
    @RequestMapping(method=RequestMethod.GET, value="/library-all")
    public List<Song> getAllSongs() {
        return songService.getAllSongs();
    }

    @RequestMapping(method=RequestMethod.PUT, value="/library/{artist}/{album}/{title}")
    public List<Song> updateSong(@RequestBody Song song, @PathVariable("artist") String artist, @PathVariable("album") String album,
                                 @PathVariable("title") String title) {
        System.out.println("REACHED PUT.");
        songService.updateSong(song, artist, album, title);
        return songService.getAllSongs();
    }
    */

}
