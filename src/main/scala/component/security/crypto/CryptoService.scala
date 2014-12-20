package component.security.crypto

class CryptoService(private val encryptor: Encryptor, private val decryptor: Decryptor, private val crypto: Crypto) {

  def sign(message: String): String = crypto.sign(message)

  def generateRandomUUID = crypto.generateRandomUUID

  def encryptAndSignData(data: String): String = {
    encryptor.encryptAndSignData(data)
  }

  def encryptAndSignData(data: Array[Byte]): String = {
    encryptor.encryptAndSignData(data)
  }

  def encryptAndSignTimeSensitiveData(data: String): String = {
    encryptor.encryptAndSignTimeSensitiveData(data)
  }

  def encryptAndSignTimeSensitiveData(data: Array[Byte]): String = {
    encryptor.encryptAndSignTimeSensitiveData(data)
  }

  def verifySignatureAndDecryptData(data: String): Option[String] = {
    decryptor.verifySignatureAndDecryptData(data)
  }

  def verifySignatureAndDecryptTimeSensitiveData(data: String): Option[String] = {
    decryptor.verifySignatureAndDecryptTimeSensitiveData(data)
  }
}
