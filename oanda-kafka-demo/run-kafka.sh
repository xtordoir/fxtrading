#!/bin/bash

# these are required to run on boot2docker
#$(boot2docker shellinit)
#export DOCKER_IP=`boot2docker ip`
#export DOCKER_IP=localhost
echo "starting zookeeper"
docker run -d -p 2181:2181 --name zookeeper jplock/zookeeper:3.4.6
# might require a bit of sleep here...
sleep 20

echo "starting kafka"
docker run -d -p 9092:9092 -p 7203:7203 --env EXPOSED_HOST=$DOCKER_IP --env ZOOKEEPER_IP=$DOCKER_IP --name kafka --link zookeeper:zookeeper ches/kafka

sleep 10

echo "creating topics"
ZK_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' zookeeper)
KAFKA_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' kafka)

docker run --rm ches/kafka kafka-topics.sh --create --topic test --replication-factor 1 --partitions 1 --zookeeper $ZK_IP:2181
docker run --rm ches/kafka kafka-topics.sh --create --topic ticks --replication-factor 1 --partitions 1 --zookeeper $ZK_IP:2181
docker run --rm ches/kafka kafka-topics.sh --create --topic heartbeats --replication-factor 1 --partitions 1 --zookeeper $ZK_IP:2181

#docker run --rm --interactive ches/kafka kafka-console-producer.sh --topic test --broker-list $KAFKA_IP:9092

#docker run --rm ches/kafka kafka-console-consumer.sh --topic ticks --from-beginning --zookeeper $ZK_IP:2181
#docker run --rm ches/kafka kafka-console-consumer.sh --topic test --from-beginning --zookeeper $ZK_IP:2181

#docker rm -f $(docker ps -aq)
