Tempest Architecture
====================

Overview
--------

A tempest is a like a storm but more windy and usually louder.  This is just what CallMeIshmaelInc. needs!
We think you'll agree once you finish this brief architectural document.

**SDFSClientApp** is implemented with the underlying failure detection in tact. It consists of **SDFSClient** that uses TCP sockets
 to connect to SDFS server machines. A machine can choose to be just an SDFSClient and not be a part of SDFS storage servers on which
 files are replicated. **SDFSClientApp** is the entry point for the SDFS desired operations: put,get and delete.
 **SDFSClientApp** is implemented using Cliche much in the same way as **TempestApp**.

Tempest consists of a socket **Gossip Server** and **Gossip Client** that use UDP sockets to Gossip, **Client** 
and **Server** for communication, a **Logger** which performs the ability to
write logs and to grep them, and there is a convenient **Console** built on Cliche(our only third party library
in the application module). The **TempestApp** is the entry point for the application and ties all of these classes
together in to a convenient application.

There are two modules in the git repository: an application module (tempest) and a test module (tempest-test).
The test module provides a suite of junit test to verify that the most important features of Tempest are
fully functional. The application module provides the fully functioning Tempest application.

Both modules use Maven for configuration management.  See the README.md file for more details about how to
get the application and test compiled and running using Maven.


SDFS Design
--------

Both SDFSClientApp and TempestApp are independent. SDFS file operations(put,get,delete) are performed via SDFSClientApp, while
machines can join or leave the SDFS file storage via TempestApp.

SDFS Server machines are hard coded in SDFSClient, so client can pick any random alive machines to perform
put, get, and delete operations. **Active replication** is used here. Once the random alive SDFS server receives the put command it
sends put command with file to the node Ids determined by **Partitioner** one after another.

Once a put command is issued, the SDFS server machine will calculate the alive nodes(in **Partitioner**)
at which the file needs to be replicated using distributed hash table. SHA256 algorithm is used to calculate node Ids
and file key Id. The file is replicated at first and second successors as in Cassandra Key-Value stores.
The replica information is also piggybacked when Put command goes to the node. For example, file is replicated at node 1,
node 2 and node 3. Node 1 recieves the replica information that node 2 and node 3 have the replicas,
similarly node 2 receives the replica information that node 3 and node 1 have replicas and so on.
The replica information of file stored in the machine is stored in **Partitioner**

Once a get command is issued, the SDFS server machine will calculate the alive nodes at which the file is and sends get command
to the one of the servers. If the server fails to respond 'Ok', next server with replica is tried.

Once a delete command is issued, the SDFS server machine will calculate the alive nodes at which the file is and sends delete command
 to the all of the servers.

**Replica Service** runs every 5 seconds to make sure all replicas of every file at the machine are alive.
**Replica Service**  loops through the replica information for every file stored in **Partitioner** and checks the membershiplist
to see if the replica nodes have failed. When the failure of replica node is detected, the **Partitioner**, will give a new list
of node Ids at which the file needs to be replicated. The failed replica is replaced with the new alive replica node Id at the machine
and the file is sent to the alive replica node obtained from **Partitioner**.

**list** and **store** commands are available on TempestApp.

Chunking has been implemented, but it is set to 1 chunk currently for demo.
Some tweaking need to be done on Chunking to perform sharding, currently not supported.
Chunking goes as follows: Once a put command is issues,
the file is broken into chunk size as set in PutHandler and **Partitioner** provides the node Ids based on Chunk Name(set as
"SDFSFileName" +i +".bin" . These Chunks are sent to respective machines using PutChunk command which is internally handled.
Similarly on get command, the chunks are obtained using get chunk commands from the respective nodeIds and merged in the **GetHandler**
and the whole file contents of all chunks are returned to the Client machine. Also delete command, will send respective delete Chunk command
to all servers that hold the chunks of a particular sdfsfile.

All SDFS operations and commands use google protocol buffers to send and receive messages and are done using TCP connection.

(i) re-replication time and bandwidth upon a
failure (you can measure for a 20 MB file);
4 data points for replication time for one failure 70,67,66,63 ms, average is 66.5 ms
bandwidth 300 Mbps on average

ii) My design is master-less.

iii) times to read and write one file of size 20 MB
4 data points for 20 MB file
write takes time 360,415,335 and 416 ms
read takes 212,217, 214 and 276 ms
write takes more time than read because replication is also included. Given that three replicas need to be written to 3 different
machines via active replication, the time taken is not significantly greater than the time taken to read.



Gossip protocol
---------------
An **Introduce** command is sent over TCP to the introducer when a member first starts the **MembershipService** and the member is added to the 
membership list of introducer. The introducer returns it's current membership list to the member and the member starts gossiping.
Gossiping of the membership list happens through **HeartBeat**, which increments the members heartbeat in the membership list and sends
the membership list using the **GossipClient** over UDP to a random member of the membership list other than itself every 250ms. 
A **GossipServer** runs on each member and recieves heartbeats. 

The **Leave** command is executed when a member stops it's membership, **Leave** is sent as over TCP to 
all known members so that a member can leave the group immedeatly. All members recieving a **Leave** message 
mark the leaving member as having left.  This prevents the leaving member from being further gossiped about.
The member who left will be removed from the membership list due to its heartbeat stopping the same as a crashed machine.

When a machine crashes, the hearbeats from the crashed machine are not received and the machine's 
status is marked as having failed after 2750ms. Once marked as failed on a member the marking member stops gossiping 
about the failed member. After 5500ms of not receiving heartbeats the member is removed from the membership list.

This ensures that a failure is detected in 3000ms and will be completed across the group in 5750ms. Using Google
Protocol Buffers a member message in the membership list uses 29 bytes. With our group size of 7 each member gossips
7members*29bytes*4gossips/second = 812B/s, not counting overhead from UDP, and recieves a similar amount. This is 
near linear growth in traffic as the member count increases since the message frequency is 
constant and the membership list gossiped grows linearly with the number of members. However, the failure detection
rate will need to be increased logarithmicly with the increase in membership size to maintain the same
false detection rate.

As currently constructed, adding a node will increase network send and recieve traffic by 116B/s. Removing a node will 
cause a decrease of the same 116B/s.

The background network usage for 4 members is 116B/s * 4nodes = 464B/s  
If a member joins usage increases to 580B/s  
A member leaving will decrease usage to 384B/s  
After detection and removal a failure will be the same 384B/s as the member leaving  

A member is marked as failed if an update hasn't been seen for 11 rounds of gossiping.
So to consider the impact of false positives from 3%, 10%, and 30% for N = 2 and 4.

When N = 2 we don't gossip to ourselves, we always gossip to another node so the probability of 
a false detection is the same as the probability for failing 11 times to send a message. 
So at 3%, the probability of a false detection is 0.0000000000000000177147
  at 10%, 0.00000000001
  and at 30%, 0.00000177147

Gossip Client and Gossip Server
-------------------------------
**Gossip Client** and **Gossip Server** communicate using UDP socktes to send membershipList and receive membershipList from machines.
Google protobufs are used to serialize/deserialize the sent/recieved DatagramPacket respectively.

**Gossip Server** listens to the multiple clients that send the membershipList. The membershipList recieved is merged with current membershipList at
the **Gossip Server** in a Synchronized manner. **Gossip Server** and **Gossip Client** both access/use the same membershipList on the machine
and so care is taken to see that membershipList is manipulated in a Synchronized way whereever appropriate.

Protos Package in src/tempest
-----------------------------

Protos package has the **Membership** java class genereted from Google Protocol buffer **Membership.proto** in protos package that are used in
serializing/deserializing the membership lists.

Additionally, **Command.proto** contains the structures used to generate java classes for all other command's messaging.


Client and Server
-----------------

At a low level **Client** and **Server** communicate using java sockets and are both able to operate multi threaded. **Server**
can handle multiple simultaneous requests from clients and **Client** can send simultaneous requests to every
**Server** in it's group.

At a higher level **Client** and **Server** communicate with each other via a pair of commands. **ClientCommand** and
**ServerCommand** work together provide an extensible interface to easily implement new features between
**Client** and **Server**.

**Client** provides the ability to distribute operations to all of the machines in the group and join their
responses. **Client** is aware of all the machines in the group through **Machines** which on construction
reads the machines from the config.properties file.

**ClientCommand** provides an interface to send a request through the **Client** to a **Server** that a
corresponding **ServerCommand** can recognise and execute on the server.  Once the **ServerCommand** finishes
executing it sends a response through the **Server** to the **Client** where the **ClientCommand** processes
the response into a **CommandResponse**. **CommandResponse** is strongly typed, rather than just a string, to
facilitate easier unit testing and easier processing in the **Console**.

Currently, there are **Grep**, **Introduce**, **Leave** and **Ping** commands which result in a **Response**.

Logger
-----

**Logger** uses the logging utility included with Java for logging and Runtime.exec() to execute the standard
grep command included with most Linux and Unix systems to read logs. Currently, **Logger** provides the
ability to log and grep on two different files.  This is purely to facilitate testing for the current spec.
We think it will prove difficult to evaluate the difference between a single machine grep distributed greps if the
logs are changing underneath you.

Console
-------

**Console** uses the Cliche library to create a simple command console application which wraps the functionality
of the above described classes into a convenient utility.  You can type ?list to get a list of available commands and
exit to exit.

Tests
-----
**Logger** has the most through tests since it performs the logging and grepping. **Client** and **Server** have tests
to demonstrate that the **Ping** and **Grep** commands function across multiple servers running on the same machines
correctly.


Summary
-------

In some ways the adding of tests complicated some of the architecture but at the same time made the classes more
reusable.  The benefits of unit testing alone should justify the added classes and interfaces but knowing that
Tempest is a platform that we will be building on throughout our relationship with CallMeIshmaelInc. cements it.