package storage.postgres

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import configuration.Settings


case class BoneCPSettings(url: String,
                          username: String,
                          password: String,
                          driverClass: String,
                          autoCommit: Boolean = true,
                          partitionCount: Int = 2,
                          minConnectionsPerPartition: Int = 5,
                          maxConnectionsPerPartition: Int = 10,
                          acquireIncrement: Int = 1,
                          acquireRetryAttempts: Int = 10,
                          acquireRetryDelay: Long,
                          connectionTimeout: Long,
                          initSql: String,
                          idleConnectionTestPeriod: Long,
                          idleMaxAge: Long,
                          statementCacheSize: Int) {
  require(url.nonEmpty, "url must be non-empty")
  require(username.nonEmpty, "username must be non-empty")
  require(driverClass.nonEmpty, "driver-class must be non-empty")
  require(partitionCount > 0, "partition-count must be greater than 0")
  require(minConnectionsPerPartition > 0, "min-connections-per-partition must be greater than 0")
  require(maxConnectionsPerPartition > 0, "max-connections-per-partition must be greater than 0")
  require(acquireIncrement >= 0, "acquire-increment must be non-negative")
  require(acquireRetryAttempts >= 0,"acquire-retry-attempts must be non-negative")
  require(acquireRetryDelay >= 0, "acquire-retry-delay must be non-negative")
  require(connectionTimeout > 0, "connection-timeout must be greater than 0")
  require(initSql.nonEmpty, "init-sql must be non-empty")
  require(idleConnectionTestPeriod > 0, "connection-timeout must be greater than 0")
  require(idleMaxAge > 0, "idle-max-age must be greater than 0")
  require(statementCacheSize >= 0, "statement-cache-size must be non-negative")
}


object BoneCPSettings extends Settings[BoneCPSettings]("bonecp") {

  override def fromSubConfig(c: Config) = {

    def timeInMills(field: String) = {
      val acquireRetryVals = c.getString(field).split(" ")
      TimeUnit.MILLISECONDS.convert(acquireRetryVals(0).toLong, TimeUnit.valueOf(acquireRetryVals(1).toUpperCase))
    }

    def timeInSecs(field: String) = {
      val acquireRetryVals = c.getString(field).split(" ")
      TimeUnit.SECONDS.convert(acquireRetryVals(0).toLong, TimeUnit.valueOf(acquireRetryVals(1).toUpperCase))
    }

    apply(
      c getString "url",
      c getString "username",
      c getString "password",
      c getString "driver-class",
      c getBoolean "autocommit",
      c getInt "partition-count",
      c getInt "min-connections-per-partition",
      c getInt "max-connections-per-partition",
      c getInt "acquire-increment",
      c getInt "acquire-retry-attempts",
      timeInMills("acquire-retry-delay"),
      timeInMills("connection-timeout"),
      c getString "init-sql",
      timeInSecs("idle-connection-test-period"),
      timeInSecs("idle-max-age"),
      c getInt "statement-cache-size"
    )
  }

  def apply(): BoneCPSettings = BoneCPSettings(ConfigFactory.load())
}


