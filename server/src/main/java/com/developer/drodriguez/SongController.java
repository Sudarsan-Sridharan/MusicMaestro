package com.developer.drodriguez;

import jdk.nashorn.internal.objects.NativeJSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Daniel on 3/19/17.
 */

@RestController
public class SongController {

    @Autowired
    private SongService songService;

    @RequestMapping(method=RequestMethod.GET, value="/library")
    public List<Song> getAllSongs() {
        return songService.getAllSongs();
    }

    @RequestMapping(method=RequestMethod.GET, value="/library/{artist}")
    public List<Song> getSong(@PathVariable String artist) {
        return songService.getSong(artist);
    }

    @RequestMapping(method=RequestMethod.GET, value="/library/{artist}/{album}")
    public List<Song> getSong(@PathVariable("artist") String artist, @PathVariable("album") String album) {
        return songService.getSong(artist, album);
    }

    @RequestMapping(method=RequestMethod.GET, value="/library/{artist}/{album}/{title}")
    public List<Song> getSong(@PathVariable("artist") String artist, @PathVariable("album") String album,
                              @PathVariable("title") String title) {
        return songService.getSong(artist, album, title);
    }

    @RequestMapping(method=RequestMethod.POST, value="/library")
    public List<Song> addSong(@RequestBody Song song) {
        return songService.addSong(song);
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/songs/{id}")
    public List<Song> deleteSong(@PathVariable String id) {
        return songService.deleteSong(id);
    }

    /*
    @RequestMapping(method=RequestMethod.PUT, value="/library/{artist}/{album}/{title}")
    public List<Song> updateSong(@RequestBody Song song, @PathVariable("artist") String artist, @PathVariable("album") String album,
                                 @PathVariable("title") String title) {
        System.out.println("REACHED PUT.");
        songService.updateSong(song, artist, album, title);
        return songService.getAllSongs();
    }
    */

}
