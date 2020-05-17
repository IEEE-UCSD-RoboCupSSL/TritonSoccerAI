# RC-Core

Robocup software core layer

The RoboCup SSL project involves running an AI software on a **remote station** (usually a computer near the game field) that remotely controls 6~8 soccer robots on the field. 

RC-Core is the **core** component of the **AI software** running on the remote station, where the rest of the AI components are micro-services in the form of **gRPC servers**. **RC-Core** serves as the **client** that calls other services through **gRPC** calls.

RC-Core is written in Java, maintained using **Apache Maven**.

[Install Maven and basic usage](https://github.com/IEEE-UCSD-RoboCup-2020/SoftExamples-Repo/blob/master/Docs/maven.md)

RC-Core uses the following Maven **standard project structure**:

```shell
RC-Core
├── src
│   ├── main
│   │   ├── java
│   │   │   └── Triton
│   │   │       ├── App.java
│   │   │       ├── Control
│   │   │       │   ├── Connection.java
│   │   │       │   ├── RemoteBotConnection.java
│   │   │       │   ├── RobotConnection.java
│   │   │       │   └── VirtualBotConnection.java
│   │   │       ├── DesignPattern
│   │   │       │   ├── AbstractData.java
│   │   │       │   ├── Observer.java
│   │   │       │   └── Subject.java
│   │   │       ├── ExternProto
│   │   │       │   ├── ......
│   │   │       ├── Geometry
│   │   │       │   ├── Line2D.java
│   │   │       │   ├── Point2D.java
│   │   │       │   └── Vec2D.java
│   │   │       ├── Utility
│   │   │       │   └── keyboardCtrl.java
│   │   │       └── Vision
│   │   │           ├── DetectionData.java
│   │   │           ├── FieldDetection.java
│   │   │           ├── FieldGeometry.java
│   │   │           ├── GeometryData.java
│   │   │           └── VisionConnection.java
│   │   └── proto
│   │       └── remote_commands.proto
│   └── test
│       └── java
│           └── Triton
│               └── AppTest.java
...............................
```

* App.java contains the main class
* Triton/ExternProto contains the .proto files and proto-java files for SSL-Vision and grSim, which are imported from external sources, don't modify them!
* main/proto contains the user-defined proto files



## Install dependency

Dependencies are managed by Maven. Maven manages dependencies according to the **pom.xml** file under root.

run the following to install maven plug-ins and dependencies

```shell
mvn clean install
```

The install will invoke protoc to generate the needed proto-java files under

```shell
./target/generated-sources/protobuf/java/*.java
```



## Compile/Test/Package

To compile, run

```shell
mvn compile
```



To run JUnit Tests, 

```shell
mvn test
```



Run the following command to compile, run JUnit test, and generate the **jar package**

```shell
mvn package
```

The jar is generated under **target/RC-Core-[Version]-SNAPSHOT.jar**



## Execute

Execute using maven:

```
mvn exec:java
```




To execute by shell script, additional step is needed to package some of the needed **dependencies** installed in   **~/.m2/repository/....**  into the generated jar

```shell
mvn clean compile assembly:single
```

Then run

```shell
java -jar target/*.jar
```



