package com.example.pipeline

object SourceProvider {

  private def getPath(fileName: String): String = {
    getClass.getResource(fileName).getPath
  }

  def getCustomerCsv: SourceMetadata = {
    SourceMetadata(getPath("/customer.csv"), hasHeader = false)
  }

  def getProductCsv: SourceMetadata = {
    SourceMetadata(getPath("/product.csv"), hasHeader = false)
  }

  def getSalesCsv: SourceMetadata = {
    SourceMetadata(getPath("/sales.csv"), hasHeader = false)
  }

}

case class SourceMetadata(path: String, hasHeader: Boolean)
