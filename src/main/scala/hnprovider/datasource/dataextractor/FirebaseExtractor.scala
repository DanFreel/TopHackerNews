package hnprovider.datasource.dataextractor

class FirebaseExtractor extends Extractor {
  def extractIds(ids: Seq[Any]): Seq[Long] = ids map (_.asInstanceOf[Long])
  def extractIds(ids: Any): Seq[Long] = {
    ids match {
      case s: Seq[_] => extractIds(s)
      case unexpectedData => handleUnexpectedData(unexpectedData)
    }
  }

  def extractBoolean(value: Any): Boolean = {
    value match {
      case bool: Boolean => bool
      case unexpectedData => handleUnexpectedData(unexpectedData)
    }
  }

  def extractString(value: Any): String = {
    value match {
      case str: String => str
      case unexpectedData => handleUnexpectedData(unexpectedData)
    }
  }

  private def handleUnexpectedData(unexpectedData: Any): Nothing =
    throw new IllegalArgumentException(s"Unexpected data received: $unexpectedData")
}
