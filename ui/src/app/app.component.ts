import { Component, OnInit } from '@angular/core';
import { SongService } from './song.service';
import { Observable } from 'rxjs/Observable';
import { Artist } from './model/Artist';
import { Album } from './model/Album';
import { Song } from './model/Song';
import { SongInfo } from './model/SongInfo';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  constructor(private songService: SongService) {}

  //isActiveSection --> Keeps one menu section active at a time:
  //["Music Library", "Edit Song", "Add a Song"->"Single", "Add a Song"->"Multiple"]
  isActiveSection: Array<boolean> = [false, false, false, false];
  isUploading: boolean = false;
  isPlayingSong: boolean = false;
  currProgress: number = 0;
  maxProgress: number = 1;
  selArtistId: number;
  selAlbumId: number;
  selSongId: number;
  artists: Array<Artist>;
  albums: Array<Album>;
  songs: Array<Song>;
  currSongInfo: SongInfo;
  songArtworkSrc: String;
  songPlayback;

  ngOnInit() {
    this.getArtists();
  }

  getArtists() {
    this.songService.getArtists().subscribe(artists => this.artists = artists);
  }

  getAlbums(artistId: number) {
    this.selArtistId = artistId;
    this.selAlbumId = null;  //Hides song listing in the view.
    this.songService.getAlbums(artistId)
    .subscribe(albums => this.albums = albums);
  }

  getSongs(artistId: number, albumId: number) {
    this.selAlbumId = albumId;
    this.songService.getSongs(artistId, albumId)
    .subscribe(songs => this.songs = songs);
  }

  getSong(songId: number) {
    this.selSongId = songId;
    this.songService.getSong(this.selArtistId, this.selAlbumId, songId)
    .subscribe( () => {
      this.getSongInfo(this.selArtistId, this.selAlbumId, this.selSongId);
    });
  }

  getSongInfo(artistId: number, albumId: number, songId: number) {
    this.songService.getSongInfo(artistId, albumId, songId)
    .subscribe(songInfo => {
      this.currSongInfo = songInfo;
      this.playSong();
    });
  }

  playSong() {
    if (this.songPlayback == null) { this.songPlayback = new Audio(); }
    this.isPlayingSong = true;
    this.songArtworkSrc = "http://localhost:8080/artwork/artist/" + this.currSongInfo.artist.id + "/album/" + this.currSongInfo.album.id + "/song/" + this.currSongInfo.song.id;
    this.songPlayback.src = "http://localhost:8080/playback/artist/" + this.currSongInfo.artist.id + "/album/" + this.currSongInfo.album.id + "/song/" + this.currSongInfo.song.id;
    this.songPlayback.load();
    this.songPlayback.play();
  }

  addSongs(fileList: FileList) {
    this.isUploading = true;   //Used in view to show progress bar.
    this.maxProgress = fileList.length; //Sets the new max value for progress bar.
    for (let i = 0; i < fileList.length; i++) { //Loop through list of files.
      this.songService.addSong(fileList[i]).subscribe( () => {  //Send song to server.
        this.currProgress++; //Increment the current value for progress bar.
        if (i == fileList.length - 1) { //During last loop,
          this.refreshLibrary(); //Refresh library
          setTimeout( () => { //Delay 0.8 seconds.
            this.exitMenu();  //Clears out of current menu.
            this.resetProgressBar(); //Resets the current and max values for the progress bar.
            this.isUploading = false; //Used in view to hide progress bar.
          }, 800);
        }
      });
    }
  }

  /*

  updateSongInfo() {
    this.songService.updateSongInfo(this.currSong).subscribe( () => {
      this.exitMenu();
      this.songs = null;
      this.refreshLibrary();
      //this.markSongTitle(this.currSong.artist, this.currSong.album, this.currSong.title);
      this.playSong(this.currSong);
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
  */

  exitMenu() {
    this.isActiveSection = [false, false, false, false];
  }

  resetProgressBar() {
    this.currProgress = 0;
    this.maxProgress = 1;
  }

  refreshLibrary() {
    this.selArtistId = null;
    this.selAlbumId = null;
    this.selSongId = null;
    this.songs = null;
    this.songs = null;
    this.songs = null;
    this.getArtists();
  }
/*
  markSongTitle(artistId: number, albumId: number, titleId: number) {
    this.selArtistId = artistId;
    this.selAlbumId = albumId;
    this.selSongId = titleId;
    this.getArtists();
    //this.getAlbums(artistId);
    //this.getSongs(albumId);
  }
  */

}
