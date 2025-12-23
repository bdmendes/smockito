# Does Smockito support Scala 2?

No. Smockito leverages a handful of powerful Scala 3 features, such as inlining, opaque types, context functions and match types. If you are on the process of migrating a Scala 2 codebase, it might be a good opportunity to replace the likes of [specs2-mock](https://mvnrepository.com/artifact/org.specs2/specs2-mock) or [mockito-scala](https://github.com/mockito/mockito-scala) as you migrate your modules.

# Is this really a mocking framework?

This is a [facade](https://en.m.wikipedia.org/wiki/Facade_pattern) for Mockito, which in itself is technically a [test spy framework](https://github.com/mockito/mockito/wiki/FAQ#is-it-really-a-mocking-framework). There is a great debate regarding the definitions of mocks, stubs, spies, test duplicates... Here, we assume a mock to be a "faked" object, and a stub a provided implementation for a subset of the input space.

# I am getting a "Sharing is only supported for boot loader classes..." warning.

That is okay and is related to the way Mockito performs runtime bytecode manipulation. See [this issue](https://github.com/mockito/mockito/issues/3111) for further discussion.

# I can't seem to stub a method/I found a bug.

Are you performing eta-expansion correctly? Check out the main [SmockitoSpec](https://github.com/bdmendes/smockito/blob/master/src/test/scala/com/bdmendes/smockito/SmockitoSpec.scala) for more examples covering a variety of situations. If everything looks fine on your side, please file [an issue](https://github.com/bdmendes/smockito/issues) with a minimal reproducible example.

# What can I do with the source code?

Mostly anything you want to. Check [the license](https://github.com/bdmendes/smockito/blob/master/LICENSE). All contributions are appreciated!
