# Changelog

<!--
Use the following schema when setting up the Changelog for a new release.

## Major-Minor-Patch - YYYY-MM-DD

Description.

### What's Changed

* A commit by @johndoe in https://github.com/bdmendes/smockito/pull/x

**Full Changelog**: https://github.com/bdmendes/smockito/compare/<prev>...<this>
-->

## 1.1.0 - 2025-07-19

This release adds a parameter to the `Smockito` trait named `mode`, with default value `Strict`. By changing it to `Relaxed`, Smockito will refrain from performing its opinionated soundness verifications. This might be useful when migrating from other mocking frameworks that incentivize a different test architecture.

In addition, `Mock[T]` is now a proper subtype of `T`, rather than being implicitly converted to `T` as needed. This change avoids previously encountered quirks when overriding a field of type `T` with a `Mock[T]`.

## What's Changed
* Remove some unneeded `ClassTag` and `Tuple.Size` requirements by @bdmendes in https://github.com/bdmendes/smockito/pull/10
* Make Mock[T] a proper subtype of T by @bdmendes in https://github.com/bdmendes/smockito/pull/12
* Allow disabling strict mode by @bdmendes in https://github.com/bdmendes/smockito/pull/13

**Full Changelog**: https://github.com/bdmendes/smockito/compare/v1.0.0...v1.1.0

## 1.0.0 - 2025-07-12

Smockito is a tiny Scala facade for Mockito. In this first release, it provides a `Mock` type, that wraps a raw Mockito instance, with a `on`, `calls` and `times` methods, that desugar into `when` and `verify` Mockito calls, respectively, at the call site.
