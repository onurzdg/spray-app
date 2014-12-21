package module

import component.account.AccountModule
import component.chat.ChatModule
import component.security.crypto.CryptoModule
import concurrency.actor.ActorsModule
import configuration.AppConfigModule
import api.HttpModule
import metrics.MetricsModule
import storage.postgres.DbModule


object Bindings {

  implicit val metricsModule = new MetricsModule
  implicit val dbModule = new DbModule
  implicit val accountsModule = new AccountModule
  implicit val cryptoModule = new CryptoModule
  implicit val appConfigModule = new AppConfigModule

  lazy implicit val appInjector = {
    dbModule :: metricsModule :: appConfigModule :: new ActorsModule :: new ChatModule ::
      cryptoModule :: accountsModule :: new HttpModule
  }

  lazy implicit val testInjector = {
    dbModule :: metricsModule  :: appConfigModule :: cryptoModule :: accountsModule
  }
}
