import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { RestService } from './service/rest/rest.service';
import { AppComponent } from './component/app/app.component';
import { PlayerComponent } from './component/player/player.component';
import { LibraryComponent } from './component/library/library.component';
import { EditComponent } from './component/edit/edit.component';
import { UploadComponent } from './component/upload/upload.component';

@NgModule({
  declarations: [
    AppComponent,
    PlayerComponent,
    LibraryComponent,
    EditComponent,
    UploadComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule
  ],
  providers: [RestService],
  bootstrap: [AppComponent]
})
export class AppModule { }
