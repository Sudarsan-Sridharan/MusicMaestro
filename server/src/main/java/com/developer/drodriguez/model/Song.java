package com.developer.drodriguez.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Comparator;

/**
 * Created by Daniel on 4/3/17.
 */
public class Song implements Comparable<Song> {

    private int id;
    private int albumId;
    private int track;
    private String name;
    private String year;

    @JsonIgnore
    private String filePath;

    //Required from Spring for JSON deserialization.
    public Song() {}

    public Song(int id, int albumId, int track, String name, String year, String filePath) {
        this.id = id;
        this.albumId = albumId;
        this.track = track;
        this.name = name;
        this.year = year;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    public int compareTo(Song song) {
        if (this.track < song.track)
            return -1;
        if (this.track > song.track)
            return 1;
        return 0;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", albumId=" + albumId +
                ", track=" + track +
                ", name='" + name + '\'' +
                ", year='" + year + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }

}
