# Dockerfile - msb-discover
FROM nexus3.onap.org:10003/onap/msb/msb_base:latest
COPY msb-discover*.tar.gz /usr/src

RUN tar -xzf /usr/src/msb-discover*.tar.gz -C /usr/local --strip-components=1; \
	rm /usr/src/msb-discover*.tar.gz
	
WORKDIR /usr/local
EXPOSE 10081
ENTRYPOINT exec $PWD/startup4docker.sh