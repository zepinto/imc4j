#!/bin/sh
# @author Paulo Dias

PATH=/sbin:/bin:/usr/sbin:/usr/bin:$PATH

NAME="Distress"
RUN_HOME="/opt/lsts/distress"
JARNAME="Distress.jar"
CONF_FILE="$RUN_HOME/distress.props"

JAVA="/opt/lsts/jre/bin/java"
DATE=`date +%Y-%m-%d_%H-%M-%S`
OUTPUT="$RUN_HOME/log/$DATE.log"
LATEST="$RUN_HOME/latest.log"
PIDFILE="$RUN_HOME/pid"

start()
{
    echo "Starting $Name"

    check_if_any_running

    mkdir -p "$RUN_HOME/log"
    unlink $LATEST 2>/dev/null
    cd "$RUN_HOME"
    echo "Running with configuration $CONF_FILE..."
    $JAVA -jar $JARNAME $CONF_FILE < /dev/null > $OUTPUT 2>&1 &
    pid=$!
    # pid=$(ps | grep $JARNAME | grep java | awk '{print $1}' c={1:-1})
    echo $pid > $PIDFILE
    echo "PID "$pid
    ln -s $OUTPUT $LATEST
}

check_if_any_running()
{
    ps | grep $JARNAME | grep java >/dev/null 2>&1
    status=$?
    if [ $status -eq 0 ]; then
        echo "$NAME is running."
        echo "Possible PIDs are: " $(ps | grep $JARNAME | grep java | awk '{print $1}' c={1:-1})
        exit 4
    fi
    return $status
}

status()
{
    ps | grep java | grep $JARNAME | grep $(cat $PIDFILE 2>/dev/null) >/dev/null 2>&1
    status=$?
    if [ $status -eq 0 ]; then
        echo "$NAME is running for PID "$(cat $PIDFILE)"."
    else
        echo "$NAME is not running for PID "$(cat $PIDFILE 2>/dev/null)"."
        echo "Possible PIDs are: " $(ps | grep $JARNAME | grep java | awk '{print $1}' c={1:-1})
    fi
}

stop()
{
    echo "Stopping $Name"
    ps | grep $JARNAME | grep java | grep $(cat $PIDFILE 2>/dev/null) >/dev/null 2>&1
    status=$?
    if [ $status -eq 0 ]; then
        NPID=$(cat $PIDFILE) && kill -15 $NPID
        echo "Stopping $Name end"
    else
        echo "$NAME is not running for PID "$(cat $PIDFILE 2>/dev/null)"."
    fi
    rm -f $PIDFILE
}

cd "$RUN_HOME"

case $1 in
    tail)
        status
        echo
        echo "Tailing  $LATEST -> $OUTPUT"
        tail -f $LATEST
        ;;
    status)
        status
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        echo "Waiting 5s before running start..."
        sleep 5
        start
        ;;
    start)
        start
        ;;
    *)
        echo "Usage: $0 {start|restart|stop|status|tail}"
        exit 2
        ;;
esac
