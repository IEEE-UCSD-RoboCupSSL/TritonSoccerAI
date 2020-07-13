# SimuBot
AI software specifically designed to run the virtual robots in the GrSim simulator

Currently in development



## Sub-Repositories

* **RC-Core** : [README.md](RC-Core/README.md)
* **VirtualBot** : [README.md](VirtualBot/README.md)

* ...... (more coming soon)

  

## Dependencies
* Linux/MacOS/Win10 WSL
* grSim simulator for RoboCup SSL
* Google protobuf library version 3.11.4 (libprotoc 3.11.4)
* Java 11
* Apache Maven 3.6.0
* ......





## Known Issues

* Currently only support single camera configuration (for coding simplicity) since grSim simulates the game setup (virtual setup) with only one global vision camera. (number of cameras for the physical setup varies depending on the actual competition setup)
* Number of robots per team is fixed to 6, this can be manually changed by modifying the DetectionType.java file, along with modifying the way how VisionConnection class handles the data packet, typically by changing the number of iterations needed to obtain the full information from multiple consecutive packets. (single packet usually misses few robot's data)



