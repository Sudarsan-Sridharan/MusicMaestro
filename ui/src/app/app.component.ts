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

  isCollapsed: Array<Boolean> = [false, false]; //Used to keep one menu button active.
  artistList: Array<String>;
  albumList: Array<String>;
  songTitleList: Array<String>;
  selectedArtist: String;
  selectedAlbum: String;
  selectedSong: String;
  currentSong: Song;
  songPlayback;
  songArtworkSrc: String;
  newSong: Song = {title: null, album: null, artist: null, year: null, filePath: null};
  newSongFile: FileList;

  ngOnInit() {
    this.getArtistList();
  }

  getArtistList() {
    this.songService.getArtistList().subscribe(artistList => this.artistList = artistList);
  }

  getAlbumList(artist: String) {
    this.selectedArtist = artist;
    this.selectedAlbum = null;  //Hides song listing in the view.
    this.songService.getAlbumList(artist).subscribe(albumList => this.albumList = albumList);
  }

  getSongList(album: String) {
    this.selectedAlbum = album;
    this.songService.getSongList(this.selectedArtist, album).subscribe(songTitleList => this.songTitleList = songTitleList);
  }

  getSong(song: String) {
    this.selectedSong = song;
    this.songService.getSong(this.selectedArtist, this.selectedAlbum, song)
    .subscribe(song => {
      this.currentSong = song;
      this.getSongPlayback();
      this.getSongArtwork();
    });
  }

  getSongPlayback() {
    if (this.songPlayback == null) { this.songPlayback = new Audio(); }
    this.songPlayback.src = "http://localhost:8080/playback/" + this.selectedArtist + "/" + this.selectedAlbum + "/" + this.selectedSong;
    this.songPlayback.load();
    this.songPlayback.play();
  }

  getSongArtwork() {
    this.songArtworkSrc = "http://localhost:8080/artwork/" + this.selectedArtist + "/" + this.selectedAlbum + "/" + this.selectedSong;
  }

  addSong() {
    this.songService.addSongFile(this.newSongFile).subscribe( () => {
      this.songService.addSongMetadata(this.newSong).subscribe( () => {
        //Clear any previous selections and refresh cached artist list.
        this.selectedArtist = null;
        this.selectedAlbum = null;
        this.selectedSong = null;
        this.getArtistList();
      });
    });
  }

}
/*
addSong(songTitleList: Song[]) {
this.songService.addSong(this.newSong).subscribe(song => this.allSongs = song);
console.log(this.allSongs);
}

removeSong(i: number) {
this.songService.removeSong(this.allSongs[i].title).subscribe(song => this.allSongs = song);
console.log(this.allSongs);
}


updateSong() {
this.songService.updateSong(this.newSong).subscribe(song => this.songTitleList = song);
}
*/
