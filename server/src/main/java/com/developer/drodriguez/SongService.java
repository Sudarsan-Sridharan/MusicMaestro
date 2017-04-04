package com.developer.drodriguez;

import com.developer.drodriguez.model.Album;
import com.developer.drodriguez.model.Artist;
import com.developer.drodriguez.model.Song;
import com.mpatric.mp3agic.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.PathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * Created by Daniel on 3/21/17.
 */

@Service
public class SongService {

    private List<Artist> artists = new ArrayList<>();
    private List<Album> albums = new ArrayList<>();
    private List<Song> songs = new ArrayList<>();
    private int artistIndex = 0;
    private int albumIndex = 0;
    private int songIndex = 0;

    @Value("${library.path}")
    private String libraryPath;

    SongService() {
        //Dustin Kensrue: I Believe
        artists.add( new Artist(++artistIndex, "Dustin Kensrue") );
        albums.add( new Album(++albumIndex, artistIndex, "Please Come Home") );
        songs.add ( new Song(++songIndex, albumIndex,"I Believe", "2007", "/Users/Daniel/Music/library/Dustin Kensrue/Please Come Home/I Believe.mp3") );

        //Dustin Kensrue: Consider the Ravens
        songs.add( new Song(++songIndex, albumIndex,"Consider the Ravens", "2007", "/Users/Daniel/Music/library/Dustin Kensrue/Please Come Home/Consider the Ravens.mp3") );

        //Foo Fighters: Times Like These
        artists.add( new Artist(++artistIndex, "Foo Fighters") );
        albums.add( new Album(++albumIndex, artistIndex, "Greatest Hits") );
        songs.add( new Song(++songIndex, albumIndex, "Times Like These", "2009", "/Users/Daniel/Music/library/Foo Fighters/Greatest Hits/Times Like These.mp3") );

        System.out.println(artistIndex);
        System.out.println(albumIndex);
        System.out.println(songIndex);

    }

    public List<Artist> getArtists() {
        return artists;
    }

    public Artist getArtist(int artistId) {
        for (Artist artist : artists)
            if (artist.getId() == artistId)
                return artist;
        return null;
    }

    public List<Album> getAlbums(int artistId) {
        List<Album> newList = new ArrayList<>();
        for (Artist artist : artists)
            if (artist.getId() == artistId)
                for (Album album : albums)
                    if (album.getArtistId() == artistId)
                        newList.add(album);
        return newList;
    }

    public Album getAlbum(int artistId, int albumId) {
        for (Artist artist : artists)
            if (artist.getId() == artistId)
                for (Album album : albums)
                    if (album.getArtistId() == artistId && album.getId() == albumId)
                        return album;
        return null;
    }

    public List<Song> getSongs(int artistId, int albumId) {
        List<Song> newList = new ArrayList<>();
        for (Artist artist : artists)
            if (artist.getId() == artistId)
                for (Album album : albums)
                    if (album.getArtistId() == artistId && album.getId() == albumId)
                        for (Song song : songs)
                            if (song.getAlbumId() == albumId)
                                newList.add(song);
        return newList;
    }

    public Song getSong(int artistId, int albumId, int songId) {
        for (Artist artist : artists)
            if (artist.getId() == artistId)
                for (Album album : albums)
                    if (album.getArtistId() == artistId && album.getId() == albumId)
                        for (Song song : songs)
                            if (album.getId() == albumId && song.getId() == songId)
                                return song;
        return null;
    }


    public ResponseEntity<InputStreamResource> getSongFile(int artistId, int albumId, int songId) throws IOException {
        String filePath = getSong(artistId, albumId, songId).getFilePath();
        PathResource file = new PathResource(filePath);
        return ResponseEntity
                .ok()
                .contentLength(file.contentLength())
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(new InputStreamResource(file.getInputStream()));
    }

    public ResponseEntity<InputStreamResource> getSongArtwork(int artistId, int albumId, int songId) throws IOException, UnsupportedTagException, InvalidDataException {
        Song song = getSong(artistId, albumId, songId);
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


    public synchronized void addSongFile(MultipartFile file)
            throws IOException, UnsupportedTagException, InvalidDataException, NoSuchTagException {

        File tempFile = convertMultipartToFile(file);
        Mp3File mp3 = new Mp3File(tempFile);

        if (mp3.hasId3v2Tag()) {

            ID3v2 tag = mp3.getId3v2Tag();

            String tArtistName = tag.getArtist();
            String tAlbumName = tag.getAlbum();
            String tSongName = tag.getTitle();
            String tYear = tag.getYear();
            String originalFileName = file.getOriginalFilename();
            String fileType = originalFileName.substring(originalFileName.lastIndexOf(".") + 1, originalFileName.length());
            String filePath = libraryPath + "/" + tArtistName + "/" + tAlbumName;
            String fileName = tSongName + "." + fileType;
            String fullPath = filePath + "/" + fileName;

            tempFile.delete();

            boolean hasNewArtist = false;
            boolean hasNewAlbum = false;
            boolean hasNewSong = false;
            int newArtistId = 0;
            int newAlbumId = 0;
            int newSongId = 0;

            for (int i = 0; i < artists.size(); i++) {
                if (artists.get(i).getName().equals(tArtistName)) {
                    newArtistId = artists.get(i).getId();
                    hasNewArtist = true;
                    break;
                } else if (i == artists.size() - 1) {
                    newArtistId = ++artistIndex;
                }
            }

            for (int j = 0; j < albums.size(); j++) {
                if (albums.get(j).getName().equals(tAlbumName)) {
                    newAlbumId = albums.get(j).getId();
                    hasNewAlbum = true;
                    break;
                } else if (j == albums.size() - 1) {
                    newAlbumId = ++albumIndex;
                }
            }

            for (int k = 0; k < songs.size(); k++) {
                if (songs.get(k).getName().equals(tSongName)) {
                    newSongId = songs.get(k).getId();
                    hasNewSong = true;
                    break;
                } else if (k == songs.size() - 1) {
                    newSongId = ++songIndex;
                }
            }

            if (!hasNewArtist)
                artists.add(new Artist(newArtistId, tArtistName));
            if (!hasNewAlbum)
                albums.add(new Album(newAlbumId, newArtistId, tAlbumName));
            if (!hasNewSong)
                songs.add(new Song(newSongId, newAlbumId, tSongName, tYear, fullPath));

            //Song newSong = new Song(songTitle, year, fullPath);
            //songs.add(newSong);

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

        }

        else {
            throw new NoSuchTagException("Could not find a ID3v2 tag for the uploaded file.");
        }

    }

    /*

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
                boolean isDeleted = file.delete();
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

    */

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