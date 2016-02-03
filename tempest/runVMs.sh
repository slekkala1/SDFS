#!/bin/bash


for i in  {1..7}
do
   scp target/uber-tempest-1.0-SNAPSHOT.jar lekkala2@fa15-cs425-g03-0$i.cs.illinois.edu:~/
done

