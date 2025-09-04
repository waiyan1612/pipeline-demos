package com.example.pipeline.flink;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.aws.config.AWSConfigConstants;
import org.apache.flink.connector.aws.config.AWSConfigOptions;
import org.apache.flink.connector.kinesis.source.KinesisStreamsSource;
import org.apache.flink.connector.kinesis.source.config.KinesisSourceConfigOptions;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.connector.ChangelogMode;

import java.util.Map;

public class KinesisDataStreamApp {
    public static void main(String[] args) throws Exception {

        String kdsArnPrefix = "arn:aws:kinesis:us-east-1:000000000000:stream/";

        Map<String, String> awsCredentials = Map.of(
            AWSConfigOptions.AWS_CREDENTIALS_PROVIDER_OPTION.key(), AWSConfigConstants.CredentialProvider.BASIC.name(),
            AWSConfigOptions.AWS_ACCESS_KEY_ID_OPTION.key(), "any",
            AWSConfigOptions.AWS_SECRET_ACCESS_KEY_OPTION.key(), "any",
            AWSConfigOptions.AWS_REGION_OPTION.key(), "us-east-1",
            AWSConfigOptions.AWS_ENDPOINT_OPTION.key(), "http://localhost:4566"
        );

        Configuration sourceConfig = Configuration.fromMap(awsCredentials);
        sourceConfig.set(KinesisSourceConfigOptions.STREAM_INITIAL_POSITION, KinesisSourceConfigOptions.InitialPosition.TRIM_HORIZON);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        KinesisStreamsSource<PayloadA> kdsA = KinesisStreamsSource.<PayloadA>builder()
                .setSourceConfig(sourceConfig)
                .setStreamArn(kdsArnPrefix + "stream-a")
                .setDeserializationSchema(new JsonDeserializationSchema<>(PayloadA.class))
                .build();
        KinesisStreamsSource<PayloadB> kdsB = KinesisStreamsSource.<PayloadB>builder()
                .setSourceConfig(sourceConfig)
                .setStreamArn(kdsArnPrefix + "stream-b")
                .setDeserializationSchema(new JsonDeserializationSchema<>(PayloadB.class))
                .build();


        WatermarkStrategy<PayloadA> watermarkStrategyA = WatermarkStrategy.noWatermarks();
        WatermarkStrategy<PayloadB> watermarkStrategyB = WatermarkStrategy.noWatermarks();
        DataStream<PayloadA> streamA = env.fromSource(kdsA, watermarkStrategyA, "Stream A")
                .returns(TypeInformation.of(PayloadA.class));
        DataStream<PayloadB> streamB = env.fromSource(kdsB, watermarkStrategyB, "Stream B")
                .returns(TypeInformation.of(PayloadB.class));
//        streamA.print(); // console sink
//        streamB.print(); // console sink

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        Table tableA = tableEnv.fromDataStream(streamA).as("id", "val");
        Table tableB = tableEnv.fromDataStream(streamB).as("id", "val");
        tableEnv.createTemporaryView("tableA", tableA);
        tableEnv.createTemporaryView("tableB", tableB);
        Table result = tableEnv.sqlQuery(
                """
                    SELECT a.id, a.val as a_val, b.val as b_val
                    FROM
                        (SELECT id, val FROM tableA) a
                    LEFT JOIN
                        (SELECT id, val FROM tableB) b
                    ON a.id = b.id
                """
        );
//        tableEnv.toDataStream(result).print();
        Schema schema = Schema.newBuilder()
            .column("id", "STRING")
            .column("a_val", "STRING")
            .column("b_val", "STRING")
            .build();

        tableEnv.toChangelogStream(result, schema, ChangelogMode.all()).print();
        env.execute("Kinesis Source Example");
    }
}
