# Changelog

<!--
Use the following schema when setting up the Changelog for a new release.

## Major-Minor-Patch - YYYY-MM-DD

Description.

### What's Changed

* A commit by @johndoe in https://github.com/bdmendes/smockito/pull/x

**Full Changelog**: https://github.com/bdmendes/smockito/compare/<prev>...<this>
-->

## [1.0.0] - 2025-07-12

Smockito is a tiny Scala facade for Mockito. In this first release, it provides a `Mock` type, that wraps a raw Mockito instance, with a `on`, `calls` and `times` methods, that desugar into `when` and `verify` Mockito calls, respectively, at the call site.
