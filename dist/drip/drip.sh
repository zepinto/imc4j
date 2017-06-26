DRIP_HOME=/opt/lsts/drip
CONF_FILE="$HOME/douro.props"
JAVA="$DRIP_HOME/jre/bin/java"
DATE=`date +%Y-%m-%d_%H-%M-%S`
OUTPUT="$DRIP_HOME/log/$DATE.log"
mkdir -p log
rm -f latest.log

cd $DRIP_HOME
echo "Running with configuration $CONF_FILE..."
$JAVA -jar Drip.jar $CONF_FILE < /dev/null > $OUTPUT 2>&1 &
ln -s $OUTPUT latest.log

