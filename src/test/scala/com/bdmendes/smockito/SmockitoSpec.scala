package com.bdmendes.smockito

import com.bdmendes.smockito.SmockitoSpec.*
import org.mockito.Mockito
import scala.compiletime.testing.typeChecks

class SmockitoSpec extends munit.FunSuite with Smockito:

  test("wrap a raw Mockito instance"):
    val repository = mock[Repository[User]]

    // A Mock[T] is implicitly converted to a `T`, so one can use Mockito methods on it.
    // Of course, that is not the purpose of this library, but this is a showcase.
    Mockito.when(repository.get).thenReturn(mockUsers)
    Mockito.when(repository.exists("bdmendes")).thenReturn(true)

    val service = Service(repository)

    assertEquals(
      service.getWith(_.username.contains("mendes")).toSet,
      Set(User("bdmendes"), User("apmendes"))
    )
    assert(service.exists("bdmendes"))

    Mockito.verify(repository).exists("bdmendes")

  test("provide a method to setup partial method stubs, on methods with 1 parameter"):
    val repository =
      mock[Repository[User]].on(_.exists) {
        case name if name.endsWith("mendes") =>
          true
      }

    assert(typeChecks("repository.on(_.exists) { case \"bdmendes\" => true }"))
    assert(typeChecks("repository.on(_.exists) { _ => true }"))
    assert(!typeChecks("repository.on(_.exists) { case \"bdmendes\" => 1 }"))
    assert(!typeChecks("repository.on(_.exists) { case 1 => \"bdmendes\" }"))

    assertEquals(repository.exists("bdmendes"), true)
    intercept[IllegalArgumentException](repository.exists("spider"))

//  test("provide a method to setup partial method stubs, on methods with 2 parameters"):
//    val a = mock[Repository[User]].getWith
//    val repository =
//      mock[Repository[User]].on(_.getWith) { case (start: String, end: String) =>
//        mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
//      }
//
//    assertEquals(repository.getWith("bd", ""), List(User("bdmendes")))
//    assertEquals(repository.getWith("", "mendes"), List(User("bdmendes"), User("apmendes")))

object SmockitoSpec:

  abstract class Repository[T](val name: String):
    val longName = s"${name}Repository"
    def get: List[T]
    def exists(username: String): Boolean
    def getWith(startsWith: String, endsWith: String): List[T]

  class Service[T](repository: Repository[T]):
    def getWith[K](f: T => Boolean): List[T] = repository.get.filter(f)
    def exists(username: String): Boolean = repository.exists(username)

  case class User(username: String)

  private val mockUsers =
    List(User("bdmendes"), User("apmendes"), User("sirze01"), User("fernandorego"))
