import { Component, OnInit } from '@angular/core';
import { SongService } from './song.service';
import { Observable } from 'rxjs/Observable';
import { Song } from './Song';
import { Howl, Howler } from 'howler';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  constructor(private songService: SongService) {}

  artistList: Array<String>;
  albumList: Array<String>;
  songTitleList: Array<String>;
  selectedArtist: String;
  selectedAlbum: String;
  selectedSong: String;
  currentSong: Song;
  songPlayback;
  songArtwork: String;

  ngOnInit() {
    this.getArtistList();

    //DEBUG
    this.selectedArtist = "Green Day";
    this.selectedAlbum = "American Idiot"
    this.getSong("Holiday");


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
  let sound = new Howl({
    src: ["http://localhost:8080/playback/" + this.selectedArtist + "/" + this.selectedAlbum + "/" + this.selectedSong],
    format: ["mp3"],
    autoplay: false,
    loop: false});

    let a = sound.play();

    console.log('sound = ' + sound);
    console.log('id = ' + a);
    console.log('duration = ' + sound.duration());
    console.log('seek = ' + sound.seek());

}

/*
  getSongPlayback() {
    if (this.songPlayback == null) { this.songPlayback = new Audio(); }
    this.songPlayback.src = "http://localhost:8080/playback/" + this.selectedArtist + "/" + this.selectedAlbum + "/" + this.selectedSong;
    this.songPlayback.load();
    this.songPlayback.currentTime = 30;
    this.songPlayback.play();
    console.log(this.songPlayback);
  }
*/

  getSongArtwork() {
    this.songArtwork = "http://localhost:8080/artwork/" + this.selectedArtist + "/" + this.selectedAlbum + "/" + this.selectedSong;
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

}
