import { Component, OnInit } from '@angular/core';
import { SongService } from './song.service';
import { Observable } from 'rxjs/Observable';
import { Artist } from './model/Artist';
import { Album } from './model/Album';
import { Song } from './model/Song';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  constructor(private songService: SongService) {}

  //Keeps one menu section active at a time:
  //["Music Library", "Edit Song", "Add a Song"->"Single", "Add a Song"->"Multiple"]
  isActiveSection: Array<boolean> = [false, false, false, false];

  artists: Array<Artist>;
  albums: Array<Album>;
  songs: Array<Song>;

  hasNoArtistList: boolean = true;
  hasNewSongPlayRequest: boolean = true;
  selArtistId: number;
  selAlbumId: number;
  selSongId: number;
  currSong: Song;
  songPlayback;
  songArtworkSrc: String;
  newSong: Song = {title: null, album: null, artist: null, year: null, filePath: null};
  newSongFiles: FileList;
  uploadProgressCurrent: number = 0;
  uploadProgressMax: number = 1;

  ngOnInit() {
    this.getArtistList();
  }

  getArtistList() {

    this.songService.getArtists().subscribe(artists => {
      this.artists = artists;
      if (this.artists.length == 0) { this.hasNoArtistList = true; }
      else { this.hasNoArtistList = false; }
    });

    /*
    this.songService.getArtistList().subscribe(artistList => {
      this.artistList = artistList;
      if (artistList.length == 0) { this.hasNoArtistList = true; }
      else { this.hasNoArtistList = false; }
    });
    */
  }

  getAlbumList(artistId: number) {
    this.selArtistId = artistId;
    this.selAlbumId = null;  //Hides song listing in the view.
    this.songService.getAlbums(artistId)
    .subscribe(albums => this.albums = albums);
  }

  getSongList(artistId: number, albumId: number) {
    this.selAlbumId = albumId;
    this.songService.getSongs(artistId, albumId)
    .subscribe(songs => this.songs = songs);
  }

  getSong(songId: number) {
    this.selSongId = songId;
    this.songService.getSong(this.selArtistId, this.selAlbumId, songId)
    .subscribe(song => this.getSongPlayback(this.selArtistId, this.selAlbumId, song));
  }

  getSongPlayback(artistId: number, albumId: number, song: Song) {
    if (this.songPlayback == null) { this.songPlayback = new Audio(); }
    this.currSong = song;
    this.getSongArtwork(artistId, albumId, this.selSongId);
    this.songPlayback.src = "http://localhost:8080/playback/" + artistId + "/" + albumId + "/" + this.selSongId;
    this.songPlayback.load();
    this.songPlayback.play();
  }

  getSongArtwork(artistId: number, albumId: number, songId: number) {
    this.songArtworkSrc = "http://localhost:8080/artwork/" + artistId + "/" + albumId + "/" + songId;
  }

/*
  addSong(files: FileList) {
    this.newSongFiles = files;
    this.songService.addSong(this.newSongFiles[0]).subscribe( newSong => {
      this.newSong = newSong;
      this.getArtistList();
    });
  }

  addMultipleSongs(fileList: FileList) {
    this.newSongFiles = fileList;   //Used in view to show progress bar.
    this.resetProgressBar(); //Resets the current and max values for the progress bar.
    this.uploadProgressMax = fileList.length; //Sets the new max value for progress bar.
    for (let i = 0; i < fileList.length; i++) { //Loop through list of files.
      this.songService.addSong(fileList[i]).subscribe( () => {  //Send song to server.
        this.uploadProgressCurrent++; //Increment the current value for progress bar.
        if (i == fileList.length - 1) { //During last loop,
          this.refreshLibrary(); //Refresh library
          setTimeout( () => { //Delay 2 seconds.
            this.exitMenu();  //Clears out of current menu.
            this.newSongFiles = null; //Used in view to hide progress bar.
          }, 2000);
        }
      });
    }

  }
  */

  /*
  updateNewSongInfo() {
    this.songService.updateSongInfo(this.newSong).subscribe( () => {
      this.newSongFiles = null;
      if (this.hasNewSongPlayRequest) {
        this.hasNewSongPlayRequest = false;
        this.getSongPlayback(this.newSong);
        //this.markSongTitle(this.newSong.artist, this.newSong.album, this.newSong.title);
      }
      this.exitMenu();
      this.clearNewSong();
    });
  }

  updatecurrSongInfo() {
    this.songService.updateSongInfo(this.currSong).subscribe( () => {
      this.exitMenu();
      this.songs = null;
      this.refreshLibrary();
      //this.markSongTitle(this.currSong.artist, this.currSong.album, this.currSong.title);
      this.getSongPlayback(this.currSong);
    });
  }
  */

  /*
  removeSong(i: number) {
    this.songService.removeSong(this.currSong).subscribe( () => {
      this.songPlayback.pause();
      this.songPlayback.remove();
      this.songPlayback = null;
      this.currSong = null;
      this.exitMenu();
      this.refreshLibrary();
    });
  }

  clearNewSong() {
    this.newSong = {title: null, album: null, artist: null, year: null, filePath: null};
  }

  exitMenu() {
    this.isActiveSection = [false, false, false, false];
  }

  resetProgressBar() {
    this.uploadProgressCurrent = 0;
    this.uploadProgressMax = 1;
  }

  refreshLibrary() {
    this.selArtistId = null;
    this.selAlbumId = null;
    this.selSongId = null;
    this.songs = null;
    this.songs = null;
    this.songs = null;
    this.getArtistList();
  }

  markSongTitle(artistId: number, albumId: number, titleId: number) {
    this.selArtistId = artistId;
    this.selAlbumId = albumId;
    this.selSongId = titleId;
    this.getArtistList();
    //this.getAlbumList(artistId);
    //this.getSongList(albumId);
  }
  */

}
