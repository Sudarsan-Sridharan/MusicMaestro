package com.developer.drodriguez.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 4/3/17.
 */
public class Album {

    private int id;
    private int artistId;
    private String name;

    public Album(int id, int artistId, String name) {
        this.id = id;
        this.artistId = artistId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", artistId=" + artistId +
                ", name='" + name + '\'' +
                '}';
    }

}
