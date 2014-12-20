package concurrency

import com.typesafe.config.{Config, ConfigFactory}
import configuration.Settings
import storage.postgres.BoneCPSettings

private[concurrency]
case class ExecutionContextsSettings(blockingThreadCount: Int, cpuIntensiveThreadCount: Int) {
  require(blockingThreadCount > 0, "pool size for blocking threads must be greater than 0")
  require(cpuIntensiveThreadCount > 0, "pool size for cpu intensive threads must be greater than 0")
}

private[concurrency]
object ExecutionContextsSettings extends Settings[ExecutionContextsSettings]("execution-contexts") {
  def fromSubConfig(c: Config) = {
    apply(
      c.getInt("blocking-thread-count") * BoneCPSettings().partitionCount,
      c.getInt("cpu-intensive-thread-count")
    )
  }

  def apply(): ExecutionContextsSettings = ExecutionContextsSettings(ConfigFactory.load())
}
