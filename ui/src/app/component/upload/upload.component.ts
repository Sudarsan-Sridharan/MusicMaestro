import { Component, Output, EventEmitter } from '@angular/core';
import { RestService } from '../../service/rest/rest.service';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.css']
})
export class UploadComponent {

  /*
   *  Output with the parent component.
   *  This uses and event emitter which trigger a parent function.
   */

  @Output() exitMenu = new EventEmitter();

  /*
   *  Instance variables and objects.
   */

  //Represents whether or not the upload process has started.
  isUploading: boolean = false;

  //Tracks the current and total count of files for the given upload.
  currProgress: number = 0;
  maxProgress: number = 1;

  constructor(private restService: RestService) { }

  /*
   *  Takes a list of files selected in the view, and sends them one-by-one to
   *  the server to store and to create new artist, album, and song objects.
   *  Once completed, a timeout is set so the user is given time to see the upload has finished.
   */
  addSongs(fileList: FileList) {
    this.isUploading = true;   //Used in view to show progress bar.
    this.maxProgress = fileList.length; //Sets the new max value for progress bar.
    for (let i = 0; i < fileList.length; i++) { //Loop through list of files.
      this.restService.addSong(fileList[i]).subscribe( () => {  //Send song to server.
        this.currProgress++; //Increment the current value for progress bar.
        if (i == fileList.length - 1) { //During last loop,
          setTimeout( () => { //Delay 0.8 seconds.
            this.exitMenu.emit();  //Clears out of current menu.
            this.resetProgressBar(); //Resets the current and max values for the progress bar.
            this.isUploading = false; //Used in view to hide progress bar.
          }, 800);
        }
      });
    }
  }

  /*
   *  Sets the default values back to the upload progress bar variables.
   */
  resetProgressBar() {
    this.currProgress = 0;
    this.maxProgress = 1;
  }

}
