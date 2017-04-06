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
        Collections.sort(newList);  //Sorts list according to track numbers of each song.
        return newList;
    }

    public Song getSong(int artistId, int albumId, int songId) {
        return songMap.get(songId);
    }

    public SongInfo getSongInfo(int artistId, int albumId, int songId) {
        return new SongInfo(artistMap.get(artistId), albumMap.get(albumId), songMap.get(songId));
    }


    public ResponseEntity<InputStreamResource> getSongFile(int artistId, int albumId, int songId) throws IOException {
        System.out.println(getSong(artistId, albumId, songId));
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
            String tTrackString = tag.getTrack();
            int tTrack = 0;
            if (tag.getTrack() != null)
                tTrack = Integer.parseInt(tag.getTrack());
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
            for (Artist artist : artistMap.values())
                if (artist.getName().equals(tArtistName)) {
                    artist.setName(tArtistName);
                    newArtistId = artist.getId();
                    break;
                }
            if (newArtistId == 0) {
                artistMap.put(++artistIndex, new Artist(artistIndex, tArtistName));
                newArtistId = artistIndex;
            }

            //Get ID for file's album name.
            for (Album album : albumMap.values())
                if (album.getName().equals(tAlbumName)) {
                    album.setName(tAlbumName);
                    album.setArtistId(newArtistId);
                    newAlbumId = album.getId();
                    break;
                }
            if (newAlbumId == 0) {
                albumMap.put(++albumIndex, new Album(albumIndex, newArtistId, tAlbumName));
                newArtistId = artistIndex;
            }

            //Get ID for file's song name.
            for (Song song : songMap.values())
                if (song.getName().equals(tSongName)) {
                    song.setName(tSongName);
                    song.setAlbumId(newAlbumId);
                    newSongId = song.getId();
                }
            if (newSongId == 0)
                songMap.put(++songIndex, new Song(songIndex, newArtistId, tTrack, tSongName, tYear, fullPath));

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

    public synchronized SongInfo updateSongInfo(SongInfo songInfo) throws IOException, UnsupportedTagException, InvalidDataException, NotSupportedException {

        String oldPath = songMap.get(songInfo.getSong().getId()).getFilePath();
        String fileType = oldPath.substring(oldPath.lastIndexOf(".") + 1, oldPath.length());
        String artistPath = libraryPath + "/" + songInfo.getArtist().getName();
        String albumPath = artistPath  + "/" + songInfo.getAlbum().getName();
        String fileName = songInfo.getSong().getName() + "." + fileType;
        String newPath = albumPath + "/" + fileName;
        songInfo.getSong().setFilePath(newPath);    //JSON song objects have null filepaths, so add here.

        File file = new File(oldPath);
        Mp3File mp3 = new Mp3File(file);

        /*
         *
         *  UPDATE THE ARTIST, ALBUM, AND SONG MAPS + DIRECTORIES
         *
         */

        /*
         *  Check if names exist at the specified IDs.
         */

        boolean hasChangedArtist = false;
        boolean hasChangedAlbum = false;
        boolean hasChangedSongName = false;

        int oldArtistId = 0;
        int oldAlbumId = 0;

        //Check artist name at ID
        for (Artist artist : artistMap.values())
            if (artist.getId() == songInfo.getArtist().getId())
                if (!artist.equals(songInfo.getArtist())) {
                    hasChangedArtist = true;
                    oldArtistId = artist.getId();
                    break;
                }

        //Check album name at ID
        for (Album album : albumMap.values())
            if (album.getId() == songInfo.getAlbum().getId())
                if (!album.equals(songInfo.getAlbum())) {
                    hasChangedAlbum = true;
                    oldAlbumId = album.getId();
                    System.out.println("Assign Album ID = " + oldAlbumId + " due to album change.");
                    break;
                } else if (hasChangedArtist) {
                    oldAlbumId = album.getId();
                    System.out.println("Assign Album ID = " + oldAlbumId + " due to artist change only.");
                }

        //Check song name at ID
        for (Song song : songMap.values())
            if (song.getId() == songInfo.getSong().getId())
                if (!song.equals(songInfo.getSong())) {
                    hasChangedSongName = true;
                    System.out.println("Has changed Song.");
                    break;
                }

        /*
         *  If changes detected, first check if the
         *  changed artist or album names exists already at any other ID.
         */

        boolean hasArtistNameInMap = false;
        boolean hasAlbumNameInMap = false;

        int artistIdWithExistingName = 0;
        int albumIdWithExistingName = 0;

        //Check artist names at any ID (if applicable)
        if (hasChangedArtist)
            for (Artist artist : artistMap.values())
                if (artist.getName().equals(songInfo.getArtist().getName())) {
                    hasArtistNameInMap = true;
                    artistIdWithExistingName = artist.getId();
                    System.out.println("hasArtistNameInMap = " + hasArtistNameInMap);
                    System.out.println("artistIdWithExistingName = " + artistIdWithExistingName);
                    break;
                }

        //Check album names at any ID (if applicable)
        if (hasChangedArtist || hasChangedAlbum)
            for (Album album : albumMap.values())
                if (album.getName().equals(songInfo.getAlbum().getName())) {
                    hasAlbumNameInMap = true;
                    albumIdWithExistingName = album.getId();
                    System.out.println("hasAlbumNameInMap = " + hasAlbumNameInMap);
                    System.out.println("albumIdWithExistingName = " + albumIdWithExistingName);
                    break;
                }

        /*
         *  Modify artist, album, and song maps noted with changes.
         */

        int newArtistId = 0;
        int newAlbumId = 0;

        //Modify artist (if applicable)
        if (hasChangedArtist) {
            if (hasArtistNameInMap)
                newArtistId = artistIdWithExistingName;
            else
                newArtistId = ++artistIndex;
            songInfo.getArtist().setId(newArtistId);
            artistMap.put(newArtistId, songInfo.getArtist());
        }

        //Modify album (if applicable)
        if (hasChangedAlbum || hasChangedArtist) {
            if (hasChangedArtist)
                songInfo.getAlbum().setArtistId(newArtistId);
            if (hasAlbumNameInMap && (hasChangedArtist && hasArtistNameInMap))
                newAlbumId = albumIdWithExistingName;
            else
                newAlbumId = ++albumIndex;
            songInfo.getAlbum().setId(newAlbumId);
            albumMap.put(newAlbumId, songInfo.getAlbum());
        }

        //Modify song (if applicable)
        if (hasChangedSongName || hasChangedAlbum || hasChangedArtist) {
            if (hasChangedAlbum || hasChangedArtist)
                songInfo.getSong().setAlbumId(newAlbumId);
            songMap.put(songInfo.getSong().getId(), songInfo.getSong());
        }

        /*
         *  Remove possible unused artist or album map entries at the old IDs.
         */

        boolean hasUsedAlbumId = false;
        boolean hasUsedArtistId = false;

        //Check for unused album
        System.out.println();
        System.out.println("CHECK FOR UNUSED ALBUM:");
        if (hasChangedAlbum || hasChangedArtist)
            for (Song song : songMap.values()) {
                System.out.println(song.getAlbumId() + " == " + oldAlbumId);
                if (song.getAlbumId() == oldAlbumId) {
                    hasUsedAlbumId = true;
                    System.out.println("hasUsedAlbumId = " + hasUsedAlbumId);
                    break;
                }
            }

        if (!hasUsedAlbumId) {
            System.out.println("REMOVE ALBUM.");
            albumMap.remove(oldAlbumId);
        }

        System.out.println();

        //Check for unused artist
        System.out.println();
        System.out.println("CHECK FOR UNUSED ARTIST:");
        if (hasChangedArtist)
            for (Album album : albumMap.values()) {
                System.out.println(album.getArtistId() + " == " + oldArtistId);
                if (album.getArtistId() == oldArtistId) {
                    hasUsedArtistId = true;
                    System.out.println("hasUsedArtistId = " + hasUsedArtistId);
                    break;
                }
            }

        if (!hasUsedArtistId) {
            System.out.println("REMOVE ARTIST.");
            artistMap.remove(oldArtistId);
        }

        System.out.println("DONE.");

        System.out.println();
        System.out.println("MAP VALUES:");
        System.out.println();
        System.out.println("ARTIST MAP:");
        for (Artist artist : artistMap.values())
            System.out.println(artist);
        System.out.println();
        System.out.println("ALBUM MAP:");
        for (Album album : albumMap.values())
            System.out.println(album);
        System.out.println();
        System.out.println("SONG MAP:");
        for (Song song : songMap.values())
            System.out.println(song);
        System.out.println();

        /*
         *  Remove directories with the unused artist or album names (i.e. empty directories).
         */

        //Updates file tied to the SongInfo object with new name (if applicable) and tag info.
        if (mp3.hasId3v2Tag()) {
            byte[] albumImageBytes = mp3.getId3v2Tag().getAlbumImage();
            String albumImageMime = mp3.getId3v2Tag().getAlbumImageMimeType();
            mp3.removeId3v2Tag();
            ID3v2 tag = new ID3v24Tag();
            tag.setTitle(songInfo.getSong().getName());
            tag.setArtist(songInfo.getArtist().getName());
            tag.setAlbum(songInfo.getAlbum().getName());
            tag.setYear(songInfo.getSong().getYear());
            tag.setAlbumImage(albumImageBytes, albumImageMime);
            mp3.setId3v2Tag(tag);
            //If names changes caused path change, then save new file and delete old one.
            if (!oldPath.equals(newPath)) {
                File artist = new File(artistPath);
                File album = new File(albumPath);
                if (!artist.exists())
                    artist.mkdir();
                if (!album.exists())
                    album.mkdir();
                mp3.save(newPath);
                file.delete();
            }
        } else {
            throw new UnsupportedTagException("The associated file does not have a valid ID3v2 tag.");
        }

        //Remove empty directories at the given old path.
        removeEmptyDirectories(oldPath);

        return songInfo;

    }

    /*

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

    */


    //Delete song (if exists), as well as the album folder and the artist folder (if empty).
    public void removeSongAndEmptyDirectories(String filePath) {
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


    /*
     *  Feed in a file (that does NOT get deleted), and the function checks
     *  parent directories two levels deep and deletes them as long as they are empty.
     */
    public void removeEmptyDirectories(String filePath) {
        File songFile = new File(filePath);
        File albumFolder = songFile.getParentFile();
        File artistFolder = albumFolder.getParentFile();
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