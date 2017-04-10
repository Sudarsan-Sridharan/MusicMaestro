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
  isUploading: boolean = false;
  isPlayingSong: boolean = false;
  hasStoppedAudio: boolean = false;
  hasSelSong: boolean = false;
  currProgress: number = 0;
  maxProgress: number = 1;
  lastAudioPos: number = 0;
  selArtistId: number;
  selAlbumId: number;
  selSongId: number;
  artists: Array<Artist>;
  albums: Array<Album>;
  songs: Array<Song>;
  currSongInfo: SongInfo;
  songArtworkSrc: String;
  audioBufferSourceNode: AudioBufferSourceNode = null;
  audioContext: AudioContext = null;

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
    this.hasSelSong = false; //Triggers animation for changing from previous song.
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
      this.hasSelSong = true;
      //this.loadSong();
      setTimeout( () => this.playSong(), 300);
    });
  }

/*
  loadSong() {
    if (this.songPlayback == null) { this.songPlayback = new Audio(); }
    this.songArtworkSrc = "http://localhost:8080/artists/" + this.currSongInfo.artist.id + "/albums/" + this.currSongInfo.album.id + "/songs/" + this.currSongInfo.song.id + "/artwork";
    this.songPlayback.src = "http://localhost:8080/artists/" + this.currSongInfo.artist.id + "/albums/" + this.currSongInfo.album.id + "/songs/" + this.currSongInfo.song.id + "/file";
    this.songPlayback.load();
  }
*/
  playSong() {

    this.isPlayingSong = true;
    this.songArtworkSrc = "http://localhost:8080/artists/" + this.currSongInfo.artist.id + "/albums/" + this.currSongInfo.album.id + "/songs/" + this.currSongInfo.song.id + "/artwork";

    let request = new XMLHttpRequest();
    request.open('GET', "http://localhost:8080/artists/" + this.currSongInfo.artist.id + "/albums/" + this.currSongInfo.album.id + "/songs/" + this.currSongInfo.song.id + "/file", true);
    request.responseType = 'arraybuffer';
    request.send();
    let self = this;

    request.onload = function () {
      self.audioContext = new AudioContext();
      let undecodedAudio = request.response;
      self.audioContext.decodeAudioData(undecodedAudio, function (buffer) {
        if (self.audioBufferSourceNode != null ) { self.audioBufferSourceNode.stop(); }
        self.audioBufferSourceNode = self.audioContext.createBufferSource();
        self.audioBufferSourceNode.buffer = buffer;
        self.audioBufferSourceNode.connect(self.audioContext.destination);
        console.log("Fully loaded!");
        console.log("testPlay(), lastAudioPos = " + self.lastAudioPos);
        self.audioBufferSourceNode.start(self.audioContext.currentTime);
        console.log("Playing!");
      });
    };

    self.audioBufferSourceNode.addEventListener('ended', function() {
      console.log("'ended' Audio event heard. Stopping song.");
      self.stopSong();
    }, false);

    self.audioBufferSourceNode.addEventListener('timeupdate', function() {
      console.log("'ended' Audio event heard. Stopping song.");
      self.stopSong();
    }, false);

    /*
    this.songPlayback.play();
    this.isPlayingSong = true;
    let self = this;
    self.songPlayback.addEventListener('ended', function() {
      console.log("'ended' Audio event heard. Stopping song.");
      self.stopSong();
    }, false);
    */
  }

  stopSong() {
    this.audioBufferSourceNode.stop();
    this.isPlayingSong = false;
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
      //this.loadSong();
      this.playSong();
    });
  }

  removeSong() {
    this.songService.removeSong(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id).subscribe( () => {
      this.hasSelSong = false;
      this.exitMenu();
      this.resetLibrary();
      //this.songPlayback.pause();
      //this.songPlayback = null;
      this.currSongInfo = null;
    });
  }

  exitMenu() {
    for (let i = 0; i < this.isActiveSection.length; i++) {
      this.isActiveSection[i] = false;
    }
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

  testPlay() {

    let request = new XMLHttpRequest();
    request.open('GET', "http://localhost:8080/artists/5/albums/5/songs/56/file", true);
    request.responseType = 'arraybuffer';
    request.send();
    let self = this;

    request.onload = function () {
      self.audioContext = new AudioContext();
      let undecodedAudio = request.response;
      self.audioContext.decodeAudioData(undecodedAudio, function (buffer) {
        if (self.audioBufferSourceNode != null ) { self.audioBufferSourceNode.stop(); }
        self.audioBufferSourceNode = self.audioContext.createBufferSource();
        self.audioBufferSourceNode.buffer = buffer;
        self.audioBufferSourceNode.connect(self.audioContext.destination);
        console.log("Fully loaded!");
        console.log("testPlay(), lastAudioPos = " + self.lastAudioPos);
        self.audioBufferSourceNode.start(self.audioContext.currentTime + 5);
        console.log("Playing!");
      });
    };
  }

  testPause() {
    this.lastAudioPos = this.audioContext.currentTime;
    console.log("testPause(), lastAudioPos = " + this.lastAudioPos);
    this.audioBufferSourceNode.stop();
  }

  testStop() {
    this.audioBufferSourceNode.stop();
  }

}
