# FWrap

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![Build Status](https://travis-ci.org/staakk/fwrap.svg?branch=master)](https://travis-ci.org/staakk/fwrap)

__FWrap__ is a library that allows you to provide callbacks for arbitrary function that will be called before and after its execution.

## Example
To get callbacks before and after function execution you just need to register provider with an identifier. Then you can use it to wrap function using `@Wrap` annotation.

```kotlin
registerProvider(
        id = "foo",
        callBefore = { println("Hello from function `${it.name}()`.") },
        callAfter = { println("Goodbye, return value was `$it`.") }
)

@Wrap(["foo"])
fun bar() = 42
```

Executing function `bar` will produce following output:
```text
Hello from function `bar()`.
Goodbye, return value was `42`.
```

You can find more usage examples in `fwrap-samples` directory.