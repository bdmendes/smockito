---
title: FAQ
---

### Does Smockito support Scala 2?

No. Smockito leverages a handful of powerful Scala 3 features, such as inlining, opaque types, context functions and match types. If you are on the process of migrating a Scala 2 codebase, it might be a good opportunity to replace the likes of [specs2-mock](https://mvnrepository.com/artifact/org.specs2/specs2-mock) or [mockito-scala](https://github.com/mockito/mockito-scala) as you migrate your modules.

### Is this really a mocking framework?

This is a [facade](https://en.m.wikipedia.org/wiki/Facade_pattern) for Mockito, which in itself is technically a [test spy framework](https://github.com/mockito/mockito/wiki/FAQ#is-it-really-a-mocking-framework). There is a great debate regarding the definitions of mocks, stubs, spies, test duplicates... Here, we assume a mock to be a "faked" object, and a stub a provided implementation for a subset of the input space.

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

### How do I spy on a real instance?

Though not the main Smockito use case, you may achieve so by setting up a stub on a mock that *forwards* to a real instance:

```scala
val repository =
  val realInstance = Repository.fromDatabase[User]
  mock[Repository[User]].forward(it.exists, realInstance)

assert(repository.times(it.exists) == 0)
```

Alternatively, create a `spy` instead:

```scala
val repository = spy(Repository.fromDatabase[User])
assert(repository.times(it.exists) == 0)
```

That said, make sure you also test the real instance in isolation.

### How do I reset a mock?

Don't. Instead of clearing history on a global mock, create a fresh mock for each test case. This approach avoids race conditions entirely, with a negligible performance cost.

### Should I override stubs to change behavior?

No. It's always best to define a unique stub and be explicit about behavior change. If you want to perform a different action on a subsequent invocation, for instance to simulate transient failures, consider using `onCall`:

```scala
val repository = 
  mock[Repository[User]].onCall(it.exists):
    case 1 | 2 => _ == "johndoe"
    case _ => _ => false
```

If you have a mock whose setup is only slightly changed between test cases, instead of overriding a stub defined in some base trait, create a factory method:

```scala
def mockRepository(username: String): Mock[Repository[User]] =
  mock[Repository[User]]
    .on(() => it.get)(_ => List(User("johndoe")))
    .on(it.exists)(_ == "johndoe")
    .on(it.greet()(using _: User))(_ => s"Hello, $username!")
```

### Can I reason about invocation orders?

Yes. Use `calledBefore` or `calledAfter`:

```scala
val repository =
  mock[Repository[User]]
    .on(it.exists)(_ => true)
    .on(() => it.get)(_ => List.empty)

val _ = repository.exists("johndoe")
val _ = repository.get

assert(repository.calledBefore(it.exists, () => it.get))
```

When doing so, consider whether this behavior is a hard requirement of your system or merely an implementation detail. If it is the latter, the assertion might be an overspecification.

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

### What happens if I stub a method more than once?

The last stub takes precedence. If possible, follow the unique stub principle.

### I can't seem to stub a method/I found a bug.

Are you performing eta-expansion correctly? Check out the main [SmockitoSpec](https://github.com/bdmendes/smockito/blob/master/src/test/scala/com/bdmendes/smockito/SmockitoSpec.scala) for more examples covering a variety of situations. If everything looks fine on your side, please file an issue with a minimal reproducible example.

### What can I do with the source code?

Mostly anything you want to. Check the license. All contributions are appreciated.
