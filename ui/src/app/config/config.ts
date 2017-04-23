/*
 *  Essentially a config file, but read in as a JSON variable to any component
 *  that imports it.
 */
export var config = {
  serverName: "10.0.0.12",  //Set to the host name or IP of server.
  serverPort: 8080,
  appName: "/music-maestro-server" //Set empty if running this app on root url.
}
