package com.developer.drodriguez;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Daniel on 3/21/17.
 */

@Service
public class SongService {

    private List<Song> songs = new ArrayList<>();

    SongService() {
        Song song1 = new Song("Jesus of Suburbia", "American Idiot", "Green Day", 2004);
        Song song2 = new Song("Empire", "Set Sail the Prairie", "Kaddisfly", 2007);
        Song song3 = new Song("Dream On", "Aerosmith", "Aerosmith", 1973);
        Song song4 = new Song("Holiday", "American Idiot", "Green Day", 2004);
        Song song5 = new Song("Basketcase", "Dookie", "Green Day", 1994);
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);
        songs.add(song4);
        songs.add(song5);
    }

    public List<Song> getAllSongs() {
        return songs;
    }

    public List<Song> getSong(String artist) {
        List<Song> list = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++)
            if (songs.get(i).getArtist().equals(artist))
                list.add(songs.get(i));
        if (!list.isEmpty())
            return list;
        else
            return null;
    }

    public List<Song> getSong(String artist, String album) {
        List<Song> list = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++)
            if (songs.get(i).getArtist().equals(artist) && songs.get(i).getAlbum().equals(album))
                list.add(songs.get(i));
        if (!list.isEmpty())
            return list;
        else
            return null;
    }

    public List<Song> getSong(String artist, String album, String title) {
        List<Song> list = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++)
            if (songs.get(i).getArtist().equals(artist) && songs.get(i).getAlbum().equals(album) && songs.get(i).getTitle().equals(title))
                list.add(songs.get(i));
        if (!list.isEmpty())
            return list;
        else
            return null;
    }

    public List<Song> addSong(Song song) {
        boolean hasSong = false;
        for (int i = 0; i < songs.size(); i++)
            if (songs.get(i).toString().equals(song.toString()))
                hasSong = true;
        if (!hasSong)
            songs.add(song);
        return getAllSongs();
    }

    /*
    public void updateSong(Song song, String artist, String album, String title) {
        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            if (s.getArtist().equals(artist) && s.getAlbum().equals(album) && s.getTitle().equals(title)) {
                songs.set(i, song);
                return;
            }
        }
    }
    */

    public List<Song> deleteSong(String id) {
        for (int i = 0; i < songs.size(); i++)
            if (songs.get(i).getTitle().equals(id))
                songs.remove(i);
        return getAllSongs();
    }

    //Return unique list of artists
    public Set<String> getArtists() {
        Set<String> set = new TreeSet<>();
        for (Song song : songs)
            set.add(song.getArtist());
        return set;
    }

    //Return unique list of albums by a given artist
    public Set<String> getAlbums(String artist) {
        Set<String> set = new TreeSet<>();
        for (Song song : songs)
            if (song.getArtist().equals(artist))
                set.add(song.getAlbum());
        return set;
    }

    //Return unique list of songs for a given album by an artist
    public Set<String> getSongs(String artist, String album) {
        Set<String> set = new TreeSet<>();
        for (Song song : songs)
            if (song.getArtist().equals(artist) && song.getAlbum().equals(album))
                set.add(song.getTitle());
        return set;
    }

}
