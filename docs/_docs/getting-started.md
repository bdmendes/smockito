# Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.bdmendes/smockito_3)](https://central.sonatype.com/artifact/com.bdmendes/smockito_3/overview)

To use Smockito in an existing sbt project with Scala 3, add the following dependency to your
`build.sbt`:

```scala
libraryDependencies += "com.bdmendes" %% "smockito" % "<version>" % Test
```

Do not depend on Mockito directly.

If targeting Java 24+, you need to add the Smockito JAR as a Java agent to enable the runtime bytecode manipulation that Mockito depends on. If you use the [sbt-javaagent plugin](https://github.com/sbt/sbt-javaagent), you can simply add to your `build.sbt`:

```scala
javaAgents += "com.bdmendes" % "smockito_3" % "<version>" % Test
```

# Quick Start

In your specification, extend `Smockito`. This will bring the `mock` method and relevant conversions to scope. To set up a mock, add stub definitions with the `on` method, which requires an [eta-expanded](https://docs.scala-lang.org/scala3/book/fun-eta-expansion.html) method reference, that you may easily express with `it`, and a [partial function](https://docs.scala-lang.org/scala3/book/fun-partial-functions.html) to handle the relevant inputs.

```scala
abstract class Repository[T](val name: String):
  def get: List[T]
  def exists(username: String): Boolean
  def greet()(using T): String
  def getWith(startsWith: String, endsWith: String): List[T]

case class User(username: String)

class RepositorySpecification extends Smockito:
  val repository = mock[Repository[User]]
    .on(() => it.name)(_ => "xpto")
    .on(() => it.get)(_ => List(User("johndoe")))
    .on(it.exists)(_ == "johndoe")
    .on(it.greet()(using _: User))(user => s"Hello, ${user.username}!")
    .on(it.getWith):
      case ("john", name) if name.nonEmpty => List(User("johndoe"))
```

A `Mock[T]` is a `T` both at compile time and runtime.

```scala
  assert(repository.getWith("john", "doe") == List(User("johndoe")))
```

You may reason about method interactions with `calls` and `times`. If arguments are not needed, `times` is more efficient.

```scala
  assert(repository.calls(it.getWith) == List(("john", "doe")))
  assert(repository.times(it.getWith) == 1)
```

# Integrating with Test Frameworks and Coding Styles

There is no special syntax required to use Smockito with different testing frameworks or coding styles. Simply extend `Smockito` in your test classes or traits, and you can use the mocking capabilities provided by Smockito seamlessly within your existing test setup. For example, with `munit`, one would do:

```scala
import munit.FunSuite

class MyTestSuite extends FunSuite with Smockito:
  test("example test"):
    val repository = mock[Repository[User]]
      .on(() => it.name)(_ => "xpto")

    assertEquals(repository.name, "xpto")
    assertEquals(repository.times(() => it.name), 1)
```

There is also no special syntax involved for dealing with effect types such as `Future`, `IO`, etc. Simply stub methods returning effect types as you would with any other return type:

```scala
import munit.CatsEffectSuite
import cats.effect.IO

class AsyncRepository:
  def computeValue(x: Int): IO[Int]

class AsyncRepositorySpecification extends CatsEffectSuite with Smockito:
  val asyncRepository = mock[AsyncRepository]
    .on(it.computeValue)(_ => IO.pure(_ * 2))
```

Then, use your test framework's capabilities to work with the effect types as needed. In the example below, we make use of `CatsEffectSuite`'s support for testing `IO` values via its custom [fixture](https://scalameta.org/munit/docs/fixtures.html).

```scala
  test("compute a value"):
    asyncRepository.computeValue(21).flatMap: result =>
      IO(assertEquals(result, 42))
```
