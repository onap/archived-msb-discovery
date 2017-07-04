#!/bin/bash
#
# Copyright 2016 ZTE, Inc. and others.
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

APP_INFO=msb-discover
APP_CMD=stop.sh

echo "### Stopping $APP_INFO...";

cd $RUNHOME

echo "### Stopping $APP_INFO..."
for i in `find -name stop.sh` 
do 
echo "exec" $i
$i 
sleep 1;
done

echo "Closing signal has been sent!";
echo "Stopping in background,wait for a moment";
