# Smockito

<img src="./docs/_assets/logo.svg" width="125" height="125" align="right">

[![Build](https://img.shields.io/github/actions/workflow/status/bdmendes/smockito/ci.yml)](https://github.com/bdmendes/smockito/actions)
[![Codecov](https://img.shields.io/codecov/c/github/bdmendes/smockito/master)](https://app.codecov.io/gh/bdmendes/smockito)
[![Maven Central](https://img.shields.io/maven-central/v/com.bdmendes/smockito_3)](https://central.sonatype.com/artifact/com.bdmendes/smockito_3/overview)
[![Javadoc](https://javadoc.io/badge2/com.bdmendes/smockito_3/javadoc.svg)](https://javadoc.io/doc/com.bdmendes/smockito_3/latest/docs/index.html)

Smockito is a tiny framework-agnostic Scala 3 facade for [Mockito](https://github.com/mockito/mockito). It enables setting up unique method and value stubs for any type in a type-safe manner, while providing an expressive interface for inspecting received arguments and call counts.

Head to the [microsite](https://javadoc.io/doc/com.bdmendes/smockito_3/latest/docs/index.html) for the full documentation and API reference.

## Quick Start

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
  def getWith(startsWith: String, endsWith: String): List[T]

case class User(username: String)

class RepositorySpecification extends Smockito:
  val repository = mock[Repository[User]]
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
