# Testing with Mocks

Software is tested so that we can have confidence that it behaves as expected, under a variety of conditions. While testing small and independent components is usually pretty straightforward, testing components that depend on other parts of the system can be more challenging.

*Integration tests* are one way to test such components, by exercising them together with their dependencies using close to production configurations. However, integration tests can be slow, brittle, and hard to set up, making them less suitable for frequent execution during development. They also do not allow a proper incremental verification of a new component under development.

As such, most often we resort to *unit tests* for testing individual components in isolation.

## Running Example

Say our platform boasts a database of users, with account information and links to activity. In backend code (using a simplified version of [Apache Pekko HTTP](https://github.com/apache/pekko-http)), it probably will look something like this:

```scala
class UsersDatabase(ctx: PostgresContext) extends Database[User]:
  override val table = ctx.getTable("users")
  def getUsers(limit: Int): List[User] = table.get.limit(limit)
  def updatePassword(user: User, password: String): Boolean = 
    table.upsert(user.updated(password = password.hashed))
```

In front of that we’ll have a user-facing API that the frontend communicates with, and that requires a database.

```scala
class UsersService(database: UsersDatabase) extends RestApi:
  val usersRoute = get:
    complete(database.getUsers(limit = params.get("limit")))
```

We now want to test this integration in our unit tests.

```scala
class UsersServiceSpec extends Specification:
  val mockUsers = List(User("jose"), User("bruno"))

  test("retrieve the first 10 known users"):
    val service = UsersService(???) // how to inject a `UsersDatabase`?
    assertEquals(service.usersRoute.get("limit" -> 10).await, mockUsers)
```

Notice what we are doing:

- We want to test that for *some example input*, in this case a `limit` of `10`, for *some current system state*, in this case a database with two users, the `UsersService` behaves as expected, returning the two known users.
- We need to know how to inject a database into the service, and are wondering *how* and *if* to do that. For starters, we don't have an obvious way to create a `PostgresContext` in tests.

## The Case for Mocks

In this day and age, hardware, cloud infrastructure, and containerization have made it easier to do things like spinning up temporary external services for testing. There are even nice abstractions like [Testcontainers](https://www.testcontainers.org/) that make it easy to manage such resources in tests. As such, in the running example, we could spin up a temporary PostgreSQL instance, populate it with some test data, and have our `UsersService` connect to it.

This approach is perfectly valid, and in fact we should have some (more end-to-end) tests that do exactly that. However, there are also some things to consider:

- **Verbosity**: preparing and tearing down external resources adds a lot of boilerplate to tests, making them harder to read and maintain. Here we are interested in testing the `UsersService`, not PostgreSQL setup or the `UsersDatabase` layer.
- **Correctness**: if the `UsersDatabase` component has bugs, they may affect our tests in unexpected ways, including making them **pass** when they should **fail**. We want to test the `UsersService` in isolation, not the `UsersDatabase` (or any intermediate logic layer) implementation.

To address these concerns, *mocks* are a great tool. A *mock* (also called a *test double*) is a lightweight implementation of a dependency that we can use in tests to simulate specific behaviors and states, without the overhead of setting up real external dependencies.

> If you are a fan of Martin Fowler's work, you might be wondering about the difference between *mocks*, *stubs* and *fakes*. In Smockito, we use the term *mock* to refer to any test double that allows us to define custom behavior for its methods, regardless of whether we are verifying interactions or just providing canned responses.

Mocking frameworks like Smockito make it easy to create and configure mocks, allowing us to focus on the behavior we want to test without needlessly extracting interfaces or writing boilerplate code for test purposes only.

## What Should I Mock?

Mock with parsimony, at the "edges" of your system. In our running example, the `UsersService` depends on a `UsersDatabase`, which is an external dependency that we want to isolate from our tests. Therefore, it makes sense to mock the `UsersDatabase` component. It would not make sense to mock the `UsersService` itself, as that is the component we are trying to test. It also does not make sense to mock example inputs, since those are typically simple data structures that do not have complex behavior.

Mock legacy components more than new ones. In a modern, FP-inspired codebase, components are often designed to be small, pure, and composable, making them easier to test in isolation without ceremony or complicated setups. Mocking something at the middle of your abstraction stack is often a sign that the component could be refactored into smaller, more testable pieces.

# Alternative Mocking Frameworks

In Scala, one may resort to Java mocking frameworks like [Mockito](https://site.mockito.org/) or to Scala-specific ones like [Scalamock](https://scalamock.org/). Those are both great tools, but they have some limitations that Smockito aims to address.

## Mockito

Mockito has a very powerful API and a large community. It has evolved over many years, and is battle-tested in a variety of scenarios. However, being a Java framework, it does not leverage Scala's type system to provide type-safe mocking capabilities. This often leads to runtime errors that could have been caught at compile time.

For the running example, one could do:

```scala
class UsersServiceSpec extends Specification:
  val mockUsers = List(User("jose"), User("bruno"))

  def setUpMockDatabase(): UsersDatabase =
    val database = Mockito.mock(classOf[UsersDatabase])
    Mockito
      .when(database.getUsers(ArgumentMatchers.any[Int]))
      .thenReturn(mockUsers)
    database

  test("retrieve the first 10 known users"):
    val service = UsersService(setUpMockDatabase())
    assertEquals(usersRoute.get("limit" -> 10).await, mockUsers)
```

Except for the verbosity, this is fine. However, we can very easily do nasty things with the Mockito API, that would only be caught at runtime:

```scala
def setUpMockDatabase(): UsersDatabase =
  val database = Mockito.mock(classOf[UsersDatabase])
  Mockito
    .when(database.getUsers(ArgumentMatchers.any[Int]))
    .thenAnswer(_ => "a chess board") // the return type is not strongly typed
  database
```

Another issue is related to the default behavior of mocks. In Mockito, if a method that has not been explicitly stubbed is called, it returns a default value (e.g., `null` for reference types, `0` for numeric types). This can lead to tests passing when they should fail, as the mock may return unexpected values that do not reflect the intended behavior, which might be for that code path not to be executed at all. If a method should return e.g. an empty collection for the tested scenario, I believe it is better to be explicit about that in the test setup.

## Scalamock

Scalamock is a native solution, with a great API and very sane defaults. In the running example, one could set up the database like so:

```scala
def setUpMockDatabase(): UsersDatabase =
  val database = stub[UsersDatabase]
  database.getUsers.returnsWith(mockUsers)
  database
```

However, at runtime we find through a `NullPointerException` that the `UsersDatabase` type is not mockable since it is a class, not a trait or abstract class, that evaluates a value dependent on a real `PostgresContext`. This is a common limitation in Scala mocking frameworks, as they often rely on proxying or subclassing to create mocks, which requires the target type to be open for extension. The only solution would be to extract a common interface for `UsersDatabase`, which is extra boilerplate and indirection just for testing purposes: in reality, we won't have any other implementation of `UsersDatabase` in production code.

# The Smockito Approach

Smockito desugars to Mockito calls in a way that leverages stronger type safety guarantees provided by Scala 3, while providing a more idiomatic and expressive API. It is also opinionated in that it configures mocks to throw an `UnexpectedArguments` exception when a method is called with arguments that have not been explicitly handled, and `UnstubbedMethod` when a method that has not been stubbed is called. This way, tests fail fast and loudly when the mock is used in unexpected ways.

Smockito is a small, pragmatic library that tries to solve a real world problem using a powerful backend (Mockito) and taking inspiration from a great API (Scalamock). Check the [Getting Started](getting-started.md) guide for a quick introduction, and then head to the [Guide](guide.md) for a deeper dive into Smockito's capabilities.
