CONF_FILE=douro.props
JAVA=/usr/bin/java
DATE=`date +%Y-%m-%d_%H-%M-%S`
OUTPUT="log/$DATE.log"
mkdir -p log
rm -f latest.log

echo "Running with configuration $CONF_FILE..."
$JAVA -jar Drip.jar $CONF_FILE < /dev/null > $OUTPUT 2>&1 &
ln -s $OUTPUT latest.log
