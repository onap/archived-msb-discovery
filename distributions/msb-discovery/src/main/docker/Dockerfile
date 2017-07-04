FROM openjdk:8.0.72
ENV BASE /opt/application/msb-discover
ENV LD_LIBRARY_PATH /lib64
ADD msb-discovery*.tar.gz /opt/application
WORKDIR /opt/application/msb-discovery
EXPOSE 10081
ENTRYPOINT exec $PWD/startup4docker.sh