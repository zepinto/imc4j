#!/bin/sh
# @author Paulo Dias

PATH=/sbin:/bin:/usr/sbin:/usr/bin:$PATH

## Uncomment to override the default ("<script_name>.props")
## (Don't use a path, it must be on the script folder.)
#CONF_FILE_NAME="conf.props"

## Settings to run with an HTTP server
## Uncomment the following lines will result on running as HTTP server
## Some backseats don't support this way of running
#RUN_AS_HTTP_SERVER="yes"
## Set the http port to serve html
# SERVER_PORT="8090"
## To use hot config put "--hot-config" or leave it empty
#HOT_CONFIG="--hot-config"

## -- No need to change beyond this point -- ##
JARNAME="Distress.jar"

# Settings to run with an HTTP server
CLASS_BACKSEAT="pt.lsts.backseat.distress.DistressSurvey"
if [ -z ${SERVER_PORT+x} ]; then
  SERVER_PORT="8090"
fi

PRG="$0"
fillBASE_DIR()
{
  readlink -f "$PRG" > /dev/null 2>&1
  status=$?
  if [ $status -eq 0 ]; then
      PRG="$(readlink -f "$PRG")"
  else
    while [ -h "$PRG" ] ; do
      ls=`ls -ld "$PRG"`
      link=`expr "$ls" : '.*-> \(.*\)$'`
      if expr "$link" : '/.*' > /dev/null; then
          PRG="$link"
      else
          PRG=`dirname "$PRG"`"/$link"
      fi
    done
  fi
  BASE_DIR="$(cd "$(dirname "$PRG")/" && pwd -P)"
}
fillBASE_DIR

PRG_NAME="$(basename "$PRG" | sed  's/\..*$//')"
NAME="$(echo "$PRG_NAME" | sed 's/[^ ]\+/\L\u&/g')"
RUN_HOME="$BASE_DIR"

if [ -z ${CONF_FILE_NAME} ]; then
  CONF_FILE="$RUN_HOME/$(echo "$PRG_NAME" | sed  's/\..*$//').props"
else
  CONF_FILE="$RUN_HOME/$CONF_FILE_NAME"
fi

JAVA="$RUN_HOME/jre/bin/java"
"$JAVA" -version > /dev/null 2>&1
status=$?
if [ $status -ne 0 ]; then
  JAVA="/opt/lsts/jre/bin/java"
  "$JAVA" -version > /dev/null 2>&1
  status=$?
  if [ $status -ne 0 ]; then
    JAVA="$JAVA_HOME/bin/java"
    "$JAVA" -version > /dev/null 2>&1
    status=$?
    if [ $status -ne 0 ]; then
      JAVA="java"
      which java || (echo "No Java found!!
If installed put it on the PATH or set the JAVA_HOME var.
A 'jre' folder is the preferred lookup for java, or the '/opt/lsts/jre/'." && exit 10)
    fi
  fi
fi

DATE=$(date +%Y-%m-%d_%H-%M-%S)
OUTPUT="$RUN_HOME/log/$DATE.log"
LATEST="$RUN_HOME/latest.log"
PIDFILE="$RUN_HOME/pid"

CLASS_SERVER="pt.lsts.httpd.BackSeatServer"

setupPsCmd()
{
    ps a >/dev/null 2>&1
    status=$?
    if [ $status -eq 0 ]; then
        PSCMD="ps a"
    else
        PSCMD="ps"
    fi
}

install()
{
    echo "Trying to install into '/etc/rc.d'. Possibly remount '/' as rw 'mount / -o remount,rw'"
    [ -e /etc/rc.d/ ] && echo "Installing $NAME at /etc/rc.d/" && ln -s "$(realpath $PRG)" "/etc/rc.d/${0%.*}"
}

start()
{
    echo "Starting $NAME"

    check_if_any_running
    wait_for_clock

    echo "Running with Java command: '$JAVA'..."

    mkdir -p "$RUN_HOME/log"
    unlink "$LATEST" 2>/dev/null
    cd "$RUN_HOME"
    if [ -z ${RUN_AS_HTTP_SERVER+x} ]; then
       echo "Running with configuration \n  -> '$CONF_FILE'..."
      "$JAVA" -jar $JARNAME "$CONF_FILE" < /dev/null > "$OUTPUT" 2>&1 &
      pid=$!
    else
      echo "Running $CLASS_SERVER for $NAME with configuration \n  -> '$CONF_FILE'..."
      "$JAVA" -cp .:"$JARNAME" $CLASS_SERVER $CLASS_BACKSEAT $SERVER_PORT \
            --config "$CONF_FILE" --log "$OUTPUT" $HOT_CONFIG < /dev/null > "$OUTPUT" 2>&1 &
      pid=$!
    fi
    echo $pid > "$PIDFILE"
    echo "$NAME running with PID "$pid
    ln -s "$OUTPUT" "$LATEST"
}

wait_for_clock()
{
   while [ $(date +%s) -lt 1507641301 ]; do
     echo "Waiting for the clock to be synchronized... ($(date)"
     sleep 1
   done
}

check_if_any_running()
{
    $PSCMD | grep $JARNAME | grep java >/dev/null 2>&1
    status=$?
    if [ $status -eq 0 ]; then
        echo "$NAME is running."
        echo "Possible PIDs are: " $($PSCMD | grep $JARNAME | grep java | awk '{print $1}' c={1:-1})
        exit 4
    fi
    return $status
}

status()
{
    $PSCMD | grep java | grep $JARNAME | grep $(cat $PIDFILE 2>/dev/null) >/dev/null 2>&1
    status=$?
    if [ $status -eq 0 ]; then
        echo "$NAME is running for PID "$(cat $PIDFILE)"."
    else
        echo "$NAME is not running for PID "$(cat $PIDFILE 2>/dev/null)"."
        echo "Possible PIDs are: " $($PSCMD | grep $JARNAME | grep java | awk '{print $1}' c={1:-1})
    fi
}

stop()
{
    PIDS=$($PSCMD | grep $JARNAME | grep java | awk '{print $1}' c={1:-1});
    if [ -z "$PIDS" ]; then
        echo "$NAME is not running"
        return
    fi

    while [ 1 ]; do
        echo "Stopping $NAME with PIDs $PIDS"
        kill $PIDS > /dev/null 2>&1

        for r in 0 1 2 3 4 5 6 7 8 9; do
            PIDS=$($PSCMD | grep $JARNAME | grep java | awk '{print $1}' c={1:-1});
            if [ -z "$PIDS" ]; then
                echo "Stopped $NAME"
                rm -f $PIDFILE
                return 0
            else
                echo "Waiting for process to exit ($r)...  PIDs $PIDS"
                sleep 1
            fi
        done

        PIDS=$($PSCMD | grep $JARNAME | grep java | awk '{print $1}' c={1:-1});
        echo "Forcing exit...  PIDs $PIDS"
        kill -9 $PIDS > /dev/null 2>&1
        sleep 1
        PIDS=$($PSCMD | grep $JARNAME | grep java | awk '{print $1}' c={1:-1});
    done
    rm -f $PIDFILE
}

setupPsCmd

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
        tail -f "$LATEST"
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
        echo "Usage: $0 {start|restart|stop|status|tail|install}"
        echo "  It will run '$JARNAME' with configuration '$CONF_FILE'"
        exit 2
        ;;
esac
