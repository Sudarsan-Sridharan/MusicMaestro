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
  isUploading: boolean = false;
  hasSelSong: boolean = false;
  currProgress: number = 0;
  maxProgress: number = 1;
  currSongInfo: SongInfo;
  currSongs: Array<Song>;

  constructor(private restService: RestService) {}

  getSongInfo(event) {
    this.restService.getSongInfo(event.artistId, event.albumId, event.songId)
    .subscribe(songInfo => {
      this.currSongInfo = songInfo;
      this.hasSelSong = true;
      console.log("currSongInfo.song.name = " + this.currSongInfo.song.name);

      //Need to grant time for player component to load.
      //Otherwise, "ViewChild" will return undefined due to ngIf conditional.
      setTimeout( () => this.player.loadSong(), 150);
      setTimeout( () => this.player.playSong(), 500);
    });
  }

  addSongs(fileList: FileList) {
    this.isUploading = true;   //Used in view to show progress bar.
    this.maxProgress = fileList.length; //Sets the new max value for progress bar.
    for (let i = 0; i < fileList.length; i++) { //Loop through list of files.
      this.restService.addSong(fileList[i]).subscribe( () => {  //Send song to server.
        this.currProgress++; //Increment the current value for progress bar.
        if (i == fileList.length - 1) { //During last loop,
          this.library.setLibrarySelections(null, null, null); //Refresh library
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
    this.restService.updateSong(this.currSongInfo, this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id).subscribe( songInfo => {
      this.currSongInfo = songInfo;
      this.library.refreshLibrary(this.currSongInfo.artist.id, this.currSongInfo.album.id);
      this.library.setLibrarySelections(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id);
      this.hasSelSong = true;
    });
  }

  removeSong() {
    this.hasSelSong = false;
    this.exitMenu();
    this.player.songPlayback.pause();
    this.player.songPlayback = null;
    this.restService.removeSong(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id).subscribe( () => {
      this.library.resetLibrary();
      this.currSongInfo = null;
    });
  }

  resetProgressBar() {
    this.currProgress = 0;
    this.maxProgress = 1;
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

}
