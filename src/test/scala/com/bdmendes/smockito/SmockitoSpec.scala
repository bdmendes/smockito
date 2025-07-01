package com.bdmendes.smockito

import com.bdmendes.smockito.SmockitoSpec.*
import org.mockito.Mockito

class SmockitoSpec extends munit.FunSuite with Smockito:

  test("wrap a raw Mockito instance"):
    val repository = mock[Repository[User]]

    // A Mock[T] is implicitly converted to a `T`, so one can use Mockito
    // methods on it.
    // Of course, that is not the purpose of this library, but this is a
    // showcase.
    Mockito.when(repository.get).thenReturn(mockUsers)
    Mockito.when(repository.exists("bdmendes")).thenReturn(true)

    val service = Service(repository)

    assertEquals(
      service.getWith(_.username.contains("mendes")).toSet,
      Set(User("bdmendes"), User("apmendes"))
    )
    assert(service.exists("bdmendes"))

    Mockito.verify(repository).exists("bdmendes")

object SmockitoSpec:

  abstract class Repository[T](val name: String):
    val longName = s"${name}Repository"
    def get: List[T]
    def exists(username: String): Boolean

  class Service[T](repository: Repository[T]):
    def getWith[K](f: T => Boolean): List[T] = repository.get.filter(f)
    def exists(username: String): Boolean = repository.exists(username)

  case class User(username: String)

  private val mockUsers =
    List(
      User("bdmendes"),
      User("apmendes"),
      User("sirze01"),
      User("fernandorego")
    )
