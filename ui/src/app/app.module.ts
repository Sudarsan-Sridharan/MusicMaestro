import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './component/app/app.component';
import { PlayerComponent } from './component/player/player.component';
import { LibraryComponent } from './component/library/library.component';
import { RestService } from './service/rest/rest.service';

@NgModule({
  declarations: [
    AppComponent,
    PlayerComponent,
    LibraryComponent
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
