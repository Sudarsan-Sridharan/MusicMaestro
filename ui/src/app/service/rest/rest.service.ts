import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { SongInfo } from '../../model/SongInfo';
import 'rxjs/add/operator/map';

@Injectable()
export class RestService {

  serverName: String = "http://localhost:8080";

  constructor(private http: Http) {}

  getArtists() {
    return this.http.get(this.serverName + "/artists")
    .map((response: Response) => response.json());
  }

  getAlbums(artistId: number) {
    return this.http.get(this.serverName + "/artists/" + artistId + "/albums")
    .map((response: Response) => response.json());
  }

  getSongs(artistId: number, albumId: number) {
    return this.http.get(this.serverName + "/artists/" + artistId + "/albums/" + albumId + "/songs")
    .map((response: Response) => response.json());
  }

  getSong(artistId: number, albumId: number, songId: number) {
    return this.http.get(this.serverName + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId)
    .map((response: Response) => response.json());
  }

  getSongInfo(artistId: number, albumId: number, songId: number) {
    return this.http.get(this.serverName + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId + "/info")
    .map((response: Response) => response.json());
  }

  addSong(file: File) {
    let formData:FormData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post(this.serverName + "/file", formData);
  }

  updateSong(songInfo: SongInfo, artistId: number, albumId: number, songId: number) {
    return this.http.put(this.serverName + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId, songInfo)
    .map((response: Response) => response.json());
  }

  removeSong(artistId: number, albumId: number, songId: number) {
    return this.http.delete(this.serverName + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId);
  }

}
