Chat Challenge
---

Simple application implementing a TCP chat server with Akka.

### How to launch the application

Execute the command `sbt run` from the root of this project.  
This will start a server listening by default on port 10000.

Once started, you could connect to the server via telnet, as:

`telnet localhost 10000`

and you can start to broadcast your messages to all the other clients connected, like this:

![Usage example](/chat-challenge.gif?raw=true "Usage example")

---

### Application configuration

If you wish to launch the server at different port or hostname, you might configure it by editing the properties defined in `application.conf`

---

### Tests

The broadcasting engine has been tested using Akka Test Kit.
