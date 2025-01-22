#!/bin/sh
SRC_SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SRC_SOURCE" ]; do
  SCR_HOME_DIR="$( cd -P "$( dirname "$SRC_SOURCE" )" && pwd )"
  SRC_SOURCE="$(readlink "$SRC_SOURCE")"
  [[ $SRC_SOURCE != /* ]] && SRC_SOURCE="$SCR_HOME_DIR/$SRC_SOURCE" # if $SRC_SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
SCR_HOME_DIR="$( cd -P "$( dirname "$SRC_SOURCE" )" && pwd )"

CURRENT_DIR=$SCR_HOME_DIR

GRADLE_FILE="${CURRENT_DIR}/build.gradle"
JAVA_LIBS_DIR="${CURRENT_DIR}"

[ -f $GRADLE_FILE ] && JAVA_LIBS_DIR="${CURRENT_DIR}/build/libs"

CLASSPATH=""

for i in $(ls $JAVA_LIBS_DIR/*.jar)
do
    CLASSPATH=$(printf "${jar_op}${i}:${CLASSPATH}")
done

# java -splash:$JAVA_LIBS_DIR/splash.gif -cp $CLASSPATH -Dpool.size=100 ui.FhirValidacaoWindow
java $@ -splash:$JAVA_LIBS_DIR/splash.gif -cp $CLASSPATH ui.FhirValidacaoWindow
