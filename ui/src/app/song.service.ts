import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Artist } from './model/Artist';
import { Album } from './model/Album';
import { Song } from './model/Song';
import 'rxjs/add/operator/map';

@Injectable()
export class SongService {

  constructor(private http: Http) { }

  getArtists() {
    return this.http.get("http://localhost:8080/library/artist")
    .map((response: Response) => response.json());
  }

  getAlbums(artistId: number) {
    return this.http.get("http://localhost:8080/library/artist/" + artistId + "/album")
    .map((response: Response) => response.json());
  }

  getSongs(artistId: number, albumId: number) {
    return this.http.get("http://localhost:8080/library/artist/" + artistId + "/album/" + albumId + "/song")
    .map((response: Response) => response.json());
  }

  getSong(artistId: number, albumId: number, songId: number) {
    return this.http.get("http://localhost:8080/library/artist/" + artistId + "/album/" + albumId + "/song/" + songId)
    .map((response: Response) => response.json());
  }

  addSong(file: File) {
    let formData:FormData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post("http://localhost:8080/upload/file", formData);
  }

/*
  updateSongInfo(song: Song) {
    return this.http.put("http://localhost:8080/library", song);
  }

  removeSong(song: Song) {
    return this.http.delete("http://localhost:8080/library/" + song.artist + "/" + song.album + "/" + song.title);
  }
  */

}
