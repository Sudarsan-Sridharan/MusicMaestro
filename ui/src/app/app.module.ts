import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { RestService } from './service/rest/rest.service';
import { UtilityService } from './service/utility/utility.service';
import { AppComponent } from './component/app/app.component';
import { PlayerComponent } from './component/player/player.component';
import { LibraryComponent } from './component/library/library.component';
import { EditComponent } from './component/edit/edit.component';
import { UploadComponent } from './component/upload/upload.component';
import { ShuffleComponent } from './component/shuffle/shuffle.component';

@NgModule({
  declarations: [
    AppComponent,
    PlayerComponent,
    LibraryComponent,
    EditComponent,
    UploadComponent,
    ShuffleComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule
  ],
  providers: [RestService, UtilityService],
  bootstrap: [AppComponent]
})
export class AppModule { }
