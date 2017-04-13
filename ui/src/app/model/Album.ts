import { Injectable } from '@angular/core';
import { Song } from './Song';

@Injectable()
export class Album {
  id: number;
  artistId: number;
  name: string;
}
