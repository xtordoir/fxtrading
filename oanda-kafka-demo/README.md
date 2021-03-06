## Oanda streaming through kafka: demo of stream consumed by SparkNotebook

To run, follow instructions:

### Run the kafka/zookeeper containers

You need to set-up the docker ip, either ```localhost``` or ````boot2docker ip```` if using boot2docker:
```
#export DOCKER_IP=`boot2docker ip`
export DOCKER_IP=localhost
```

Then run the script:

```
./run-kafka.sh
```


### Compile the ticksfeeder:


```
./compile-ticksfeed.sh
```

This creates an assembly copied in the ```target```  directory.

### Have the credentials ready:

Copy your oandaapi.conf file in ```target```

### build the ticksfeeder container image:

docker build -t xtordoir/ticksfeeder .


### Run the ticksfeeder

Export the ```KAFKA_IP``` variable and run the container (EUR_USD and GBP_USD here):

```
#export KAFKA_IP=`boot2docker ip`
export KAFKA_IP=localhost
docker run -t -i -e "KAFKA_IP=$KAFKA_IP" -e "INSTRUMENTS=EUR_USD,GBP_USD" xtordoir/ticksfeeder:latest
```

### The notebook is then available for playing

It has dependencies: ```fx-thrift-kafka``` and ```oanda``` to be installed in m2:

```
cd ..
sbt fxthriftkafka/publish
sbt soanda/publish
```
