import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { RestService } from '../../service/rest/rest.service';
import { UtilityService } from '../../service/utility/utility.service';
import { Artist } from '../../model/Artist';
import { Album } from '../../model/Album';
import { Song } from '../../model/Song';
import { SongInfo } from '../../model/SongInfo';

@Component({
  selector: 'app-roulette',
  template: ''
})

export class RouletteComponent implements OnInit {

  @Output() setCurrSongs = new EventEmitter();
  @Output() getSongInfo = new EventEmitter();

  constructor(private utilityService: UtilityService, private restService: RestService) { }

  ngOnInit() {

    let artists: Array<Artist>;
    let albums: Array<Album>;
    let songs: Array<Song>;
    let artistIndex: number;
    let albumIndex: number;
    let songIndex: number;
    let artistId: number;
    let albumId: number;
    let songId: number;
    let hasRouletted: boolean;

    this.restService.getArtists().subscribe(artistList => {
      artists = artistList;
      artistIndex = this.utilityService.getRandomInt(0, artists.length - 1);
      artistId = artists[artistIndex].id;
      this.restService.getAlbums(artistId).subscribe(albumList => {
        albums = albumList;
        albumIndex = this.utilityService.getRandomInt(0, albums.length - 1);
        albumId = albums[albumIndex].id;
        this.restService.getSongs(artistId, albumId).subscribe(songList => {
          hasRouletted = true;
          songs = songList;
          songIndex = this.utilityService.getRandomInt(0, songs.length - 1);
          songId = songs[songIndex].id;
          this.setCurrSongs.emit(songs);
          this.getSongInfo.emit({artistId, albumId, songId, hasRouletted});
        });
      });
    });

  }

}
