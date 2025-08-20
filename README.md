# Smockito

<img src="./assets/logo.svg" width="125" height="125" align="right">

[![Build](https://img.shields.io/github/actions/workflow/status/bdmendes/smockito/ci.yml)](https://github.com/bdmendes/smockito/actions)
[![Codecov](https://img.shields.io/codecov/c/github/bdmendes/smockito/master)](https://app.codecov.io/gh/bdmendes/smockito)
[![Maven Central](https://img.shields.io/maven-central/v/com.bdmendes/smockito_3)](https://central.sonatype.com/artifact/com.bdmendes/smockito_3/overview)
[![Javadoc](https://javadoc.io/badge2/com.bdmendes/smockito_3/javadoc.svg)](https://javadoc.io/doc/com.bdmendes/smockito_3)

Smockito is a tiny framework-agnostic Scala 3 facade for [Mockito](https://github.com/mockito/mockito). It enables setting up unique method and value stubs for any type in a type-safe manner, while providing an expressive interface for inspecting received arguments and call counts.

<br clear="right">

## Motivation

Even when software components make use of proper dependency injection, a mocking framework is useful as a construction and interception sugar. [scalamock](https://scalamock.org/) is an excellent native tool for that use case, but has [its limitations](https://scalamock.org/faq#what-is-not-mockable). [Mockito](https://github.com/mockito/mockito), on the other hand, is very powerful and popular, but exposes a Java API that arguably does not fit well with Scala's expressiveness and safety.

Smockito leverages a subset of Mockito’s features and offers a minimal, opinionated interface, guided by a few core principles:

- A method should be stubbed only once, and use the same implementation for the lifetime of the mock.
- A method stub should handle only the inputs it expects.
- A method stub should always be executed, as the real method would.
- An unstubbed method must throw and not return a lenient sentinel value.

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

## FAQ

### Does Smockito support Scala 2?

No. Smockito leverages a handful of powerful Scala 3 features, such as inlining, opaque types, context functions and match types. If you are on the process of migrating a Scala 2 codebase, it might be a good opportunity to replace the likes of [specs2-mock](https://mvnrepository.com/artifact/org.specs2/specs2-mock) or [mockito-scala](https://github.com/mockito/mockito-scala) as you migrate your modules.

### Is this really a mocking framework?

This is a [facade](https://en.m.wikipedia.org/wiki/Facade_pattern) for Mockito, which in itself is technically a [test spy framework](https://github.com/mockito/mockito/wiki/FAQ#is-it-really-a-mocking-framework). There is a great debate regarding the definitions of mocks, stubs, spies, test duplicates... Here, we assume a mock to be a "faked" object, and a stub a provided implementation for a subset of the input space.

### How do I spy on a real instance?

Though not the main Smockito use case, you may achieve so by setting up a stub on a mock that *forwards* to a real instance:

```scala
val repository =
  val realInstance = Repository.fromDatabase[User]
  mock[Repository[User]].forward(it.exists, realInstance)

assert(repository.times(it.exists) == 0)
```

That said, make sure you also test the real instance in isolation.

### Is Smockito compatible with effect systems?

Yes. Implement your stub as you would in application code. For example, with [cats-effect](https://github.com/typelevel/cats-effect):

```scala
abstract class EffectRepository[T]:
  def exists(username: String): IO[Boolean]

val repository =
  mock[EffectRepository[User]].on(it.exists):
    case "johndoe" =>
      IO(true)
    case _ =>
      IO.raiseError(IllegalArgumentException("Unexpected user"))
```

Notice we are handling partiality explicitly. This is useful if you don't want Smockito to throw `UnexpectedArguments` behind the scenes.

### How do I reset a mock?

Don't. Instead of clearing history on a global mock, create a fresh mock for each test case. This approach avoids race conditions entirely, with a negligible performance cost.

### Should I override stubs to change behavior?

No. It's always best to define a unique stub and be explicit about behavior change. If you want to mock system state, keep things simple:

```scala
val repository = 
  var callCount = 0
  mock[Repository[User]].on(() => it.get): _ =>
    callCount += 1
    if (callCount == 1) then List(User("john")) else List.empty
```

If you have a mock whose setup is only slightly changed between test cases, instead of overriding a stub defined in some base trait, create a factory method:

```scala
def mockRepository(username: String): Mock[Repository[User]] =
  mock[Repository[User]]
    .on(() => it.get)(_ => List(User("johndoe")))
    .on(it.exists)(_ == "johndoe")
    .on(it.greet()(using _: User))(_ => s"Hello, $username!")
```

### What happens if I call an unstubbed method?

An unstubbed method call will throw an `UnstubbedMethod` exception. This decision is based on the belief that returning a lenient value would reduce test readability and increase the likelihood of bugs.

Even so, you may want to dispatch an adapter method to its actual implementation in order to stub a method at the bottom of the hierarchy. This can be achieved using `real`:

```scala
trait Getter:
  def getNames: List[String]
  def getNamesAdapter(setting: String) = getNames

val getter = mock[Getter]
  .on(() => it.getNames)(_ => List("john"))
  .real(it.getNamesAdapter)

assert(getter.getNamesAdapter("dummy") == List("john"))
assert(getter.times(() => it.getNames) == 1)
```

### I need to assert invocation orders/X/Y/Z.

You may fall back to the Mockito API anytime you see fit; a `Mock[T]` may be passed safely. Smockito wants to be as small as possible, but if there is an interesting new use case you'd want to see handled here, please open an issue.

### I can't seem to stub a method/I found a bug.

Are you performing eta-expansion correctly? If everything looks fine on your side, please file an issue with a minimal reproducible example.

### What can I do with the source code?

Mostly anything you want to. Check the license. All contributions are appreciated.

## Special Thanks

- [Mockito](https://github.com/mockito/mockito): for the reliable core.
- [Scalamock](https://scalamock.org/): for the excellent Stubs API design that inspired this library.
- [@biromiro](https://github.com/biromiro): for designing the cute cocktail logo.
