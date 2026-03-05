#!/bin/bash
# Script to manage the PR Tracker Spring Boot JAR
# Usage: ./manage_prtracker.sh start|stop|status

JAR_NAME="demo-0.0.1-SNAPSHOT.jar"
JAR_PATH="$(dirname "$0")/target/$JAR_NAME"
PID_FILE="prtracker.pid"
LOG_FILE="prtracker.log"
PORT=9090

start() {
  if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
    echo "PR Tracker is already running (PID $(cat $PID_FILE))"
    exit 0
  fi
  echo "Starting PR Tracker..."
  nohup java -jar "$JAR_PATH" --server.port=$PORT > "$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"
  echo "Started with PID $! (log: $LOG_FILE)"
}

stop() {
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 $PID 2>/dev/null; then
      echo "Stopping PR Tracker (PID $PID)..."
      kill $PID
      rm -f "$PID_FILE"
      echo "Stopped."
    else
      echo "Process not running, removing stale PID file."
      rm -f "$PID_FILE"
    fi
  else
    echo "PR Tracker is not running."
  fi
}

status() {
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 $PID 2>/dev/null; then
      echo "PR Tracker is running (PID $PID)"
    else
      echo "PR Tracker PID file exists but process is not running."
    fi
  else
    echo "PR Tracker is not running."
  fi
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  status)
    status
    ;;
  *)
    echo "Usage: $0 {start|stop|status}"
    exit 1
    ;;
esac

