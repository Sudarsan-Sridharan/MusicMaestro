import { Component, OnInit } from '@angular/core';
import { SongService } from './song.service';
import { Observable } from 'rxjs/Observable';
import { Song } from './Song';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

constructor(private songService: SongService) {}

  title: String = 'Music Library';
  songs: Song[];
  newSong: Song = {title: "Test Title", album: "Test Album", artist: "Test Artist", year: 2222};

  ngOnInit() {
    this.getSongs();
  }

  getSongs() {
    this.songService.getSongs().subscribe(songs => this.songs = songs);
  }

  addSong(songs: Song[]) {
    this.songService.addSong(this.newSong).subscribe(song => this.songs = song);
  }

  removeSong(i: number) {
    this.songService.removeSong(this.songs[i].title).subscribe(song => this.songs = song);
  }

  /*
  updateSong() {
    this.songService.updateSong(this.newSong).subscribe(song => this.songs = song);
  }
  */

}
