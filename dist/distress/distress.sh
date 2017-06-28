DISTRESS_HOME=/opt/lsts/distress
CONF_FILE="$DISTRESS_HOME/distress.props"
JAVA="$DISTRESS_HOME/jre/bin/java"
DATE=`date +%Y-%m-%d_%H-%M-%S`
OUTPUT="$DISTRESS_HOME/log/$DATE.log"
mkdir -p log
rm -f latest.log

cd $DISTRESS_HOME
echo "Running with configuration $CONF_FILE..."
$JAVA -jar Distress.jar $CONF_FILE < /dev/null > $OUTPUT 2>&1 &
ln -s $OUTPUT latest.log

