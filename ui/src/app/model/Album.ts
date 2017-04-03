import { Injectable } from '@angular/core';
import { Song } from './Song';

@Injectable()
export class Album {
  id: number;
  name: String;

  //Server-side only:
  //songs: Array<Song>;
}
