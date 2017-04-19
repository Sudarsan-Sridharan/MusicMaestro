import { Component, ViewChild } from '@angular/core';
import { PlayerComponent } from '../../component/player/player.component';
import { LibraryComponent } from '../../component/library/library.component';
import { RestService } from '../../service/rest/rest.service';
import { Observable } from 'rxjs/Observable';
import { Artist } from '../../model/Artist';
import { Album } from '../../model/Album';
import { Song } from '../../model/Song';
import { SongInfo } from '../../model/SongInfo';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  @ViewChild(LibraryComponent)
  private library: LibraryComponent;

  @ViewChild(PlayerComponent)
  private player: PlayerComponent;

  //isActiveSection --> Keeps one menu section active at a time:
  //["Music Library", "Edit Song", "Add Song"]
  isActiveSection: Array<boolean> = [false, false, false];
  isActiveTab: Array<boolean> = [false, false, false];
  hasSelSong: boolean = false;
  isShuffling: boolean = false;
  currSongInfo: SongInfo;
  currSongs: Array<Song>;

  constructor(private restService: RestService) {}

  getSongInfo(event) {
    if (this.player == undefined) { this.hasSelSong = false; } //Triggers loading animation.
    this.restService.getSongInfo(event.artistId, event.albumId, event.songId)
    .subscribe(songInfo => {
      this.currSongInfo = songInfo;
      this.hasSelSong = true;
      this.loadPlayer(event.doShuffle);
    });
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

  setCurrSongs(songs: Array<Song>) {
    this.currSongs = songs;
  }

  setDoShuffle() {
    if (this.player != undefined) {
      this.player.doShuffle = true;
    }
  }

  refreshLibraryMessenger() {
    if (this.library != undefined) {
      this.library.refreshLibrary(this.currSongInfo.album.id, this.currSongInfo.album.id);
    }
  }

  setLibrarySelectionsMessenger() {
    if (this.library != undefined) {
      this.library.setLibrarySelections(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id);
    }
  }

  resetLibraryMessenger() {
    if (this.library != undefined) { this.library.resetLibrary(); }
  }

  stopPlayer() {
    if (this.player != undefined) {
      this.player.isPlaying = false;
      this.player.songPlayback.pause();
      this.hasSelSong = false;
      this.currSongInfo = null;
    }
  }

  loadPlayer(doShuffle: boolean) {
    //Need to grant time for player component to load.
    //Otherwise, "ViewChild" will return undefined due to ngIf conditional.
    if (doShuffle != null) {
      setTimeout( () => {
        this.player.doShuffle = doShuffle;
        this.isShuffling = false;
      }, 300);
    }
    setTimeout( () => this.player.load(), 150);
    setTimeout( () => this.player.play(), 500);
  }

  startShuffle() {
    this.isShuffling = true;
  }


}
