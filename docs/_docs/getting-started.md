# Getting Started

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
