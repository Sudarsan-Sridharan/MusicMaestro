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

    SongService() throws IOException {
        readLibraryFile();
    }

    public List<Artist> getArtists() {
        List<Artist> newList = new ArrayList<>(artistMap.values());
        Collections.sort(newList);
        return newList;
    }

    public Artist getArtist(int artistId) {
        return artistMap.get(artistId);
    }

    public List<Album> getAlbums(int artistId) {
        List<Album> newList = new ArrayList<>();
        for (Album album : albumMap.values())
            if (album.getArtistId() == artistId)
                newList.add(album);
        Collections.sort(newList);
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

            String tArtistName = null;
            String tAlbumName = null;
            String tSongName = null;
            int tSongTrack = 0;
            String tSongYear = null;

            if (tag.getArtist() != null)
                tArtistName = tag.getArtist();
            else
                tArtistName = "";

            if (tag.getAlbum() != null)
                tAlbumName = tag.getAlbum();
            else
                tAlbumName = "";

            if (tag.getTitle() != null)
                tSongName = tag.getTitle();
            else
                tSongName = "";

            if (tag.getTrack() != null)
                if (tag.getTrack().contains("/"))
                    tSongTrack = Integer.parseInt(tag.getTrack().substring(0, tag.getTrack().lastIndexOf("/"))); //Substring removes "out of total tracks" (x"/xx") extension.
                else
                    tSongTrack = Integer.parseInt(tag.getTrack());

            if (tag.getYear() != null)
                tSongYear = tag.getYear();
            else
                tSongYear = "";

            String originalFilename = file.getOriginalFilename();
            String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
            String filePath = libraryPath + File.separator
                    + removeInvalidPathChars(tArtistName) + File.separator + removeInvalidPathChars(tAlbumName);
            String fileName = removeInvalidPathChars(tSongName) + "." + fileType;
            String fullPath = filePath + File.separator + fileName;

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
                newArtistId = ++artistIndex;
                artistMap.put(newArtistId, new Artist(newArtistId, tArtistName));
            }

            //Get ID for file's album name.
            for (Album album : albumMap.values())
                if (album.getName().equals(tAlbumName) && (newArtistId == 0 || album.getArtistId() == newArtistId) ) {
                    album.setName(tAlbumName);
                    album.setArtistId(newArtistId);
                    newAlbumId = album.getId();
                    break;
                }
            if (newAlbumId == 0) {
                newAlbumId = ++albumIndex;
                albumMap.put(newAlbumId, new Album(newAlbumId, newArtistId, tAlbumName));
            }

            //Get ID for file's song name.
            for (Song song : songMap.values())
                if (song.getName().equals(tSongName) && (newAlbumId == 0 || song.getAlbumId() == newAlbumId) ) {
                    song.setName(tSongName);
                    song.setAlbumId(newAlbumId);
                    newSongId = song.getId();
                }
            if (newSongId == 0) {
                newSongId = ++songIndex;
                songMap.put(newSongId, new Song(newSongId, newAlbumId, tSongTrack, tSongName, tSongYear, fullPath));
            }

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

            writeLibraryFile();

        }

        else {
            throw new NoSuchTagException("Could not find a ID3v2 tag for the uploaded file.");
        }

    }

    public synchronized SongInfo updateSongInfo(SongInfo songInfo, int artistId, int albumId, int songId) throws IOException, UnsupportedTagException, InvalidDataException, NotSupportedException {

        String oldPath = songMap.get(songInfo.getSong().getId()).getFilePath();
        String fileType = oldPath.substring(oldPath.lastIndexOf(".") + 1, oldPath.length());
        String artistPath = libraryPath + File.separator + removeInvalidPathChars(songInfo.getArtist().getName());
        String albumPath = artistPath  + File.separator + removeInvalidPathChars(songInfo.getAlbum().getName());
        String fileName = removeInvalidPathChars(songInfo.getSong().getName()) + "." + fileType;
        String newPath = albumPath + File.separator + fileName;
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
            if (artist.getId() == artistId)
                if (!artist.equals(songInfo.getArtist())) {
                    hasChangedArtist = true;
                    oldArtistId = artist.getId();
                    break;
                }

        //Check album name at ID
        for (Album album : albumMap.values())
            if (album.getId() == albumId)
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
            if (song.getId() == songId)
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

        writeLibraryFile();

        return songInfo;

    }

    public void deleteSong(int artistId, int albumId, int songId) {

        String filePath = songMap.remove(songId).getFilePath();

        boolean hasArtistId = false;
        boolean hasAlbumId = false;

        //If no song references the given album ID, delete the album in its map.
        for (Song song : songMap.values())
            if (song.getAlbumId() == albumId)
                hasAlbumId = true;
        if (!hasAlbumId)
            albumMap.remove(albumId);

        //If no album references the given artist ID, delete the artist in its map.
        for (Album album : albumMap.values())
            if (album.getArtistId() == artistId)
                hasArtistId = true;
        if (!hasArtistId)
            artistMap.remove(artistId);

        removeFileAndEmptyDirectories(filePath);

        System.out.println("REMOVED SONG ID: " + songId);

    }

    //Delete song (if exists), as well as the album folder and the artist folder (if empty).
    public void removeFileAndEmptyDirectories(String filePath) {
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

    //Replaces any occurrence of an invalid path character with '_'
    public String removeInvalidPathChars(String originalString) {
        char[] originalChars = originalString.toCharArray();
        char[] badChars = {'\\', '/', ':', '*', '?', '<', '>', '|'};
        for (int i = 0; i < originalChars.length; i++)
            for (int j = 0; j < badChars.length; j++)
                if (originalChars[i] == badChars[j])
                    originalChars[i] = '_';
        return new String(originalChars);
    }

    public void readLibraryFile() throws IOException, FileNotFoundException {

        System.out.println("READING LIBRARY FILE...");
        File library = new File("/Users/Daniel/Music/library/library.mpl");
        if (!library.exists())  //Create empty library file if it does not exist.
            writeLibraryFile();

        Scanner scanner = new Scanner(new FileReader(library));
        String indices[] = null;
        String section = null;
        String line = null;

        //Only read file if it contains the proper header.
        if (!scanner.nextLine().equals("**MPLIBRARY**"))
            return;

        if (scanner.nextLine().equals("--INDEX--"))
            indices = scanner.nextLine().split(",");

        artistIndex = Integer.parseInt(indices[0]);
        albumIndex = Integer.parseInt(indices[1]);
        songIndex = Integer.parseInt(indices[2]);

        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.equals("--ARTIST--") || line.equals("--ALBUM--") || line.equals("--SONG--")) {
                section = line;
                line = scanner.nextLine();
            }
            if (line.equals("**MPEND**"))
                break;

            String[] fields = line.split(",");

            if (section.equals("--ARTIST--") && !line.equals("--ALBUM--"))
                artistMap.put(Integer.parseInt(fields[0]), new Artist(Integer.parseInt(fields[0]), fields[1]));
            else if (section.equals("--ALBUM--") && !line.equals("--SONG--"))
                albumMap.put(Integer.parseInt(fields[0]), new Album(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), fields[2]));
            else if (section.equals("--SONG--"))
                songMap.put(Integer.parseInt(fields[0]), new Song(Integer.parseInt(fields[0]),
                        Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), fields[3], fields[4], fields[5]));
        }

        scanner.close();

        for (Artist artist : artistMap.values())
            System.out.println(artist);
        for (Album album : albumMap.values())
            System.out.println(album);
        for (Song song : songMap.values())
            System.out.println(song);

    }

    public void writeLibraryFile () throws IOException, FileNotFoundException {

        //Backup current library
        File library = new File("/Users/Daniel/Music/library/library.mpl");
        File libraryBackup = new File("/Users/Daniel/Music/library/library.mpl.bak");
        library.renameTo(libraryBackup);
        library.delete();

        //Create and open stream for new library
        File newLibrary = new File("/Users/Daniel/Music/library/library.mpl");
        FileOutputStream fos = new FileOutputStream(newLibrary);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        bw.write("**MPLIBRARY**");
        bw.newLine();

        bw.write("--INDEX--");
        bw.newLine();

        bw.write(artistIndex + "," + albumIndex + "," + songIndex);
        bw.newLine();

        bw.write("--ARTIST--");
        bw.newLine();

        for (Artist artist : artistMap.values()) {
            bw.write(artist.getId() + "," + artist.getName());
            bw.newLine();
        }

        bw.write("--ALBUM--");
        bw.newLine();

        for (Album album : albumMap.values()) {
            bw.write(album.getId() + "," + album.getArtistId() + "," + album.getName());
            bw.newLine();
        }

        bw.write("--SONG--");
        bw.newLine();

        for (Song song : songMap.values()) {
            bw.write(song.getId() + "," + song.getAlbumId() + "," + song.getTrack()
                    + "," + song.getName() + "," + song.getYear() + "," + song.getFilePath());
            bw.newLine();
        }

        bw.write("**MPEND**");

        bw.close();
        fos.close();

        libraryBackup.delete();

    }

}