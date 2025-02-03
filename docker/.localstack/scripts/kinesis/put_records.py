import boto3
from datetime import datetime
import json
import os
import random
import time


STREAM_NAME = os.getenv("KINESIS_STREAM_NAME")
STREAM_ARN = f"arn:aws:kinesis:us-east-1:000000000000:stream/{STREAM_NAME}"
SLEEP_SECONDS = os.getenv("SLEEP_SECONDS", 10)


client = boto3.client("kinesis")


def generate_payload():
    return json.dumps(
        {
            "ts": datetime.now(),
            "productId": random.randint(1, 3),
            "customerId": random.randint(1, 3),
            "qty": random.randint(1, 100),
        },
        default=str,
    ).encode(encoding="utf-8")


while True:

    response = client.put_records(
        Records=[
            {"Data": generate_payload(), "PartitionKey": str(random.randint(0, 100))}
            for x in range(1, 10)
        ],
        StreamARN=STREAM_ARN,
    )

    print(response)
    time.sleep(SLEEP_SECONDS)
