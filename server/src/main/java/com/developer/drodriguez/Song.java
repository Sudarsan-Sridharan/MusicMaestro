package com.developer.drodriguez;

/**
 * Created by Daniel on 3/19/17.
 */
public class Song {

    private String title;
    private String album;
    private String artist;
    private int year;

    public Song() {}

    public Song(String title, String album, String artist, int year) {
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Song{" +
                "title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", year=" + year +
                '}';
    }

}
