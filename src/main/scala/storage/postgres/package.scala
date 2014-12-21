package storage

import scaldi.Module

import scala.language.existentials

package object postgres {
  val dbProfile = EnhancedPostgresDriver.profile
  val dbSimple = EnhancedPostgresDriver.simple

  class DbModule extends Module {
    import scala.slick.jdbc.JdbcBackend.Database
    bind[Database] to Database.forDataSource(ConnectionPool())
  }

}
