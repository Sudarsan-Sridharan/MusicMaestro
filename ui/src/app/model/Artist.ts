import { Injectable } from '@angular/core';
import { Album } from './Album';

@Injectable()
export class Artist {
  id: number;
  name: String;

  //Server-side only:
  //albums: Array<Album>;
}
