package storage.postgres

import com.jolbox.bonecp.BoneCPDataSource


private[postgres]
object ConnectionPool {
  private val settings = BoneCPSettings()
  private val ds: BoneCPDataSource = new BoneCPDataSource()
  ds.setJdbcUrl(settings.url)
  ds.setUsername(settings.username)
  ds.setPassword(settings.password)
  ds.setDriverClass(settings.driverClass)
  ds.setDefaultAutoCommit(settings.autoCommit)
  ds.setPartitionCount(settings.partitionCount)
  ds.setMinConnectionsPerPartition(settings.minConnectionsPerPartition)
  ds.setMaxConnectionsPerPartition(settings.maxConnectionsPerPartition)
  ds.setAcquireIncrement(settings.acquireIncrement)
  ds.setAcquireRetryAttempts(settings.acquireRetryAttempts)
  ds.setAcquireRetryDelayInMs(settings.acquireRetryDelay)
  ds.setConnectionTimeoutInMs(settings.connectionTimeout)
  ds.setInitSQL(settings.initSql)
  ds.setIdleConnectionTestPeriodInSeconds(settings.idleConnectionTestPeriod)
  ds.setIdleMaxAgeInSeconds(settings.idleMaxAge)
  ds.setStatementsCacheSize(settings.statementCacheSize)
  def apply() = ds
}
