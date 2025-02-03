# LocalStack

A docker-compose variant of localstack so we can control the localstack version. Alternatively, you can install localstack cli and awslocal.



## Set up LocalStack

1. Start kafka container.
   ```shell
   docker compose -f docker/kafka.yaml up
   ```
2. Test connectivity using console producer and consumer.
   ```shell
   docker container exec -it kafka-standalone /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic random
   docker container exec -it kafka-standalone /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic random
   ```
