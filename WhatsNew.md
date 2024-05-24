# What's New

* [2.1.0](#210)
* [2.0.0](#200)
* [1.1.0](#110)
* [1.0.0](#100)


## 2.1.0
### Lib
* Support for binary and octal numeric literals 
* The index and count values used in Value Interpolation substrings are EEL expressions. Previously only integer literals were supported
* Text to Number conversions can contain the `_` character
* Added functions:
  * `format.binary()` to format a number in binary
  * `format.start()` to format the EEL context date
  * `toDegrees()` to convert a radian value to degrees 
  * `toRadians()` to convert a degree value to radians 
* Add optional offsets to `date.start()`
* Added convenience factory methods to SymbolsTable to create a scoped symbols table from a single data source 
* Overloaded Eel.execute() with methods for create an in-line scoped SymbolsTable from a single data source 
* Updated the JavaDocs that describe how values are converted in EEL 2.x.x 
* Some refactoring of the EEL core to improve efficiency


## 2.0.0
### Lib
* **Breaking Change: Some operator Symbols have been changed to match Python**

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

* **Breaking Change: Remove non-short-circuited logical operators** - They are not required as EEL functions do not have external side effects 
* **Breaking Change: Type conversion functions are now in lower case** 
* **Breaking Change: `DefaultArgument` annotation for UDFs no longer has an _"of"_ attribute - _"value"_ is used instead**
* **Breaking Change: Remove _pi_, _e_ and _c_ as a predefined constants, replacing them with the functions
  `number.pi()`, `number.e()` and `number.c()`**
* **Breaking Change: By default `count()` is now a zero based; previously it was 1 based.** 
* Support for named counters 
* EelContext can be used to read build time information including the current version of the language
* Add functions `eel.version()` and `eel.buildDate()`
* Added Function interpolation to simplify expressions that only contain a single function call
* Added support for Functional expressions; EEL functions and UDFs can accept `EelLambda` objects as parameters 
* Functions `indexOf()`, `lastIndexOf()`, `fileSize()`, `createAt()` and `accessedAt()` have an extra optional argument;
  a function that returns the default value.
* Functions `firstCreated()`, `lastCreated()`, `firstAccessed()`, `lastAccessed()`, `firstModified()` and 
 `lastModified()` have two extra optional arguments. The first is a zero based index in the directory listing of the
 required file. The second additional optional argument is a function to determine the default value if no file can be found
* UDFs can accept a `FunctionalResource` object as a parameter so they can manage stateful resources
* The Symbols Table supports 'scopes' to disambiguate keys from different sources that have the same name
* Numeric literals can contain the `_` character
* Empty text is converted to logic false 
* Value interpolation supports multiple case change operators
* Value interpolation supports substrings
* Added the `isBefore` and `isAfter` operators
* Added the "divide-floor" (`//`) and "divide-truncate" (`-/`) operators
* Add functions `number.ceil()` and `number.floor()`
* Add functions `before()`, `after()`, `between()` to extract text based on a delimiter string
* Add function `contains()` to count the number of instances of some text 
* Functions `afterFirst()` and `afterLast()` will return an empty string if the delimiter is empty
* The value returned by the logging functions is the last parameter passed. Previously the value was always converted to Text
* Added constants in `EelValue` for commonly used values
* Bug Fix: All value interpolations evaluate to text. Previously Text length operator would evaluate to a Number
  which caused inconsistencies. 
* Bug Fix: Previously `firstCreated()`, `lastCreated()`, `firstAccessed()`, `lastAccessed()`, `firstModified()` and 
  `lastModified()` did not always return absolute paths.  

### Evaluate
* **Breaking Change: Change return codes**
* Display return codes on the help page
* Add `--version` option.


## 1.1.0
### Lib
* Reduced default execution timeout from 10 seconds to 2 seconds.
* Fully define the type conversions used by the `=` and `!=` operators, in a way that is consistent with the inequality operators for dates  
* Characters `[` and `]` can be part of an identifier. This is so that Symbols tables can use these characters to read structured data types.  
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
* Add function `title()` to convert text to title case
* Add functions `padLeft()` and `padRight()` to pad text
* Add functions `char()` and `codepoint()` to convert between characters and unicode codepoints  
* Add functions `number.round()` and `number.truncate()` to convert fractional numbers to non-fraction numbers
* `format.date()` now accepts offsets as optional arguments
* `dirName()` will no longer canonicalize the path
* `extension()` returns the extensions without a leading `.`
* `exists()` can now accept a glob pattern as part of the file name 

### Evaluate
* Added the `--defs` option to read multiple definitions from a properties file. The values in this properties file 
  are EEL expressions in their own right, and will be evaluated as the file is loaded.
* Added the `--timeout` option to set the EEL execution timeout


## 1.0.0
* Everything
