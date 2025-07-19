package com.bdmendes.smockito

import com.bdmendes.smockito.Smockito.SmockitoException.*
import com.bdmendes.smockito.Smockito.SmockitoMode
import com.bdmendes.smockito.SmockitoSpec.*
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import scala.compiletime.summonFrom
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

  test("interoperate with raw Mockito verifications"):
    val repository = mock[Repository[User]].on(it.exists)(_ => true)

    assert(repository.exists("pedronuno"))

    Mockito.verify(repository).exists(ArgumentMatchers.any)

    assertEquals(repository.calls(it.exists), List(Tuple1("pedronuno")))

  test("be a subtype of T"):
    inline def isSubtypeOf[A, B] =
      summonFrom {
        case _: (A <:< B) =>
          true
        case _ =>
          false
      }

    assert(isSubtypeOf[Mock[User], User])
    assert(isSubtypeOf[Mock[Repository[User]], Repository[User]])
    assert(!isSubtypeOf[Mock[User], String])
    assert(!isSubtypeOf[Mock[User], Repository[User]])
    assert(!isSubtypeOf[Mock[Repository[User]], User])

  test("set up method stubs, on methods with 0 parameters"):
    val repository = mock[Repository[User]].on(() => it.get)(_ => mockUsers)

    assert(typeChecks("repository.on(() => it.get)(_ => List.empty)"))
    assert(!typeChecks("repository.on(() => it.get)(List.empty)"))
    assert(!typeChecks("repository.on(it.get)(_ => List.empty)"))
    assert(!typeChecks("repository.on(it.get)(List.empty)"))

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
    intercept[UnexpectedArguments](repository.exists("spider"))

  test("set up method stubs, on methods with 2 parameters"):
    val repository =
      mock[Repository[User]].on(it.getWith) { case (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
      }

    assert(typeChecks("repository.on(it.getWith){ case (\"bd\", \"mendes\") => List.empty }"))
    assert(!typeChecks("repository.on(it.getWith){ case (\"bd\", \"mendes\") => true }"))
    assert(!typeChecks("repository.on(it.getWith){ case (\"bd\", 1) => List.empty }"))
    assert(!typeChecks("repository.on(it.getWith){ case (\"bd\", \"mendes\") => List(1) }"))

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

    assertEquals(repository.calls(() => it.get), List(EmptyTuple))
    assertEquals(sideEffectfulCounter, 1)

    assertEquals(repository.get, List.empty)
    assertEquals(repository.get, List.empty)

    assertEquals(repository.calls(() => it.get), List(EmptyTuple, EmptyTuple, EmptyTuple))
    assertEquals(sideEffectfulCounter, 3)

  test("inspect calls, on methods with 0 parameters, even when runtime return types collide"):
    val repository =
      mock[Repository[String]]
        .on(() => it.get)(_ => List.empty)
        .on(() => it.getNames)(_ => List.empty)

    assertEquals(repository.calls(() => it.get), List.empty)
    assertEquals(repository.calls(() => it.getNames), List.empty)

    assertEquals(repository.get, List.empty)
    assertEquals(repository.getNames, List.empty)
    assertEquals(repository.getNames, List.empty)

    assertEquals(repository.calls(() => it.get), List(EmptyTuple))
    assertEquals(repository.calls(() => it.getNames), List(EmptyTuple, EmptyTuple))

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

  test("count calls, on methods with 0 parameters, even when runtime return types collide"):
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

  test("throw on repeated stub set up"):
    val repository = mock[Repository[User]].on(() => it.get)(_ => List.empty)

    // Start by setting up stubs in relaxed mode. Next stubs won't throw.
    new Smockito(SmockitoMode.Relaxed):
      repository.on(it.contains)(_ => true)
      repository.on(it.getWith)(_ => List.empty)

    repository.on(it.contains)(_ => true)
    repository.on(it.getWith)(_ => List.empty)

    // Previous stubs were set up in strict mode, so expect failures.
    intercept[AlreadyStubbedMethod] {
      repository.on(it.contains)(_ => false)
    }
    intercept[AlreadyStubbedMethod] {
      repository.on(it.getWith)(_ => List.empty)
    }

  test("throw on reasoning on unstubbed methods"):
    val repository = mock[Repository[User]].on(() => it.get)(_ => List.empty)

    // We should not be able to reason about unstubbed methods.
    intercept[UnstubbedMethod.type] {
      repository.times(it.getWith)
    }
    intercept[UnstubbedMethod.type] {
      repository.calls(it.getWith)
    }

    // Although we cannot be sure if there is a matching stub.
    repository.calls(() => it.getNames)

    // Unstubbed method verification is disabled in relaxed mode.
    new Smockito(SmockitoMode.Relaxed):
      repository.times(it.getWith)
      repository.calls(it.getWith)

object SmockitoSpec:

  abstract class Repository[T](val name: String):
    val longName = s"${name}Repository"
    def get: List[T]
    def getNames: List[String]
    def exists(username: String): Boolean
    def contains(user: User): Boolean
    def getWith(startsWith: String, endsWith: String): List[T]
    def getWithCurried(startsWith: String)(endsWith: String): List[T]
    def getWithContextual(startsWith: String)(using endsWith: String): List[T]

  class Service[T](repository: Repository[T]):
    def getWith(f: T => Boolean): List[T] = repository.get.filter(f)
    def exists(username: String): Boolean = repository.exists(username)

  case class User(username: String)

  private val mockUsers =
    List(User("bdmendes"), User("apmendes"), User("sirze01"), User("fernandorego"))
