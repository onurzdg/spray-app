package metrics

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result

import scala.slick.jdbc.JdbcBackend.Database
import scala.slick.jdbc.{StaticQuery => Q}

private[metrics]
class DbHealthCheck(val db: Database) extends HealthCheck  {
  override def check(): Result = {
    val res = db.withSession{  implicit s => Q.queryNA[Int]("SELECT 1").first }
    if(res == 1) Result.healthy() else Result.unhealthy("Unable to ping database")
  }
}
