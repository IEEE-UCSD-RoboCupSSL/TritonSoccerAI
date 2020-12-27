# TritonSoccerAI

Robocup Soccer Robot AI software 

The RoboCup SSL project involves running an AI software on a **remote station** (usually a computer near the game field) that remotely controls 6~8 soccer robots on the field. 



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



## VS Code

If you are using VS Code, make sure to run "mvn clean install" every time you open this repository with VS Code. This is due to an issue with VS Code deleting generated-resources files on start up: https://github.com/redhat-developer/vscode-java/issues/177



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

The jar is generated under **target/[ProjectName]-[Version]-SNAPSHOT.jar**



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



