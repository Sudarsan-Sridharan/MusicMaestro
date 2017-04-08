import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Artist } from './model/Artist';
import { Album } from './model/Album';
import { Song } from './model/Song';
import { SongInfo } from './model/SongInfo';
import 'rxjs/add/operator/map';

@Injectable()
export class SongService {

  constructor(private http: Http) { }

  getArtists() {
    return this.http.get("http://localhost:8080/artists")
    .map((response: Response) => response.json());
  }

  getAlbums(artistId: number) {
    return this.http.get("http://localhost:8080/artists/" + artistId + "/albums")
    .map((response: Response) => response.json());
  }

  getSongs(artistId: number, albumId: number) {
    return this.http.get("http://localhost:8080/artists/" + artistId + "/albums/" + albumId + "/songs")
    .map((response: Response) => response.json());
  }

  getSong(artistId: number, albumId: number, songId: number) {
    return this.http.get("http://localhost:8080/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId)
    .map((response: Response) => response.json());
  }

  getSongInfo(artistId: number, albumId: number, songId: number) {
    return this.http.get("http://localhost:8080/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId + "/info")
    .map((response: Response) => response.json());
  }

  addSong(file: File) {
    let formData:FormData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post("http://localhost:8080/file", formData);
  }

  updateSong(songInfo: SongInfo, artistId: number, albumId: number, songId: number) {
    return this.http.put("http://localhost:8080/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId, songInfo)
    .map((response: Response) => response.json());
  }

  removeSong(artistId: number, albumId: number, songId: number) {
    return this.http.delete("http://localhost:8080/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId);
  }

}
