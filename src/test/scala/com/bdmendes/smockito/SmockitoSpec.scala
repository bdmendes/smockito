package com.bdmendes.smockito

import com.bdmendes.smockito.Smockito.SmockitoException.*
import com.bdmendes.smockito.SmockitoSpec.*
import org.mockito.Mockito
import scala.compiletime.testing.typeChecks

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
    intercept[ClassCastException] {
      val _: User = users.head
    }

  test("set up method stubs, on methods with 0 parameters"):
    val repository = mock[Repository[User]].on(() => it.get)(_ => mockUsers)

    assert(typeChecks("repository.on(() => it.get)(_ => List.empty)"))
    assert(!typeChecks("repository.on(() => it.get)(List.empty)"))
    assert(!typeChecks("repository.on(it.get)(_ => List.empty)"))
    assert(!typeChecks("repository.on(it.get)(List.empty)"))
    assert(!typeChecks("repository.on(println)(_ => List.empty)"))

    assertEquals(repository.get, mockUsers)

  test("set up method stubs, on methods with 1 parameter"):
    val repository =
      mock[Repository[User]].on(it.exists) {
        case Tuple1(name) if name.endsWith("mendes") =>
          true
      }

    assert(typeChecks("repository.on(it.exists)(_._1.startsWith(\"bdmendes\"))"))
    assert(typeChecks("repository.on(it.exists) { case Tuple1(\"bdmendes\") => true }"))
    assert(typeChecks("repository.on(it.exists) { _ => true }"))
    assert(!typeChecks("repository.on(it.exists)(println)"))
    assert(!typeChecks("repository.on(it.exists) { case Tuple1(\"bdmendes\") => 1 }"))
    assert(!typeChecks("repository.on(it.exists) { case 1 => \"bdmendes\" }"))
    assert(!typeChecks("repository.on(it.exists) { case Tuple1(1) => \"bdmendes\" }"))

    assertEquals(repository.exists("bdmendes"), true)
    intercept[IllegalArgumentException](repository.exists("spider"))

  test("set up method stubs, on methods with 2 parameters"):
    val repository =
      mock[Repository[User]].on(it.getWith) { case (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
      }

    assertEquals(repository.getWith("bd", ""), List(User("bdmendes")))
    assertEquals(repository.getWith("", "mendes"), List(User("bdmendes"), User("apmendes")))

  test("set up method stubs, on curried methods"):
    val repository =
      mock[Repository[User]].on(it.getWithCurried(_: String)(_: String)) { case (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
      }

    assertEquals(repository.getWithCurried("bd")(""), List(User("bdmendes")))
    assertEquals(repository.getWithCurried("")("mendes"), List(User("bdmendes"), User("apmendes")))

  test("set up method stubs, on methods with contextual parameters"):
    val repository =
      mock[Repository[User]].on(it.getWithContextual(_: String)(using _: String))(_ => List.empty)

    assertEquals(repository.getWithContextual("bd")(using ""), List.empty)
    assertEquals(repository.getWithContextual("")(using "mendes"), List.empty)

  test("inspect calls, on methods with 0 parameters"):
    var sideEffectfulCounter = 0

    val repository =
      mock[Repository[String]].on(() => it.get)(_ =>
        sideEffectfulCounter += 1
        List.empty
      )

    assertEquals(repository.get, List.empty)

    assertEquals(repository.calls(() => it.get).size, 1)
    assertEquals(sideEffectfulCounter, 1)

    assertEquals(repository.get, List.empty)
    assertEquals(repository.get, List.empty)

    assertEquals(repository.calls(() => it.get).size, 3)
    assertEquals(sideEffectfulCounter, 3)

  test("inspect calls, on methods with 1 parameter"):
    val repository = mock[Repository[String]].on(it.exists)(_._1 == "bdmendes")

    assertEquals(repository.calls(it.exists), List.empty)

    assert(repository.exists("bdmendes"))
    assert(!repository.exists("apmendes"))

    assertEquals(repository.calls(it.exists), List(Tuple1("bdmendes"), Tuple1("apmendes")))

  test("inspect calls, on methods with 2 parameters"):
    val repository =
      mock[Repository[User]].on(it.getWith) { case (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
      }

    assertEquals(repository.getWith("bd", "mendes"), List(User("bdmendes")))
    assertEquals(repository.getWith("bd", ""), List(User("bdmendes")))

    assertEquals(repository.calls(it.getWith), List(("bd", "mendes"), ("bd", "")))

  test("count calls, on methods with 0 parameters"):
    val repository: Mock[Repository[String]] =
      mock[Repository[String]].on(() => it.get)(_ => List.empty)

    assertEquals(repository.get, List.empty)

    assertEquals(repository.times(() => it.get), 1)

    assertEquals(repository.get, List.empty)
    assertEquals(repository.get, List.empty)

    assertEquals(repository.times(() => it.get), 3)

  test("count calls, on methods with 1 parameter"):
    val repository = mock[Repository[String]].on(it.exists)(_._1 == "bdmendes")

    assertEquals(repository.calls(it.exists), List.empty)

    assert(repository.exists("bdmendes"))
    assert(!repository.exists("apmendes"))

    assertEquals(repository.times(it.exists), 2)

  test("count calls, on methods with 2 parameters"):
    val repository =
      mock[Repository[User]].on(it.getWith) { case (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
      }

    assertEquals(repository.getWith("bd", "mendes"), List(User("bdmendes")))
    assertEquals(repository.getWith("bd", ""), List(User("bdmendes")))

    assertEquals(repository.times(it.getWith), 2)

  test("chain stubs"):
    val repository =
      mock[Repository[User]]
        .on(it.exists)(name => mockUsers.map(_.username).contains(name._1))
        .on(() => it.get)(_ => mockUsers)
        .on(it.getWith) { case (start, end) =>
          mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
        }

    val service = Service(repository)

    assert(service.exists("bdmendes"))
    assert(!service.exists("luismontenegro"))
    assertEquals(service.getWith(_.username.contains("01")), List(User("sirze01")))

    assertEquals(repository.calls(it.exists), List(Tuple1("bdmendes"), Tuple1("luismontenegro")))
    assert(repository.calls(() => it.get).size == 1)

  test("alert on invalid received method"):
    val repository = mock[Repository[User]]
    val user = mockUsers.head

    // If for some reason someone forges the API, we should be alert.
    intercept[NotAMethodOnType.type] {
      repository.on(() => user.caps)(_ => "MENDES")
    }

    // Scala converts `it.get` to an `Int => User` since the method returns a `List[User]`.
    // This is quite unfortunate, so we should at least provide a useful error in runtime.
    intercept[NotAMethodOnType.type] {
      repository.on(it.get)(_ => mockUsers.head)
    }

  test("alert on reasoning on unstubbed methods"):
    val repository = mock[Repository[User]].on(() => it.get)(_ => List.empty)

    // We should not be able to reason about unstubbed methods.
    intercept[UnstubbedMethod.type] {
      assertEquals(repository.times(it.getWith), 1)
    }

    // That is even more useful when it comes to the unfortunate example above.
    intercept[UnstubbedMethod.type] {
      assertEquals(repository.calls(it.get), List.empty)
    }

    // But we can disable this check at the call site, if really wanted.
    assertEquals(repository.times(it.getWith, strict = false), 0)
    assertEquals(repository.calls(it.getWith, strict = false), List.empty)

  test("alert on duplicate stub tentative"):
    val repository =
      mock[Repository[User]]
        .on(() => it.get)(_ => List.empty)
        .on(it.exists)(args => mockUsers.exists(_.username == args._1))
        .on(it.contains)(args => mockUsers.contains(args._1))

    // One should not stub a method twice.
    intercept[AlreadyStubbedMethod.type] {
      repository.on(() => it.get)(_ => mockUsers)
    }

    // But this can be disabled in the call site, falling back to Mockito overrides.
    val _ = repository.on(() => it.get, strict = false)(_ => mockUsers)

object SmockitoSpec:

  abstract class Repository[T](val name: String):
    val longName = s"${name}Repository"
    def get: List[T]
    def exists(username: String): Boolean
    def contains(user: User): Boolean
    def getWith(startsWith: String, endsWith: String): List[T]
    def getWithCurried(startsWith: String)(endsWith: String): List[T]
    def getWithContextual(startsWith: String)(using endsWith: String): List[T]

  class Service[T](repository: Repository[T]):
    def getWith(f: T => Boolean): List[T] = repository.get.filter(f)
    def exists(username: String): Boolean = repository.exists(username)

  case class User(username: String):
    def caps = username.toUpperCase()

  private val mockUsers =
    List(User("bdmendes"), User("apmendes"), User("sirze01"), User("fernandorego"))
