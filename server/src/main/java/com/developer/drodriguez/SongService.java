package com.developer.drodriguez;

import com.developer.drodriguez.model.Album;
import com.developer.drodriguez.model.Artist;
import com.developer.drodriguez.model.Song;
import com.developer.drodriguez.model.SongInfo;
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

    private Map<Integer, Artist> artistMap = new TreeMap<>();
    private Map<Integer, Album> albumMap = new TreeMap<>();
    private Map<Integer, Song> songMap = new TreeMap<>();
    private int artistIndex = 0;
    private int albumIndex = 0;
    private int songIndex = 0;

    @Value("${library.path}")
    private String libraryPath;

    SongService() {
        //Dustin Kensrue: I Believe
        artistMap.put(++artistIndex, new Artist(artistIndex, "Dustin Kensrue") );
        albumMap.put(++albumIndex, new Album(albumIndex, artistIndex, "Please Come Home") );
        songMap.put(++songIndex, new Song(songIndex, albumIndex,"I Believe", "2007", "/Users/Daniel/Music/library/Dustin Kensrue/Please Come Home/I Believe.mp3") );

        //Dustin Kensrue: Consider the Ravens
        songMap.put( ++songIndex, new Song(songIndex, albumIndex,"Consider the Ravens", "2007", "/Users/Daniel/Music/library/Dustin Kensrue/Please Come Home/Consider the Ravens.mp3") );

        //Foo Fighters: Times Like These
        artistMap.put( ++artistIndex, new Artist(artistIndex, "Foo Fighters") );
        albumMap.put( ++albumIndex, new Album(albumIndex, artistIndex, "Greatest Hits") );
        songMap.put( ++songIndex, new Song(songIndex, albumIndex, "Times Like These", "2009", "/Users/Daniel/Music/library/Foo Fighters/Greatest Hits/Times Like These.mp3") );
    }

    public List<Artist> getArtists() {
        return new ArrayList<>(artistMap.values());
    }

    public Artist getArtist(int artistId) {
        return artistMap.get(artistId);
    }

    public List<Album> getAlbums(int artistId) {
        List<Album> newList = new ArrayList<>();
        for (Album album : albumMap.values())
            if(album.getArtistId() == artistId)
                newList.add(album);
        return newList;
    }

    public Album getAlbum(int artistId, int albumId) {
        return albumMap.get(albumId);
    }

    public List<Song> getSongs(int artistId, int albumId) {
        List<Song> newList = new ArrayList<>();
        for (Song song : songMap.values())
            if (song.getAlbumId() == albumId)
                newList.add(song);
        return newList;
    }

    public Song getSong(int artistId, int albumId, int songId) {
        return songMap.get(songId);
    }

    public SongInfo getSongInfo(int artistId, int albumId, int songId) {
        return new SongInfo(artistMap.get(artistId), albumMap.get(albumId), songMap.get(songId));
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

            int newArtistId = 0;
            int newAlbumId = 0;
            int newSongId = 0;

            /*
             *  If there are artists, albums, or songs with existing names, then update,
             *  otherwise create a new object with a new index.
             */

            //Get ID for file's artist name.
            for (Artist artist : artistMap.values()) {
                if (artist.getName().equals(tArtistName)) {
                    artist.setName(tArtistName);
                    newArtistId = artist.getId();
                    break;
                }
            }
            if (newArtistId == 0) {
                artistMap.put(++artistIndex, new Artist(artistIndex, tArtistName));
                newArtistId = artistIndex;
            }

            //Get ID for file's album name.
            for (Album album : albumMap.values()) {
                if (album.getName().equals(tAlbumName)) {
                    album.setName(tAlbumName);
                    album.setArtistId(newArtistId);
                    newAlbumId = album.getId();
                    break;
                }
            }
            if (newAlbumId == 0) {
                albumMap.put(++albumIndex, new Album(albumIndex, newArtistId, tAlbumName));
                newArtistId = artistIndex;
            }

            //Get ID for file's song name.
            for (Song song : songMap.values()) {
                if (song.getName().equals(tSongName)) {
                    song.setName(tSongName);
                    song.setAlbumId(newAlbumId);
                    newSongId = song.getId();
                }
            }
            if (newSongId == 0)
                songMap.put(++songIndex, new Song(songIndex, newArtistId, tSongName, tYear, fullPath));

            System.out.println();

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