import { Component, Output, EventEmitter } from '@angular/core';
import { RestService } from '../../service/rest/rest.service';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.css']
})
export class UploadComponent {

  @Output() exitMenu = new EventEmitter();

  isUploading: boolean = false;
  currProgress: number = 0;
  maxProgress: number = 1;

  constructor(private restService: RestService) { }

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

  resetProgressBar() {
    this.currProgress = 0;
    this.maxProgress = 1;
  }

}
