# Music Player
The front end project was generated with [Angular CLI](https://github.com/angular/angular-cli) v1.0.0-rc.2. The back end was generated using Spring Boot v1.5.3.

### Configuration
Modify the “config.ts” and “application.yml” files to contain the correct server information. Also make sure “application.yml” uses the correct library root directory. This directory is where the library file will be accessed and the songs will be stored. Change the base URL in index.html to what the deployment name will be for the server in Tomcat (match with the “appName” parameter in the config.ts).

## Back End

### Configuration
Modify the “application.yml” file to contain the correct server information. Also make sure the file uses the correct library root directory. This directory is where the library file will be accessed and the songs will be stored.

### Deployment instructions
Install Maven, if it hasn’t been already. Run “mvn clean install” to build to .WAR file, and then deploy it to Tomcat 8.5.

## Front End

### Configuration
Modify the “config.ts” JSON file to contain the correct server information (same as configured for the server above). Change the base URL in index.html to what the deployment name will be for the server in Tomcat (match with the “appName” parameter in the config.ts).

### Deployment instructions (Dev)
Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

### Deployment instructions (Prod)
Build (see below), then deploy folder to Tomcat 8.5. Using '-prod' flag may throw errors.

### Code scaffolding
Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive/pipe/service/class/module`.

### Build
Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.

### Running unit tests
Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

### Running end-to-end tests
Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).
Before running the tests make sure you are serving the app via `ng serve`.

### Further help
To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
