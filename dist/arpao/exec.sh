#!/bin/sh

ARPAO_HOME=/opt/lsts/arpao
CONF_FILE="$ARPAO_HOME/np3.props"
JAVA="$ARPAO_HOME/jre/bin/java"
DATE=`date +%Y-%m-%d_%H-%M-%S`
OUTPUT="$ARPAO_HOME/log/$DATE.log"
mkdir -p log
rm -f latest.log

cd $ARPAO_HOME
echo "Running with configuration $CONF_FILE..."
$JAVA -jar ArpaoExec.jar $CONF_FILE < /dev/null > $OUTPUT 2>&1 &
ln -s $OUTPUT latest.log

