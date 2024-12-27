package com.example.pipeline.spark.providers

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

class SparkSessionProvider() {

  def get(appName: String, isLocal: Boolean = false): SparkSession = {

    val sparkConf = new SparkConf()
      .set("spark.sql.caseSensitive", "true")
      .set("spark.sql.session.timeZone", "UTC")

    if (isLocal) {
      // local specific configs
    }

    val sparkMode = if (isLocal) "local[*]" else "yarn"

    SparkSession
      .builder
      .appName(appName)
      .master(sparkMode)
      .config(sparkConf)
      .getOrCreate()
  }
}
