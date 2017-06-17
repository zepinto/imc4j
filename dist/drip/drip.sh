CONF_FILE=apdl.props
JAVA=/usr/bin/java
DATE=`date +%Y-%m-%d_%H-%M-%S`
OUTPUT="$DATE.log"
$JAVA -jar Drip.jar $CONF_FILE < /dev/null > $OUTPUT 2>&1 &
ln -s $OUTPUT latest.log
