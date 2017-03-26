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

  allSongs: Array<Song>;
  artists: Array<String>;
  albums: Array<String>;
  songs: Array<String>;
  selectedAlbum: String;
  selectedArtist: String;
  selectedSong: String;
  currentSong: Song;
  songPlayback;
  newSong: Song = {title: "Test Title", album: "Test Album", artist: "Test Artist", year: 2222};

  ngOnInit() {
    this.getArtistList();
  }

  getArtistList() {
    this.songService.getArtistList().subscribe(artists => this.artists = artists);
  }

  getAlbumList(artist: String) {
    this.selectedArtist = artist;
    this.selectedAlbum = null;  //Hides song listing in the view.
    this.songService.getAlbumList(artist).subscribe(albums => this.albums = albums);
  }

  getSongList(album: String) {
    this.selectedAlbum = album;
    this.songService.getSongList(this.selectedArtist, album).subscribe(songs => this.songs = songs);
  }

  getSong(song: String) {
    this.selectedSong = song;
    this.songService.getSong(this.selectedArtist, this.selectedAlbum, song)
    .subscribe(song => {
      this.currentSong = song;
      this.getSongPlayback();
    });
  }

  getSongPlayback() {
    if (this.songPlayback == null) {
      this.songPlayback = new Audio();
    }
    this.songPlayback.src = "http://localhost:8080/playback/" + this.selectedArtist + "/" + this.selectedAlbum + "/" + this.selectedSong;
    this.songPlayback.load();
    this.songPlayback.play();
  }

  /*
  addSong(songs: Song[]) {
  this.songService.addSong(this.newSong).subscribe(song => this.allSongs = song);
  console.log(this.allSongs);
}

removeSong(i: number) {
this.songService.removeSong(this.allSongs[i].title).subscribe(song => this.allSongs = song);
console.log(this.allSongs);
}


updateSong() {
this.songService.updateSong(this.newSong).subscribe(song => this.songs = song);
}
*/

}
