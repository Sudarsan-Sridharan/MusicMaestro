import { Component, Input, Output, EventEmitter } from '@angular/core';
import { RestService } from '../../service/rest/rest.service';
import { Artist } from '../../model/Artist';
import { Album } from '../../model/Album';
import { Song } from '../../model/Song';
import { SongInfo } from '../../model/SongInfo';

@Component({
  selector: 'app-edit',
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.css']
})

export class EditComponent {

  @Input() hasSelSong: boolean;
  @Input() currSongInfo: SongInfo;
  @Output() loadPlayer = new EventEmitter();
  @Output() refreshLibraryMessenger = new EventEmitter();
  @Output() setLibrarySelectionsMessenger = new EventEmitter();
  @Output() resetLibraryMessenger = new EventEmitter();
  @Output() stopSongMessenger = new EventEmitter();
  @Output() exitMenu = new EventEmitter();

  constructor(private restService: RestService) { }

  updateSong() {
    this.restService.updateSong(this.currSongInfo, this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id).subscribe( songInfo => {
      this.currSongInfo = songInfo;
      this.refreshLibraryMessenger.emit();
      this.setLibrarySelectionsMessenger.emit();
      this.loadPlayer.emit();
    });
  }

  removeSong() {
    this.hasSelSong = false;
    this.exitMenu.emit();
    this.stopSongMessenger.emit();
    this.restService.removeSong(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id).subscribe( () => {
      this.resetLibraryMessenger.emit();
      this.currSongInfo = null;
    });
  }

}
