package concurrency

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext


object ExecutionContexts {
  private val contextsSettings = ExecutionContextsSettings()
  implicit val dbExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(contextsSettings.blockingThreadCount))
  implicit val cpuIntensiveExecutionContext = ExecutionContext.fromExecutor(Executors.newWorkStealingPool(contextsSettings.cpuIntensiveThreadCount))
}



