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
  artists: Array<String>;
  selectedArtist: String;
  albums: Array<String>;
  selectedAlbum: String;
  songs: Array<String>;
  selectedSong: String;
  allSongs: Array<Song>;
  newSong: Song = {title: "Test Title", album: "Test Album", artist: "Test Artist", year: 2222};

  ngOnInit() {
    this.getAllSongs();
    this.getArtists();
  }

  getAllSongs() {
    this.songService.getAllSongs().subscribe(songs => this.allSongs = songs);
  }

  getArtists() {
    this.songService.getArtists().subscribe(artists => this.artists = artists);
  }

  getAlbums(artist: String) {
    this.selectedArtist = artist;
    this.songService.getAlbums(artist).subscribe(albums => this.albums = albums);
  }

  getSongs(album: String) {
    this.selectedAlbum = album;
    this.songService.getSongs(this.selectedArtist, album).subscribe(songs => this.songs = songs);
  }

  addSong(songs: Song[]) {
    this.songService.addSong(this.newSong).subscribe(song => this.allSongs = song);
    console.log(this.allSongs);
  }

  removeSong(i: number) {
    this.songService.removeSong(this.allSongs[i].title).subscribe(song => this.allSongs = song);
    console.log(this.allSongs);
  }

  /*
  updateSong() {
    this.songService.updateSong(this.newSong).subscribe(song => this.songs = song);
  }
  */

}
