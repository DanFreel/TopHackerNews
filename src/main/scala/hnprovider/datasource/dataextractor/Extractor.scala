package hnprovider.datasource.dataextractor

trait Extractor {
  def extractIds(ids: Seq[Any]): Seq[Long]
  def extractIds(ids: Any): Seq[Long]
  def extractBoolean(value: Any): Boolean
  def extractString(value: Any): String
}
