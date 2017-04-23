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

  /*
   *  Input and output between the parent component.
   *  The output objects are event emitters that trigger parent function.
   */

  @Input() selSongId: number;
  @Input() currSongInfo: SongInfo;
  @Input() currSongs: Array<Song>;
  @Output() getSongInfo = new EventEmitter();

  /*
   *  Instance variables and objects.
   */

  //Audio object (HTML5 Audio) used to load and play songs.
  audio;

  //URL for the artwork image associated with the audio object.
  songArtworkSrc: string;

  //Represents if the current song is playing in the view.
  isPlaying: boolean = false;

  //Represents if the extra control settings showed be visible in the view.
  doShowSettings: boolean = false;

  //Represents if the current song should be repeated.
  doRepeat: boolean = false;

  //Represents if the current album should be shuffled.
  doShuffle: boolean = false;

  //The current and duration times for the song progress.
  currPlayPos: number = 0;
  maxPlayPos: number = 1;

  //The current and duration times for the song progress formatted into "MM:SS".
  currPlayPosFormatted: string = "00:00";
  maxPlayPosFormatted: string = "00:00";

  constructor(private utilityService: UtilityService) {}

  /*
   *  Retrieves the song file from the server (through XHR), and links it to the audio
   *  HTML5 Audio object as a BLOB. The corresponding song artwork REST url is
   *  also saved for use in the view. When the file is fully loaded, an event listener
   *  fires off, telling thee function to convert the time info into "MM:SS" and
   *  play the song.
   */
  load() {
    if (this.audio == null) { this.audio = new Audio(); }
    this.songArtworkSrc = "http://" + config.serverName + ":" + config.serverPort
      + "/artists/" + this.currSongInfo.artist.id
      + "/albums/" + this.currSongInfo.album.id
      + "/songs/" + this.currSongInfo.song.id
      + "/artwork";
    let self = this;
    let xhr = new XMLHttpRequest();
    xhr.addEventListener('load', function(blob) {
      if (xhr.status == 200) { self.audio.src = window.URL.createObjectURL(xhr.response); }
      setTimeout( () => {
        self.maxPlayPos = self.audio.duration;
        self.maxPlayPosFormatted = self.convertPlayTimeFormat(self.maxPlayPos);
        self.play();
      }, 200);
    });
    xhr.open('GET', "http://" + config.serverName + ":" + config.serverPort
      + "/artists/" + this.currSongInfo.artist.id
      + "/albums/" + this.currSongInfo.album.id
      + "/songs/" + this.currSongInfo.song.id
      + "/file");
    xhr.responseType = 'blob';
    xhr.send(null);
  }

  /*
   *  Tells the audio HTML5 Audio object to play.
   *  Event listeners are used to track the current time progress to show in view,
   *  and to set current audio object to null then look for the next song.
   */
  play() {
    console.log("PLAY");
    let self = this;
    self.audio.addEventListener('ended', function() {
      self.audio = null;
      self.next();
    }, false);
    self.audio.addEventListener('timeupdate', function() {
      if (self.isPlaying) {
        self.currPlayPos = self.audio.currentTime;
        self.currPlayPosFormatted = self.convertPlayTimeFormat(self.currPlayPos);
      }
    });
    this.audio.play();
    this.isPlaying = true;
  }

  /*
   *  Pauses the current audio object.
   */
  pause() {
    this.isPlaying = false;
    this.audio.pause();
  }

  /*
   *  Stops the current audio object. Resets playback info.
   */
  stop() {
    if (this.audio != null) {
      this.audio.pause();
      this.audio.currentTime = 0;
    }
    this.isPlaying = false;
    this.currPlayPos = 0;
    this.currPlayPosFormatted = "00:00";
  }

  /*
   *  Looks through current song list (i.e. album), and plays the previous song.
   *  If there is no existing previous song, then do nothing.
   */
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

  /*
   *  Looks through current song list (i.e. album), and plays the next song.
   *  If the user selected the repeat button in the view, reload the song.
   *  If the user selected the shuffle button in the view, shuffle the song
   *  by selecting a random index in the list of songs and using the ID of the
   *  song with the given index (cannot be same as existing song). Load new song.
   *  Otherwise load the next song in the current list of songs (i.e. album).
   *  Else stop playback.
   */
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

  /*
   *  Change the current playback time of the audio object.
   */
  changePos(value: number) {
    this.audio.currentTime = value;
  }

  /*
   *  Change the volume of the audio object.
   */
  changeVolume(value: number) {
    this.audio.volume = value;
  }

  /*
   *  Converts milliseconds from audio object to the "MM:SS" format.
   */
  convertPlayTimeFormat(seconds: number) {
    let minutes: any = Math.floor(seconds / 60);
    let secs: any = Math.floor(seconds % 60);
    if (minutes < 10) { minutes = '0' + minutes; }
    if (secs < 10) { secs = '0' + secs; }
    return minutes +  ':' + secs;
  }

}
