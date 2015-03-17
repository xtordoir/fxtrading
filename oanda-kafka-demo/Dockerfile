FROM relateiq/oracle-java7

# me for instance...
MAINTAINER xavier xtordoir@data-fellas.guru

# copy the locally built fat-jar to the image
ADD target/ticksfeed-assembly-0.1-SNAPSHOT.jar /app/server.jar

# copy the credentials file for oanda API
ADD target/oandaapi.conf /root/.oandaapi/oandaapi.conf

ENV INSTRUMENTS EUR_USD
ENV KAFKA_IP localhost

# run the server when a container based on this image is being run
ENTRYPOINT java -jar /app/server.jar  $INSTRUMENTS
