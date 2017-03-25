import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Song } from './Song';
import 'rxjs/add/operator/map';

@Injectable()
export class SongService {

  constructor(private http: Http) { }

  getAllSongs() {
    return this.http.get("http://localhost:8080/library-all")
      .map((response: Response) => response.json());
  }

  getArtists() {
    return this.http.get("http://localhost:8080/library")
      .map((response: Response) => response.json());
  }

  getAlbums(artist: String) {
    return this.http.get("http://localhost:8080/library/" + artist)
      .map((response: Response) => response.json());
  }

  getSongs(artist: String, album: String) {
    return this.http.get("http://localhost:8080/library/" + artist + "/" + album)
      .map((response: Response) => response.json());
  }

  addSong(song: Song) {
    return this.http.post("http://localhost:8080/library", song)
      .map((response: Response) => response.json());
  }

  removeSong(title: String) {
    return this.http.delete("http://localhost:8080/library/" + title)
      .map((response: Response) => response.json());
  }

  /*
  updateSong(song: Song) {
    return this.http.put("http://localhost:8080/library/" + song.artist + "/" + song.album + "/" + song.title, song)
      .map((response: Response) => response.json());
  }
  */

}
