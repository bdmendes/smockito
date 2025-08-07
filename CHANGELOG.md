# Changelog

<!--
Use the following schema when setting up the Changelog for a new release.

## Major-Minor-Patch - YYYY-MM-DD

Description.

### What's Changed

* A commit by @johndoe in https://github.com/bdmendes/smockito/pull/x

**Full Changelog**: https://github.com/bdmendes/smockito/compare/<prev>...<this>
-->

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
