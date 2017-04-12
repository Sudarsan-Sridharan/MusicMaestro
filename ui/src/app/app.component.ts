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
  //["Music Library", "Edit Song", "Add Song"]
  isActiveSection: Array<boolean> = [false, false, false];
  isActiveTab: Array<boolean> = [false, false, false];
  isUploading: boolean = false;
  isPlayingSong: boolean = false;
  hasSelSong: boolean = false;
  doShowSettings: boolean = false;
  doRepeat: boolean = false;
  doShuffle: boolean = false;
  currProgress: number = 0;
  maxProgress: number = 1;
  selArtistId: number;
  selAlbumId: number;
  selSongId: number;
  artists: Array<Artist>;
  albums: Array<Album>;
  songs: Array<Song>;
  currSongs: Array<Song>;
  currSongInfo: SongInfo;
  songArtworkSrc: String;
  songPlayback;
  currPlayPos: number = 0;
  maxPlayPos: number = 1;
  currPlayPosFormatted: String = "00:00";
  maxPlayPosFormatted: String = "00:00";

  ngOnInit() {
    this.getArtists();
  }

  getArtists() {
    this.songService.getArtists().subscribe(artists => this.artists = artists);
  }

  getAlbums(artistId: number) {
    this.selArtistId = null; //Triggers albums animation by reseting to null first..
    this.selAlbumId = null;  //Hides song listing in the view.
    this.songService.getAlbums(artistId)
    .subscribe(albums => {
      this.selArtistId = artistId;
      this.albums = albums;
    });

  }

  getSongs(artistId: number, albumId: number) {
    this.selAlbumId = albumId;
    this.songService.getSongs(artistId, albumId)
    .subscribe(songs => this.songs = songs);
  }

  getSong(songId: number) {
    //If getSong() is for a song in a different album, then trigger animation.
    if (this.currSongInfo == null || this.currSongInfo.album.id != this.selAlbumId) {
      this.hasSelSong = false;  //Triggers animation for changing from previous song.
    }
    this.selSongId = songId;
    this.currSongs = this.songs;  //For playback through album in player.
    console.log("INSIDE GETSONG(). selSongId = " + this.selSongId);
    this.songService.getSong(this.selArtistId, this.selAlbumId, songId)
    .subscribe( () => {
      console.log("SUBSCRIBING TO songService.getSong()");
      this.getSongInfo(this.selArtistId, this.selAlbumId, this.selSongId);
    });
  }

  getSongInfo(artistId: number, albumId: number, songId: number) {
    this.songService.getSongInfo(artistId, albumId, songId)
    .subscribe(songInfo => {
      this.currSongInfo = songInfo;
      this.hasSelSong = true;
      console.log("currSongInfo.song.name = " + this.currSongInfo.song.name);
      this.loadSong();
      setTimeout( () => this.playSong(), 300);
    });
  }

  loadSong() {
    if (this.songPlayback == null) { this.songPlayback = new Audio(); }
    this.songArtworkSrc = "http://localhost:8080/artists/" + this.currSongInfo.artist.id + "/albums/" + this.currSongInfo.album.id + "/songs/" + this.currSongInfo.song.id + "/artwork";
    let self = this;
    let xhr = new XMLHttpRequest();
    xhr.addEventListener('progress', function(e) {
      if (e.lengthComputable) { let percentComplete = e.loaded / e.total; }
    });
    xhr.addEventListener('load', function(blob) {
      console.log("LOADED SONG.");
      if (xhr.status == 200) { self.songPlayback.src = window.URL.createObjectURL(xhr.response); }
    });
    let src = "http://localhost:8080/artists/" + this.currSongInfo.artist.id + "/albums/" + this.currSongInfo.album.id + "/songs/" + this.currSongInfo.song.id + "/file";
    xhr.open('GET', src);
    xhr.responseType = 'blob';
    xhr.send(null);
  }

  playSong() {
    this.exitMenu();
    this.songPlayback.play();
    this.isPlayingSong = true;
    this.maxPlayPos = this.songPlayback.duration;
    this.maxPlayPosFormatted = this.convertPlayTimeFormat(this.maxPlayPos);
    let self = this;
    self.songPlayback.addEventListener('ended', function() {
      console.log("STOPPED SONG.");
      self.songPlayback = null;
      self.nextSong();
    }, false);
    self.songPlayback.addEventListener('timeupdate', function() {
      if (self.hasSelSong) {
        self.currPlayPos = self.songPlayback.currentTime;
        self.currPlayPosFormatted = self.convertPlayTimeFormat(self.currPlayPos);
      }
    });
    console.log("STARTED SONG.");
  }

  pauseSong() {
    this.songPlayback.pause();
    this.isPlayingSong = false;
  }

  stopSong() {
    this.loadSong();
    this.isPlayingSong = false;
  }

  previousSong() {
    for (let i = 0; i < this.currSongs.length; i++) {
      if (this.currSongs[i].id == this.currSongInfo.song.id && i > 0) {
        this.getSong(this.currSongs[i-1].id);
        break;
      }
    }
  }

  nextSong() {
    if (this.doRepeat) { this.getSong(this.selSongId); }
    else if (this.doShuffle) { this.getSong(this.getShuffledSongId()); }
    else {
      for (let i = 0; i < this.currSongs.length; i++) {
        if (this.currSongs[i].id == this.currSongInfo.song.id && (i < this.currSongs.length - 1)) {
          this.getSong(this.currSongs[i+1].id);
          break;
        }
      }
    }
  }

  changeSongPos(value: number) {
    this.songPlayback.currentTime = value;
  }

  changeVolume(value: number) {
    this.songPlayback.volume = value;
  }

  convertPlayTimeFormat(seconds: number) {
    let minutes: any = Math.floor(seconds / 60);
    let secs: any = Math.floor(seconds % 60);
    if (minutes < 10) { minutes = '0' + minutes; }
    if (secs < 10) { secs = '0' + secs; }
    return minutes +  ':' + secs;
  }


  addSongs(fileList: FileList) {
    this.isUploading = true;   //Used in view to show progress bar.
    this.maxProgress = fileList.length; //Sets the new max value for progress bar.
    for (let i = 0; i < fileList.length; i++) { //Loop through list of files.
      this.songService.addSong(fileList[i]).subscribe( () => {  //Send song to server.
        this.currProgress++; //Increment the current value for progress bar.
        if (i == fileList.length - 1) { //During last loop,
          this.setLibrarySelections(null, null, null); //Refresh library
          setTimeout( () => { //Delay 0.8 seconds.
            this.exitMenu();  //Clears out of current menu.
            this.resetProgressBar(); //Resets the current and max values for the progress bar.
            this.isUploading = false; //Used in view to hide progress bar.
          }, 800);
        }
      });
    }
  }

  updateSong() {
    this.hasSelSong = false;  //Set to false before update to execute loading animation.
    this.songService.updateSong(this.currSongInfo, this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id).subscribe( songInfo => {
      this.currSongInfo = songInfo;
      this.refreshLibrary(this.currSongInfo.artist.id, this.currSongInfo.album.id);
      this.setLibrarySelections(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id);
      this.hasSelSong = true;
      this.playSong();
    });
  }

  removeSong() {
    this.hasSelSong = false;
    this.exitMenu();
    this.songPlayback.pause();
    this.songPlayback = null;
    this.songService.removeSong(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id).subscribe( () => {
      this.resetLibrary();
      this.currSongInfo = null;
    });
  }

  resetProgressBar() {
    this.currProgress = 0;
    this.maxProgress = 1;
  }

  resetLibrary() {
    this.artists = null;
    this.albums = null;
    this.songs = null;
    this.currSongInfo = null;
    this.setLibrarySelections(null, null, null);
  }

  refreshLibrary(artistId: number, albumId: number) {
    this.getArtists();
    this.getAlbums(artistId);
    this.getSongs(artistId, albumId);
  }

  setLibrarySelections(artistId: number, albumId: number, titleId: number) {
    this.selArtistId = artistId;
    this.selAlbumId = albumId;
    this.selSongId = titleId;
    this.getArtists();
  }

  getShuffledSongId(): number {
    let currSongIndex = 0;
    for (let i = 0; i < this.currSongs.length; i++) {
      if (this.currSongs[i].id == this.selSongId) { currSongIndex = i; }
    }
    let randomInt = currSongIndex;  //Initialize this way to enter while loop.
    while (randomInt == currSongIndex) {
      randomInt = this.songService.getRandomInt(0, this.currSongs.length - 1);
    }
    return this.currSongs[randomInt].id;
  }

  setActiveTab(boolIndex) {
    for (let i = 0; i < this.isActiveSection.length; i++) {
      if (i == boolIndex) {
        this.isActiveSection[boolIndex] = true;
        this.isActiveTab[boolIndex] = !this.isActiveTab[boolIndex];
      }
      else {
        this.isActiveSection[i] = false;
        this.isActiveTab[i] = false;
      }
    }
  }

  exitMenu() {
    for (let i = 0; i < this.isActiveSection.length; i++) {
      this.isActiveSection[i] = false;
      this.isActiveTab[i] = false;
    }
  }

}
