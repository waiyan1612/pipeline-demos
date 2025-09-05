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
   
3. Create streams inside the container
   ```shell
   aws kinesis create-stream --stream-name stream-a --shard-count 1
   aws kinesis create-stream --stream-name stream-b --shard-count 1
   aws kinesis create-stream --stream-name stream-ab --shard-count 1
   ```

4. Write records inside the container
   ```shell
   aws kinesis put-record --stream-name stream-a --partition-key 1 --cli-binary-format raw-in-base64-out --data '{"id":"1", "value":"one"}'
   aws kinesis put-record --stream-name stream-a --partition-key 1 --cli-binary-format raw-in-base64-out --data '{"id":"2", "value":"two"}'
   aws kinesis put-record --stream-name stream-b --partition-key 1 --cli-binary-format raw-in-base64-out --data '{"id":"1", "value":"oneone"}'
   aws kinesis put-record --stream-name stream-b --partition-key 1 --cli-binary-format raw-in-base64-out --data '{"id":"2", "value":"twotwo"}'
   ```

5. Read records inside the container
   ```shell
   STREAM_NAME=stream-ab
   ITER=$(aws kinesis get-shard-iterator --stream-name $STREAM_NAME --shard-iterator-type TRIM_HORIZON --shard-id shardId-000000000000 --query 'ShardIterator' --output text)
   while [ "$ITER" != "null" ]; do
      RECORDS_JSON=$(aws kinesis get-records --shard-iterator "$ITER" --limit 100 --output json)
      ITER=$(echo "$RECORDS_JSON" | grep -oP '(?<="NextShardIterator": ")[^"]+')
      echo "$RECORDS_JSON" | grep -oP '(?<="Data": ")[^"]+' | while read line; do
        echo "$line" | base64 --decode
        echo
      done
      sleep 1
   done
   ```

6. Delete streams inside the container
   ```shell
   aws kinesis delete-stream --stream-name stream-ab
   ```
