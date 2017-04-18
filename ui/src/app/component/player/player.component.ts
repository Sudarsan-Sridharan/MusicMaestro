import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Artist } from '../../model/Artist';
import { Album } from '../../model/Album';
import { Song } from '../../model/Song';
import { SongInfo } from '../../model/SongInfo';

@Component({
  selector: 'app-player',
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.css']
})
export class PlayerComponent {

  @Input() hasSelSong: boolean;
  @Input() selSongId: number;
  @Input() currSongInfo: SongInfo;
  @Input() currSongs: Array<Song>;
  @Output() exitMenu = new EventEmitter();
  @Output() getSongInfo = new EventEmitter();

  songPlayback;
  isPlayingSong: boolean = false;
  doShowSettings: boolean = false;
  doRepeat: boolean = false;
  doShuffle: boolean = false;
  currPlayPos: number = 0;
  maxPlayPos: number = 1;
  songArtworkSrc: string;
  currPlayPosFormatted: string = "00:00";
  maxPlayPosFormatted: string = "00:00";

  constructor() {}

  loadSong() {
    console.log("In loadSong().");
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
    this.exitMenu.emit();
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
    this.isPlayingSong = false;
    this.songPlayback.pause();
    this.loadSong(); //Need to reload to in case of replaying song.
  }

  previousSong() {
    for (let i = 0; i < this.currSongs.length; i++) {
      if (this.currSongs[i].id == this.currSongInfo.song.id && i > 0) {
        let artistId = this.currSongInfo.artist.id;
        let albumId = this.currSongInfo.album.id;
        let songId = this.currSongs[i-1].id
        this.getSongInfo.emit({artistId, albumId, songId});
        break;
      }
    }
  }

  nextSong() {
    console.log("NEXT SONG.");
    console.log("currSongs = " + this.currSongs);
    let artistId = this.currSongInfo.artist.id;
    let albumId = this.currSongInfo.album.id;
    let songId = 0;
    if (this.doRepeat) {
      songId = this.currSongInfo.song.id
      this.getSongInfo.emit({artistId, albumId, songId});
    }
    else if (this.doShuffle) {
      songId = this.getShuffledSongId();
      this.getSongInfo.emit({artistId, albumId, songId});
    }
    else {
      for (let i = 0; i < this.currSongs.length; i++) {
        if (this.currSongs[i].id == this.currSongInfo.song.id && i < this.currSongs.length - 1) {
          songId = this.currSongs[i+1].id;
          this.getSongInfo.emit({artistId, albumId, songId});
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

  getShuffledSongId(): number {
    let currSongIndex = 0;
    for (let i = 0; i < this.currSongs.length; i++) {
      if (this.currSongs[i].id == this.selSongId) { currSongIndex = i; }
    }
    let randomInt = currSongIndex;  //Initialize this way to enter while loop.
    while (randomInt == currSongIndex) {
      randomInt = this.getRandomInt(0, this.currSongs.length - 1);
    }
    return this.currSongs[randomInt].id;
  }

  getRandomInt(min, max) {
    return Math.round(Math.random() * (max - min) + min);
  }

}