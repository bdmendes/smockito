# Changelog

<!--
Use the following schema when setting up the Changelog for a new release.

## Major-Minor-Patch - YYYY-MM-DD

Description.

### What's Changed

* A commit by @johndoe in https://github.com/bdmendes/smockito/pull/x

**Full Changelog**: https://github.com/bdmendes/smockito/compare/<prev>...<this>
-->

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
