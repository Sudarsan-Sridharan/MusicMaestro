import { Injectable } from '@angular/core';
import { Artist } from './Artist';
import { Album } from './Album';
import { Song } from './Song';

@Injectable()
export class SongInfo {
  artist: Artist;
  album: Album;
  song: Song;
}
