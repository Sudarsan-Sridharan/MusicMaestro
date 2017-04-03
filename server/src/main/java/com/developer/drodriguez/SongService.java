package com.developer.drodriguez;

import com.developer.drodriguez.model.Album;
import com.developer.drodriguez.model.Artist;
import com.developer.drodriguez.model.Song;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.PathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by Daniel on 3/21/17.
 */

@Service
public class SongService {

    private List<Artist> artists = new ArrayList<>();

    @Value("${library.path}")
    private String libraryPath;

    SongService() {
        //Dustin Kensrue: I Believe
        Song song1 = new Song(1, "I Believe", 2007, "/Users/Daniel/Music/library/Dustin Kensrue/Please Come Home/I Believe.mp3");
        Album album1 = new Album(1, "Please Come Home", song1);
        Artist artist1 = new Artist(1, "Dustin Kensrue", album1);
        artists.add(artist1);

        //Dustin Kensrue: Consider the Ravens
        Song song2 = new Song(2, "Consider the Ravens", 2007, "/Users/Daniel/Music/library/Dustin Kensrue/Please Come Home/Consider the Ravens.mp3");
        album1.addSong(song2);

        //Foo Fighters: Times Like These
        Song song3 = new Song(3, "Times Like These", 2009);
        Album album2 = new Album(2, "Greatest Hits", song3);
        Artist artist2 = new Artist(2, "Foo Fighters", album2);
        artists.add(artist2);
    }

    public List<Artist> getArtists() {
        return artists;
    }

    public List<Album> getAlbums(int artistId) {
        for (Artist artist : artists)
            if (artist.getId() == artistId)
                return artist.getAlbums();
        return null;
    }

    public List<Song> getSongs(int artistId, int albumId) {
        for (Artist artist : artists)
            if (artist.getId() == artistId)
                for (Album album : artist.getAlbums())
                    if (album.getId() == albumId)
                        return album.getSongs();
        return null;
    }

    public Song getSong(int artistId, int albumId, int songId) {
        for (Artist artist : artists)
            if (artist.getId() == artistId)
                for (Album album : artist.getAlbums())
                    if (album.getId() == albumId)
                        for (Song song : album.getSongs())
                            if (song.getId() == songId)
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

    /*

    public Song addSongFile(MultipartFile file)
            throws IOException, UnsupportedTagException, InvalidDataException, NoSuchTagException {

        System.out.println("POST SongFile.");

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

    public File convertMultipartToFile(MultipartFile file) throws IOException
    {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    */

}