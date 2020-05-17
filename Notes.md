* To-do: simple algorithm for constructing a polygon formed by multiple vertices in random order, and decide whether a point or an object with certain radius is within that polygon
* Incorporate gRPC
  *  usages:
    * Remote ctrl bot via distributed computing model: Through gRPC, construct distributed computing model among the AI-software and computer on each robots (gRPC through http2 and netty framework)  
    * Emulate remote bot: turn virtual bot as a separate stand-alone program and emulate the real remote process
    * Python-java interaction through gRPC: outsource middle layer computation from inside java to a separate python program that handle algorithm & sci/math computation for faster development 
    * Python AI code: neuralnet/ML based  AI-models are simpler to implement in python, integrate a AI program written in python with the current java application via gRPC  
  * To-do
    * convert current prj to a Gradle prj, or integrate the Ant script inside Gradle, because gRPC needs either Maven or Gradle to be plugged in
    * use gradle to manage protoc
    * update remote_command.proto to include grpc services
    * begin with the simple grpc service without any stream
      * consider this java-software(remote) as the client, and robots as servers
      *  stream could be added later for the physical robot implementations to stream logging to the remote 