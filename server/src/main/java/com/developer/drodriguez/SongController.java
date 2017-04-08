package com.developer.drodriguez;

import com.developer.drodriguez.model.Album;
import com.developer.drodriguez.model.Artist;
import com.developer.drodriguez.model.Song;
import com.developer.drodriguez.model.SongInfo;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.NoSuchTagException;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Created by Daniel on 3/19/17.
 */

@RestController
public class SongController {

    @Autowired
    private SongService songService;

    @RequestMapping(method=RequestMethod.GET, value="/artists")
    public List<Artist> getArtists() {
        return songService.getArtists();
    }

    @RequestMapping(method=RequestMethod.GET, value="/artists/{artistId}")
    public Artist getArtist(@PathVariable int artistId) {
        return songService.getArtist(artistId);
    }

    @RequestMapping(method=RequestMethod.GET, value="/artists/{artistId}/albums")
    public List<Album> getAlbums(@PathVariable int artistId) {
        return songService.getAlbums(artistId);
    }

    @RequestMapping(method=RequestMethod.GET, value="/artists/{artistId}/albums/{albumId}")
    public Album getAlbum(@PathVariable int artistId, @PathVariable int albumId) {
        return songService.getAlbum(artistId, albumId);
    }

    @RequestMapping(method=RequestMethod.GET, value="/artists/{artistId}/albums/{albumId}/songs")
    public List<Song> getSongs(@PathVariable int artistId, @PathVariable int albumId) {
        return songService.getSongs(artistId, albumId);
    }

    @RequestMapping(method=RequestMethod.GET, value="/artists/{artistId}/albums/{albumId}/songs/{songId}")
    public Song getSong(@PathVariable int artistId, @PathVariable int albumId, @PathVariable int songId) {
        return songService.getSong(artistId, albumId, songId);
    }

    @RequestMapping(method=RequestMethod.GET, value="/artists/{artistId}/albums/{albumId}/songs/{songId}/info")
    public SongInfo getSongInfo(@PathVariable int artistId, @PathVariable int albumId, @PathVariable int songId) {
        return songService.getSongInfo(artistId, albumId, songId);
    }

    @RequestMapping(method=RequestMethod.GET, value="/artists/{artistId}/albums/{albumId}/songs/{songId}/file")
    public ResponseEntity<InputStreamResource> getSongFile(@PathVariable int artistId, @PathVariable int albumId, @PathVariable int songId) throws IOException {
        return songService.getSongFile(artistId, albumId, songId);
    }

    @RequestMapping(method=RequestMethod.GET, value="/artists/{artistId}/albums/{albumId}/songs/{songId}/artwork")
    public ResponseEntity<InputStreamResource> getSongArtwork(@PathVariable int artistId, @PathVariable int albumId, @PathVariable int songId)
            throws IOException, UnsupportedTagException, InvalidDataException {
        return songService.getSongArtwork(artistId, albumId, songId);
    }

    @RequestMapping(method=RequestMethod.POST, value="/file")
    public void addSongFile(@RequestParam("file") MultipartFile file)
            throws IOException, UnsupportedTagException, InvalidDataException, NoSuchTagException {
        songService.addSongFile(file);
    }

    @RequestMapping(method=RequestMethod.PUT, value="/artists/{artistId}/albums/{albumId}/songs/{songId}")
    public SongInfo updateSongInfo(@RequestBody SongInfo songInfo, @PathVariable int artistId, @PathVariable int albumId, @PathVariable int songId)
            throws IOException, InvalidDataException, NotSupportedException, UnsupportedTagException {
        return songService.updateSongInfo(songInfo, artistId, albumId, songId);
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/artists/{artistId}/albums/{albumId}/songs/{songId}")
    public void deleteSong(@PathVariable int artistId, @PathVariable int albumId, @PathVariable int songId) {
        songService.deleteSong(artistId, albumId, songId);
    }

}
