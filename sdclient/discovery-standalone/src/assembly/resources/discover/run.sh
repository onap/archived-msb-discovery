#!/bin/sh
#
# Copyright 2016-2017 ZTE, Inc. and others.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


DIRNAME=`dirname $0`
RUNHOME=`cd $DIRNAME/; pwd`
echo @RUNHOME@ $RUNHOME

if [ -f "$RUNHOME/setenv.sh" ]; then
  . "$RUNHOME/setenv.sh"
else
echo "can not found $RUNHOME/setenv.sh"
fi

# set CONSUL_IP in ha mode
if [ -n "${HA_MODE}" ]; then
        export CONSUL_IP=`cat /root/consul.ip`
		export CONSUL_REGISTER_MODE="catalog"
fi

echo ================== ENV_INFO  =============================================
echo @RUNHOME@  $RUNHOME
echo @JAVA_HOME@  $JAVA_HOME
echo @Main_Class@  $Main_Class
echo @APP_INFO@  $APP_INFO
echo @Main_JAR@  $Main_JAR
echo @Main_Conf@ $Main_Conf
echo @CONSUL_IP@ ${CONSUL_IP}
echo ==========================================================================

echo start $APP_INFO ...

JAVA="$JAVA_HOME/bin/java"

JAVA_VERSION=`$JAVA -version 2>&1 |awk 'NR==1{ sub(/"/,""); print substr($3,1,3)}'`
echo @JAVA_VERSION@ $JAVA_VERSION

MAXIMUM_HEAP_SIZE=128m
echo @MAXIMUM_HEAP_SIZE@ ${MAXIMUM_HEAP_SIZE}

if [ $JAVA_VERSION = "1.8" ]
then
    JAVA_OPTS="-Xms16m -Xmx${MAXIMUM_HEAP_SIZE} -XX:+UseSerialGC -XX:MaxMetaspaceSize=64m -XX:NewRatio=2"
else
    JAVA_OPTS="-Xms16m -Xmx${MAXIMUM_HEAP_SIZE} -XX:+UseSerialGC -XX:MaxPermSize=64m -XX:NewRatio=2"
fi

port=8789

CLASS_PATH="$RUNHOME/:$RUNHOME/$Main_JAR"

echo ================== RUN_INFO  =============================================
echo @JAVA_HOME@ $JAVA_HOME
echo @JAVA@ $JAVA
echo @JAVA_OPTS@ $JAVA_OPTS
echo @CLASS_PATH@ $CLASS_PATH
echo @EXT_DIRS@ $EXT_DIRS
echo ==========================================================================

cd $RUNHOME
echo @JAVA@ $JAVA
echo @JAVA_CMD@
"$JAVA" $JAVA_OPTS -classpath "$CLASS_PATH" $Main_Class server "$RUNHOME/$Main_Conf"


