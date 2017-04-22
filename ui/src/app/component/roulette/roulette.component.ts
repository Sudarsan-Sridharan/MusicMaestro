import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RestService } from '../../service/rest/rest.service';
import { UtilityService } from '../../service/utility/utility.service';
import { Artist } from '../../model/artist';
import { Album } from '../../model/album';
import { Song } from '../../model/song';
import { SongInfo } from '../../model/songInfo';

@Component({
  selector: 'app-roulette',
  template: ''
})

export class RouletteComponent implements OnInit {

  /*
   *  Output with the parent component.
   *  These are event emitters which trigger parent functions.
   */

  @Output() setCurrSongs = new EventEmitter();
  @Output() getSongInfo = new EventEmitter();

  constructor(private utilityService: UtilityService, private restService: RestService) { }

  ngOnInit() {

    //List of artist, album, and song objects retrieved from the server.
    let artists: Array<Artist>;
    let albums: Array<Album>;
    let songs: Array<Song>;

    //The indices used to find a random ID within the artist, album, and song lists.
    let artistIndex: number;
    let albumIndex: number;
    let songIndex: number;

    //The new randomly retrieved IDs from the artist, album, and song lists.
    let artistId: number;
    let albumId: number;
    let songId: number;

    //Used to tell parent that this component has been run after initiialization.
    let hasRoulette: boolean;

    /*
     *  Grabs the artist, album, and song lists, but for each step of the way,
     *  the function generates a random ID to retrieve the next list by
     *  generating a random index number from the list and using the ID for the given index.
     *  Once the process has completed, the retrieved song list is saved and sent
     *  to the parent component for album playback in the player componenet.
     *  Finally, the SongInfo object gets retrieved from the server.
     */
    this.restService.getArtists().subscribe(artistList => {
      artists = artistList;
      artistIndex = this.utilityService.getRandomInt(0, artists.length - 1);
      artistId = artists[artistIndex].id;
      this.restService.getAlbums(artistId).subscribe(albumList => {
        albums = albumList;
        albumIndex = this.utilityService.getRandomInt(0, albums.length - 1);
        albumId = albums[albumIndex].id;
        this.restService.getSongs(artistId, albumId).subscribe(songList => {
          hasRoulette = true;
          songs = songList;
          songIndex = this.utilityService.getRandomInt(0, songs.length - 1);
          songId = songs[songIndex].id;
          this.setCurrSongs.emit(songs);
          this.getSongInfo.emit({artistId, albumId, songId, hasRoulette});
        });
      });
    });

  }

}
