package com.example.pipeline.flink.providers

import com.example.pipeline.SourceMetadata
import com.example.pipeline.flink.models.{Customer, Product, Sales}
import org.apache.flink.connector.file.src.FileSource
import org.apache.flink.core.fs.Path
import org.apache.flink.formats.csv.CsvReaderFormat

object SourceProvider {

  def getCsvSource[T](clazz: Class[T]): FileSource[T] = {

    val sourceMetadata: SourceMetadata = if (clazz == classOf[Customer]) {
      com.example.pipeline.SourceProvider.getCustomerCsv
    } else if (clazz == classOf[Product]) {
      com.example.pipeline.SourceProvider.getProductCsv
    } else if (clazz == classOf[Sales]) {
      com.example.pipeline.SourceProvider.getSalesCsv
    } else {
      throw new IllegalArgumentException(s"Unmapped source for class: $clazz")
    }

    val csvReaderFormat: CsvReaderFormat[T] = CsvReaderFormat.forPojo(clazz)
    FileSource.forRecordStreamFormat(csvReaderFormat, new Path(sourceMetadata.path)).build()
  }
}
