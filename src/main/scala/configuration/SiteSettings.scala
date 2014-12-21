package configuration

import com.typesafe.config.{Config, ConfigFactory}
import spray.util._


case class SiteSettings (
  interface: String,
  port: Int,
  devMode: Boolean,
  applicationSecretKey: String,
  macSecretKey: String,
  encryptionLogRounds: Int,
  accountLockout: Boolean,
  accountLockoutMaxAttempts: Int,
  accountLockoutPeriod: Int,
  sessionCookieName: String,
  sessionCookieMaxAge: Long,
  rememberCookieName: String,
  rememberCookieMaxAge: Long,
  loginCsfrToken: String,
  sessionCsrfToken: String) {
  require(applicationSecretKey.nonEmpty,"application-secret must be non-empty")
  require(sessionCookieName.nonEmpty,"session-cookie-name must be non-empty")
  require(rememberCookieName.nonEmpty,"remember-cookie-name must be non-empty")
  require(loginCsfrToken.nonEmpty,"login-csrf-token must be non-empty")
  require(sessionCsrfToken.nonEmpty,"session-csrf-token must be non-empty")
  require(0 < encryptionLogRounds && encryptionLogRounds < 16,"encyrption log-rounds must be between 1 and 15.")
  require(interface.nonEmpty, "interface must be non-empty")
  require(0 < port && port < 65536, "illegal port")
}


object SiteSettings extends Settings[SiteSettings]("site") {
  def fromSubConfig(c: Config) = {
    apply(
      c getString "interface",
      c getInt  "port",
      c getBoolean "dev-mode",
      c getString  "application-secret-key",
      c getString  "mac-secret-key",
      c getInt "encryption-log-rounds",
      c getBoolean  "account-lockout",
      c getInt "account-lockout-max-attempts",
      c getInt "account-lockout-period",
      c getString "session-cookie-name",
      c getInt "session-cookie-max-age",
      c getString "remember-cookie-name",
      c getInt "remember-cookie-max-age",
      c getString "login-csrf-token",
      c getString "session-csrf-token"
    )
  }

  def apply(): SiteSettings = SiteSettings(ConfigFactory.load())

}
