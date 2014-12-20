package component.security.crypto

import java.time.Instant
import java.util.Base64
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import configuration.SiteSettings


private[crypto]
class Encryptor(private val crypto: Crypto) {

  def encryptAndSignData(data: String): String = {
    encryptAndSignDataHelperFn(data.getBytes(Crypto.CharSet), timeSensitive = false)
  }

  def encryptAndSignData(data: Array[Byte]): String = {
    encryptAndSignDataHelperFn(data, timeSensitive = false)
  }

  def encryptAndSignTimeSensitiveData(data: String): String = {
    encryptAndSignDataHelperFn(data.getBytes(Crypto.CharSet), timeSensitive = true)
  }

  def encryptAndSignTimeSensitiveData(data: Array[Byte]): String = {
    encryptAndSignDataHelperFn(data, timeSensitive = true)
  }

  private def encryptAndSignDataHelperFn(data: Array[Byte], timeSensitive: Boolean): String = {

    implicit val ivSpec: IvParameterSpec = crypto.createIvSpec
    val base64Encoder = Base64.getEncoder

    implicit val secretKeySpec: SecretKeySpec = crypto.createSecretKeySpec

    val encryptedMessageBase64 = base64Encoder.encodeToString(crypto.encryptData(data, ivSpec)(secretKeySpec))
    val encryptedTimeStampBase64 = base64Encoder.encodeToString(crypto.encryptData(Instant.now().toEpochMilli.toString.getBytes(Crypto.CharSet), ivSpec))
    val encryptedIvBase64 = base64Encoder.encodeToString(crypto.encryptIv(ivSpec))
    // Creating a MAC signature is important and it should be done on the encrypted message. This prevents
    // padding oracle attacks and does not violate doom principle.
    val hmacSignatureBase64 =
      if (timeSensitive)
        base64Encoder.encodeToString(crypto.signData(encryptedMessageBase64 + encryptedIvBase64 + encryptedTimeStampBase64))
      else
        base64Encoder.encodeToString(crypto.signData(encryptedMessageBase64 + encryptedIvBase64))

    if(timeSensitive) {
      hmacSignatureBase64 + "-" + encryptedMessageBase64 + "-" + encryptedIvBase64 + "-" + encryptedTimeStampBase64
    }
    else {
      hmacSignatureBase64 + "-" + encryptedMessageBase64 + "-" + encryptedIvBase64
    }
  }
}

private[crypto]
class Decryptor(private val crypto: Crypto, private val siteSettings: SiteSettings) {
  private object Position extends Enumeration {
    val HmacSignature = Value(0)
    val EncryptedMessage = Value(1)
    val EncryptedIv = Value(2)
    val TimeStamp = Value(3)
  }


  def verifySignatureAndDecryptData(data: String): Option[String] = {
    verifySignatureAndDecryptDataHelperFn(data, timeSensitive = false)
  }

  def verifySignatureAndDecryptTimeSensitiveData(data: String): Option[String] = {
    verifySignatureAndDecryptDataHelperFn(data, timeSensitive = true)
  }

  private def verifySignatureAndDecryptDataHelperFn(data: String, timeSensitive: Boolean): Option[String] = {
    try {
      val base64Decoder =  Base64.getDecoder
      val splitted: Array[String] = data.split("-")
      val encryptedIv = base64Decoder.decode(splitted(Position.EncryptedIv.id))
      implicit val secretKeySpec: SecretKeySpec = crypto.createSecretKeySpec
      implicit val iv: IvParameterSpec = crypto.decryptIv(encryptedIv)

      lazy val encryptedTimeStampBase64 = splitted(Position.TimeStamp.id)
      val encryptedDataBase64: DataBlob = splitted(Position.EncryptedMessage.id)
      val decodedHmacSignature = base64Decoder.decode(splitted(Position.HmacSignature.id))
      val computedHmacSignature =
        if (timeSensitive)
          crypto.signData( splitted(Position.EncryptedMessage.id) +  splitted(Position.EncryptedIv.id) + encryptedTimeStampBase64 )
        else
          crypto.signData( splitted(Position.EncryptedMessage.id) +  splitted(Position.EncryptedIv.id) )

      if (decodedHmacSignature.toIterable.sameElements(computedHmacSignature.toIterable)) {
        // verify that timestamp is not expired
        if(timeSensitive) {
          val decryptedTimeStamp: Array[Byte] = crypto.decryptData(base64Decoder.decode(encryptedTimeStampBase64))
          val timeStamp = new String(decryptedTimeStamp, Crypto.CharSet).toLong
          if (Instant.now.toEpochMilli >= (timeStamp + siteSettings.sessionCookieMaxAge * 1000)) {
            Option.empty
          }
        }
        val decryptedData = crypto.decryptData(base64Decoder.decode(encryptedDataBase64))
        val message: String = new String(decryptedData, Crypto.CharSet)
        Option(message)
      }
      else {
        Option.empty
      }
    } catch {
      case scala.util.control.NonFatal(_) => Option.empty
    }
  }
}

