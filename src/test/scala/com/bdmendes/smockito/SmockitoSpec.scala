package com.bdmendes.smockito

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

  test("provide a method to set up partial method stubs, on methods with 0 parameters"):
    val repository =
      mock[Repository[User]].on(() => it.get) { _ =>
        mockUsers
      }

    assert(typeChecks("repository.on(() => it.get)(_ => List.empty)"))
    assert(!typeChecks("repository.on(it.get)(_ => List.empty)"))
    assert(!typeChecks("repository.on(println)(_ => List.empty)"))

    assertEquals(repository.get, mockUsers)

  test("provide a method to set up partial method stubs, on methods with 1 parameter"):
    val repository =
      mock[Repository[User]].on(it.exists) {
        case Tuple1(name) if name.endsWith("mendes") =>
          true
      }

    assert(typeChecks("repository.on(it.exists) { case Tuple1(\"bdmendes\") => true }"))
    assert(typeChecks("repository.on(it.exists) { _ => true }"))
    assert(!typeChecks("repository.on(it.exists) { case Tuple1(\"bdmendes\") => 1 }"))
    assert(!typeChecks("repository.on(it.exists) { case 1 => \"bdmendes\" }"))
    assert(!typeChecks("repository.on(it.exists) { case Tuple1(1) => \"bdmendes\" }"))

    assertEquals(repository.exists("bdmendes"), true)
    intercept[IllegalArgumentException](repository.exists("spider"))

  test("provide a method to set up partial method stubs, on methods with 2 parameters"):
    val repository =
      mock[Repository[User]].on(it.getWith) { case (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
      }

    assertEquals(repository.getWith("bd", ""), List(User("bdmendes")))
    assertEquals(repository.getWith("", "mendes"), List(User("bdmendes"), User("apmendes")))

  test("provide a method to set up partial method stubs, on curried methods"):
    val repository =
      mock[Repository[User]].on(it.getWithCurried(_: String)(_: String)) { case (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
      }

    assertEquals(repository.getWithCurried("bd")(""), List(User("bdmendes")))
    assertEquals(repository.getWithCurried("")("mendes"), List(User("bdmendes"), User("apmendes")))

  test("provide a method to set up partial method stubs, on methods with context parameters"):
    val repository =
      mock[Repository[User]].on(it.getWithContextual(_: String)(using _: String))(_ => List.empty)

    assertEquals(repository.getWithContextual("bd")(using ""), List.empty)
    assertEquals(repository.getWithContextual("")(using "mendes"), List.empty)

  test("provide a method to inspect calls, on methods with 0 parameters"):
    var sideEffectfulCounter = 0

    val repository =
      mock[Repository[String]].on(() => it.get)(_ =>
        sideEffectfulCounter += 1
        List.empty
      )

    assert(!typeChecks("repository.calls(() => it.get)(_ => List.empty[Int])"))
    assert(!typeChecks("repository.calls(println)(_ => List.empty)"))

    assertEquals(repository.get, List.empty)

    assertEquals(repository.calls(() => it.get).size, 1)
    assertEquals(sideEffectfulCounter, 1)

    assertEquals(repository.get, List.empty)
    assertEquals(repository.get, List.empty)

    assertEquals(repository.calls(() => it.get).size, 3)
    assertEquals(sideEffectfulCounter, 3)

  test("provide a method to inspect calls, on methods with 1 parameter"):
    val repository = mock[Repository[String]].on(it.exists)(_._1 == "bdmendes")

    assertEquals(repository.calls(it.exists), List.empty)

    assert(repository.exists("bdmendes"))
    assert(!repository.exists("apmendes"))

    assertEquals(repository.calls(it.exists), List(Tuple1("bdmendes"), Tuple1("apmendes")))

  test("provide a method to inspect calls, on methods with 2 parameters"):
    val repository =
      mock[Repository[User]].on(it.getWith) { case (start, end) =>
        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
      }

    assertEquals(repository.getWith("bd", "mendes"), List(User("bdmendes")))

    assertEquals(repository.calls(it.getWith), List(("bd", "mendes")))

  test("allow chaining"):
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

object SmockitoSpec:

  abstract class Repository[T](val name: String):
    val longName = s"${name}Repository"
    def get: List[T]
    def exists(username: String): Boolean
    def getWith(startsWith: String, endsWith: String): List[T]
    def getWithCurried(startsWith: String)(endsWith: String): List[T]
    def getWithContextual(startsWith: String)(using endsWith: String): List[T]

  class Service[T](repository: Repository[T]):
    def getWith(f: T => Boolean): List[T] = repository.get.filter(f)
    def exists(username: String): Boolean = repository.exists(username)

  case class User(username: String)

  private val mockUsers =
    List(User("bdmendes"), User("apmendes"), User("sirze01"), User("fernandorego"))
