package com.example.pipeline.spark

import com.example.pipeline.SourceProvider
import com.example.pipeline.spark.providers.SparkSessionProvider


object Main {

  def main(args: Array[String]): Unit = {
    val sparkSessionProvider = new SparkSessionProvider()
    val spark = sparkSessionProvider.get("spark-batch", isLocal=true)

    val customerCsv = SourceProvider.getCustomerCsv
    val customerDf = spark.read.option("header", value = customerCsv.hasHeader).csv(customerCsv.path)
    customerDf.coalesce(1).write.option("header", value = customerCsv.hasHeader).mode("overwrite").csv("/tmp/spark-batch")
    println("Done")
  }

}
