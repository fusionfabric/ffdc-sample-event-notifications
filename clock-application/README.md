# Welcome

This sample project is an implementation of a server backend that consumes event notifications attached to the [Clock Service API](https://developer.fusionfabric.cloud/api/clock-v1-5ce28ddc-dbbc-11e9-9d36-2a2ae2dbcce4/docs). 

It is provided with the purpose of illustrating the messaging capabilities of FusionFabric.cloud. It implements a listener endpoint at `https://{{eventsBaseUrl}}/sample/clock-service/v1/datetime-published` that processes the broadcasted messages.


**To run this sample**

> This sample server application is configured to run on http://localhost:8080. You must take care of exposing it to the public internet by a secure method. Consult your local IT department for assistance. The public URL is the base URL that you need at step 2 of the following procedure.


1. Start this sample server application with:

```
mvn spring-boot:run
```

2. Register an application on [**Fusion**Fabric.cloud Developer Portal](https://developer.fusionfabric.cloud), and include the [Clock Service](https://developer.fusionfabric.cloud/api/clock-v1-5ce28ddc-dbbc-11e9-9d36-2a2ae2dbcce4/docs).  In the application creation wizard, pay attention to the following details:
   + At the **Details** step, use `*` as the **Reply URL**.
   + At  the **Event** step use **the public URL that points to `localhost:8080`** as the **Base URL**. This is the `{{eventsBaseUrl}}` of the the listener endpoint.
3. Start the UI sample application by following the instructions from [../clock-application-ui/README.md](../clock-application-ui/README.md). 

> If you permanently disable the server application, or the base URL is not available for any other reason, unsubscribe from the Clock Service events notifications from your dashboard on FusionCreator.
