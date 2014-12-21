package component.security

import scaldi.Module

package object crypto {
   type DataBlob = String

  class CryptoModule extends Module {
    binding to injected[Crypto]
    // or binding to new Decryption(inject[Crypto])
    binding to injected[Encryptor]
    binding to injected[Decryptor]
    binding to injected[CryptoService]
  }
}
