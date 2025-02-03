import boto3
import os
import time


STREAM_NAME = os.getenv("KINESIS_STREAM_NAME")
STREAM_ARN = f"arn:aws:kinesis:us-east-1:000000000000:stream/{STREAM_NAME}"
SLEEP_SECONDS = os.getenv("SLEEP_SECONDS", 10)

client = boto3.client("kinesis")


def get_shard_ids(stream_name):
    resp = client.describe_stream(StreamARN=stream_name)
    return [shard["ShardId"] for shard in resp["StreamDescription"]["Shards"]]


def get_shard_iter(shard_id, stream_name):
    shard_iter = client.get_shard_iterator(
        StreamARN=stream_name,
        ShardId=shard_id,
        ShardIteratorType="LATEST",  # Can also use 'TRIM_HORIZON' for oldest records
    )["ShardIterator"]
    return shard_id, shard_iter


shard_ids = get_shard_ids(STREAM_ARN)
shard_id_iter_list = [get_shard_iter(shard_id, STREAM_ARN) for shard_id in shard_ids]


while True:

    for shard_id, shard_iterator in shard_id_iter_list:
        response = client.get_records(
            StreamARN=STREAM_ARN,
            ShardIterator=shard_iterator,
            Limit=10,  # Max number of records per request
        )

        for record in response["Records"]:
            data = record["Data"].decode("utf-8")
            print(f"Received record: {data} from {shard_id}")

        # Update shard iterator for next iteration
        if response["NextShardIterator"]:
            shard_id_iter_list.append((shard_id, response["NextShardIterator"]))

        time.sleep(SLEEP_SECONDS)
