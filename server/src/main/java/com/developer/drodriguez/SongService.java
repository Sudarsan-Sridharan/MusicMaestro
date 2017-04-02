package com.developer.drodriguez;

import com.mpatric.mp3agic.*;
import com.sun.org.apache.xpath.internal.operations.Mult;
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

    public Song addSongFile(MultipartFile file)
            throws IOException, UnsupportedTagException, InvalidDataException, NoSuchTagException {

        File tempFile = convertMultipartToFile(file);
        Mp3File mp3 = new Mp3File(tempFile);

        if (mp3.hasId3v2Tag()) {

            ID3v2 tag = mp3.getId3v2Tag();

            String artist = tag.getArtist();
            String album = tag.getAlbum();
            String songTitle = tag.getTitle();
            String year = tag.getYear();
            String originalFileName = file.getOriginalFilename();
            String fileType = originalFileName.substring(originalFileName.lastIndexOf(".") + 1, originalFileName.length());
            String filePath = libraryPath + "/" + artist + "/" + album;
            String fileName = songTitle + "." + fileType;
            String fullPath = filePath + "/" + fileName;

            tempFile.delete();

            Song newSong = new Song(songTitle, album, artist, year, fullPath);
            songs.add(newSong);

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
            byte[] bytes = file.getBytes();
            BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(newFile));
            bout.write(bytes);
            bout.flush();
            bout.close();

            return newSong;

        }

        else {
            throw new NoSuchTagException("Could not find a ID3v2 tag for the uploaded file.");
        }

    }

    public void updateSongInfo(Song song) throws IOException, UnsupportedTagException, InvalidDataException, NotSupportedException {

        String originalFilePath = song.getFilePath();
        String fileType = originalFilePath.substring(originalFilePath.lastIndexOf(".") + 1, originalFilePath.length());
        String filePath = libraryPath + "/" + song.getArtist() + "/" + song.getAlbum();
        String fileName = song.getTitle() + "." + fileType;
        String fullPath = filePath + "/" + fileName;

        File file = new File(song.getFilePath());
        Mp3File mp3 = new Mp3File(file);

        if (mp3.hasId3v2Tag()) {
            byte[] albumImageBytes = mp3.getId3v2Tag().getAlbumImage();
            String albumImageMime = mp3.getId3v2Tag().getAlbumImageMimeType();
            mp3.removeId3v2Tag();
            ID3v2 tag = new ID3v24Tag();
            tag.setTitle(song.getTitle());
            tag.setArtist(song.getArtist());
            tag.setAlbum(song.getAlbum());
            tag.setYear(song.getYear());
            tag.setAlbumImage(albumImageBytes, albumImageMime);
            mp3.setId3v2Tag(tag);
            //Only delete if the song title, album, or artist was modified by the user.
            if (!song.getFilePath().equals(fullPath)) {
                mp3.save(fullPath);
                file.delete();
            }
        } else {
            throw new UnsupportedTagException("The associated file does not have a valid ID3v2 tag.");
        }

        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            if (s.getFilePath().equals(originalFilePath)) {
                song.setFilePath(fullPath);
                songs.set(i, song);
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

    public File convertMultipartToFile(MultipartFile file) throws IOException
    {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

}

    /*



    */
