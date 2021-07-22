#!/bin/sh
# @author Paulo Dias

PATH=/sbin:/bin:/usr/sbin:/usr/bin:$PATH

JARNAME="Distress.jar"

PRG="$0"
fillBASE_DIR()
{
  readlink -f "$PRG" &> /dev/null
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

NAME="$(basename "$PRG")"
RUN_HOME="$BASE_DIR"

CONF_FILE="$RUN_HOME/$(echo "$NAME" | sed  's/\..*$//').props"

JAVA="/opt/lsts/jre/bin/java"
"$JAVA" -version &> /dev/null
status=$?
if [ $status -ne 0 ]; then
  JAVA="$RUN_HOME/jre/bin/java"
  "$JAVA" -version &> /dev/null
  status=$?
  if [ $status -ne 0 ]; then
    JAVA="java"
  fi
fi

DATE=$(date +%Y-%m-%d_%H-%M-%S)
OUTPUT="$RUN_HOME/log/$DATE.log"
LATEST="$RUN_HOME/latest.log"
PIDFILE="$RUN_HOME/pid"

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

    mkdir -p "$RUN_HOME/log"
    unlink "$LATEST" 2>/dev/null
    cd "$RUN_HOME"
    echo "Running with configuration '$CONF_FILE'..."
    "$JAVA" -jar $JARNAME "$CONF_FILE" < /dev/null > $OUTPUT 2>&1 &
    pid=$!
    # pid=$($PSCMD | grep $JARNAME | grep java | awk '{print $1}' c={1:-1})
    echo $pid > "$PIDFILE"
    echo "PID "$pid
    ln -s "$OUTPUT" "$LATEST"
}

wait_for_clock()
{
   while [ $(date +%s) -lt 1507641301 ]; do
     echo "Waiting for the clock to be synchronized... ($date)"
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
