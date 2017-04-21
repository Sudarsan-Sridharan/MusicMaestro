import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { SongInfo } from '../../model/songInfo';
import { config } from '../../config/config';
import 'rxjs/add/operator/map';

@Injectable()
export class RestService {

  constructor(private http: Http) {}

  getArtists() {
    return this.http.get("http://" + config.serverName + ":" + config.serverPort
      + "/artists")
    .map((response: Response) => response.json());
  }

  getAlbums(artistId: number) {
    return this.http.get("http://" + config.serverName + ":" + config.serverPort
      + "/artists/" + artistId + "/albums")
    .map((response: Response) => response.json());
  }

  getSongs(artistId: number, albumId: number) {
    return this.http.get("http://" + config.serverName + ":" + config.serverPort
      + "/artists/" + artistId + "/albums/" + albumId + "/songs")
    .map((response: Response) => response.json());
  }

  getSong(artistId: number, albumId: number, songId: number) {
    return this.http.get("http://" + config.serverName + ":" + config.serverPort
      + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId)
    .map((response: Response) => response.json());
  }

  getSongInfo(artistId: number, albumId: number, songId: number) {
    return this.http.get("http://" + config.serverName + ":" + config.serverPort
      + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId + "/info")
    .map((response: Response) => response.json());
  }

  addSong(file: File) {
    let formData:FormData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post("http://" + config.serverName + ":" + config.serverPort
      + "/file", formData);
  }

  updateSong(songInfo: SongInfo, artistId: number, albumId: number, songId: number) {
    return this.http.put("http://" + config.serverName + ":" + config.serverPort
      + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId, songInfo)
    .map((response: Response) => response.json());
  }

  removeSong(artistId: number, albumId: number, songId: number) {
    return this.http.delete("http://" + config.serverName + ":" + config.serverPort
      + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId);
  }

}
