# Dependency Manual Installation Guide



## Dev essentials





## IDE/Editors

Default IDE for this project: Visual Studio Code

Other IDEs such as IDEA, Eclipse should work as well, but might need some configuration



## grSim



## Java / change java version

Install java 11:

1. ```shell
   sudo apt update
   ```
   
2. ```shell
   sudo apt-get install openjdk-11-jdk
   ```

4. verify installation 

   ```shell
   java -version
   ```

4. set $JAVA_HOME to jdk 11:

   Append the following line to ~/.bashrc file (or ~/.zshrc file if using zsh)

   ```shell
   export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
   ```

   Then 

   ```shell
   source ~/.bashrc
   ```

5. verify $JAVA_HOME

   ```
   echo $JAVA_HOME
   ```

   

If having other java version installed, select a particular java version by

```shell
sudo update-alternatives --config java
```



## Maven

1. ```shell
   sudo apt update
   ```

2. ```shell
   sudo apt install maven
   ```

3. verify installation:

   ```shell
   mvn -version
   ```

[Learn to use maven in 5 min](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)



## Google Protobuff library





## gRPC



