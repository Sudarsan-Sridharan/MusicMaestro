import { Injectable } from '@angular/core';
import { Artist } from './artist';
import { Album } from './album';
import { Song } from './song';

@Injectable()
export class SongInfo {
  artist: Artist;
  album: Album;
  song: Song;
}
