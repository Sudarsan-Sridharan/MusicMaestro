import { Component, Input, Output, EventEmitter } from '@angular/core';
import { RestService } from '../../service/rest/rest.service';
import { Artist } from '../../model/artist';
import { Album } from '../../model/album';
import { Song } from '../../model/song';
import { SongInfo } from '../../model/songInfo';

@Component({
  selector: 'app-edit',
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.css']
})

export class EditComponent {

  /*
   *  Input and output between the parent component.
   *  The output objects are event emitters that trigger parent function.
   */

  @Input() isPlayerLoaded: boolean;
  @Input() currSongInfo: SongInfo;
  @Output() loadPlayer = new EventEmitter();
  @Output() refreshLibraryMessenger = new EventEmitter();
  @Output() setLibrarySelectionsMessenger = new EventEmitter();
  @Output() resetLibraryMessenger = new EventEmitter();
  @Output() stopPlayer = new EventEmitter();
  @Output() exitMenu = new EventEmitter();

  constructor(private restService: RestService) { }

  /*
   *  Updates the artist, album, and song information based on any changes made
   *  to the current SongInfo object both on the front end and the server.
   *  The library view is reloaded with new lists and selections, and the player
   *  is reinitialized.
   */
  updateSong() {
    this.restService.updateSong(this.currSongInfo, this.currSongInfo.artist.id,
      this.currSongInfo.album.id, this.currSongInfo.song.id).subscribe( songInfo => {
        this.currSongInfo = songInfo;
        this.refreshLibraryMessenger.emit();
        this.setLibrarySelectionsMessenger.emit();
        this.loadPlayer.emit();
      });
    }

    /*
     *  Removes the current song (i.e. currSongInfo object) from the album list
     *  for both the front end and server. Every meny component is closed, the
     *  player is stopped/closed, and the library's lists and selections are set
     *  to null, as is the current song information (currSongInfo).
     */
    removeSong() {
      this.isPlayerLoaded = false;
      this.exitMenu.emit();
      this.stopPlayer.emit();
      this.restService.removeSong(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id).subscribe( () => {
        this.resetLibraryMessenger.emit();
        this.currSongInfo = null;
      });
    }

  }
