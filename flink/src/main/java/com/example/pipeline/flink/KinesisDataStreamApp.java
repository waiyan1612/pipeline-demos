package com.example.pipeline.flink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.aws.config.AWSConfigConstants;
import org.apache.flink.connector.aws.config.AWSConfigOptions;
import org.apache.flink.connector.kinesis.sink.KinesisStreamsSink;
import org.apache.flink.connector.kinesis.source.KinesisStreamsSource;
import org.apache.flink.connector.kinesis.source.config.KinesisSourceConfigOptions;
import org.apache.flink.formats.json.*;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.types.Row;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class KinesisDataStreamApp {

    public static String getAwsPropertiesForSql(Map<String, String> awsConfigs) {
        StringBuilder sb = new StringBuilder();
        awsConfigs.forEach((key, value) -> {
            // tweak keys: remove ".provider" from basic credentials
            if (key.startsWith("aws.credentials.provider.basic.")) {
                key = key.replace("aws.credentials.provider.basic.", "aws.credentials.basic.");
            }
            sb.append("'").append(key).append("' = '").append(value).append("',\n");
        });
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {

        Map<String, String> awsConfigs = Map.of(
            AWSConfigOptions.AWS_CREDENTIALS_PROVIDER_OPTION.key(), AWSConfigConstants.CredentialProvider.BASIC.name(),
            AWSConfigOptions.AWS_ACCESS_KEY_ID_OPTION.key(), "any",
            AWSConfigOptions.AWS_SECRET_ACCESS_KEY_OPTION.key(), "any",
            AWSConfigOptions.AWS_REGION_OPTION.key(), "us-east-1",
            AWSConfigOptions.AWS_ENDPOINT_OPTION.key(), "http://localhost:4566"
        );

        Configuration sourceConfig = Configuration.fromMap(awsConfigs);
        sourceConfig.set(KinesisSourceConfigOptions.STREAM_INITIAL_POSITION, KinesisSourceConfigOptions.InitialPosition.TRIM_HORIZON);
//        sourceConfig.set(KinesisSourceConfigOptions.STREAM_INITIAL_POSITION, KinesisSourceConfigOptions.InitialPosition.LATEST);

        // stream-a is loaded using Stream API
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        KinesisStreamsSource<PayloadA> kdsA = KinesisStreamsSource.<PayloadA>builder()
                .setSourceConfig(sourceConfig)
                .setStreamArn("arn:aws:kinesis:us-east-1:000000000000:stream/stream-a")
                .setDeserializationSchema(new JsonDeserializationSchema<>(PayloadA.class))
                .build();
        WatermarkStrategy<PayloadA> watermarkStrategyA = WatermarkStrategy.noWatermarks();
        DataStream<PayloadA> streamA = env.fromSource(kdsA, watermarkStrategyA, "Stream A")
                .returns(TypeInformation.of(PayloadA.class));
//        streamA.print(); // console sink

        // stream-b is loaded using Table API
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        tableEnv.executeSql(String.format("""
            CREATE TABLE table_b (
                id STRING,
                `value` STRING
            )
            WITH (
                %s
                'connector' = 'kinesis',
                'stream.arn' = 'arn:aws:kinesis:us-east-1:000000000000:stream/stream-b',
                'source.init.position' = 'TRIM_HORIZON',
                'format' = 'json'
            );
            """, getAwsPropertiesForSql(awsConfigs)
        ));

        // Convert stream a from data stream to table
        Table tableA = tableEnv.fromDataStream(streamA).as("id", "val");
        tableEnv.createTemporaryView("table_a", tableA);

        String joinQuery = """
                SELECT a.id, a.val as a_val, b.val as b_val
                FROM
                    (SELECT id, val FROM table_a) a
                LEFT JOIN
                    (SELECT id, `value` as val FROM table_b) b
                ON a.id = b.id
            """;

        /*
            This is an attempt to write to table_ab directly from Table API but this throws
            Table sink 'default_catalog.default_database.table_ab' doesn't support consuming update and delete changes
            which is produced by node Join(joinType=[LeftOuterJoin], where=[(id = id0)], select=[id, val, id0, value],
            leftInputSpec=[NoUniqueKey], rightInputSpec=[NoUniqueKey])
         */

//        tableEnv.executeSql(String.format("""
//            CREATE TABLE table_ab (
//                id STRING,
//                a_val STRING,
//                b_val STRING
//            )
//            WITH (
//                %s
//                'connector' = 'kinesis',
//                'stream.arn' = 'arn:aws:kinesis:us-east-1:000000000000:stream/stream-ab',
//                'format' = 'json'
//            );
//            """, getAwsPropertiesForSql(awsConfigs)
//        ));
//        tableEnv.executeSql("INSERT INTO table_ab " + joinQuery);


        // This will create a changelog stream with +I, -D etc
        Table tableAB = tableEnv.sqlQuery(joinQuery);
        Schema schema = Schema.newBuilder().fromResolvedSchema(tableAB.getResolvedSchema()).build();
        DataStream<Row> changelogStream = tableEnv.toChangelogStream(tableAB, schema, ChangelogMode.all());
        changelogStream.print();

        Properties kinesisClientProperties = new Properties();
        kinesisClientProperties.putAll(awsConfigs);
        SerializationSchema<Row> jsonSerializer = new JacksonRowJsonSerializer(tableAB.getResolvedSchema().getColumnNames().toArray(String[]::new));
        KinesisStreamsSink<Row> kdsAB = KinesisStreamsSink.<Row>builder()
                .setKinesisClientProperties(kinesisClientProperties)
                .setStreamArn("arn:aws:kinesis:us-east-1:000000000000:stream/stream-ab")
                .setSerializationSchema(jsonSerializer)
                .setPartitionKeyGenerator(row -> Objects.requireNonNull(row.getField("id")).toString())
                .build();
        changelogStream.sinkTo(kdsAB);

        env.execute("Kinesis Example");
    }
}
