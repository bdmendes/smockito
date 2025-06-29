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

  test("wrap a Mockito instance") {
    val repository = mock[Repository[User]]
    Mockito.when(repository.inner.get).thenReturn(mockUsers)

    val service = Service(repository.inner)

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
