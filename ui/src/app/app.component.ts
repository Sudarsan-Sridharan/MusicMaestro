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

  isCollapsed: Array<Boolean> = [true, true, true]; //Used to keep one menu button active.
  hasNoArtistList: Boolean = true;
  hasNewSongPlayRequest: Boolean = true;
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
  newSongFiles: FileList;

  ngOnInit() {
    this.getArtistList();
  }

  getArtistList() {
    this.songService.getArtistList().subscribe(artistList => {
      this.artistList = artistList;
      if (artistList.length == 0) { this.hasNoArtistList = true; }
      else { this.hasNoArtistList = false; }
    });
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
    .subscribe(song => this.getSongPlayback(song));
  }

  getSongPlayback(song) {
    if (this.songPlayback == null) { this.songPlayback = new Audio(); }
    this.getSongArtwork(song);
    this.currentSong = song;
    this.songPlayback.src = "http://localhost:8080/playback/" + song.artist + "/" + song.album + "/" + song.title;
    this.songPlayback.load();
    this.songPlayback.play();
  }

  getSongArtwork(song) {
    this.songArtworkSrc = "http://localhost:8080/artwork/" + song.artist + "/" + song.album + "/" + song.title;
  }

  addSong(files: FileList) {
    this.newSongFiles = files;
    this.songService.addSong(this.newSongFiles).subscribe( newSong => {
      this.newSong = newSong;
      this.getArtistList();
    });
  }

  updateNewSongInfo() {
    this.songService.updateSongInfo(this.newSong).subscribe( () => {
      this.newSongFiles = null;
      if (this.hasNewSongPlayRequest) {
        this.getSongPlayback(this.newSong);
        this.refreshLists(this.newSong.artist, this.newSong.album, this.newSong.title);
        this.hasNewSongPlayRequest = false;
      }
      this.resetSong(this.newSong);
    });
  }

  updateCurrentSongInfo() {
    this.songService.updateSongInfo(this.currentSong).subscribe();
    this.refreshLists(this.currentSong.artist, this.currentSong.album, this.currentSong.title);
  }

  removeSong(i: number) {
    this.songService.removeSong(this.currentSong).subscribe( () => {
      this.songPlayback.pause();
      this.songPlayback.remove();
      this.selectedArtist = null;
      this.selectedAlbum = null;
      this.selectedSong = null;
      this.songPlayback = null;
      this.currentSong = null;
      this.getArtistList();
    });
  }

  resetSong(newSong: Song) {
    this.newSong = {title: null, album: null, artist: null, year: null, filePath: null};
  }

  refreshLists(artist: String, album: String, title: String) {
    this.isCollapsed = [true, true, true];
    this.selectedArtist = artist;
    this.selectedAlbum = album;
    this.selectedSong = title;
    this.getArtistList();
    this.getAlbumList(artist);
    this.getSongList(album);
  }

}
