import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RestService } from '../../service/rest/rest.service';
import { Artist } from '../../model/artist';
import { Album } from '../../model/album';
import { Song } from '../../model/song';
import { SongInfo } from '../../model/songInfo';

@Component({
  selector: 'app-library',
  templateUrl: './library.component.html',
  styleUrls: ['./library.component.css']
})
export class LibraryComponent implements OnInit {

  /*
   *  Input and output between the parent component.
   *  The output objects are event emitters that trigger parent function.
   */

  @Input() currSongInfo: SongInfo;
  @Output() setCurrSongs = new EventEmitter();
  @Output() getSongInfo = new EventEmitter();

  /*
   *  Instance variables and objects.
   */

  //The IDs for the selections in the library's artist, album, and song lists.
  selArtistId: number;
  selAlbumId: number;
  selSongId: number;

  //The lists of artists, albums, and songs pulled from the server.
  artists: Array<Artist>;
  albums: Array<Album>;
  songs: Array<Song>;

  constructor(private restService: RestService) { }

  /*
   *  On first initialization (i.e. everytime this component is opened from the menu),
   *  load the artist list if a song is not yet selected and playing.
   *  If a song is selected and playing, then load the album and song lists as well.
   *  Delay the whole process 0.5s to avoid an awkward collapse animation upon initialization.
   */
  ngOnInit() {
    setTimeout(() => {
      if (this.currSongInfo != null) {
        this.setLibrarySelections(this.currSongInfo.artist.id, this.currSongInfo.album.id, this.currSongInfo.song.id);
        this.getArtists();
        this.getAlbums(this.currSongInfo.artist.id);
        this.getSongs(this.currSongInfo.artist.id, this.currSongInfo.album.id);
      } else {
        this.getArtists();
      }
    }, 500);
  }

  /*
   *  Gets a list of artists form the server. A timeout is done to sync timing
   *  with the animations for the album and song lists (since they require s delay).
   */
  getArtists() {
    this.restService.getArtists().subscribe(artists => {
      setTimeout( () => this.artists = artists, 100);
    });
  }

  /*
   *  Gets a list of albums from the server for the selected artist ID.
   *  Sets the selected album ID to null then reset after delay to trigger the
   *  list loading animation.
   *  The album ID is set to null to close any already-opened song list.
   */
  getAlbums(artistId: number) {
    this.selArtistId = null; //Triggers albums animation by reseting to null first..
    this.selAlbumId = null;  //Hides song listing in the view.
    setTimeout( () => this.selArtistId = artistId, 100); //Reset the artist ID.
    this.restService.getAlbums(artistId).subscribe(albums => this.albums = albums);
  }

  /*
   *  Gets a list of songs from the server for the selected artist and album IDs.
   *  Sets the selected album ID to null then reset after delay to trigger the
   *  list loading animation.
   */
  getSongs(artistId: number, albumId: number) {
    this.selAlbumId = null;
    setTimeout( () => this.selAlbumId = albumId, 100); //Reset the artist ID.
    this.restService.getSongs(artistId, albumId).subscribe(songs => this.songs = songs);
  }

  /*
   *  Stores the selected song ID and makes a call to the parent component to
   *  Retrieve a "SongInfo" object, which essentially contains
   *  an artist, album, and song object together for full song playback info.
   *  Also stores the song list for album playback use in the player component.
   */
  getSong(songId: number) {
    let artistId = this.selArtistId;
    let albumId = this.selAlbumId;
    this.selSongId = songId;
    this.setCurrSongs.emit(this.songs);  //For playback through album in player.
    this.getSongInfo.emit({artistId, albumId, songId});
  }

  /*
   *  Sets all lists, song information, and list selections to default (null).
   *  Also reloads the artist list.
   */
  resetLibrary() {
    this.artists = null;
    this.albums = null;
    this.songs = null;
    this.currSongInfo = null;
    this.setLibrarySelections(null, null, null);
    this.getArtists();
  }

  /*
   *  Reloads all artist, album, and song lists.
   */
  refreshLibrary(artistId: number, albumId: number) {
    this.getArtists();
    this.getAlbums(artistId);
    this.getSongs(artistId, albumId);
  }

  /*
   *  Sets the selected artist, album, and song names using their given IDs.
   */
  setLibrarySelections(artistId: number, albumId: number, titleId: number) {
    this.selArtistId = artistId;
    this.selAlbumId = albumId;
    this.selSongId = titleId;
  }

}
