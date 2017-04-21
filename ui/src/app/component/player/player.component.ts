import { Component, Input, Output, EventEmitter } from '@angular/core';
import { UtilityService } from '../../service/utility/utility.service';
import { Artist } from '../../model/artist';
import { Album } from '../../model/album';
import { Song } from '../../model/song';
import { SongInfo } from '../../model/songInfo';
import { config } from '../../config/config';

@Component({
  selector: 'app-player',
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.css']
})
export class PlayerComponent {

  @Input() selSongId: number;
  @Input() currSongInfo: SongInfo;
  @Input() currSongs: Array<Song>;
  @Output() getSongInfo = new EventEmitter();

  songPlayback;
  isPlaying: boolean = false;
  doShowSettings: boolean = false;
  doRepeat: boolean = false;
  doShuffle: boolean = false;
  currPlayPos: number = 0;
  maxPlayPos: number = 1;
  songArtworkSrc: string;
  currPlayPosFormatted: string = "00:00";
  maxPlayPosFormatted: string = "00:00";

  constructor(private utilityService: UtilityService) {}

  load() {
    if (this.songPlayback == null) { this.songPlayback = new Audio(); }
    this.songArtworkSrc = "http://" + config.serverName + ":" + config.serverPort
      + "/artists/" + this.currSongInfo.artist.id
      + "/albums/" + this.currSongInfo.album.id
      + "/songs/" + this.currSongInfo.song.id
      + "/artwork";
    let self = this;
    let xhr = new XMLHttpRequest();
    xhr.addEventListener('load', function(blob) {
      if (xhr.status == 200) { self.songPlayback.src = window.URL.createObjectURL(xhr.response); }
      setTimeout( () => {
        self.maxPlayPos = self.songPlayback.duration;
        self.maxPlayPosFormatted = self.convertPlayTimeFormat(self.maxPlayPos);
        self.play();
      }, 200);
    });
    let src = "http://" + config.serverName + ":" + config.serverPort
      + "/artists/" + this.currSongInfo.artist.id
      + "/albums/" + this.currSongInfo.album.id
      + "/songs/" + this.currSongInfo.song.id
      + "/file";
    xhr.open('GET', src);
    xhr.responseType = 'blob';
    xhr.send(null);
  }

  play() {
    this.songPlayback.play();
    this.isPlaying = true;
    let self = this;
    self.songPlayback.addEventListener('ended', function() {
      self.songPlayback = null;
      self.next();
    }, false);
    self.songPlayback.addEventListener('timeupdate', function() {
      if (self.isPlaying) {
        self.currPlayPos = self.songPlayback.currentTime;
        self.currPlayPosFormatted = self.convertPlayTimeFormat(self.currPlayPos);
      }
    });
  }

  pause() {
    this.isPlaying = false;
    this.songPlayback.pause();
  }

  stop() {
    if (this.songPlayback != null) {
      this.songPlayback.pause();
      this.songPlayback.currentTime = 0;
    }
    this.isPlaying = false;
    this.currPlayPos = 0;
    this.currPlayPosFormatted = "00:00";
  }

  previous() {
    if (this.currSongs.length > 1) {
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
  }

  next() {
    let artistId = this.currSongInfo.artist.id;
    let albumId = this.currSongInfo.album.id;
    let songId = 0;
    if (this.doRepeat) {
      songId = this.currSongInfo.song.id
      this.getSongInfo.emit({artistId, albumId, songId});
    }
    else if (this.doShuffle) {
      songId = this.utilityService.getShuffledSongId(this.currSongs, this.selSongId);
      this.getSongInfo.emit({artistId, albumId, songId});
    }
    else if (this.currSongs.length > 1) {
      for (let i = 0; i < this.currSongs.length; i++) {
        if (this.currSongs[i].id == this.currSongInfo.song.id && i < this.currSongs.length - 1) {
          songId = this.currSongs[i+1].id;
          this.stop();
          this.getSongInfo.emit({artistId, albumId, songId});
          break;
        }
      }
    }
    else {
      this.stop();
    }
  }

  changePos(value: number) {
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

}
