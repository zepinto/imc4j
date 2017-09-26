#!/bin/sh
# @author Paulo Dias

PATH=/sbin:/bin:/usr/sbin:/usr/bin:$PATH

NAME="Drip"
RUN_HOME="/opt/lsts/drip"
JARNAME="Drip.jar"
SERVER_PORT="80"
CLASS_BACKSEAT="pt.lsts.backseat.drip.RiverPlumeTracker"
# To use hot config put "--hot-config" or leave it empty
HOT_CONFIG="--hot-config"

CLASS_SERVER="pt.lsts.httpd.BackSeatServer"
JAVA="/opt/lsts/jre/bin/java"
DATE=`date +%Y-%m-%d_%H-%M-%S`
OUTPUT="$RUN_HOME/log/$DATE.log"
LATEST="$RUN_HOME/latest.log"
PIDFILE="$RUN_HOME/pid"

start()
{
    echo "Starting with back seat server $Name with back seat server on port $SERVER_PORT"

    check_if_any_running

    mkdir -p "$RUN_HOME/log"
    unlink $LATEST 2>/dev/null
    cd "$RUN_HOME"
    echo "Running $CLASS_SERVER for ..."
    $JAVA -cp .:$JARNAME $CLASS_SERVER $CLASS_BACKSEAT $SERVER_PORT $HOT_CONFIG < /dev/null > $OUTPUT 2>&1 &
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
        echo "$NAME is running with back seat server."
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
        echo "$NAME is running for PID "$(cat $PIDFILE)" with back seat server."
    else
        echo "$NAME is not running for PID "$(cat $PIDFILE 2>/dev/null)" with back seat server."
        echo "Possible PIDs are: " $(ps | grep $JARNAME | grep java | awk '{print $1}' c={1:-1})
    fi
}

stop()
{
    PIDS=$(ps | grep $JARNAME | grep java | awk '{print $1}' c={1:-1});
    if [ -z "$PIDS" ]; then
        echo "$NAME is not running with back seat server"
        return
    fi

    while [ 1 ]; do
        echo "Stopping with back seat server $NAME with PIDS $PIDS"
        kill $PIDS > /dev/null 2>&1

        for r in 0 1 2 3 4 5 6 7 8 9; do
            PIDS=$(ps | grep $JARNAME | grep java | awk '{print $1}' c={1:-1});
            if [ -z "$PIDS" ]; then
                echo "Stopped $NAME"
                rm -f $PIDFILE
                return 0
            else
                echo "Waiting for process to exit ($r)...  PIDS $PIDS"
                sleep 1
            fi
        done

        PIDS=$(ps | grep $JARNAME | grep java | awk '{print $1}' c={1:-1});
        echo "Forcing exit...  PIDS $PIDS"
        kill -9 $PIDS > /dev/null 2>&1
        sleep 1
        PIDS=$(ps | grep $JARNAME | grep java | awk '{print $1}' c={1:-1});
    done
    rm -f $PIDFILE
}

cd "$RUN_HOME"

case "$0" in
    *services*)
        return
        ;;
esac;

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
