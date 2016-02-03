#!/bin/bash


for i in $(seq 1 2 7)
do
   scp /tempest/target/uber-tempest-1.0-SNAPSHOT.jar lekkala2@fa15-cs425-g03-0$i.cs.illinois.edu:~/
done

