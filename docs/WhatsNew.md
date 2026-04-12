# What's New

* [3.2.1](#321)
* [3.2.0](#320)
* [3.1.1](#311)
* [3.1.0](#310)
* [3.0.0](#300)
* [2.1.0](#210)
* [2.0.0](#200)
* [1.1.0](#110)
* [1.0.0](#100)


# 3.2.1
- Updated the documentation
- Fix a failing Integration Test

# 3.2.0
## Lib
- **Deprecate `Value.asFile()`**  
  This function will be removed in the next major release.
- Added `Context.getFile(String)` and `Context.getFile(Value)`  
  These functions return files that are guaranteed not to reside in sensitive parts of the file system. They replace `Value.asFile()`.
- Added `EelContextBuilder.withFileFactory()`  
  Allows clients to apply additional checks when creating files.
- Lookback indices are now full expressions  
  Previously, only positive numeric literals were supported.
- Added support for default values in lookbacks  
  Applies when the index is out of range.
- Added `text.index()`  
  Returns the 1-based index of a value within a list. This can be used to dynamically generate a lookback index.
- Added `logic.index()`  
  Returns the 1-based index of the first `true` expression in a list. This can be used to dynamically generate a lookback index.
- Added `date.parse()`  
  Parses text values in a specified format into `Date` values.
- **Bug fixes**
  - `realPath()`, `dirName()`, and `baseName()` now correctly handle empty paths and paths that end with slashes.
  - `exists()` can now check for file system objects that are not files.
  - File system functions fail when the file system path is an empty string.
  - File system functions now work with objects in the root directory.


# 3.1.1
## General
- Added a ZIP file containing HTML documentation for the standard EEL functions, suitable for download.


# 3.1.0
## General
- Added the Doclet module  
  Used to generate HTML documentation for EEL functions.
- Generated HTML documentation for the standard EEL functions.


# 3.0.0
## General
- Split the documentation into multiple documents.

## Lib
- **Breaking change: Removed interfaces EelValue and EelLambda from UDFs** - replaced by the
  `Value` interface, which extends `ValueReader`. Value objects use
  '[Thunk](https://en.wikipedia.org/wiki/Thunk)' semantics so parameters can be processed lazily.
- **Breaking change: The EelFunction annotation now defines the function name as its'value' parameter.**
- **Breaking change: Moved the FunctionalResource interface to package com.github.tymefly.eel.udf**
- **Breaking change: Expanded supported escape sequences in expressions and text literals** -
  includes `\f`, `\n`, `\b`, and Unicode (`\uxxxx`). Also supports `\t`, `\r`, and `\$`. Invalid escape sequences
  now result in an error.
- **Breaking change: `io` is now a reserved function prefix** - added `io.head()` and `io.tail()`
  to read text from files. The file size is limited to guard against DOS attacks. The default limit is 32,768
  characters and can be configured via EelContext.
- **Breaking change: Updated operator precedence.**
- **Breaking change: Removed `date.offset()`** - replaced by `date.plus()` and `date.minus()`.
- **Breaking change: Removed `date.truncate()`** - functionality replaced bysnap modifiers (`@<period>`) in the
  `date.set()`, `date.plus()`, and `date.minus()` functions.
- Added support for compound expressions, allowing values to be evaluated once and reused. This reduces
  duplication and ensures side effects only happen once.
- Extended Date range to include BCE (BC), represented by negative years.
- Increased Date precision to nanoseconds and added new time periods (`milli`, `micro`, `nano`) and
  corresponding setters (`milliOfSecond`, `microOfSecond`, `nanoOfSecond`).
- Allowed underscores (`_`) in date period specifiers.
- `date.set()` now supports weeks while preserving the day of the week.
- EelContext can define the minimum days in the first week (default: 4 as defined by ISO-8601).
- EelContext can define the first day of the week (default: Monday as defined by ISO-8601).
- Added `text.random()` to generate random text.
-* Added convenience methods `asChar()`, `asBigInteger()`, `asDouble()`, `asLong()`, and `asInt()`
  to `Value` and `Result`.
- `number.round()` and `number.truncate()` now accept an optional precision argument (default: 0).
- The third argument to `before()` and `after()` is now optional (default: 1).
- Added logical operator `xor`.
- Added relational operator `in`.
- Added the `:-` modifier to value interpolation.
- Text substring in value interpolation no longer requires a count; defaults to the remaining text.
- Text substring in value interpolation supports negative start positions and lengths.
- `mid()` supports negative start positions and lengths, consistent with interpolation behavior.
- The third argument to `mid()` is now optional and defaults to the remaining text.
- `left()` and `right()` support negative lengths, counting from the opposite end of the text.
- Optimised generated expressions.
- Reserved words can now be used as symbol table keys.
- UDFs can accept `java.io.File` parameters; For security reasons, EEL will throw a `EelFunctionException` if the 
  file references a sensitive part of the local file system.
- Text literals support lookbacks, value, function, and expression interpolations.
- Expressions can include line-feed and form-feed characters.
- Expression and function interpolations can include comments.
- Overloaded `Eel.compile` to support expressions from InputStreams.
- **Bug fixes**
  - Improved `EelSyntaxException` messages for unexpected numeric literals.
  - Allowed function and expression interpolations be be nested inside expression interpolations
  - Fixed potential ConcurrentModificationException in multithreaded evaluation.
  
## Evaluate
- Added `--script` option to evaluate expressions from a file.
- Added `--io-limit` option to set the maximum bytes readable by IO functions.
- Added `--start-of-week` option to define the first day of the first week.
- Added `--days-in-first-week` option to define the minimum days in the first week.

# 2.1.0
## Lib
- Support for binary and octal numeric literals.
- Index and count values in Value Interpolation substrings are now EEL expressions; previously only integer
  literals were supported.
- Text-to-number conversions can include the `_` character between digits.
- Added functions:
  - `format.binary()` to format a number in binary.
  - `format.start()` to format the EEL context date.
  - `toDegrees()` to convert a radian value to degrees.
  - `toRadians()` to convert a degree value to radians.
- Added optional offsets to `date.start()`.
- Added convenience factory methods in `SymbolsTable` to create a SymbolsTable from a single data source.
- Overloaded `Eel.execute()` to create an in-line scoped SymbolsTable from a single data source.
- Updated Javadocs to describe how values are converted
- Refactored parts of the EEL core to improve efficiency.

# 2.0.0
## Lib
- **Breaking change: Some operator symbols have been changed to match Python**

  | Operator       |      Ver 1.x.x      |     Ver 2.0.0+      |
  |----------------|:-------------------:|:-------------------:|
  | logical-not    |         `!`         |        `not`        |
  | logical-and    |         `&`         |        `and`        |
  | logical-or     | <code>&#124;</code> |        `or`         | 
  | bitwise-not    |        `NOT`        |         `~`         | 
  | bitwise-and    |        `AND`        |         `&`         | 
  | bitwise-or     |        `OR`         | <code>&#124;</code> |
  | bitwise-xor    |        `XOR`        |         `^`         |
  | exponentiation |         `^`         |        `**`         | 

- **Breaking change: Removed non-short-circuited logical operators** - EEL functions do not have external side effects.
- **Breaking change: Type conversion functions are now lowercase.**
- **Breaking change: `DefaultArgument` annotation for UDFs no longer has an _"of"_ attribute; _"value"_ is used instead.**
- **Breaking change: Removed predefined constants _pi_, _e_, and _c_; replaced by `number.pi()`, `number.e()`, and `number.c()`.**
- **Breaking change: `count()` is now zero-based; previously it was 1-based.**
- Support for named counters.
- EelContext can provide build-time information, including the current language version.
- Added functions `eel.version()` and `eel.buildDate()`.
- Added function interpolation for expressions containing a single function call.
- Added support for functional expressions; EEL functions and UDFs can accept `EelLambda` objects.
- Functions `indexOf()`, `lastIndexOf()`, `fileSize()`, `createAt()`, and `accessedAt()` now accept an optional function
  for default values.
- Functions `firstCreated()`, `lastCreated()`, `firstAccessed()`, `lastAccessed()`, `firstModified()`, and `lastModified()` 
  accept two optional arguments: a zero-based index of the file in the listing and a function to determine the default
  value if no such file exists
- UDFs can accept a `FunctionalResource` object to manage stateful resources.
- SymbolsTable supports 'scopes' to disambiguate keys from different sources with the same name.
- Numeric literals can include the `_` character.
- Empty text is converted to logical false.
- Value interpolation supports multiple case change operators.
- Value interpolation supports substrings.
- Added `isBefore` and `isAfter` operators.
- Added "divide-floor" (`//`) and "divide-truncate" (`-/`) operators.
- Added functions `number.ceil()` and `number.floor()`.
- Added functions `before()`, `after()`, and `between()` to extract text by text delimiter.
- Added `contains()` to count occurrences of text strings.
- `afterFirst()` and `afterLast()` return empty text if the delimiter is empty.
- Logging functions now return the last parameter passed instead of converting it to Text.
- Added constants in `EelValue` for commonly used values.
- **Bug fixes**
  - Value interpolations now consistently evaluate to Text; previously Text length operator could 
  return a Number.
  - Bug fix:** `firstCreated()`, `lastCreated()`, `firstAccessed()`, `lastAccessed()`, `firstModified()`, and 
  `lastModified()` now always return absolute paths.

## Evaluate
- **Breaking change: Changed return codes.**
- Display return codes on the help page.
- Added `--version` option.


# 1.1.0
## Lib
- Reduced default execution timeout from 10 seconds to 2 seconds.
- Fully defined type conversions used by `=` and `!=` operators to be consistent with date inequality operators.
- Characters `[` and `]` can now be part of an identifier to allow SymbolsTables to read structured data types.
- Positive numbers convert to logic `true`; zero and negative numbers convert to logic `false`. 
  Previously only 0 and 1 could be converted. Functions returning -1 as a non-value are now considered `false`.
- Dates can be converted to logic values and vice versa:
  - Dates before or at `1970-01-01 00:00Z` are `false`; all others are `true`.
  - Logic `false` converts to `1970-01-01 00:00:00Z`; logic `true` converts to `1970-01-01 00:00:01Z`.
- Cleaned up exceptions.
- Added constant `_c_` for the speed of light.
- Added interface `com.github.tymefly.eel.EelValue` to create and read immutable EEL values.
- UDFs can accept and return `EelValue` and `char` types.
- Function prefixes `'text'`, `'logic'`, `'number'`, and `'date'` are now reserved.
- Added functions to search for files: `fileCount()`, `firstCreated()`, `lastCreated()`, `firstAccessed()`, 
 `lastAccessed()`, `firstModified()`, and `lastModified()`.
- Added `isBlank()` to check if text is blank.
- Added `printf()` to format text.
- Added `title()` to convert text to title case.
- Added `padLeft()` and `padRight()` to pad text.
- Added `char()` and `codepoint()` to convert between characters and Unicode code points.
- Added `number.round()` and `number.truncate()` to remove fractional parts of numbers.
- `format.date()` now accepts optional offsets.
- `dirName()` no longer canonicalizes the path.
- `extension()` returns extensions without a leading `.`.
- `exists()` can accept a glob pattern in the file name.

## Evaluate
- Added `--defs` option to read multiple definitions from a properties file; expressions are evaluated as the file is loaded.
- Added `--timeout` option to set the EEL execution timeout.

# 1.0.0
- Everything.