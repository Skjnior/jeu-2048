#!/bin/sh

##############################################################################
# Gradle start-up script
##############################################################################

# Resolve links
app_path=$0
while [ -h "$app_path" ]; do
  ls=$(ls -ld "$app_path")
  link=${ls##*-> }
  case $link in
    /*) app_path=$link ;;
    *) app_path=$(dirname "$app_path")/$link ;;
  esac
done

APP_HOME=$(cd "$(dirname "$app_path")" && pwd -P)
APP_NAME="Gradle"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD=java
fi

exec "$JAVACMD" ${JAVA_OPTS} -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
