package component.security.crypto

import java.security.SecureRandom
import java.util.{Base64, UUID}
import javax.crypto._
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import configuration.SiteSettings

private[crypto]
class Crypto(private val siteSettings: SiteSettings) {
  private val IvCipher = "AES/ECB/PKCS5Padding"
  private val DataCipher = "AES/CBC/PKCS5Padding"
  private val MacName = "HmacSHA1"
  private val EncryptionAlgorithm = "AES"
  import Crypto._

  def sign(message: String): String =
    sign(message, siteSettings.macSecretKey.getBytes(CharSet))

  def generateRandomUUID = UUID.randomUUID().toString

  private[crypto] def createSecretKeySpec: SecretKeySpec = {
    val key: Array[Byte] = Base64.getDecoder.decode(siteSettings.applicationSecretKey)
    val sKeySpec = new SecretKeySpec(key, EncryptionAlgorithm)
    sKeySpec
  }

  private[crypto] def signData(dataBlob: DataBlob) = {
    val key: Array[Byte] = Base64.getDecoder.decode(siteSettings.macSecretKey)
    val mac = Mac.getInstance(MacName)
    mac.init(new SecretKeySpec(key, MacName))
    mac.doFinal(dataBlob.getBytes(CharSet))
  }

  private[crypto] def decryptIv(encryptedIv: Array[Byte])(implicit  sKeySpec: SecretKeySpec) = {
    val cipher = Cipher.getInstance(IvCipher)
    cipher.init(Cipher.DECRYPT_MODE, sKeySpec)
    new IvParameterSpec(cipher.doFinal(encryptedIv))
  }

  private[crypto] def decryptData(data: Array[Byte])(implicit sKeySpec: SecretKeySpec, ivSpec: IvParameterSpec) = {
    val cipher = Cipher.getInstance(DataCipher)
    cipher.init(Cipher.DECRYPT_MODE, sKeySpec, ivSpec)
    cipher.doFinal(data)
  }

  private[crypto] def createIvSpec = {
    val random = new SecureRandom()
    val iv = new Array[Byte](16)
    random.nextBytes(iv) //generate random 16 byte; IV AES is always 16bytes
    new IvParameterSpec(iv)
  }

  // Actually, IV does not need be sent encrypted, but there's no harming in doing so.
  private[crypto] def encryptIv(iv: IvParameterSpec)(implicit sKeySpec: SecretKeySpec) = {
    val cipher = Cipher.getInstance(IvCipher)
    cipher.init(Cipher.ENCRYPT_MODE, sKeySpec)
    cipher.doFinal(iv.getIV)
  }

  private[crypto] def encryptData(data: Array[Byte], iv: IvParameterSpec)(implicit sKeySpec: SecretKeySpec) = {
    val cipher = Cipher.getInstance(DataCipher)
    cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv)
    cipher.doFinal(data)
  }

  private def createSecretKey = {
    val keygen: KeyGenerator = KeyGenerator.getInstance(EncryptionAlgorithm)
    keygen.init(128)  // To use 256 bit keys, you need the "unlimited strength" encryption policy files from Sun.
    val key: Array[Byte] = keygen.generateKey().getEncoded
    Base64.getEncoder.encodeToString(key)
  }

  private def sign(message: String, key: Array[Byte]) = {
    val mac = Mac.getInstance(MacName)
    mac.init(new SecretKeySpec(key, MacName))
    Base64.getEncoder.encodeToString(mac.doFinal(message.getBytes(CharSet)))
  }

}
object Crypto {
  val CharSet = "utf-8"
}

