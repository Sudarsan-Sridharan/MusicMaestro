package com.developer.drodriguez;

import com.developer.drodriguez.model.Album;
import com.developer.drodriguez.model.Artist;
import com.developer.drodriguez.model.Song;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by Daniel on 3/19/17.
 */

@RestController
public class SongController {

    @Autowired
    private SongService songService;

    @RequestMapping(method=RequestMethod.GET, value="/library")
    public List<Artist> getArtists() {
        return songService.getArtists();
    }

    @RequestMapping(method=RequestMethod.GET, value="/library/{artistId}")
    public List<Album> getAlbums(@PathVariable int artistId) {
        return songService.getAlbums(artistId);
    }


    @RequestMapping(method=RequestMethod.GET, value="/library/{artistId}/{albumId}")
    public List<Song> getSongs(@PathVariable int artistId, @PathVariable int albumId) {
        return songService.getSongs(artistId, albumId);
    }

    @RequestMapping(method=RequestMethod.GET, value="/library/{artistId}/{albumId}/{songId}")
    public Song getSong(@PathVariable int artistId, @PathVariable int albumId, @PathVariable int songId) {
        return songService.getSong(artistId, albumId, songId);
    }

    @RequestMapping(method=RequestMethod.GET, value="/playback/{artistId}/{albumId}/{songId}")
    public ResponseEntity<InputStreamResource> getSongFile(@PathVariable int artistId, @PathVariable int albumId, @PathVariable int songId) throws IOException {
        return songService.getSongFile(artistId, albumId, songId);
    }

    @RequestMapping(method=RequestMethod.GET, value="/artwork/{artistId}/{albumId}/{songId}")
    public ResponseEntity<InputStreamResource> getSongArtwork(@PathVariable int artistId, @PathVariable int albumId, @PathVariable int songId)
            throws IOException, UnsupportedTagException, InvalidDataException {
        return songService.getSongArtwork(artistId, albumId, songId);
    }

    /*

    @RequestMapping(method=RequestMethod.POST, value="/upload/song")
    public Song addSongFile(@RequestParam("file") MultipartFile file)
            throws IOException, UnsupportedTagException, InvalidDataException, NoSuchTagException {
        return songService.addSongFile(file);
    }

    @RequestMapping(method=RequestMethod.PUT, value="/library/song")
    public void updateSongInfo(@RequestBody Song song)
            throws IOException, InvalidDataException, NotSupportedException, UnsupportedTagException {
        songService.updateSongInfo(song);
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/library/song/{id}")
    public void deleteSong(@PathVariable int id) {
        songService.deleteSong(id);
    }

    */

}
