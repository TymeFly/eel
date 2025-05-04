# What's New

* [3.0.0](#300)
* [2.1.0](#210)
* [2.0.0](#200)
* [1.1.0](#110)
* [1.0.0](#100)


# 3.0.0
## General
* Split the Documentation into multiple documents

## Lib
* **Breaking Change: Remove interfaces EelValue and EelLambda from UDFs** - these have been replaced by the interface 
  `Value` which extends `ValueReader`. Value objects pass data using '[Thunk](https://en.wikipedia.org/wiki/Thunk)' 
  semantics so that parameters can be processed lazily.
* **Breaking Change: the EelFunction annotation defines the function name in the value parameter** 
* **Breaking Change: the FunctionalResource interface has been moved to package com.github.tymefly.eel.udf**
* **Breaking Change: In addition to `\"` and `\'`, expressions and Text literals can contain Form Feed (`\f`),  
  Carriage Return (`\n`) and Backspace (`\b`) characters. Unicode characters in the form (`\uxxxx`) as also supported. 
  Tab (`\t`), New Line (`\r`), `\$` can be used to include a dollar character without invoking an interpolation sequence. 
  Unexpected escape sequences will generate an error**
* **Breaking Change: `io` is a reserved function prefix.** Add functions `io.head()` and `io.tail()` to read text files. 
  These functions only read a limited number of characters before failing to guard against DOS attacks. 
  The default limit is 32768 characters, but this can be changed via the EelContext
* **Breaking Change: Changes to operator priority**. 
* **Breaking Change: Remove function `date.offset()`**. This has been replaced with `date.plus()` and `date.minus()`
* **Breaking Change: Remove function `date.truncate()`**. This is no longer required as `date.set()`, `date.plus()` and
  `date.minus()` support snapping date modifiers. Snaps are written in the form `@<period>`. 
  For example `date.set(<date>, '@h'`), `date.plus(<date>, '@day'`), `date.minus(<date>, '@month'`) will truncate 
  _<date>_ to the start of the hour, day and month respectively.
* Added support for chained expressions. This allows Expression interpolations to evaluate a term once and reuse the
  generated value without evaluating it. 
  This can be used to simplify expressions by reducing DRY code in the expression, or ensure side effects only 
  happen once
* Increased range of Date values to include the BCE (aka BC) era. This is represented in text form by a negative year.
* Increased accuracy of Date values to nanoseconds. New time periods milliseconds (`milli`,`millis` or `I`),
  microseconds (`micro`,`micros` or `U`) and nanoseconds (`nano`,`nanos` or `N`), manipulate the fractions of a second
  **_independently_**. Periods milliOfSecond (`milliOfSecond`, `millisOfSecond`, `i`),
  microOfSecond ( `microOfSecond`, `microsOfSecond` or `u`) and nanoOfSecond (`nanoOfSecond`,`nanosofsecond` or `n`) 
  can be used to set all fractional digits in a second at once.
* The standard date functions allow for an underscore (`_`) character between the digits of a period specifier
* `date.set()` supports weeks as a time period - the day and month are changed but the day of the week is maintained
* The EelContext can be used to set the minimum number of days in a week. This is used determine the first week of the
  year when setting the week number. If unset this defaults to 4, as defined in ISO-8601
* The EelContext can be used to set the first day of the week. This is used to snap dates to the start of a week.
  If unset this defaults to Monday, as defined in ISO-8601
* Add function `text.random()` to generate random text
* Add convenience functions `asChar()`, `asBigInteger()`, `asDouble()`, `asLong()` and `asInt` to the 
 `Value` and `Result` objects
* `number.round()` and `number.truncate()` accept an extra optional argument - the precision. This defaults to 0 to
  return integral values
* The third argument to `before()` and `after()` is now optional and defaults to 1
* Add the logical operator `xor` 
* Add the relational operator `in`
* Add the `:-` modifier to Value Interpolation
* Text substring as part of Value Interpolation no longer require a count; If a count is not provided then 
  the remaining part of the text is returned
* Text substring as part of Value Interpolation support a negative start position and length. A negative values count
  from the end of the text instead of the start
* `mid()` also supports a negative start position and lengths consistently with Text substrings in Value Interpolation
* The third argument for `mid()` is now optional, and defaults to the remaining characters in the text 
* `left()` and `right()` support negative lengths. They are used as a 1-based count from the other end of the text
* Optimise generated expressions
* Reserved words can also be Symbols Table keys.
* UDF's can accept java.io.File parameters. For security reasons, EEL will throw a `EelFunctionException` if the file
  is in a sensitive part of the local file system.
* Text literals can contain look backs, Value interpolations, Function interpolations and Expression interpolations
* Expressions can contain line-feed and form-feed characters 
* Expression and function interpolations can contain comments. 
* Overload `Eel.compile` to allow InputStreams to be compiled
* Bugfix: Fix EelSyntaxException messages for unexpected numeric literals 
* Bugfix: Allow function interpolation and expression interpolation to be nested inside expression interpolation
* Bugfix: Fix a ConcurrentModificationException that was occasionally thrown when evaluating expressions in
  multithreaded environments

## Evaluate
* Add `--script` option to evaluate expressions from a file
* Add `--io-limit` option to set the maximum number of bytes that an EEL IO function can read.
* Add `--start-of-week` option to set the first days of the first week which is used by some EEL functions.
* Add `--days-in-first-week` option to set the minimal number of days in the first week which is used by some EEL functions.


# 2.1.0
## Lib
* Support for binary and octal numeric literals 
* The index and count values used in Value Interpolation substrings are EEL expressions. Previously only integer 
  literals were supported
* Text to Number conversions can contain the `_` character between digits
* Added functions:
  * `format.binary()` to format a number in binary
  * `format.start()` to format the EEL context date
  * `toDegrees()` to convert a radian value to degrees 
  * `toRadians()` to convert a degree value to radians 
* Add optional offsets to `date.start()`
* Added convenience factory methods to SymbolsTable to create a scoped symbols table from a single data source 
* Overloaded Eel.execute() to create in-line scoped SymbolsTable from a single data source 
* Updated the JavaDocs to describe how values are converted in EEL 2.x.x 
* Some refactoring of the EEL core to improve efficiency


# 2.0.0
## Lib
* **Breaking Change: Some operator symbols have been changed to match Python**

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

* **Breaking Change: Remove non-short-circuited logical operators** - They are not required as EEL functions do not have
  external side effects 
* **Breaking Change: Type conversion functions are now in lower case** 
* **Breaking Change: `DefaultArgument` annotation for UDFs no longer has an _"of"_ attribute - _"value"_ is used instead**
* **Breaking Change: Remove _pi_, _e_ and _c_ as a predefined constants, replacing them with the functions
  `number.pi()`, `number.e()` and `number.c()` respectively**
* **Breaking Change: By default `count()` is now a zero-based; previously it was 1 based.** 
* Support for named counters 
* EelContext can be used to read build time information including the current version of the language
* Add functions `eel.version()` and `eel.buildDate()`
* Added Function interpolation to simplify expressions that only contain a single function call
* Added support for Functional expressions; EEL functions and UDFs can accept `EelLambda` objects as parameters 
* Functions `indexOf()`, `lastIndexOf()`, `fileSize()`, `createAt()` and `accessedAt()` have an extra optional argument;
  a function that returns the default value.
* Functions `firstCreated()`, `lastCreated()`, `firstAccessed()`, `lastAccessed()`, `firstModified()` and 
 `lastModified()` have two extra optional arguments. The first is a zero-based index in the directory listing of the
 required file. The second additional optional argument is a function to determine the default value if no file is found
* UDFs can accept a `FunctionalResource` object as a parameter to manage stateful resources
* The Symbols Table supports 'scopes' to disambiguate keys from different sources that have the same name
* Numeric literals can contain the `_` character
* Empty text is converted to logic false 
* Value interpolation supports multiple case change operators
* Value interpolation supports substrings
* Added the `isBefore` and `isAfter` operators
* Added the "divide-floor" (`//`) and "divide-truncate" (`-/`) operators
* Add functions `number.ceil()` and `number.floor()`
* Add functions `before()`, `after()`, `between()` to extract text based on a text delimiter
* Add function `contains()` to count the number of occurrences of some text 
* Functions `afterFirst()` and `afterLast()` will return an empty text if the delimiter is empty
* The value returned by the logging functions is the last parameter passed. Previously it was converted to Text
* Added constants in `EelValue` for commonly used values
* Bug Fix: All value interpolations evaluate to text. Previously Text length operator would evaluate to a Number
  which caused inconsistencies. 
* Bug Fix: Previously `firstCreated()`, `lastCreated()`, `firstAccessed()`, `lastAccessed()`, `firstModified()` and 
  `lastModified()` did not always return absolute paths.  

## Evaluate
* **Breaking Change: Change return codes**
* Display return codes on the help page
* Add `--version` option.


# 1.1.0
## Lib
* Reduced default execution timeout from 10 seconds to 2 seconds.
* Fully define the type conversions used by the `=` and `!=` operators, in a way that is consistent with the inequality
  operators for dates  
* Characters `[` and `]` can be part of an identifier. This is so that Symbols tables can use these characters to read
  structured data types.  
* All positive numbers can be converted to logic value `true` while zero and negative numbers values can be converted to 
  logic `false`. Previously only numbers `0` and `1` could be converted to logic values. This change means functions 
  that return `-1` to indicate a non-value can also be considered as returning `false`. 
* Dates can be converted to Logic values and vice versa. 
  * Dates before or at `1970-01-01 00:00Z` are converted to logic `false`, all other dates are converted to logic `true`.
  * Logic `false` can be converted to date `1970-01-01 00:00:00Z` and logic `true` to `1970-01-01 00:00:01Z`  
  These are the same values that would be returned if an explicit conversion to NUMBER is used as an intermediate step. 
  This change means that functions that return `1970-01-01 00:00:00Z` to indicate a non-value can also be considered as 
  returning `false`
* Cleanup exceptions 
* Add _c_, the speed of light, as a constant 
* Add interface `com.github.tymefly.eel.EelValue` to create and read immutable EEL values.
* UDFs can accept and return `EelValue`'s and `chars`'s  
* Function prefixes _'text'_, _'logic'_, _'number'_ and _'date'_ are now reserved.
* Add functions to search for files in a directory. These are `fileCount()`, `firstCreated()`, `lastCreated()`, 
 `firstAccessed()`, `lastAccessed()`, `firstModified()` and `lastModified()`
* Add function `isBlank()` to check is text is blank
* Add function `printf()` to format text
* Add function `title()` to give text in title case
* Add functions `padLeft()` and `padRight()` to pad text
* Add functions `char()` and `codepoint()` to convert between characters and unicode codepoints  
* Add functions `number.round()` and `number.truncate()` to convert fractional numbers to non-fraction numbers
* `format.date()` now accepts offsets as optional arguments
* `dirName()` will no longer canonicalize the path
* `extension()` returns the extensions without a leading `.`
* `exists()` can now accept a glob pattern as part of the file name 

## Evaluate
* Added the `--defs` option to read multiple definitions from a properties file. The values in this properties file 
  are EEL expressions in their own right, and will be evaluated as the file is loaded.
* Added the `--timeout` option to set the EEL execution timeout


# 1.0.0
* Everything
