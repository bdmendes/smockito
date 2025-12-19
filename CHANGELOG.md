# Changelog

<!--
Use the following schema when setting up the Changelog for a new release.

## Major-Minor-Patch - YYYY-MM-DD

Description.

### What's Changed

* A commit by @johndoe in https://github.com/bdmendes/smockito/pull/x

**Full Changelog**: https://github.com/bdmendes/smockito/compare/<prev>...<this>
-->

## 2.3.1 - 2025-12-19

This release bumps Mockito to version 5.21. In addition, the documentation was vastly improved. Check out the new [microsite](https://javadoc.io/doc/com.bdmendes/smockito_3/latest/docs/index.html)!

### What's Changed
* Bump Mockito to 5.21 by @bdmendes in https://github.com/bdmendes/smockito/pull/126
* Simplify docs and hide MockedMethod extensions by @bdmendes in https://github.com/bdmendes/smockito/pull/129
* Bootstrap microsite by @bdmendes in https://github.com/bdmendes/smockito/pull/130
* Complete microsite and update doc refs by @bdmendes in https://github.com/bdmendes/smockito/pull/131

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v2.3.0...v2.3.1

## 2.3.0 - 2025-12-03

This release introduces the `spy` convenience method that delegates to a Mockito spy, returning a `Spy[T]`. In the Smockito land, a `Spy[T]` is also a `Mock[T]`, which means that you can use the same `on`, `calls` and `times` methods to set up stubs and reason about interactions, while retaining a real instance behavior. Bear in mind that setting up stubs on spied instances is effectively partial mocking which is generally discouraged, and that a spy is not the same as forwarding all methods to a real instance – it is a deep copy of the real instance.

In addition, this release updates the Scala version to 3.3.7.

### What's Changed
* Bump Scala to 3.3.7 by @bdmendes in https://github.com/bdmendes/smockito/pull/114
* Introduce Spy by @bdmendes in https://github.com/bdmendes/smockito/pull/123

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v2.2.1...v2.3.0

## 2.2.1 - 2025-09-28

This release updates the underlying Mockito version and fixes an edge case related with implicit capturing in eta-expanded methods.

### What's Changed
* Catch specific exception on call orders by @bdmendes in https://github.com/bdmendes/smockito/pull/102
* Extract verifies utility by @bdmendes in https://github.com/bdmendes/smockito/pull/103
* Update mockito-core to 5.20.0 by @scala-steward in https://github.com/bdmendes/smockito/pull/107

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v2.2.0...v2.2.1

## 2.2.0 - 2025-08-24

This release changes the `onCall` signature to require a `Int => Pack[A] => R` instead of a `Int => R`. This should enable more use cases and discourage the liberal usage of it instead of `on`.

### What's Changed
* Use AtomicInteger on onCall by @bdmendes in https://github.com/bdmendes/smockito/pull/97
* Require function in `onCall` by @bdmendes in https://github.com/bdmendes/smockito/pull/98

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v2.1.0...v2.2.0

## 2.1.0 - 2025-08-24

This feature release adds an `onCall` convenience method to set up a stub that receives the call number instead of the method arguments, which might be useful when simulating transient failures. Besides this, there are new methods for reasoning about invocation orders on a mock — `calledBefore` and `calledAfter`.

### What's Changed
* Add `onCall` method by @bdmendes in https://github.com/bdmendes/smockito/pull/92
* Add `calledBefore` method by @bdmendes in https://github.com/bdmendes/smockito/pull/93

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v2.0.0...v2.1.0

## 2.0.0 - 2025-08-22

Smockito has been stable for a couple of weeks, benefiting from real-world usage and several bug fixes since 1.0. Now, it’s ready for an API update, a change significant enough to justify a major version bump.

The first breaking change is related to the behavior of unstubbed methods. Previously, we relied on the default sentinel Mockito answers (e.g., `false` for methods returning `Boolean` or generically `null`). While this led to terse mocking, especially for side-effectful methods, it was also confusing and could hide implementation issues if unexpected. Starting from this release, we'll throw with an `UnstubbedMethod` exception, pointing to the method name and received arguments to aid the migration and guide the user towards explicitly setting up a stub for all methods expected to be called.

Secondly, Smockito will no longer check for repeated stubbing. While useful, this was not always possible and forced the introduction of a mode parameter, which complicated test model hierarchies. As such, the `SmockitoMode` concept is now retired and `Smockito` is again a simple trait with no parameters.

At last - a new feature! `real` allows dispatching to the real implementation, which in Scala is very useful to preserve the implementation of adapter methods in traits. Refer to the README for a more detailed explanation.

An issue related to capturing default arguments was also fixed.

Happy mocking!

### What's Changed
* Fix default args by @bdmendes in https://github.com/bdmendes/smockito/pull/80
* Remove relaxed mode by @bdmendes in https://github.com/bdmendes/smockito/pull/83
* Remove unstubbed verification by @bdmendes in https://github.com/bdmendes/smockito/pull/84
* Default to throwing on unstubbed methods by @bdmendes in https://github.com/bdmendes/smockito/pull/88
* Add real implementation and document more by @bdmendes in https://github.com/bdmendes/smockito/pull/89

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.3.3...v2.0.0

## 1.3.3 - 2025-08-16

This is a maintenance release that mostly just aligns Smockito with `mockito-core` 5.19.0. In the meantime, some messages and documentation got improved.

### What's Changed
* Remove unused variables and imports by @bdmendes in https://github.com/bdmendes/smockito/pull/63
* Use optional braces around parameters syntax by @bdmendes in https://github.com/bdmendes/smockito/pull/65
* Improve unknown method error message by @bdmendes in https://github.com/bdmendes/smockito/pull/68
* Add more use cases and opinions to the README by @bdmendes in https://github.com/bdmendes/smockito/pull/69
* Update mockito-core to 5.19.0 by @scala-steward in https://github.com/bdmendes/smockito/pull/71

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.3.2...v1.3.3

## 1.3.2 - 2025-08-08

Even though the `mode` value has to be public due to Scala rules, this patch makes naming clashes less likely by renaming it to `smockitoMode`.

### What's Changed
* Rename mode value to avoid clashes by @bdmendes in https://github.com/bdmendes/smockito/pull/60

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.3.1...v1.3.2

## 1.3.1 - 2025-08-07

This release fixes an issue related to stub overrides in relaxed mode.

### What's Changed
* Fix null arg lookup in relaxed mode by @bdmendes in https://github.com/bdmendes/smockito/pull/58

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.3.0...v1.3.1

## 1.3.0 - 2025-08-07

Starting from this release, Smockito will prevent circumventing the API by making sure the received method exists on the mock class type. This is especially relevant since eta-expansion on methods with contextual parameters may capture givens in scope, which is rarely what the user wants and would raise an internal Mockito exception due to shape differences.

Besides this, an issue related to erasure of contextual parameter types was fixed.

### What's Changed
* Fix reason about contextual params by @bdmendes in https://github.com/bdmendes/smockito/pull/53
* Verify unknown method by @bdmendes in https://github.com/bdmendes/smockito/pull/54

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.2.5...v1.3.0

## 1.2.5 - 2025-08-06

Because the JVM is full of surprises, this release fixes an edge case where the lazy evaluation of a mock would raise an internal Mockito exception.

### What's Changed
* Fix Matcher initialization before mock by @bdmendes in https://github.com/bdmendes/smockito/pull/51

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.2.4...v1.2.5

## 1.2.4 - 2025-08-05

This release refines the `on` method signature. There are no changes in usage, but the updated definition should provide better compatibility with language servers and IDEs, particularly IntelliJ.

### What's Changed
* Simplify stub return witness by @bdmendes in https://github.com/bdmendes/smockito/pull/48

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.2.3...v1.2.4

## 1.2.3 - 2025-08-03

This release makes `Mock[T]` covariant in `T`, which should enable even more use cases (e.g., abstract mock set-ups). In addition, integration with effect systems is now documented.

### What's Changed
* Make Mock covariant in T by @bdmendes in https://github.com/bdmendes/smockito/pull/43
* Document and test effect systems integration by @bdmendes in https://github.com/bdmendes/smockito/pull/44

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.2.2...v1.2.3

## 1.2.2 - 2025-08-02

This release includes internal optimizations and improved documentation, namely on how to stub class values and methods with contextual parameters.

### What's Changed
* Unify tuple mapper by @bdmendes in https://github.com/bdmendes/smockito/pull/35
* Test mocking values and tuple mapper by @bdmendes in https://github.com/bdmendes/smockito/pull/37
* Remove duplicate arg type params by @bdmendes in https://github.com/bdmendes/smockito/pull/38
* Document value stubbing by @bdmendes in https://github.com/bdmendes/smockito/pull/39
* Document stubbing methods with contextuals by @bdmendes in https://github.com/bdmendes/smockito/pull/40

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.2.1...v1.2.2

## 1.2.1 - 2025-07-31

This release makes it possible to load Smockito as a Java Agent even if Mockito is not in the class path.

### What's Changed
* Piggyback to Mockito agent via reflection by @bdmendes in https://github.com/bdmendes/smockito/pull/31

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.2.0...v1.2.1

## 1.2.0 - 2025-07-21

This release updates the API for unary methods. Instead of using `Tuple1[A]`, you should now work directly with `A`. This change improves clarity and reduces coupling with internal representations.

Smockito also now prevents using `calls` on nullary methods at compile time. Please use `times` instead.

### What's Changed
* Introduce proxy `Pack` type to simplify mocking unary functions by @bdmendes in https://github.com/bdmendes/smockito/pull/25
* Disallow calls on nullary methods by @bdmendes in https://github.com/bdmendes/smockito/pull/26

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.1.1...v1.2.0

## 1.1.1 - 2025-07-20

This release adds a `forward` method on `Mock[T]` to quickly stub a method that forwards to a real instance. This is somewhat similar to a Mockito `spy`.

### What's Changed
* Add a forward method for spying by @bdmendes in https://github.com/bdmendes/smockito/pull/22

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.1.0...v1.1.1

## 1.1.0 - 2025-07-19

This release adds a parameter to the `Smockito` trait named `mode`, with default value `Strict`. By changing it to `Relaxed`, Smockito will refrain from performing its opinionated soundness verifications. This might be useful when migrating from other mocking frameworks that incentivize a different test architecture.

In addition, `Mock[T]` is now a proper subtype of `T`, rather than being implicitly converted to `T` as needed. This change avoids previously encountered quirks when overriding a field of type `T` with a `Mock[T]`.

### What's Changed
* Remove some unneeded `ClassTag` and `Tuple.Size` requirements by @bdmendes in https://github.com/bdmendes/smockito/pull/10
* Make Mock[T] a proper subtype of T by @bdmendes in https://github.com/bdmendes/smockito/pull/12
* Allow disabling strict mode by @bdmendes in https://github.com/bdmendes/smockito/pull/13

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.0.0...v1.1.0

## 1.0.0 - 2025-07-12

Smockito is a tiny Scala facade for Mockito. In this first release, it provides a `Mock` type, that wraps a raw Mockito instance, with a `on`, `calls` and `times` methods, that desugar into `when` and `verify` Mockito calls, respectively, at the call site.
