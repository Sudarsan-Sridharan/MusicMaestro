import { Injectable } from '@angular/core';

@Injectable()
export class Song {
  id: number;
  albumId: number;
  track: number;
  name: String;
  year: String;
}
