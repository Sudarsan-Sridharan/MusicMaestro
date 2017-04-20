import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RestService } from '../../service/rest/rest.service';
import { Artist } from '../../model/Artist';
import { Album } from '../../model/Album';
import { Song } from '../../model/Song';
import { SongInfo } from '../../model/SongInfo';

@Component({
  selector: 'app-library',
  templateUrl: './library.component.html',
  styleUrls: ['./library.component.css']
})
export class LibraryComponent implements OnInit {

  @Input() currSongInfo: SongInfo;
  @Output() setCurrSongs = new EventEmitter();
  @Output() getSongInfo = new EventEmitter();

  hasLoadedArtists: boolean;
  selArtistId: number;
  selAlbumId: number;
  selSongId: number;
  artists: Array<Artist>;
  albums: Array<Album>;
  songs: Array<Song>;

  constructor(private restService: RestService) { }

  ngOnInit() {
    if (this.currSongInfo != null) {
      this.setLibrarySelections(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id);
      this.getArtists();
      this.getAlbums(this.currSongInfo.artist.id);
      this.getSongs(this.currSongInfo.artist.id, this.currSongInfo.album.id);
    } else {
      this.getArtists();
    }
  }

  getArtists() {
    this.restService.getArtists().subscribe(artists => {
      this.hasLoadedArtists = false;
      this.artists = artists;
      //Brief, loading facade avoids awkward collapse animation in view.
      setTimeout( () => this.hasLoadedArtists = true, 500);
    });
  }

  getAlbums(artistId: number) {
    this.selArtistId = null; //Triggers albums animation by reseting to null first..
    this.selAlbumId = null;  //Hides song listing in the view.
    this.restService.getAlbums(artistId)
    .subscribe(albums => {
      this.selArtistId = artistId;
      this.albums = albums;
    });

  }

  getSongs(artistId: number, albumId: number) {
    this.selAlbumId = albumId;
    this.restService.getSongs(artistId, albumId)
    .subscribe(songs => this.songs = songs);
  }

  getSong(songId: number) {
    this.selSongId = songId;
    this.setCurrSongs.emit(this.songs);  //For playback through album in player.
    this.restService.getSong(this.selArtistId, this.selAlbumId, songId)
    .subscribe( () => {
      let artistId = this.selArtistId;
      let albumId = this.selAlbumId;
      let songId = this.selSongId;
      this.getSongInfo.emit({artistId, albumId, songId});
    });
  }

  resetLibrary() {
    this.artists = null;
    this.albums = null;
    this.songs = null;
    this.currSongInfo = null;
    this.setLibrarySelections(null, null, null);
    this.getArtists();
  }

  refreshLibrary(artistId: number, albumId: number) {
    this.getArtists();
    this.getAlbums(artistId);
    this.getSongs(artistId, albumId);
  }

  setLibrarySelections(artistId: number, albumId: number, titleId: number) {
    this.selArtistId = artistId;
    this.selAlbumId = albumId;
    this.selSongId = titleId;
  }

}
