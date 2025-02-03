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
   ```
3. Run AWS commands.
   ```shell
   aws sts get-caller-identity
   ```
