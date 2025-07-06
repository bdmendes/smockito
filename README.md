# Smockito

<!-- <img src="" width="" height="" align="right"> !-->

[![Build Status](https://github.com/bdmendes/smockito/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/bdmendes/smockito/actions?query=workflow%3ACI+branch%3Amaster)
<!-- [![Coverage Status](https://coveralls.io/repos/bdmendes/smockito/badge.svg?branch=master)](https://coveralls.io/bdmendes/smockito?branch=master) !-->
<!-- [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.bdmendes.smockito/smockito_3/badge.svg)](https://search.maven.org/artifact/com.bdmendes.smockito/smockito_3) --!>
<!-- [![Scaladoc](https://javadoc.io/badge/com.bdmendes.smockito/smockito_3.svg)](https://javadoc.io/page/com.bdmendes.smockito/smockito_3/latest/smockito/index.html) -->

Smockito is a tiny framework-agnostic Scala 3 facade for [Mockito](https://github.com/mockito/mockito). It enables setting up unique method stubs for any type in a type-safe manner, while providing an expressive interface for inspecting received arguments and call counts.

> ⚠️ Smockito is in testing stage. The API is subject to change, and the artifacts below may not be published yet. If you are interested, all feedback is appreciated!

<br clear="right">


## Motivation

Even when designed software components make use of proper dependency injection, a mocking framework is useful as a construction and interception sugar. [scalamock](https://scalamock.org/) is an excellent native tool for that use case, but has [its limitations](https://scalamock.org/faq#what-is-not-mockable). [Mockito](https://github.com/mockito/mockito), on the other hand, is very powerful and popular, but exposes a Java API that arguably does not fit well with Scala's expressiveness and safety.

Smockito leverages a subset of Mockito’s features and offers a minimal, opinionated interface, guided by a few core principles:

- A method may be stubbed only once, and use the same implementation for the lifetime of the mock.
- A method stub should handle all inputs it is interested on; partiality is automatically handled.
- A method stub is always executed, as the real method would; there is no equivalent of the `thenReturn` Mockito counterpart.
- One may reason directly about the received arguments and number of calls of a stub.
- One may not reason about the history of a method that was not stubbed.

Some of these checks may be disabled at the call site, if needed. Check the API docs for more details.

## Quick Start

To use Smockito in an existing sbt project with Scala 3, add the following dependency to your
`build.sbt`:

```scala
libraryDependencies += "com.bdmendes" %% "smockito" % "<version>" % Test
```

Do not depend on Mockito directly.

If targeting Java 24+, you need to add the Smockito JAR as a Java agent to enable the runtime byte code manipulation Mockito depends on. If you use the [sbt-javaagent plugin](https://github.com/sbt/sbt-javaagent), you can simply add to your `build.sbt`:

```scala
javaAgents += "com.bdmendes" % "smockito_3" % "<version>" % Test
```

In your specification, extend `Smockito`. This will bring the `mock` method and relevant conversions to scope. To set up a mock, add stub definitions with the `on` method, which requires a [eta-expanded](https://docs.scala-lang.org/scala3/book/fun-eta-expansion.html) method reference, that you may easily express with `it`, and a [partial function](https://docs.scala-lang.org/scala3/book/fun-partial-functions.html) on the tupled arguments to handle the relevant inputs.

```scala
object Specification:
    abstract class Repository[T](val name: String):
        def get: List[T]
        def exists(username: String): Boolean
        def getWith(startsWith: String, endsWith: String): List[T]
    case class User(username: String)
    val mockUsers = List(User("johndoe"), User("barackobama"))

class Specification extends Smockito:
    val repository = mock[Repository[User]]
        .on(() => it.get)(_ => mockUsers)
        .on(it.exists)(args => mockUsers.map(_.username).contains(args._1))
        .on(it.getWith) { case (start, end) =>
            mockUsers.filter(u => u.username.startsWith(start) && u.username.endsWith(end))
        } // Mock[Repository[User]]
```

A `Mock[T]` is a `T` both at compile and runtime.

```scala
    assert(repository.getWith("john", "doe") == User("johndoe"))
```

You may reason about the method interactions with `calls` and `times`. If the arguments are not needed, `times` is more efficient.

```scala
    assert(repository.calls(it.getWith) == List(("john", "doe")))
    assert(repository.times(it.getWith) == 1)
```

## FAQ

### Does Smockito support Scala 2?

No. Smockito leverages a handful of powerful Scala 3 features, such as inlining, opaque types and contextual functions. If you are on the process of migrating a Scala 2 codebase, it might be a good opportunity to replace the likes of [specs2-mock](https://mvnrepository.com/artifact/org.specs2/specs2-mock) or [mockito-scala](https://github.com/mockito/mockito-scala) as you migrate your modules.

### What should I mock?

A matter of personal taste. I would say - the bare minimum to increase your test surface. If possible, use simple higher order-functions and/or traits to inject behaviour. Read the [Mockito wiki guide](https://github.com/mockito/mockito/wiki/How-to-write-good-tests) for more opinions on the matter.

### I need to override stubs/reset mocks/X/Y/Z.

You may fallback to the Mockito API anytime you see fit; a `Mock[T]` may be passed safely. The Smockito API should support most sane use cases, though. Can you express your test following the above guidelines?

### Smockito flagged *already stubbed method*/X/Y/Z incorrectly.

Smockito relies on a method's type signature to identify the stubbings in the core side. As such, e.g. if you mocked a `effect(): Unit` and a `setUp(): Unit`, both `() => Unit`, an error will be raised. In that case, disable the check at the call site. If the said behavior still does not make sense to you, you might be facing a bug.

### I can't seem to stub a method/I found a bug.

Are you performing eta-expansion correctly? If everything looks fine on your side, please file an issue with a minimal reproducible example.

### What can I do with the source code?

Mostly anything you want to. Check the license. All contributions are appreciated.

## Special Thanks

- [Mockito](https://github.com/mockito/mockito): for the reliable core.
- [Scalamock](https://scalamock.org/): for the excellent Stubs API design that inspired this library.
