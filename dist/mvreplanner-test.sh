#!/bin/bash
DUNE_HOME=/home/zp/workspace/dune/build
MVPLAN_HOME=/home/zp/workspace/imc4j/dist

echo "Starting Dune for NP1"
cd $DUNE_HOME
./dune -c lauv-noptilus-1 -p Simulation &
echo "Starting BackSeat for NP1"
cd $MVPLAN_HOME
java -jar MvReplanner.jar --port=6002 &

echo "Starting Dune for NP2"
cd $DUNE_HOME
./dune -c lauv-noptilus-2 -p Simulation &
echo "Starting BackSeat for NP2"
cd $MVPLAN_HOME
java -jar MvReplanner.jar --port=6003 &

echo "Starting Dune for XP1"
cd $DUNE_HOME
./dune -c lauv-xplore-1 -p Simulation &
echo "Starting BackSeat for XP1"
cd $MVPLAN_HOME
java -jar MvReplanner.jar --port=6006 &



