import java.util.concurrent.TimeUnit

import com.codahale.metrics.health.HealthCheckRegistry
import com.codahale.metrics.{JmxReporter, MetricRegistry, Slf4jReporter}
import nl.grons.metrics.scala.InstrumentedBuilder
import org.slf4j.LoggerFactory
import scaldi.{Injectable, Module}


package object metrics {

  class MetricsModule extends Module {
    binding to {
      val metricRegistry: MetricRegistry = new MetricRegistry()
      Slf4jReporter.forRegistry(metricRegistry).
        outputTo(LoggerFactory.getLogger("metrics")).
        convertRatesTo(TimeUnit.SECONDS).
        convertDurationsTo(TimeUnit.MILLISECONDS).
        build().
        start(10, TimeUnit.SECONDS)

      /*ConsoleReporter.forRegistry(metricRegistry).
          convertRatesTo(TimeUnit.SECONDS).
          convertDurationsTo(TimeUnit.MILLISECONDS).
          build().
          start(10, TimeUnit.SECONDS)
      */

      JmxReporter.forRegistry(metricRegistry).build().start()
      metricRegistry
    }

    binding to injected[DbHealthCheck]

    binding to {
      val healthCheckRegistry = new HealthCheckRegistry()
      healthCheckRegistry.register("postgresDb", inject[DbHealthCheck])
      healthCheckRegistry
    }
  }
  object MetricsInstrumented extends Injectable {
    import module.Bindings.metricsModule
    val metricRegistry: MetricRegistry = inject[MetricRegistry]
  }

  trait MetricsInstrumented extends InstrumentedBuilder {
    override val metricRegistry =  MetricsInstrumented.metricRegistry
  }
}
