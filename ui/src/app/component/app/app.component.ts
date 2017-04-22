import { Component, ViewChild, OnInit } from '@angular/core';
import { LibraryComponent } from '../../component/library/library.component';
import { UploadComponent } from '../../component/upload/upload.component';
import { PlayerComponent } from '../../component/player/player.component';
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

  /*
   *  Makes child components accessible directly in this parent component.
   */

  @ViewChild(LibraryComponent)
  private library: LibraryComponent;

  @ViewChild(UploadComponent)
  private upload: UploadComponent;

  @ViewChild(PlayerComponent)
  private player: PlayerComponent;

  /*
   *  Instance variables and objects.
   */

  //Active menu sections: ["Music Library", "Edit Song", "Add Song"]
  isActiveMenuSection: Array<boolean> = [false, false, false];

  //Represents whether or not the player component is initialized.
  isPlayerLoaded: boolean = false;

  //Represents whether or not the roulette component is initialized.
  hasRoulette: boolean = false;

  //Contains an artist, album, and song object for song info during playback.
  currSongInfo: SongInfo;

  //A list of song objects (i.e. album) retrieved when a selecting a song from library component.
  currSongs: Array<Song>;

  constructor(private restService: RestService) {}

  /*
   *  Retrieves an object containing an artist, album, and song object for given
   *  artist, album, and song IDs. This is used primarily for song playback info.
   */
  getSongInfo(event) {
    this.stopPlayer();
    if (event.hasRoulette) { this.hasRoulette = false; }
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

  /*
   *  Sets the active section in the menu.
   *  Triggers upon clicking one of the menu buttons.
   */
  setActiveTab(boolIndex) {
    for (let i = 0; i < this.isActiveMenuSection.length; i++) {
      if (i == boolIndex) {
        this.isActiveMenuSection[boolIndex] = !this.isActiveMenuSection[boolIndex];
      }
      else {
        this.isActiveMenuSection[i] = false;
      }
    }
  }

  /*
   *  Tells the child component "Player" to load a new song.
   *  A timeout is set to allow the component to load, otherwise it will appear
   *  undefined if processed quickly enough.
   */
  loadPlayer() {
    this.exitMenu();
    setTimeout( () => this.player.load(), 200);
  }

  /*
   *  Reloads player based on conditional in the view. This triggers its CSS animation.
   *  A timeout is set to allow the component to load, otherwise it will appear
   *  undefined if processed quickly enough.
   */
  reloadPlayer() {
    this.isPlayerLoaded = false;
    setTimeout( () => this.isPlayerLoaded = true, 200);
  }

  /*
   *  Stops any existing song playback.
   *  Checks if the player component has been loaded,
   *  and if there is already a loaded song. Sets the loaded song to null.
   */
  stopPlayer() {
    if (this.player != undefined) {
      if (this.player.audio != null ) {
        this.player.audio.pause();
        this.player.audio = null;
      }
      this.player.isPlaying = false;
    }
  }

  /*
   *  Initiates the roulette component by passing its conditional in the view.
   *  When the roulette component has finished processing, it will set
   *  hasRoulette to false to close the component.
   */
  loadRoulette() {
    this.hasRoulette = true;
  }

  /*
   *  Sets every menu item's boolean variable to false, which fails their
   *  conditionals in the view.
   */
  exitMenu() {
    for (let i = 0; i < this.isActiveMenuSection.length; i++) {
      this.isActiveMenuSection[i] = false;
    }
  }

  /*
   *  Setter for the currSongs object. This is used by the library component to
   *  store a the last accessed list of songs to effectively allow album playback
   *  in the player object.
   */
  setCurrSongs(songs: Array<Song>) {
    this.currSongs = songs;
  }

  /*
   *  "Messenger" functions are used to interface functionality between child
   *  components through this parent component.
   */

  //Sets the highlighted text in the library (i.e. artist, album, song) for the given IDs.
  setLibrarySelectionsMessenger() {
    if (this.library != undefined) {
      this.library.setLibrarySelections(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id);
    }
  }

  //Reloads the library artist, album, and song lists for the given IDs.
  refreshLibraryMessenger() {
    if (this.library != undefined) {
      this.library.refreshLibrary(this.currSongInfo.album.id, this.currSongInfo.album.id);
    }
  }

  //Resets library lists and selections to default.
  resetLibraryMessenger() {
    if (this.library != undefined) { this.library.resetLibrary(); }
  }

}
