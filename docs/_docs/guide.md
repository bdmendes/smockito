# Referencing Methods with Eta-Expansion

Most user-facing APIs in Smockito operate with method references, of the type `Mock[T] ?=> MockedMethod[A, R]`. Let's analyze this by parts. `MockedMethod[A, R]` is a type that represents a method that takes arguments of type `A` and returns a value of type `R`; it is automatically synthesized from a regular function via implicit conversions provided by the `Smockito` trait. The `Mock[T] ?=>` part indicates that this method reference is contextually dependent on a `Mock[T]` instance, which is the mock object being configured.

When one does:

```scala
class Filter:
  def filterBy(predicate: Int => Boolean): List[Int] = ???

val filter = mock[Filter].on(it.filterBy)(_ => List(1, 2, 3))
```

`it.filterBy` is the function value resulted from the [eta-expansion](https://docs.scala-lang.org/scala3/book/fun-eta-expansion.html) of the `filterBy` method of the mocked `Filter` instance. Behind the scenes, Smockito will desugar this to something like:

```scala
val filter = Mockito.mock(classOf[Filter], DefaultThrowAnswer)
val answer =
  (invocation: InvocationOnMock) =>
    val args = invocation.getRawArguments.asInstanceOf[Int => Boolean]
    stub(using filter)(args) // in this case, stub is (_ => List(1, 2, 3))
val handle = Mockito.doAnswer(answer).when(filter)
mockedMethod(using handle).apply(ArgumentMatchers.any[Int => Boolean]())
```

## Mock context parameter

What's the deal with the [context parameter](https://docs.scala-lang.org/scala3/reference/contextual/context-functions.html)? Couldn't we just operate on `Mock[T] => MockedMethod[A, R]` and refer to self as `_` as common in many Scala APIs? We could, but there are some cases where this would confuse the compiler. For instance, if a method is overloaded, we have to explicitly specify the argument types to avoid ambiguity:

```scala
class Executor:
  def compute(x: Int): Int = ???
  def compute(x: String): String = ???

val mock = mock[Executor].on(it.compute(_: Int))(_ => 42)
```

Should we have used a regular function type and required `.on(_.compute(_: Int))`, this would result in a compilation error; the compiler would see the second `_` as the second argument of `Mock[T] => MockedMethod[A,R]`. By using a context parameter, we avoid this behavior, as the compiler knows that `it` refers to the `Mock[T]` instance in scope, and the `_` inside `compute` is part of the eta-expansion.

This idea was shamelessly borrowed from Kotlin's [implicit name of a single parameter in lambdas](https://kotlinlang.org/docs/lambdas.html#it-implicit-name-of-a-single-parameter). In the `Smockito` trait, the `it` method is simply defined as:

```scala
def it[T](using mock: Mock[T]): Mock[T] = mock
```

## Method accessor sanity check

Smockito asserts at compile time that received methods are expressions that select a method on the mock type via `it`. This is achieved with a macro that analyzes the abstract syntax tree of the method reference passed to `on`, `calls`, `times` and friends to ensure it conforms to the expected shape. If it doesn't, a compilation error is raised with a helpful message.

```scala
|    val repository = mock[Repository[User]].on(unrelated.contains)(_ => true)
|                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|Expected selection of a mockable method of Repository[User]
```

Eta-expansion in Scala has its quirks: for instance, it captures any context parameters in scope, rendering a method that effectively does not exist in the JVM bytecode and that Mockito cannot directly mock. Smockito guards against this by rejecting method references whose shape does not match the referenced method in the mocked type, directing the user to eta-expand manually.

```scala
|    trait Printer[A]
|    trait Foo:
|      def describe(upper: Boolean)(using Printer[String]): String
|
|    given Printer[String] = ???
|
|    val invalidFoo = mock[Foo].on(it.describe)(_ => "foo")
|                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|Method describe in Foo expects (Boolean, Printer[String])
|but received function expects (Boolean)
```

# Setting up Mocks

With the eta-expansion and context parameters in place, setting up mocks becomes straightforward.

In Smockito, mocks are usually created with the `mock` method, which requires a type parameter representing the class to be mocked. For example:

```scala
val filter = mock[Filter]
```

At this point, calling any method on `filter` will throw an exception, as Smockito's default behavior is to fail on unconfigured method calls. To configure a method, we use the `on` method, and a function of the same shape as the method we want to mock. For example:

```scala
val filter = mock[Filter].on(it.filterBy)(_ => List(1, 2, 3))
```

`on` returns the same mock instance, allowing for method chaining. We can set up multiple methods in a single statement:

```scala
val executor = mock[Executor]
  .on(it.compute(_: Int))(_ => 42)
  .on(it.compute(_: String))(_ => "Hello, World!")
```

As the configured method (hereby referred to as a *stub*) is a [partial function](https://docs.scala-lang.org/scala3/book/fun-partial-functions.html), it may as well only handle the inputs it *expects*:

```scala
val executor = mock[Executor]
  .on(it.compute(_: Int)):
    case 2 => 4
```

Smockito will throw an exception behind the scenes if this stub gets called with an unexpected argument (here, any integer other than `2`), making sure that all interactions with the mock are explicitly defined.

## To mutate or not to mutate

You may think of `on` as a transformer method on the mock it is called on. In this schema, setting up a stub for method `foo` on mock `m1` yields an `m2` with the same stubs as `m1` plus the new one for `foo`. This is a perfectly valid mental model, and one that incentivizes immutability and discourages shared state between tests.

That said, Smockito desugars to mutable Mockito APIs and does not create a new mock instance for each stub, so the `on` method does mutate the mock instance it is called on, and actually returns the same instance. Circumventing this would require setting up a fresh mock instance for each stub with the same stubs as the previous one, which would be less efficient and disallow use cases where one does want to mutate a mock instance.

In the general case, Smockito advocates for creating a mock once per test case, with all needed stubs configured upfront.

# Setting up Spies

In Smockito, a spy is a special kind of mock whose methods get prefilled with the behavior of a real instance. They are created with the `spy` method, which takes a real instance as an argument. For example:

```scala
val realFilter = new Filter:
  override def filterBy(predicate: Int => Boolean): List[Int] =
    List(1, 2, 3, 4, 5).filter(predicate)

val filterSpy = spy(realFilter)
```

`filterSpy` is now a copy of `realFilter`. Calling `filterSpy.filterBy(_ > 3)` will return `List(4, 5)`, as expected.

The main advantage of a spy is being able to reason about method interactions while using a real implementation, as explained in the next subsection, with minimal boilerplate. You may also configure methods to override the real behavior, just like with regular mocks, with `on`, but that leads to *partial mocking* which is generally considered a bad practice.

# Reasoning about Interactions

`mock` returns a `Mock[T]` instance, which is also a `T`, so you can use it wherever a `T` is expected. In addition, `Mock[T]` provides additional methods to reason about interactions with the mock, such as verifying how many times a method was called and with which arguments. For example, we can check that a method was called via the `times` method:

```scala
val filter = mock[Filter].on(it.filterBy)(_ => List(1, 2, 3))
val _ = filter.filterBy(_ > 2)
assert(filter.times(it.filterBy) == 1)
```

`times` returns the number of invocations of the specified method, so you can use it in regular assertions as recommended by your testing framework. The same principle applies for `calls`, which returns a well-typed list of all arguments used in invocations of the specified method:

```scala
val filter = mock[Executor].on(it.compute(_: Int))(_ => 42)
val _ = filter.compute(1)
val _ = filter.compute(2)
assert(filter.calls(it.compute(_: Int)) == List(1, 2))
```

For methods with multiple parameters, `calls` returns a tuple of arguments for each invocation.

# Other Use Cases

With the basics covered, you should be able to use Smockito for most common mocking scenarios. However, there are some additional use cases that may arise in more complex tests.

## Resetting Mocks

A specification typically includes several test cases that share some common setup. In such scenarios, you might be inclined to reuse the same mock instance across multiple tests, and look for ways to reset its state between tests. However, this approach can lead to brittle tests that are hard to reason about and is by design non-thread-safe.

As such, Smockito provides no built-in mechanism for resetting mocks. Instead, it is recommended to create a new mock instance for each test case to ensure isolation and prevent state leakage.

## Overriding Stubs

In the same spirit of avoiding shared state between tests, you should avoid overriding stubs on the same mock instance. If you need different behavior for the same method in different test cases, create separate mock instances with the desired stubs for each test. That is as simple as creating an helper method:

```scala
def mockExecutor(returnValue: Int): Mock[Executor] =
  mock[Executor].on(it.compute(_: Int))(_ => returnValue)
```

That said, if you really need to override a stub, you may do so by calling `on` again for the same method. The last stub takes precedence.

## Mocking Objects

A Scala `object` is a type with a singleton instance. If you explicitly require it as a dependency and program against it, you may mock it like any other class. For instance, if you have:

```scala
object Config:
  def getSetting(key: String): String = ???
  
class Service(config: Config.type = Config):
  def fetchData(): String =
    val url = config.getSetting("url")
    ???
```

You may mock `Config` in your tests like so:

```scala
val configMock = mock[Config.type]
  .on(it.getSetting):
    case "url" => "http://example.com"
  
val service = Service(configMock)
```

If the object is simple enough, it might be worth to depend on functions or traits instead, to make testing easier.

## Yielding Based on Call Number

Smockito provides a helper for generating a stub that changes behavior based on call number, for instance to simulate transient failures. This can be achieved using `onCall`, which expects a partial mapping from call number to stub. It will throw `UnexpectedCallNumber` if the method is called an unexpected number of times.

```scala
val executor = 
  mock[Executor].onCall(it.compute(_: Int)):
    case 1 => _ => throw RuntimeException("Boom")
    case 2 => _ * 2
```

This avoids explicitly tracking call counts in your test code, leading to cleaner and more maintainable tests.

## Calling Real Methods

In Scala, contrary to a Java interface, a trait may provide default implementations for its methods. In that case, you may want to dispatch an adapter method to its actual implementation in order to stub a method at the bottom of the hierarchy. This can be achieved using `real`:

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

## Forwarding Calls

Instead of using a spy and bringing in all behavior of a real instance, you may want to forward calls of certain methods to a real instance while keeping the rest of the mock behavior. This can be achieved using `forward`:

```scala
val realExecutor = new Executor:
  override def compute(x: Int): Int = x * 2
  override def compute(x: String): String = x.reverse
val executor = mock[Executor]
  .forward(it.compute(_: Int), realExecutor)
  .on(it.compute(_: String))(_ => "mocked")
```

This is, in a way, a form of *partial mocking*, although more explicit. Make sure to use it sparingly.

## Verifying Invocation Orders

Use `calledBefore` and `calledAfter` to verify the order of method invocations:

```scala
val executor = 
  mock[Executor]
    .on(it.compute(_: Int))(_ => 42)
    .on(it.compute(_: String))(_ => "Hello, World!")
val _ = executor.compute(1)
val _ = executor.compute("test")
assert(executor.calledBefore(it.compute(_: Int), it.compute(_: String)))
```

When doing so, consider whether this behavior is a hard requirement of your system or merely an implementation detail. If it is the latter, the assertion might be an overspecification.

## Referencing the Mocked Instance in a Stub

A reference to the mocked instance itself is available in the stubbing context of the `on` and `onCall` methods via `it`.

```scala
  trait Counter:
    def increment(): Counter

  val counter = mock[Counter].on(() => it.increment())(_ => it)
  assert(counter.increment().increment() == counter)
```
