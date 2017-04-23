import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { SongInfo } from '../../model/songInfo';
import { config } from '../../config/config';
import 'rxjs/add/operator/map';

@Injectable()
export class RestService {

  constructor(private http: Http) {}

  /*
   *  Returns a list of artist objects.
   */
  getArtists() {
    return this.http.get("http://" + config.serverName + ":" + config.serverPort
      + config.appName + "/artists")
    .map((response: Response) => response.json());
  }

  /*
   *  Returns a list of album objects for a given artist ID.
   */
  getAlbums(artistId: number) {
    return this.http.get("http://" + config.serverName + ":" + config.serverPort
      + config.appName + "/artists/" + artistId + "/albums")
    .map((response: Response) => response.json());
  }

  /*
   *  Returns a list of song objects for a given artist and album ID.
   */
  getSongs(artistId: number, albumId: number) {
    return this.http.get("http://" + config.serverName + ":" + config.serverPort
      + config.appName + "/artists/" + artistId + "/albums/" + albumId + "/songs")
    .map((response: Response) => response.json());
  }

  /*
   *  Returns an object containing an artist, album, and song object from a
   *  given artist, album, and song ID.
   */
  getSongInfo(artistId: number, albumId: number, songId: number) {
    return this.http.get("http://" + config.serverName + ":" + config.serverPort
      + config.appName + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId + "/info")
    .map((response: Response) => response.json());
  }

  /*
   *  Sends a song file to the server to store, and create new song information.
   */
  addSong(file: File) {
    let formData:FormData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post("http://" + config.serverName + ":" + config.serverPort
      + config.appName + "/file", formData);
  }

  /*
   *  Updates a song designated with the given artist, album, and song IDs.
   *  A SongInfo object is sent to the server to parse out and update existing data.
   */
  updateSong(songInfo: SongInfo, artistId: number, albumId: number, songId: number) {
    return this.http.put("http://" + config.serverName + ":" + config.serverPort
      + config.appName + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId, songInfo)
    .map((response: Response) => response.json());
  }

  /*
   *  Removes a song file and its corresponding data for the given
   *  artist, album, and song IDs from the server.
   */
  removeSong(artistId: number, albumId: number, songId: number) {
    return this.http.delete("http://" + config.serverName + ":" + config.serverPort
      + config.appName + "/artists/" + artistId + "/albums/" + albumId + "/songs/" + songId);
  }

}
