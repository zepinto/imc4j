#!/bin/bash
DUNE_HOME=/home/zp/workspace/dune/build
MVPLAN_HOME=/home/zp/workspace/imc4j/dist
DATE=`date +%Y-%m-%d_%H-%M-%S`
OUTPUT=/home/zp/workspace/imc4j/dist/log_$DATE

mkdir $OUTPUT
echo "Logging to $OUTPUT."

echo "Starting Dune for NP1"
cd $DUNE_HOME
./dune -c lauv-noptilus-1 -p Simulation > $OUTPUT/np1-dune.log 2>&1 &
echo "Starting BackSeat for NP1"
cd $MVPLAN_HOME
java -jar MvReplanner.jar --port=6002 > $OUTPUT/np1-mvplanner.log 2>&1 &

echo "Starting Dune for NP3"
cd $DUNE_HOME
./dune -c lauv-noptilus-3 -p Simulation > $OUTPUT/np3-dune.log 2>&1 &
echo "Starting BackSeat for NP3"
cd $MVPLAN_HOME
java -jar MvReplanner.jar --port=6004 > $OUTPUT/np3-mvplanner.log 2>&1 &

echo "Starting Dune for XP1"
cd $DUNE_HOME
./dune -c lauv-xplore-1 -p Simulation > $OUTPUT/xp1-dune.log 2>&1 &
echo "Starting BackSeat for XP1"
cd $MVPLAN_HOME
java -jar MvReplanner.jar --port=6006 > $OUTPUT/xp1-mvplanner.log 2>&1 &



