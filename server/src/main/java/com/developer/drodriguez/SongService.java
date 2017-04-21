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

    //Stores the artist, album, and song file metadata.
    private Map<Integer, Artist> artistMap = new TreeMap<>();
    private Map<Integer, Album> albumMap = new TreeMap<>();
    private Map<Integer, Song> songMap = new TreeMap<>();

    //Keeps track of the last available index for new map keys.
    private int artistIndex = 0;
    private int albumIndex = 0;
    private int songIndex = 0;

    //Delimiters for reading from and writing to the library file.
    private String escDelimiter = "\\|";
    private String delimiter = "|";

    //Contains the path to the library directory, which stores the imported songs and library file.
    private String libraryPath;

    //Initialize the class by loading the library file data into the indices and map objects.
    SongService(@Value("${library.path}") String initLibraryPath) throws IOException {
        libraryPath = initLibraryPath;
        readLibraryFile();
    }

    //Return all artist objects from the artist map to the requester.
    public List<Artist> getArtists() {
        List<Artist> newList = new ArrayList<>(artistMap.values());
        Collections.sort(newList);
        return newList;
    }

    //Return the artist object with the specified ID from the artist map to the requester.
    public Artist getArtist(int artistId) {
        return artistMap.get(artistId);
    }

    //Return all album objects from the album map to the requester.
    public List<Album> getAlbums(int artistId) {
        List<Album> newList = new ArrayList<>();
        for (Album album : albumMap.values())
            if (album.getArtistId() == artistId)
                newList.add(album);
        Collections.sort(newList);
        return newList;
    }

    //Return the album object with the specified ID from the album map to the requester.
    public Album getAlbum(int artistId, int albumId) {
        return albumMap.get(albumId);
    }

    //Return all song objects from the song map to the requester.
    public List<Song> getSongs(int artistId, int albumId) {
        List<Song> newList = new ArrayList<>();
        for (Song song : songMap.values())
            if (song.getAlbumId() == albumId)
                newList.add(song);
        Collections.sort(newList);  //Sorts list according to track numbers of each song.
        return newList;
    }

    //Return the song object with the specified ID from the song map to the requester.
    public Song getSong(int artistId, int albumId, int songId) {
        return songMap.get(songId);
    }

    //Return an object containing the ID-associated artist, album, and song objects to the requester.
    public SongInfo getSongInfo(int artistId, int albumId, int songId) {
        return new SongInfo(artistMap.get(artistId), albumMap.get(albumId), songMap.get(songId));
    }

    //Returns an mp3 file to the requester for the given IDs.
    public ResponseEntity<InputStreamResource> getSongFile(int artistId, int albumId, int songId) throws IOException {
        String filePath = getSong(artistId, albumId, songId).getFilePath();
        PathResource file = new PathResource(filePath);
        return ResponseEntity
                .ok()
                .contentLength(file.contentLength())
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(new InputStreamResource(file.getInputStream()));
    }

    //Returns the album artwork as a byte array to the requester for the given IDs.
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


    /*
     *  Adds a song file to the library path,
     *  and writes the data from the song tags to the artist, album, and song objects then into their maps..
     *  Checks for already-existing artist and album objects to update to, otherwise create new ones.
     */

    public synchronized void addSongFile(MultipartFile file)
            throws IOException, UnsupportedTagException, InvalidDataException, NoSuchTagException {

        //Read in multipart file data as a file object, then into a mp3agic library file.
        File tempFile = convertMultipartToFile(file);
        Mp3File mp3 = new Mp3File(tempFile);

        //Checks if mp3 has an ID3v2 tag to read data from. ID3v1 is deprecated.
        if (mp3.hasId3v2Tag()) {

            //Create ID3v2 tag object.
            ID3v2 tag = mp3.getId3v2Tag();

            //Declare the fields to be stored in an artist, album, and song object.
            String tArtistName = null;
            String tAlbumName = null;
            String tSongName = null;
            String tSongYear = null;
            int tSongTrack = 0;

            //Conditions to make sure the fields are never null. Avoids NullPointerException bug in program.
            if (tag.getArtist() != null) { tArtistName = tag.getArtist(); }
            else {tArtistName = ""; }
            if (tag.getAlbum() != null) { tAlbumName = tag.getAlbum(); }
            else { tAlbumName = ""; }
            if (tag.getTitle() != null) { tSongName = tag.getTitle(); }
            else { tSongName = ""; }
            if (tag.getYear() != null) { tSongYear = tag.getYear(); }
            else { tSongYear = ""; }

            //Crop year out of possible date tags.
            if (tSongYear.length() > 4)
                tSongYear = tSongYear.substring(0, 4);

            //Parse string-casted track number to int type. Substring removes "out of total tracks" (x"/xx") extension.
            if (tag.getTrack() != null)
                if (tag.getTrack().contains("/"))
                    tSongTrack = Integer.parseInt(tag.getTrack().substring(0, tag.getTrack().lastIndexOf("/")));
                else
                    tSongTrack = Integer.parseInt(tag.getTrack());

            //Parse out the new file path used to write the file to the library path and the data to the song object.
            String originalFilename = file.getOriginalFilename();
            String fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
            String filePath = libraryPath + File.separator
                    + removeInvalidPathChars(tArtistName) + File.separator + removeInvalidPathChars(tAlbumName);
            String fileName = removeInvalidPathChars(tSongName) + "." + fileType;
            String fullPath = filePath + File.separator + fileName;

            //No longer need the file object, since it is currently stored as an Mp3File object.
            tempFile.delete();

            //Used to write keys to artist, album, and song objects, either with existing matched ones or new indices.
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

            //Update the library file.
            writeLibraryFile();

        }

        else {
            throw new NoSuchTagException("Could not find a ID3v2 tag for the uploaded file.");
        }

    }

    /*
     *  Updates the song data. This is a more complicated algorithm than expected since the song data has many dependencies.
     *  First, the method checks for the which name changes were made and records their IDs.
     *  Next, it checks to see if there are any other IDs that match the name changes, if one exists, set it as the new ID.
     *  Finally, modify the object maps, apply changes to the file, remove any now-empty directories, and recreate the file tag.
     */

    public synchronized SongInfo updateSong(SongInfo songInfo, int artistId, int albumId, int songId) throws IOException, UnsupportedTagException, InvalidDataException, NotSupportedException {

        //Stores the old file path and parses out the new one to be used.
        String oldPath = songMap.get(songInfo.getSong().getId()).getFilePath();
        String fileType = oldPath.substring(oldPath.lastIndexOf(".") + 1, oldPath.length());
        String artistPath = libraryPath + File.separator + removeInvalidPathChars(songInfo.getArtist().getName());
        String albumPath = artistPath  + File.separator + removeInvalidPathChars(songInfo.getAlbum().getName());
        String fileName = removeInvalidPathChars(songInfo.getSong().getName()) + "." + fileType;
        String newPath = albumPath + File.separator + fileName;
        songInfo.getSong().setFilePath(newPath);    //JSON song objects have null filepaths, so add here.

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
                if (album.getName().equals(songInfo.getAlbum().getName()) && album.getArtistId() == artistIdWithExistingName) {
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
         *  Create a new tag for the song file, and
         *  remove directories with the unused artist or album names (i.e. empty directories).
         */

        File file = new File(oldPath);
        Mp3File mp3 = new Mp3File(file);

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
        } else {
            throw new UnsupportedTagException("The associated file does not have a valid ID3v2 tag.");
        }

        //If names changes caused path change, then save new file and delete old one.
        if (!oldPath.equals(newPath)) {
            File artist = new File(artistPath);
            File album = new File(albumPath);
            if (!artist.exists())
                artist.mkdir();
            if (!album.exists())
                album.mkdir();
            mp3.save(newPath);
            deleteFileAndEmptyDirs(oldPath);
        }

        //Updates the library file.
        writeLibraryFile();

        return songInfo;

    }

    /*
     *  Deletes the song object from the given maps.
     *  Deletes the album and/or artist objects from their maps if now empty.
     *  Deletes the file and any now-empty containing album or artist directories.
     */
    public void deleteSong(int artistId, int albumId, int songId) {

        //Removes the song object from its map and stores its file path.
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

        //Deletes the file and any now-empty album or artist directories.
        deleteFileAndEmptyDirs(filePath);

    }

    //Delete song (if exists), as well as the album folder and the artist folder (if empty).
    public void deleteFileAndEmptyDirs(String filePath) {
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

    //Converts a MultipartFile object to a File object through a FileOutputStream object.
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

    //Reads the library file to initialize the artist, album, and song maps + indices.
    public void readLibraryFile() throws IOException, FileNotFoundException {

        System.out.println("READING LIBRARY FILE...");
        File library = new File(libraryPath + File.separator + "library.mpl");
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
            indices = scanner.nextLine().split(escDelimiter);

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

            String[] fields = line.split(escDelimiter);

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

    //Writes the current artist, album, and song maps + indices to the library file.
    public void writeLibraryFile () throws IOException, FileNotFoundException {

        //Backup current library
        File library = new File(libraryPath + File.separator + "library.mpl");
        File libraryBackup = new File(libraryPath + File.separator + "library.mpl.bak");
        library.renameTo(libraryBackup);
        library.delete();

        //Create and open stream for new library
        File newLibrary = new File(libraryPath + File.separator + "library.mpl");
        FileOutputStream fos = new FileOutputStream(newLibrary);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        bw.write("**MPLIBRARY**");
        bw.newLine();

        bw.write("--INDEX--");
        bw.newLine();

        bw.write(artistIndex + delimiter + albumIndex + delimiter + songIndex);
        bw.newLine();

        bw.write("--ARTIST--");
        bw.newLine();

        for (Artist artist : artistMap.values()) {
            bw.write(artist.getId() + delimiter + artist.getName());
            bw.newLine();
        }

        bw.write("--ALBUM--");
        bw.newLine();

        for (Album album : albumMap.values()) {
            bw.write(album.getId() + delimiter + album.getArtistId() + delimiter + album.getName());
            bw.newLine();
        }

        bw.write("--SONG--");
        bw.newLine();

        for (Song song : songMap.values()) {
            bw.write(song.getId() + delimiter + song.getAlbumId() + delimiter + song.getTrack()
                    + delimiter + song.getName() + delimiter + song.getYear() + delimiter + song.getFilePath());
            bw.newLine();
        }

        bw.write("**MPEND**");

        bw.close();
        fos.close();

        //Deletes backed-up library.
        libraryBackup.delete();

    }

}