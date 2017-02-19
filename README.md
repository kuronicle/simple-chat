# simple-chat
A simple chat service by Spring Boot. You can use it with only Java and web browser. 

![screen image](https://raw.githubusercontent.com/kuronicle/simple-chat/master/readme-image/screen-image.png "screen image")

## Features

* No need to install. Server needs only Java. 
* Client tool is web browser.

## Requirement

### to compile

* JDK 1.8
* Maven 

### to execute server

* Java 1.8

## How to use

### compile server side jar file

1. `git clone https://github.com/kuronicle/simple-chat.git` or download zip file and unzip it.
2. `cd simple-chat`
3. `mvn install` (JAVA_HOME should be JDK).

### execute server side jar file

1. `java -jar target/simple-chat-0.0.1-SNAPSHOT.jar`

If you want to change the port of server (default 8080), please add `-Dserver.port=<port number>` (ex `java -jar -Dserver.port 18080 target/simple-chat-0.0.1-SNAPSHOT.jar`). 

### access from web browser

1. access `http://<server-hostname>:8080/chat`
2. put name and push "on" button.
3. write message and push "Enter" key or "send" button.
