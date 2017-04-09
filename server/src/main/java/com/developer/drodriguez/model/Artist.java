package com.developer.drodriguez.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 4/3/17.
 */
public class Artist implements Comparable<Artist> {

    private int id;
    private String name;

    //Required from Spring for JSON deserialization.
    public Artist() {}

    public Artist(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Artist artist) {
        return this.name.compareTo(artist.name);
    }

    @Override
    public String toString() {
        return "Artist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

}
