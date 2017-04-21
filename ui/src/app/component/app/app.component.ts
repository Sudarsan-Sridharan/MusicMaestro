import { Component, ViewChild, OnInit } from '@angular/core';
import { PlayerComponent } from '../../component/player/player.component';
import { LibraryComponent } from '../../component/library/library.component';
import { UploadComponent } from '../../component/upload/upload.component';
import { RestService } from '../../service/rest/rest.service';
import { Observable } from 'rxjs/Observable';
import { Artist } from '../../model/artist';
import { Album } from '../../model/album';
import { Song } from '../../model/song';
import { SongInfo } from '../../model/songInfo';

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

  @ViewChild(UploadComponent)
  private upload: UploadComponent;

  //isActiveSection --> Keeps one menu section active at a time:
  //["Music Library", "Edit Song", "Add Song"]
  isActiveTab: Array<boolean> = [false, false, false];
  isPlayerLoaded: boolean = false;
  hasRoulette: boolean = false;
  currSongInfo: SongInfo;
  currSongs: Array<Song>;

  constructor(private restService: RestService) {}

  getSongInfo(event) {
    if (event.hasRouletted) { this.hasRoulette = false; }
    this.stopPlayer();
    if (this.currSongInfo != null && this.currSongInfo.album.id != event.albumId) {
      this.reloadPlayer();
    }
    this.restService.getSongInfo(event.artistId, event.albumId, event.songId)
    .subscribe(songInfo => {
      this.isPlayerLoaded = true;
      this.currSongInfo = songInfo;
      this.loadPlayer();
    });
  }

  setActiveTab(boolIndex) {
    for (let i = 0; i < this.isActiveTab.length; i++) {
      if (i == boolIndex) {
        this.isActiveTab[boolIndex] = !this.isActiveTab[boolIndex];
      }
      else {
        this.isActiveTab[i] = false;
      }
    }
  }

  exitMenu() {
    for (let i = 0; i < this.isActiveTab.length; i++) {
      this.isActiveTab[i] = false;
    }
  }

  setCurrSongs(songs: Array<Song>) {
    this.currSongs = songs;
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
      if (this.player.songPlayback != null ) { this.player.songPlayback.pause(); }
      this.player.isPlaying = false;
    }
  }

  loadPlayer() {
    //Need to grant time for player component to load.
    //Otherwise, "ViewChild" will return undefined due to ngIf conditional.
    setTimeout( () => this.player.load(), 200);
    this.exitMenu();
  }

  reloadPlayer() {
    this.isPlayerLoaded = false;
    setTimeout( () => this.isPlayerLoaded = true, 200);
  }

  loadRoulette() {
    this.hasRoulette = true;
  }


}
