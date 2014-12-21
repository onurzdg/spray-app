Spray Seeder App
===================
This small app is intended to get people writing web services/applications quickly with [Spray](http://spray.io/)
without having to spend a lot of time on boilerplate project set-up. The main reason I felt compelled to
make this work public is that many of the frameworks used in this small application are presented on the Internet
with shallow "Helloworld" examples that are fraught with misleading and wrong patterns.
Most of these examples do not even give you an overall idea of how things might look when pieced together while writing
a production ready app.
This seeder project attempts to demonstrate the use of all of these frameworks - by incorporating good practices - 
in the small context of a chat app that supports login, logout, account information lookup, and message delivery 
through [Server-sent events(SSE)](http://en.wikipedia.org/wiki/Server-sent_events).

This project was born out of [Spray-SPA](https://github.com/enetsee/Spray-SPA). The functionality of 
this seeder app is very similar to that of Spray-SPA. Spray-SPA proved to be an excellent starting point for me.
It was one of the most complete examples at the time when I decided to use Spray. 

However, I felt that a better designed version of that project could be a better reference for people.
I virtually re-wrote the entire back-end to take the project's structure and design further from its simple state to a close-to-production-ready state.
Thus, if you compare both projects, you will see that they differ in design and structure.
In addition, Spray-SPA, not surprisingly, does not compile with the latest Scala and latest versions of these frameworks.
In that sense, this project also presents a more modern codebase with the most recent versions of the frameworks in question employed.
However, I did not have an incentive to change the structure of the front-end and update the libraries used on that front.
My goal was to re-write the back-end and make slight improvements in the front-end to reconcile it with the changes happening
in the back-end.


Back-end Design
-----------------------------------
 This app relies on [scaldi](http://scaldi.org/) Dependency Injection framework for wiring modules together.
 I chose this over Guava as it is written in Scala, less intrusive, concise, and a lot less dependent on reflection.
 This app prefers [package-by-feature](http://www.javapractices.com/topic/TopicAction.do?Id=205) over package-by-layer
 However, for the REST/route layer I went with a variant of Cake Pattern to make code more concise. 
 

Database
-----------------------------------
 It assumes that default database is Postgres and it uses [slick-pg](https://github.com/tminglei/slick-pg), 
 an enhanced slick driver. If you want to use a different database, all you need to do is get rid of 
 "EnhancedPostgresDriver" trait and hook it up with one of the default drivers in [Slick](http://slick.typesafe.com/) and 
 include LocalDateTimeTypeMapper in the mappers for "AccountTable".
 Slick queries are [pre-compiled](http://slick.typesafe.com/doc/2.1.0/queries.html#compiled-queries) for performance reasons.
 Application is already configured to use [Flyway](http://flywaydb.org/) to manage database schema changes. 
 Database access is also configured to use the [BoneCp](http://jolbox.com/) connection pool.
 
 
Security
-----------------------------------
 I have written Spray directives to prevent cross-site request forgery(csrf). The routes 
 already go through this check. Session management is cookie based and stateless. Cookie information is not stored in memory or database.
 They are robustly encrypted and verified in the back-end each time a request that requires authentication comes in.
 I also wrote a directive("Privilege.hasAccess") for route authorization. This is not turned on in the codebase, but you can
 enable it by changing its implementation. This app requires [Java Cryptography Extension(JCE)]
 (http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) due to the use 256-bit encryption
 algorithms in the SSL configuration(SSLSupported.scala). You need to download JCE and put it in the appropriate folder in your OS. 
 However, you can avoid this requirement by choosing to use weaker algorithms.
 Furthermore, the app uses [JBCrypt](http://www.mindrot.org/projects/jBCrypt/) to hash passwords.
 
Logging & Application-level Metrics
-----------------------------------
 [Logback](http://logback.qos.ch/) is used through the SLF4J interface for logging. [Grizzled](http://software.clapper.org/grizzled-slf4j/) is a thin wrapper that 
 I used around SLF4J to have more idiomatic logging code. For monitoring it relies on [Metrics-scala](https://github.com/erikvanoosten/metrics-scala),
 another thin wrapper on top of Dropwizard's [Metrics](https://dropwizard.github.io/metrics/3.1.0/).
 
Java 8
-----------------------------------
 It's required for the encoding and date-time libraries used in the project.
 In case you are having difficulty getting sbt to use a Java8 JDK, I would highly recommend using [jenv](https://github.com/gcuisinier/jenv)
 Usually, all it takes is typing a command like `jenv local oracle64-1.8.0.11` in the project directory 
  
Testing
-----------------------------------
 [Scalatest](http://www.scalatest.org/) is used for mini-integration xUnit and REST API tests. Even though I've
 included [ScalaMock](scalamock.org) mocking framework as part of the build, recently I've come to believe that mocking is not an absolute necessity
 for testing. I actually think testing with concrete object is better in that it does not tie you to the internal
 details of the methods of the mocked objects. That's why I consider my self in the ["classicist"](http://martinfowler.com/articles/mocksArentStubs.html) camp.  
  
JSON and Templating
-----------------------------------
 I chose [spray-json](https://github.com/spray/spray-json) over other json libraries for its simplicity, decent speed, and no reliance on reflection.
 [json-lenses](https://github.com/jrudolph/json-lenses) is included as a complementary querying/updating json library to spray-json.
 The app uses [Twirl](https://github.com/playframework/twirl) for server-side templating.
  
Scalability
-----------------------------------
 As you can see, the application does not make blocking calls in Spray routes, and this is something that 
 should be adhered to to avoid degrading the performance of the Spray. Use the Bulkhead Pattern; always offload blocking and time-consuming
 calls to futures/actors that have a dedicated execution context. For instance, db queries are wrapped in futures 
 that have a dedicated thread pool(e.g., dbExecutionContext) Use detach() if your business code outside of route layer 
 is not doing any offloading by using Futures or Actors 
 However, when using the detach() directive, make sure to provide it with a specific execution context.
 Likewise, create a separate dispatcher for actors that block(e.g., akka.blocking-dispatcher). By default, all actors
 use the same default dispatcher and its default thread pool. If you use the same dispatcher for all actors(blocking, non-blocking), 
 you will run into starvation issues. Give thought to the execution context that should be used by each component 
 in your asynchronous system. Since you are not going to be blocking on the default-dispatcher, try to size its thread pool close to
 the number of cores on the system where you plan to deploy your application.   
 Also, the database connection pool size is the equivalent 
 of the number of threads in the thread pool dedicated to db access; it's wasteful and unnecessary to have a connection 
 pool size greater than the number of threads that can actually use them. 
  
Deployment
------------------------------------
 * Install git
 * Clone example project:
 * Install sbt. See http://www.scala-sbt.org/release/tutorial/Setup.html
 * Type `sbt` in the project directory to start it
 * Type compile to compile the project
 * exit sbt 
 * run `sbt flywayMigrate` in the project directory
 * Run `re-Start` in sbt to startup the server


Tips and tricks
------------------------------------
 You may take advantage of [sbt-revolver](https://github.com/spray/sbt-revolver)(included and configured already) if you wish to.
 
 * Type `sbt` in the project directory
 * Type `~re-start` to have your application boot up. If you make any changes to the source files, the server will
 compile your changes and restart instantaneously.
 
