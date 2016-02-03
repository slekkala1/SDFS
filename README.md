Tempest
=======

How to run?
-----------
1) On each machine go to /home/lekkala2 and use java -cp uber-tempest-1.0-SNAPSHOT.jar tempest.TempestApp to get the tempestApp running
In a new terminal window login to the VM which you want to run SDFSClientApp java -cp uber-tempest-1.0-SNAPSHOT.jar tempest.SDFSClientApp

2) Go to the machine from which you want join the membership and type ‘?list’ at the >Tempest prompt to see the function calls that can be made from Client end

3) Introducer which is VM 'fa15-cs425-g03-01.cs.illinois.edu' needs to be up for members to join the group.
Type 'mstart' or 'start-membership'(and Enter) at >Tempest to Join the membership/Gossip group and 'mstop' or 'stop-membership'(and Enter) to Leave the membership/Gossip group.  

4) Type 'mstart'(and Enter) at introducer 'fa15-cs425-g03-01.cs.illinois.edu' so others machines can join and similarly execute 'sm' on all machines at >Tempest command prompt tomstop the group.

5) Type 'mstop'(and Enter) on the machine you want to leave.

6) Type 'gml'(and Enter) or 'get-membership-list' at >Tempest command prompt to get the latest membership list on the machine. 

7) Once mstart is done the machine is a member of SDFS file storage machines.

8) To have the Client operations (put, get, delete) at the machine, java -cp uber-tempest-1.0-SNAPSHOT.jar tempest.SDFSClientApp at that particular machine in a new window and '?list' at the SDFSClientApp.

9) 'put localfilename sdfsfilename' will put the localfile with sdfsfilename at the SDFS file storage machines.

10) 'get sdfsfilename' will get the sdfsfilename from SDFS machines and store it at local machine with same name.

11) 'delete sdfsfilename' will delete the sdfsfile from all the SDFS machines where it is replicated.

12) After 'put localfilename sdfsfilename' at SDFSClientApp, 'store' can be used at TempestApp running to display files at each SDFS server machine.
'list sdfsfilename' will display all the VMs that have sdfsfilename.


Install
-------
1) Git Clone the repo 

2) cd cs425-mp-lekkala-morrow/tempest folder and mvn clean install 

3) cd target and java -cp uber-tempest-1.0-SNAPSHOT.jar tempest.TempestApp to get the Tempest App running on your machine.

4) java -cp uber-tempest-1.0-SNAPSHOT.jar tempest.SDFSClientApp to be the SDFS Client at that machine. 

Run Tests (once you have installed)
-----------------------------------
1) cd ../../tempest-test

2) mvn clean test
