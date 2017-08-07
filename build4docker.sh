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

echo @JAVA_HOME@ $JAVA_HOME

#build
mvn clean install 



#cooy

RELEASE_BASE_DIR=$RUNHOME/release
CONSUL_RELEASE_DIR=${RELEASE_BASE_DIR}/msb-consul

rm -rf $CONSUL_RELEASE_DIR
mkdir  $CONSUL_RELEASE_DIR -p

DOCKER_RUN_NAME=msb_consul
DOCKER_IMAGE_NAME=msb_consul
DOCKER_RELEASE_VERSION=0.7.5

cp -r $RUNHOME/consul/target/version/* ${CONSUL_RELEASE_DIR}
cp  $RUNHOME/ci/build_docker_image.sh ${CONSUL_RELEASE_DIR}
#build docker image
cd ${CONSUL_RELEASE_DIR}
chmod 777 build_docker_image.sh

#clear old version
docker rm -f ${DOCKER_RUN_NAME}
docker rmi ${DOCKER_IMAGE_NAME}:${DOCKER_RELEASE_VERSION}


./build_docker_image.sh -n=${DOCKER_IMAGE_NAME} -v=${DOCKER_RELEASE_VERSION} -d=./docker

#docker run
docker run -d --net=host  --name ${DOCKER_RUN_NAME} ${DOCKER_IMAGE_NAME}:${DOCKER_RELEASE_VERSION}
docker ps |grep ${DOCKER_RUN_NAME} 



RELEASE_DIR=${RELEASE_BASE_DIR}/msb-discovery
DOCKER_RUN_NAME=msb_discovery
DOCKER_IMAGE_NAME=msb_discovery
DOCKER_RELEASE_VERSION=latest

rm -rf $RELEASE_DIR
mkdir  $RELEASE_DIR -p


cp -r $RUNHOME/distributions/msb-discovery/target/version/* ${RELEASE_DIR}
cp  $RUNHOME/ci/build_docker_image.sh ${RELEASE_DIR}
#build docker image
cd ${RELEASE_DIR}
chmod 777 build_docker_image.sh

docker rm -f ${DOCKER_RUN_NAME}
docker rmi ${DOCKER_IMAGE_NAME}:${DOCKER_RELEASE_VERSION}


./build_docker_image.sh -n=${DOCKER_IMAGE_NAME} -v=${DOCKER_RELEASE_VERSION} -d=./docker

#docker run

docker run -d --net=host  --name ${DOCKER_RUN_NAME} ${DOCKER_IMAGE_NAME}:${DOCKER_RELEASE_VERSION}
docker ps |grep ${DOCKER_RUN_NAME} 
