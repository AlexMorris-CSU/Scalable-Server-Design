Alex Morris, Scalable Server Design: Using Thread Pools & Micro Batching to Manage and Load Balance Active Network Connections

1.Included Files:
All source files are within the following packages.
    cs455/scaling/client
    cs455/scaling/server
    cs455/scaling/util
README.txt
build.gradle

Notes:
-Atleast one client must connect within 20 seconds of starting server


2. How to run:
    Use "gradle build" to build
    Run Server
        -cd build/libs
        -"java -cp Morris_Alex_ASG2-1.0-SNAPSHOT.jar cs455.scaling.server.Server  <portnum> <thread-pool-size> <batch-size> <batch-time>"
    Run clients
        -set server IP, port and message rate in start-nodes.sh
        -"./start-nodes.sh" to run, machine-list is specified in /src/main/resources/machine-list
        -"java -cp Morris_Alex_ASG2-1.0-SNAPSHOT.jar cs455.scaling.client.Client <server-host> <server-port> <message-rate>"

3. Description of files:
  Client(in cs455/scaling/client):
    -Connects and registers with server
    -Creates random 8kb byte array
      -Hashes for local storage as pending hash(SHA)
      -sends to server
    -Once server responds will check to see if it is a pending hash and removes it
    -Sends messageRate messages a second
    -Every 20 seconds prints sent messages and received messages, for the last 20 second period

  Server(in cs455/scaling/server):
    -Accepts incoming client connections
    -Reads incoming client messages 8kb byte array
      -computes hash for byte array using same hashing algorithm(SHA)
      -sends hash back to client
    -nonblocking
    -Every 20 seconds prints Server Throughput, Active Client Connections, Mean Per client Throughput and  Std. Dev. Of Per-client Throughput

   ThreadPool(in cs455/scaling/server):
     -Starts number of threads specified on startup
     -Batches data together, batches of tasks
     -when either batch size or batchTime is reached, it passes the batch off to next available worker
     -workers add/remove themselves from an array list to specify whether they are working or available for work
     -Threads can perform any and all parts of read, compute and write tasks

  Util(in cs455/scaling/util):
    -Contains hashing algorithm
    -Contains object Task, one task is one unit of work
