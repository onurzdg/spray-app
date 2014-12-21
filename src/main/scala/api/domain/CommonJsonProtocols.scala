package api.domain

import spray.json.{DefaultJsonProtocol, JsonFormat}

private[api]
trait CommonJsonProtocols extends ResponseJsonProtocol

private[api]
case class RequestResponse[+T](success:Boolean, content:Option[T], redirect:Option[String], errors:List[String]) {}

private[api]
object RequestResponse extends ResponseJsonProtocol

private[api]
trait ResponseJsonProtocol extends DefaultJsonProtocol  {
  implicit def responseResultFormat[T:JsonFormat] = jsonFormat4(RequestResponse.apply[T])
}