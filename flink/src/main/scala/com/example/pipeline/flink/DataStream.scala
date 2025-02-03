package com.example.pipeline.flink

import com.example.pipeline.flink.models.{Customer, Product, Sales}
import com.example.pipeline.flink.providers.SourceProvider
import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment


object DataStream {

  def main(args: Array[String]): Unit = {

    csvToConsole(streaming = true)
    csvToConsole(streaming = false)

  }

  private def csvToConsole(streaming: Boolean): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    val executionMode = if(streaming) {
      RuntimeExecutionMode.STREAMING
    } else {
      RuntimeExecutionMode.BATCH
    }
    env.setRuntimeMode(executionMode)
    val appName = s"READ CSV ${executionMode.name()}"

    println()
    println(appName)

    val customerDataStream = env.fromSource(SourceProvider.getCsvSource(classOf[Customer]),
      WatermarkStrategy.noWatermarks(), s"customer-csv-source")
    customerDataStream.print("customer-console-sink")

    val productDataStream = env.fromSource(SourceProvider.getCsvSource(classOf[Product]),
      WatermarkStrategy.noWatermarks(), s"product-csv-source")
    productDataStream.print("product-console-sink")

    val salesDataStream = env.fromSource(SourceProvider.getCsvSource(classOf[Sales]),
      WatermarkStrategy.noWatermarks(), s"sales-csv-source")
    salesDataStream.print("sales-console-sink")

    env.execute(appName)
  }
}
