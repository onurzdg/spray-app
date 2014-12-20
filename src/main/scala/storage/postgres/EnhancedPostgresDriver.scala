package storage.postgres

import com.github.tminglei.slickpg._

import scala.slick.driver.PostgresDriver

/**
 * Enhanced Slick Postgres driver
 */

trait EnhancedPostgresDriver extends PostgresDriver
with PgArraySupport
with PgDate2Support
with PgSprayJsonSupport
with PgEnumSupport
with PgJsonSupport
with PgRangeSupport
with PgHStoreSupport
with PgSearchSupport
with PgPostGISSupport {

  override lazy val Implicit = new ImplicitsPlus {}
  override val simple = new SimpleQLPlus {}

  //////
  trait ImplicitsPlus extends Implicits
  with ArrayImplicits
  with DateTimeImplicits
  with RangeImplicits
  with HStoreImplicits
  with JsonImplicits
  with SearchImplicits
  with PostGISImplicits

  trait SimpleQLPlus extends SimpleQL
  with ImplicitsPlus
  with SearchAssistants
  with PostGISAssistants
  with DateTimeImplicits
}

object EnhancedPostgresDriver extends EnhancedPostgresDriver