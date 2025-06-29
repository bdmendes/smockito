package com.bdmendes.smockito

import com.bdmendes.smockito.SmockitoSpec.{Repository, Service, User}
import org.mockito.Mockito

class SmockitoSpec extends munit.FunSuite with Smockito:
  val mockUsers = List(
    User("bdmendes"),
    User("apmendes"),
    User("sirze01"),
    User("fernandorego")
  )

  test("wrap a raw Mockito instance") {
    val repository = mock[Repository[User]]

    // A Mock[T] is implicitly converted to a `T`, so one can use Mockito
    // methods on it.
    // Of course, that is not the purpose of this library, but this is a
    // showcase.
    Mockito.when(repository.get).thenReturn(mockUsers)

    val service = Service(repository)

    assertEquals(
      service.getWith(_.username.contains("mendes")).toSet,
      Set(User("bdmendes"), User("apmendes"))
    )
  }

object SmockitoSpec:
  abstract class Repository[T](val name: String):
    val longName = s"${name}Repository"
    def get: List[T]

  class Service[T](repository: Repository[T]):
    def getWith[K](f: T => Boolean): List[T] =
      repository.get.filter(f)

  case class User(username: String)
