package component.account.domain

import spray.json.DefaultJsonProtocol

case class Picture(id: Option[Long] = None, url: String,  accountId: Long)

object Picture extends DefaultJsonProtocol {
  implicit val pictureFormat = jsonFormat3(Picture.apply)
}