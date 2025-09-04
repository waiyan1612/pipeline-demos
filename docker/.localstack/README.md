# LocalStack

A docker-compose variant of localstack so we can control the localstack version. Alternatively, you can install localstack cli and awslocal.

## Set up LocalStack

1. Start localstack container and awscli containers.
   ```shell
   docker compose -f docker/localstack.yaml up
   ```
2. Log into awscli container.
   ```shell
   docker container exec -it  docker-awscli-1 /bin/bash
   aws sts get-caller-identity
   ```
   
3. Create streams
   ```shell
   aws kinesis create-stream --stream-name stream-a --shard-count 1
   aws kinesis create-stream --stream-name stream-b --shard-count 1
   aws kinesis create-stream --stream-name stream-ab --shard-count 1
   ```

4. Write records
   ```shell
   aws kinesis put-record --stream-name stream-a --partition-key 1 --cli-binary-format raw-in-base64-out --data '{"id":"1", "value":"one"}'
   aws kinesis put-record --stream-name stream-a --partition-key 1 --cli-binary-format raw-in-base64-out --data '{"id":"2", "value":"two"}'
   aws kinesis put-record --stream-name stream-b --partition-key 1 --cli-binary-format raw-in-base64-out --data '{"id":"1", "value":"oneone"}'
   aws kinesis put-record --stream-name stream-b --partition-key 1 --cli-binary-format raw-in-base64-out --data '{"id":"2", "value":"twotwo"}'
   ```
