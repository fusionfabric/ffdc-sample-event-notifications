# Welcome

This is the user interface of the [clock-application](../clock-application) sample app, that consumes event notifications broadcasted by the [Clock Service](https://developer.fusionfabric.cloud/api/clock-v1-5ce28ddc-dbbc-11e9-9d36-2a2ae2dbcce4/docs) at every 15 seconds.

**To run this sample**


1. Start the server application from `../clock-application` as described in `../clock-application/README.md`.
2. Copy `src/environments/environment.ts.sample` to `src/environments/environment.ts`, open it, and replace `<%WEBSOCKET-URL%>` with the public web socket endpoint of the [clock-application](../clock-application) server app, such as: `http://localhost:8080/socket`, if the sample is running on `localhost`.
3. Install the required **npm** packages: 

```
npm install
```
4. Build the Clock Application UI:

```
npm start
```
The application is build into `dist/`, and a webserver is started and listening on the port `4200`.

5. Point your browser to http://localhost:4200. The home page of this sample client application is displayed.
