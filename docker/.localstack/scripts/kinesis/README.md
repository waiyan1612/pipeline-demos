# Kinesis

## Prerequisites

1. Get [pyenv](https://github.com/pyenv/pyenv). Install python 3.13 if not installed yet.
    ```commandline
    pyenv install 3.13
    ```
2. Set up `venv`.
    ```commandline
    python -m venv venv
    source venv/bin/activate
    pip install -r requirements.txt
    ```

3. Download spark-streaming jar from [AWS Labs](https://github.com/awslabs/spark-sql-kinesis-connector)
    ```commandline
   SCALA_VERSION=2.12
   JAR_VERSION=1.4.1
   wget --directory-prefix=spark/lib \
       https://awslabs-code-us-east-1.s3.amazonaws.com/spark-sql-kinesis-connector/spark-streaming-sql-kinesis-connector_$SCALA_VERSION-$JAR_VERSION.jar
    ```
   
## Scripts

### Create a stream

```shell
docker container exec -it  docker-awscli-1 /bin/bash
aws kinesis create-stream --endpoint-url http://localstack:4566 --stream-name $KINESIS_STREAM_NAME 
```

### Read records

awscli base image is still using python 2 so we will be running these scripts from local env. See https://github.com/aws/aws-cli/issues/5100/.

```shell
source venv/bin/activate && set -a && source docker/.localstack/.env && set +a
python docker/.localstack/scripts/kinesis/read_records.py
```

### Put records

awscli base image is still using python 2 so we will be running these scripts from local env. See https://github.com/aws/aws-cli/issues/5100.

```shell
source venv/bin/activate && set -a && source docker/.localstack/.env && set +a
python docker/.localstack/scripts/kinesis/put_records.py
```
