package com.developer.drodriguez;

/**
 * Created by Daniel on 3/19/17.
 */
public class Song {

    private String title;
    private String album;
    private String artist;
    private String year;
    private String filePath;

    public Song() {}

    public Song(String title, String album, String artist, String year) {
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "Song{" +
                "title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", year=" + year +
                ", filePath='" + filePath + '\'' +
                '}';
    }

}
