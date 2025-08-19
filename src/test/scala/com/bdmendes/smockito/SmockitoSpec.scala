package com.bdmendes.smockito

import com.bdmendes.smockito.Smockito.SmockitoException.*
import com.bdmendes.smockito.Smockito.SmockitoMode
import com.bdmendes.smockito.SmockitoSpec.*
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import scala.compiletime.summonFrom
import scala.compiletime.testing.typeChecks
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class SmockitoSpec extends munit.FunSuite with Smockito:

  test("wrap a raw Mockito instance"):
    val repository = mock[Repository[User]]

    // A Mock[T] is implicitly a `T`, so one can use Mockito methods on it.
    Mockito.when(repository.get).thenReturn(mockUsers)
    Mockito.when(repository.exists("bdmendes")).thenReturn(true)

    val service = Service(repository)

    assertEquals(
      service.getWith(_.username.contains("mendes")).toSet,
      Set(User("bdmendes"), User("apmendes"))
    )
    assert(service.exists("bdmendes"))

    Mockito.verify(repository).exists("bdmendes")

    // But, of course, the raw Mockito API is not very type safe, so use with caution.
    Mockito.when(repository.getWith("bd", "mendes")).thenReturn(List(1, 2))
    val users = repository.getWith("bd", "mendes")
    intercept[ClassCastException]:
      val _: User = users.head

  test("interoperate with raw Mockito verifications"):
    val repository = mock[Repository[User]].on(it.exists)(_ => true)

    assert(repository.exists("pedronuno"))

    Mockito.verify(repository).exists(ArgumentMatchers.any)

    assertEquals(repository.calls(it.exists), List("pedronuno"))

  test("be a subtype of T"):
    inline def isSubtypeOf[A, B] =
      summonFrom:
        case _: (A <:< B) =>
          true
        case _ =>
          false

    assert(isSubtypeOf[Mock[User], User])
    assert(isSubtypeOf[Mock[Repository[User]], Repository[User]])
    assert(!isSubtypeOf[Mock[User], String])
    assert(!isSubtypeOf[Mock[User], Repository[User]])
    assert(!isSubtypeOf[Mock[Repository[User]], User])

  test("be covariant in T"):
    abstract class FancyRepository[T](name: String) extends Repository[T](name):
      def fancyGreet()(using T): String

    def setUpMock(mock: Mock[Repository[User]], answer: Boolean) = mock.on(it.exists)(_ => answer)

    assert(setUpMock(mock[FancyRepository[User]], true).exists("bdmendes"))
    assert(!setUpMock(mock[FancyRepository[User]], false).exists("bdmendes"))

  test("set up method stubs on values"):
    var counter = 0
    val repository =
      mock[Repository[User]].on(() => it.longName): _ =>
        counter += 1
        "database"

    assert(!typeChecks("repository.on(() => it.longName)(_ => 2)"))
    assert(!typeChecks("repository.on(it.longName)(_ => \"database\")"))

    assertEquals(repository.longName, "database")
    assertEquals(counter, 1)

    // It's worth to note that even values always trigger a method stub.
    assertEquals(repository.longName, "database")
    assertEquals(counter, 2)

  test("set up method stubs on methods with 0 parameters"):
    val repository = mock[Repository[User]].on(() => it.get)(_ => mockUsers)

    assert(typeChecks("repository.on(() => it.get)(_ => List.empty)"))
    assert(!typeChecks("repository.on(() => it.get)(List.empty)"))
    assert(!typeChecks("repository.on(it.get)(_ => List.empty)"))

    assertEquals(repository.get, mockUsers)

  test("set up method stubs on methods with 1 parameter"):
    val repository =
      mock[Repository[User]].on(it.exists):
        case name if name.endsWith("mendes") =>
          true

    assert(typeChecks("repository.on(it.exists)(_.startsWith(\"bdmendes\"))"))
    assert(typeChecks("repository.on(it.exists) { case \"bdmendes\" => true }"))
    assert(typeChecks("repository.on(it.exists) { _ => true }"))
    assert(!typeChecks("repository.on(it.exists)(println)"))
    assert(!typeChecks("repository.on(it.exists) { case \"bdmendes\" => 1 }"))
    assert(!typeChecks("repository.on(it.exists) { case 1 => \"bdmendes\" }"))
    assert(!typeChecks("repository.on(it.exists) { case 1 => \"bdmendes\" }"))

    assertEquals(repository.exists("bdmendes"), true)
    intercept[UnexpectedArguments](repository.exists("spider"))

  test("set up method stubs on methods with 2 parameters"):
    val repository =
      mock[Repository[User]].on(it.getWith): (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))

    assert(typeChecks("repository.on(it.getWith){ case (\"bd\", \"mendes\") => List.empty }"))
    assert(!typeChecks("repository.on(it.getWith){ case (\"bd\", \"mendes\") => true }"))
    assert(!typeChecks("repository.on(it.getWith){ case (\"bd\", 1) => List.empty }"))
    assert(!typeChecks("repository.on(it.getWith){ case (\"bd\", \"mendes\") => List(1) }"))

    assertEquals(repository.getWith("bd", ""), List(User("bdmendes")))
    assertEquals(repository.getWith("", "mendes"), List(User("bdmendes"), User("apmendes")))

  test("set up method stubs on curried methods"):
    val repository =
      mock[Repository[User]].on(it.getWithCurried(_: String)(_: String)): (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))

    assertEquals(repository.getWithCurried("bd")(""), List(User("bdmendes")))
    assertEquals(repository.getWithCurried("")("mendes"), List(User("bdmendes"), User("apmendes")))

  test("set up method stubs on methods with contextual parameters"):
    val repository =
      mock[Repository[User]].on(it.greet(_: Boolean)(using _: User)): (_, user) =>
        s"Hello, ${user.username}!"

    assertEquals(repository.greet(false)(using User("bdmendes")), "Hello, bdmendes!")

  test("set up method stubs on methods with variable arguments"):
    val repository =
      mock[Repository[User]].on((names: Seq[String]) => it.containsOneOf(names*)): names =>
        names.contains("bdmendes")

    assert(repository.containsOneOf("bdmendes", "apmendes"))
    assert(!repository.containsOneOf("fernandorego", "apmendes"))

  test("set up method stubs on overloaded methods"):
    val repository = mock[Repository[User]].on(it.contains(_: String))(_ => true)

    assert(repository.contains("bdmendes"))

    // The default Mockito response for unstubbed methods returning Boolean.
    assert(!repository.contains(mockUsers.head))

  test("disallow inspecting calls on values"):
    val repository = mock[Repository[String]].on(() => it.longName)(_ => "database")

    val _ = repository.times(() => it.longName)

    assert(!typeChecks("repository.calls(() => it.longName)"))

  test("disallow inspecting calls on methods with 0 parameters"):
    val repository = mock[Repository[String]].on(() => it.get)(_ => List.empty)

    val _ = repository.times(() => it.get)

    assert(!typeChecks("repository.calls(() => it.get)"))

  test("inspect calls on methods with 1 parameter"):
    val repository = mock[Repository[String]].on(it.exists)(_ == "bdmendes")

    assertEquals(repository.calls(it.exists), List.empty)

    assert(repository.exists("bdmendes"))
    assert(!repository.exists("apmendes"))

    assertEquals(repository.calls(it.exists), List("bdmendes", "apmendes"))

  test("inspect calls on methods with 2 parameters"):
    val repository =
      mock[Repository[User]].on(it.getWith): (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))

    assertEquals(repository.getWith("bd", "mendes"), List(User("bdmendes")))
    assertEquals(repository.getWith("bd", ""), List(User("bdmendes")))

    assertEquals(repository.calls(it.getWith), List(("bd", "mendes"), ("bd", "")))

  test("inspect calls on methods with contextual parameters"):
    val repository =
      mock[Repository[User]].on(it.greet(_: Boolean)(using _: User)): (_, user) =>
        s"Hello, ${user.username}!"

    assertEquals(repository.greet(false)(using User("bdmendes")), "Hello, bdmendes!")

    assertEquals(
      repository.calls(it.greet(_: Boolean)(using _: User)),
      List(false -> User("bdmendes"))
    )

  test("inspect calls on methods with variable arguments"):
    val repository =
      mock[Repository[User]].on((names: Seq[String]) => it.containsOneOf(names*)): names =>
        names.contains("bdmendes")

    val _ = repository.containsOneOf("bdmendes", "apmendes")

    assertEquals(
      repository.calls((names: Seq[String]) => it.containsOneOf(names*)),
      List(Seq("bdmendes", "apmendes"))
    )

  test("inspect calls on overloaded methods"):
    val repository = mock[Repository[User]].on(it.contains(_: String))(_ => true)

    val _ = repository.contains("bdmendes")
    val _ = repository.contains(mockUsers.head)

    assertEquals(repository.calls(it.contains(_: String)), List("bdmendes"))

  test("inspect calls on methods with default arguments"):
    val repository = mock[Repository[User]].on(it.getWithDefaults)(_ => List.empty)

    val _ = repository.getWithDefaults("bdmendes")

    assertEquals(repository.calls(it.getWithDefaults), List(("bdmendes", None)))

  test("count calls on values"):
    val repository: Mock[Repository[String]] =
      mock[Repository[String]].on(() => it.longName)(_ => "database")

    assertEquals(repository.longName, "database")

    assertEquals(repository.times(() => it.longName), 1)

    assertEquals(repository.longName, "database")
    assertEquals(repository.longName, "database")

    assertEquals(repository.times(() => it.longName), 3)

  test("count calls on methods with 0 parameters"):
    val repository: Mock[Repository[String]] =
      mock[Repository[String]].on(() => it.get)(_ => List.empty)

    assertEquals(repository.get, List.empty)

    assertEquals(repository.times(() => it.get), 1)

    assertEquals(repository.get, List.empty)
    assertEquals(repository.get, List.empty)

    assertEquals(repository.times(() => it.get), 3)

  test("count calls on methods with 0 parameters, minding erasure"):
    val repository =
      mock[Repository[String]]
        .on(() => it.get)(_ => List.empty)
        .on(() => it.getNames)(_ => List.empty)

    assertEquals(repository.times(() => it.get), 0)
    assertEquals(repository.times(() => it.getNames), 0)

    assertEquals(repository.get, List.empty)
    assertEquals(repository.getNames, List.empty)
    assertEquals(repository.getNames, List.empty)

    assertEquals(repository.times(() => it.get), 1)
    assertEquals(repository.times(() => it.getNames), 2)

  test("count calls on methods with 1 parameter"):
    val repository = mock[Repository[String]].on(it.exists)(_ == "bdmendes")

    assertEquals(repository.calls(it.exists), List.empty)

    assert(repository.exists("bdmendes"))
    assert(!repository.exists("apmendes"))

    assertEquals(repository.times(it.exists), 2)

  test("count calls on methods with 2 parameters"):
    val repository =
      mock[Repository[User]].on(it.getWith): (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))

    assertEquals(repository.getWith("bd", "mendes"), List(User("bdmendes")))
    assertEquals(repository.getWith("bd", ""), List(User("bdmendes")))

    assertEquals(repository.times(it.getWith), 2)

  test("count calls on methods with contextual parameters"):
    val repository =
      mock[Repository[User]].on(it.greet(_: Boolean)(using _: User)): (_, user) =>
        s"Hello, ${user.username}!"

    assertEquals(repository.greet(false)(using User("bdmendes")), "Hello, bdmendes!")

    assertEquals(repository.times(it.greet(_: Boolean)(using _: User)), 1)

  test("count calls on methods with variable arguments"):
    val repository =
      mock[Repository[User]].on((names: Seq[String]) => it.containsOneOf(names*)): names =>
        names.contains("bdmendes")

    val _ = repository.containsOneOf("bdmendes", "apmendes")

    assertEquals(repository.times((names: Seq[String]) => it.containsOneOf(names*)), 1)

  test("count calls on overloaded methods"):
    val repository = mock[Repository[User]].on(it.contains(_: String))(_ => true)

    val _ = repository.contains("bdmendes")
    val _ = repository.contains(mockUsers.head)

    assertEquals(repository.times(it.contains(_: String)), 1)

  test("throw on unknown received method"):
    def merge(x: User, y: User) = User(x.username + y.username)

    intercept[UnknownMethod.type]:
      val _ = mock[Repository[User]].on(merge)(_ => mockUsers.head)

    // It also happens frequently that a method with contextuals gets eta-expanded
    // in a way that's not intended due to an implicit capture.
    given User = mockUsers.head

    intercept[UnknownMethod.type]:
      val _ = mock[Repository[User]].on(it.greet)(_ => "hi!")

  test("throw on reasoning on unstubbed methods"):
    val repository = mock[Repository[User]].on(() => it.get)(_ => List.empty)

    // We should not be able to reason about unstubbed methods.
    intercept[UnstubbedMethod.type]:
      repository.times(it.getWith)

    intercept[UnstubbedMethod.type]:
      repository.calls(it.getWith)

    // Although we cannot be sure if there is a matching stub.
    repository.times(() => it.getNames)

    // Unstubbed method verification is disabled in relaxed mode.
    new Smockito(SmockitoMode.Relaxed):
      repository.times(it.getWith)
      repository.calls(it.getWith)

  test("provide a forward sugar for spying on a real instance"):
    val repository =
      new Repository[User]("dummy"):
        override def get: List[User] = mockUsers
        override def getNames: List[String] = mockUsers.map(_.username)
        override def exists(username: String): Boolean = getNames.contains(username)
        override def contains(user: User): Boolean = mockUsers.contains(user)
        override def contains(username: String): Boolean = exists(username)
        override def containsOneOf(username: String*): Boolean = username.exists(contains)
        override def getWith(startsWith: String, endsWith: String): List[User] =
          mockUsers.filter: user =>
            user.username.startsWith(startsWith) && user.username.endsWith(endsWith)
        override def getWithDefaults(
            startsWith: String,
            endsWith: Option[String] = None
        ): List[User] = getWith(startsWith, endsWith.getOrElse(""))
        override def getWithCurried(startsWith: String)(endsWith: String): List[User] =
          getWith(startsWith, endsWith)
        override def greet(upper: Boolean)(using user: User): String =
          val s = s"Hello, ${user.username}!"
          if upper then
            s.toUpperCase
          else
            s

    val mockRepository = mock[Repository[User]].forward(it.exists, repository)

    assert(mockRepository.exists("bdmendes"))
    assert(!mockRepository.exists("luismontenegro"))

    assertEquals(mockRepository.calls(it.exists), List("bdmendes", "luismontenegro"))

    assert(repository.exists("bdmendes"))
    assert(!repository.exists("luismontenegro"))

    // Invocations of the real instance are not intercepted.
    assertEquals(mockRepository.times(it.exists), 2)

    // This method was not forwarded, so expect the sentinel Mockito response.
    assertEquals(mockRepository.get, null)

    // One should not be able to reason about unstubbed methods, even in this forwarding scenario.
    intercept[UnstubbedMethod.type]:
      mockRepository.times(it.getWith)

  test("integrate with an effects system"):
    given ExecutionContext = ExecutionContext.global

    // Let's simulate cats-effect `IO`.
    class IO[+V](val execute: () => Future[V]):
      def flatMap[T](f: V => IO[T]): IO[T] =
        new IO(() => execute().flatMap(res => f(res).execute()))
      def map[T](f: V => T): IO[T] = new IO(() => execute().map(f))
      def unsafeRunSync() = Await.result(execute(), 1.second)

    object IO:
      def apply[V](v: => V) = pure(v)
      def pure[V](v: => V) = new IO(() => Future(v))
      def raiseError[V](e: => Throwable) = new IO[V](() => Future.failed(e))

    // An "effectful" class.
    abstract class EffectRepository[T]:
      def exists(username: String): IO[Boolean]

    val repository =
      mock[EffectRepository[User]].on(it.exists):
        case "bdmendes" =>
          IO(true)
        case _ =>
          IO.raiseError(new IllegalArgumentException("Unexpected user"))

    intercept[IllegalArgumentException]:
      val errored =
        for
          _ <- repository.exists("apmendes")
          bd <- repository.exists("bdmendes")
        yield bd
      val _ = errored.unsafeRunSync()

    assert(repository.exists("bdmendes").unsafeRunSync())

  test("support defining stubs outside of declaring scope"):
    trait MockData:
      lazy val repository = mock[Repository[User]]

    new MockData:
      repository.on(it.getWith)(_ => List.empty)
      assertEquals(repository.getWith("bd", "mendes"), List.empty)

  test("support calling a real method that dispatches to a stub"):
    abstract class Getter:
      def getNames: List[String]
      def getNamesAdapter = getNames
      def getNamesAdapterWithParam(dummy: String) = getNames

    val names = mockUsers.map(_.username)
    val getter = mock[Getter].on(() => it.getNames)(_ => names)

    assertEquals(getter.getNamesAdapter, names)
    assertEquals(getter.getNamesAdapterWithParam("dummy"), names)

    assertEquals(getter.times(() => it.getNames), 2)

  test("not call the real method as a side effect of stubbing"):
    var tracker = 0

    class Getter:
      def find(name: String): Boolean =
        tracker += 1
        true
      def get: List[String] =
        tracker += 1
        List.empty

    val getter = mock[Getter]

    val _ = getter.on(it.find)(_ => false)
    assertEquals(tracker, 0)

    val _ = getter.on(() => it.get)(_ => List.empty)
    assertEquals(tracker, 0)

object SmockitoSpec:

  abstract class Repository[T](val name: String):
    val longName = s"${name}Repository"
    def get: List[T]
    def getNames: List[String]
    def exists(username: String): Boolean
    def contains(user: User): Boolean
    def contains(username: String): Boolean
    def containsOneOf(username: String*): Boolean
    def getWith(startsWith: String, endsWith: String): List[T]
    def getWithDefaults(startsWith: String, endsWith: Option[String] = None): List[T]
    def getWithCurried(startsWith: String)(endsWith: String): List[T]
    def greet(upper: Boolean)(using T): String

  class Service[T](repository: Repository[T]):
    def getWith(f: T => Boolean): List[T] = repository.get.filter(f)
    def exists(username: String): Boolean = repository.exists(username)

  case class User(username: String)

  private val mockUsers =
    List(User("bdmendes"), User("apmendes"), User("sirze01"), User("fernandorego"))
