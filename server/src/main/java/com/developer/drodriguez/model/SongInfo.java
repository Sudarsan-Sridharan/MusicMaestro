package com.developer.drodriguez.model;

/**
 * Created by Daniel on 4/4/17.
 */
public class SongInfo {

    private Artist artist;
    private Album album;
    private Song song;

    //Required from Spring for JSON deserialization.
    public SongInfo() {}

    public SongInfo(Artist artist, Album album, Song song) {
        this.artist = artist;
        this.album = album;
        this.song = song;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    @Override
    public String toString() {
        return "SongInfo{" +
                "artist=" + artist +
                ", album=" + album +
                ", song=" + song +
                '}';
    }

}
