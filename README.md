# spring-akka-actor-demo
The purpose of this sample application is to demonstrate 
how to build highly scalable and concurrent streaming 
service using Spring boot, AKKA actor and Project Reactor.

To run the service locally, please follow the below steps -

* Pass the VM argument `reactor.netty.ioWorkerCount` . This the worker 
thread count of reactor netty event loop. In my `Apple-M1 8 core macbook`, 
I'd set it as 20 and got a good performance in r2dbc
  <br/><br/>
* Since this service stores the incoming events to a mssql database, it attempts to
connect to mssql db at startup. Please make sure to provide a 
valid database details in `application.properties`.
  <br/><br/>
 Remember to create a new database named as `akka_demo` before starting the service.
  <br/><br/>
 You can also run a dockerized mssql in your local using the 
 below command - <br/><br/>
`  docker run -e "ACCEPT_EULA=1" -e "MSSQL_SA_PASSWORD=<YOUR_CHOICE_PASSWORD>" -e "MSSQL_PID=Developer" -e "MSSQL_USER=<YOUR_CHOICE_USERNAME>" -p 1433:1433 -d --name=sql mcr.microsoft.com/azure-sql-edge
` <br/><br/>
 
   Update the above chosen username and password in `application.properties`
   <br/><br/>
* This service use NATS jetstream as a sink for ACK publish. It uses the below default NATS configs through spring properties -

  <br/><br/>
  `nats.server=nats://localhost:4222`<br/>
  `nats.stream=akka-demo`<br/>
  `nats.subject=akka-demo-ack-events`
   <br/><br/>
   To run NATS streaming server in local, use the below command -  
   <br/>
   `docker run -ti -p 4222:4222 --name jetstream synadia/jsm:latest server`
   




