package com.developer.drodriguez;

import com.mpatric.mp3agic.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.PathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * Created by Daniel on 3/21/17.
 */

@Service
public class SongService {

    private List<Song> songs = new ArrayList<>();

    @Value("${library.path}")
    private String libraryPath;

    SongService() throws IOException {
        Song song1 = new Song("Jesus of Suburbia", "American Idiot", "Green Day", "2004");
        song1.setFilePath("/Users/Daniel/Music/library/Green Day/American Idiot/Jesus of Suburbia.mp3");
        Song song2 = new Song("Empire", "Set Sail the Prairie", "Kaddisfly", "2007");
        song2.setFilePath("/Users/Daniel/Music/library/Kaddisfly/Set Sail the Prairie/Empire.mp3");
        Song song3 = new Song("Dream On", "Aerosmith", "Aerosmith", "1973");
        song3.setFilePath("/Users/Daniel/Music/library/Aerosmith/Aerosmith/Dream On.mp3");
        Song song4 = new Song("Holiday", "American Idiot", "Green Day", "2004");
        song4.setFilePath("/Users/Daniel/Music/library/Green Day/American Idiot/Holiday.mp3");
        Song song5 = new Song("Basket Case", "Dookie", "Green Day", "1994");
        song5.setFilePath("/Users/Daniel/Music/library/Green Day/Dookie/Basket Case.mp3");
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);
        songs.add(song4);
        songs.add(song5);
    }

    /*
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
    */

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

    public Song getSong(String artist, String album, String title) {
        for (int i = 0; i < songs.size(); i++)
            if (songs.get(i).getArtist().equals(artist) && songs.get(i).getAlbum().equals(album) && songs.get(i).getTitle().equals(title))
                return songs.get(i);
        return null;
    }

    public ResponseEntity<InputStreamResource> getSongFile(String artist, String album, String songTitle) throws IOException {
        String filePath = getSong(artist, album, songTitle).getFilePath();
        PathResource file = new PathResource(filePath);
        return ResponseEntity
                .ok()
                .contentLength(file.contentLength())
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(new InputStreamResource(file.getInputStream()));
    }

    public ResponseEntity<InputStreamResource> getSongArtwork(String artist, String album, String songTitle) throws IOException, UnsupportedTagException, InvalidDataException {
        Song song = getSong(artist, album, songTitle);
        String filePath = song.getFilePath();
        String mimeType = null;
        byte[] imageData = null;

        Mp3File songData = new Mp3File(filePath);
        songData.getLengthInSeconds();

        if (songData.hasId3v2Tag()) {
            ID3v2 songTags = songData.getId3v2Tag();
            System.out.println(songTags.getArtist());
            System.out.println(songTags.getTitle());
            System.out.println(songTags.getAlbum());
            System.out.println(songTags.getYear());

            imageData = songTags.getAlbumImage();
            if (imageData != null) {
                mimeType = songTags.getAlbumImageMimeType();
                // Write image to file - can determine appropriate file extension from the mime type
                RandomAccessFile file = new RandomAccessFile("album-artwork", "rw");
                file.write(imageData);
                file.close();

                ByteArrayResource bar = new ByteArrayResource(imageData);

                return ResponseEntity
                        .ok()
                        .contentLength(bar.contentLength())
                        .contentType(MediaType.parseMediaType(mimeType))
                        .body(new InputStreamResource(bar.getInputStream()));

            }
        }

        //If file does not contain an image, then provide placeholder.
        ClassPathResource noImgFoundFile = new ClassPathResource("no-album-art.jpg");

        return ResponseEntity
                .ok()
                .contentLength(noImgFoundFile.contentLength())
                .contentType(MediaType.parseMediaType("image/jpeg"))
                .body(new InputStreamResource(noImgFoundFile.getInputStream()));

    }

    public void addSongMetadata(Song newSong) {
        //Replace any existing songs.
        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            if (s.getArtist().equals(newSong.getArtist()) && s.getAlbum().equals(newSong.getAlbum()) && s.getTitle().equals(newSong.getTitle())) {
                songs.set(i, newSong);
                return;
            }
        }
        songs.add(newSong);
    }

    public void addSongFile(MultipartFile file, String artist, String album, String songTitle) throws IOException{
        String originalFileName = file.getOriginalFilename();
        String fileType = originalFileName.substring(originalFileName.lastIndexOf(".") + 1, originalFileName.length());
        String filePath = libraryPath + "/" + artist + "/" + album;
        String fileName = songTitle + "." + fileType;
        String fullPath = filePath + "/" + fileName;

        //Create any non-existing directories for file.
        File newDirs = new File(filePath);
        if (!newDirs.exists())
            newDirs.mkdirs();

        File newFile = new File(fullPath);

        //Replace any existing files.
        if (newFile.exists())
            newFile.delete();
        newFile.createNewFile();

        //Write bytes to the new, empty file.
        byte bytes [] = file.getBytes();
        BufferedOutputStream bout=new BufferedOutputStream(new FileOutputStream(newFile));
        bout.write(bytes);
        bout.flush();
        bout.close();

        //Add file path to the given song in the songs list.
        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            if (s.getArtist().equals(artist) && s.getAlbum().equals(album) && s.getTitle().equals(songTitle)) {
                songs.get(i).setFilePath(fullPath);
                return;
            }
        }
    }

    public void deleteSong(String artist, String album, String songTitle) {
        String filePath = null;
        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            if (s.getArtist().equals(artist) && s.getAlbum().equals(album) && s.getTitle().equals(songTitle)) {
                filePath = songs.get(i).getFilePath();
                songs.remove(i);
                break;
            }
        }
        removeFiles(filePath);
    }


    //Delete song (if exists), as well as the album folder and the artist folder (if empty).
    public void removeFiles(String filePath) {
        File songFile = new File(filePath);
        File albumFolder = songFile.getParentFile();
        File artistFolder = albumFolder.getParentFile();
        if (songFile.isFile())
            if (songFile.exists())
                songFile.delete();
        if (albumFolder.isDirectory())
            if (albumFolder.list().length == 0)
                albumFolder.delete();
        if (artistFolder.isDirectory())
            if (artistFolder.list().length == 0)
                artistFolder.delete();
    }

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
