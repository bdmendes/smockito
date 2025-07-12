# Smockito

<img src="./assets/logo.svg" width="120" height="120" align="right">

[![Build](https://img.shields.io/github/actions/workflow/status/bdmendes/smockito/ci.yml)](https://github.com/bdmendes/smockito/actions)
[![Codecov](https://img.shields.io/codecov/c/github/bdmendes/smockito/master)](https://app.codecov.io/gh/bdmendes/smockito)
[![Release](https://img.shields.io/github/v/release/bdmendes/smockito?color=orange)](https://github.com/bdmendes/smockito/releases)
<!-- [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.bdmendes.smockito/smockito_3/badge.svg)](https://search.maven.org/artifact/com.bdmendes.smockito/smockito_3) --!>
<!-- [![Scaladoc](https://javadoc.io/badge/com.bdmendes.smockito/smockito_3.svg)](https://javadoc.io/page/com.bdmendes.smockito/smockito_3/latest/smockito/index.html) -->

Smockito is a tiny framework-agnostic Scala 3 facade for [Mockito](https://github.com/mockito/mockito). It enables setting up unique method stubs for any type in a type-safe manner, while providing an expressive interface for inspecting received arguments and call counts.

<br clear="right">

## Motivation

Even when software components make use of proper dependency injection, a mocking framework is useful as a construction and interception sugar. [scalamock](https://scalamock.org/) is an excellent native tool for that use case, but has [its limitations](https://scalamock.org/faq#what-is-not-mockable). [Mockito](https://github.com/mockito/mockito), on the other hand, is very powerful and popular, but exposes a Java API that arguably does not fit well with Scala's expressiveness and safety.

Smockito leverages a subset of Mockito’s features and offers a minimal, opinionated interface, guided by a few core principles:

- A method should be stubbed only once, and use the same implementation for the lifetime of the mock.
- A method stub should handle only the inputs it expects; errors should be handled by the framework.
- A method stub should always be executed, as the real method would.
- One may reason directly about the received arguments and number of calls of a stub.
- One should not reason about the history of a method that was not stubbed.

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

In your specification, extend `Smockito`. This will bring the `mock` method and relevant conversions to scope. To set up a mock, add stub definitions with the `on` method, which requires an [eta-expanded](https://docs.scala-lang.org/scala3/book/fun-eta-expansion.html) method reference, that you may easily express with `it`, and a [partial function](https://docs.scala-lang.org/scala3/book/fun-partial-functions.html) on the tupled arguments to handle the relevant inputs.

```scala
abstract class Repository[T](val name: String):
    def get: List[T]
    def exists(username: String): Boolean
    def getWith(startsWith: String, endsWith: String): List[T]

case class User(username: String)

class RepositorySpecification extends Smockito:
    val repository = mock[Repository[User]]
        .on(() => it.get)(_ => List(User("johndoe")))
        .on(it.getWith) { 
            case ("john", name) if name.nonEmpty => List(User("johndoe"))
        } // Mock[Repository[User]]
```

A `Mock[T]` is a `T` both at compile and runtime.

```scala
    assert(repository.getWith("john", "doe") == List(User("johndoe")))
```

You may reason about method interactions with `calls` and `times`. If arguments are not needed, `times` is more efficient.

```scala
    assert(repository.calls(it.getWith) == List(("john", "doe")))
    assert(repository.times(it.getWith) == 1)
```

## FAQ

### Does Smockito support Scala 2?

No. Smockito leverages a handful of powerful Scala 3 features, such as inlining, opaque types and contextual functions. If you are on the process of migrating a Scala 2 codebase, it might be a good opportunity to replace the likes of [specs2-mock](https://mvnrepository.com/artifact/org.specs2/specs2-mock) or [mockito-scala](https://github.com/mockito/mockito-scala) as you migrate your modules.

### Is this really a mocking framework?

This is a facade for Mockito, which in itself is technically a [test spy framework](https://github.com/mockito/mockito/wiki/FAQ#is-it-really-a-mocking-framework). There is a great debate regarding the definitions of mocks, stubs, spies, test duplicates... Here, we assume a mock to be a "faked" object, and a stub a provided implementation for a subset of the input space.

### What should I mock?

A matter of personal taste. Arguably, the bare minimum to increase your test surface. If possible, use simple higher-order functions and/or traits to inject behaviour. Read the [Mockito wiki guide](https://github.com/mockito/mockito/wiki/How-to-write-good-tests) for more opinions on the matter.

### Is Smockito thread-safe?

[As thread-safe as Mockito](https://github.com/mockito/mockito/wiki/FAQ#is-mockito-thread-safe).

### I need to override stubs/assert invocation orders/X/Y/Z.

You may fallback to the Mockito API anytime you see fit; a `Mock[T]` may be passed safely. Smockito wants to be as small as possible, but if there is an interesting new use case you'd want to see handled here, please open an issue.

### I can't seem to stub a method/I found a bug.

Are you performing eta-expansion correctly? If everything looks fine on your side, please file an issue with a minimal reproducible example.

### What can I do with the source code?

Mostly anything you want to. Check the license. All contributions are appreciated.

## Special Thanks

- [Mockito](https://github.com/mockito/mockito): for the reliable core.
- [Scalamock](https://scalamock.org/): for the excellent Stubs API design that inspired this library.
- [@biromiro](https://github.com/biromiro): for designing the cute cocktail logo.
