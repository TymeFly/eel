# EEL
EEL is a small, compiled, **E**xtensible **E**xpression **L**anguage which can be used by a JVM application to evaluate 
expressions at run time. One important use case for EEL is to configure applications in a dynamic and flexible way.

---
## Overview

There is a common requirement when developing systems to make certain values configurable. This is traditionally
achieved by reading values as-is from the environment variables, JVM properties, databases, or properties files. 
These solutions work well if nothing more sophisticated than simple text substitution is required, the exact values
are known at deployment time and none of values are guaranteed to be related. However, sometimes something more sophisticated is needed. 
For example, an application might want to write data to a file were the path is based on the current date or time. 
On other occasions multiple settings may need to be combined, for example to build up set of directories that have
a common root. 

Dates in particular are often problematic as developers and/or end users can have different ideas
about how to format dates based on their own cultural preference - of course they are all wrong, the correct
way is always ISO-8601 ;^)

To solve this problem a "_Pragmatic Programmer_" would consider using a Domain Specific Language to derive these
values at run time, and that's exactly what the **E**xtensible **E**xpression **L**anguage (EEL) is.
The strings that were previously used to configure an application can be treated as EEL Expressions that are
simple yet powerful and are quickly evaluated at run time.

---
## EEL Requirements

The requirements for EEL are:
* Expressions that are just strings can be passed through as-is. This makes it possible to take settings
  that were previously hardcoded strings and use them as EEL Expressions without change.
* The language must be easy to use, especially by somebody who is familiar with Java programming and/or shell scripting. 
* The Java Programming API should be simple and concise.
* The language must include all the usual text, maths and logic operations that languages typically support.
* The language must be able to manipulate date/time stamps.
* The language must support logging so clients can see what is being evaluated for security and/or debugging purposes.
* The language must be _Null Hostile_ to avoid NullPointerExceptions.  
* The language must be secure:
  * It must be impossible to write an expression that will not complete at either compile time or runtime.
  * It must not be possible to inject false information into the system logs. 
  * Except for logging, expressions are executed without side effects.
* The language must be extensible - additional functions can be added to the language without needing to update EEL.

**Note:** There is no requirement that EEL be Turing complete. 
While there is support for conditions, neither iteration nor recursion is supported 

---
## What's New
### 1.0.0
* Everything

### 1.1.0
####Lib
* Reduced default execution timeout from 10 seconds to 2 seconds.
* Fully define the type conversions used by the `=` and `!=` operators, in a way that is consistent with the inequality operators for dates  
* Characters `[` and `]` can be part of an identifier. This is so that Symbols tables can use these characters to read structured data types.  
* All positive numbers can be converted to logic value `true` while zero and negative numbers values can be converted to 
  logic `false`. Previously only numbers `0` and `1` could be converted to logic values. This change means functions 
  that return `-1` to indicate a non-value can also be considered as returning `false`. 
* Dates can be converted to Logic values and vise versa. 
  * Dates before or at `1970-01-01 00:00Z` are converted to logic `false`, all other dates are converted to logic `true`.
  * Logic `false` can be converted to date `1970-01-01 00:00:00Z` and logic `true` to `1970-01-01 00:00:01Z`  
  This are the same values that would be returned if an explicit conversion to NUMBER is used as an intermediate step. 
  This change means that functions that return `1970-01-01 00:00:00Z` to indicate a non-value can also be considered as 
  returning `false`
* Cleanup exceptions 
* Add _c_, the speed of light, as a constant 
* Add interface `com.github.tymefly.eel.EelValue` to create and read immutable EEL data objects.
* UDFs can accept and return `EelValue`'s and `chars`'s  
* Function prefixes _'text'_, _'logic'_, _'number'_ and _'date'_ are now reserved.
* Add functions to search for files in a directory. These are `fileCount()`, `firstCreated()`, `lastCreated()`, 
 `firstAccessed()`, `lastAccessed()`, `firstModified()` and `lastModified()`
* Add function `isBlank()` to check is text is blank
* Add function `printf()` to format text
* Add function `title()` to convert a string to title case
* Add functions `padLeft()` and `padRight()` to pad text
* Add functions `char()` and `codepoint()` to convert between characters and unicode codepoints  
* Add functions `number.round()` and `number.truncate()` to convert fractional numbers to non-fraction numbers
* `format.date()` now accepts offsets as optional arguments
* `dirName()` will no longer canonicalize the path
* `extension()` returns the extensions without a leading `.`
* `exists()` can now accept a glob pattern as part of the file name 

####Evaluate
* Added the `--defs` option to read multiple definitions from a properties file. The values in this properties file 
  are EEL expressions in their own right, and will be evaluated as the file is loaded.
* Added the `--timeout` option to set the EEL execution timeout


---
## Building EEL
EEL requires:
* Java 17+
* Maven 3.6+

The source code contains three modules:

* **lib** - the EEL compiler and runtime
* **integration** - helper classes that can be used to integrate EEL with JVM applications
* **evaluate** - CLI wrapper for _lib_ is used to execute EEL Expressions 

---
## 'lib' - The EEL Language and its Java API
### The EEL API
The API for EEL is based around four components. These are:

#### EEL Context

The EEL Context holds compile time information for an EEL Expression. An EelContext can be used to compile
multiple EEL Expressions.

The client can use the Context to:
* Set the precision for maths operations
* Import **U**ser **D**efined **F**unctions (UDFs).
* Guard against rogue expressions causing Denial Of Service (DOS) attacks by:
  * setting the maximum length of the expression in characters
  * setting a timeout for evaluating the expression

In addition, the Context also:
* allows EEL functions to read the time the Context was created 
* provides a scope for the `count` function  

All the Context settings have defaults; if these are acceptable then the Default EelContext can be used.

EelContext objects are built using a fluent API; the entry point is `EelContext.factory()` 
 

#### EEL Expression

This is where the source expression is compiled. Like any other program, a compiled expression can be executed multiple
times. This could be handy if an expression makes use of functions that return different values each time 
the expression is evaluated, for example reading the current date/time, generating random numbers or reading a counter. 
Alternatively, the expression could be evaluated with a different Symbols Table (see below) which could also cause 
EEL to return a different Result (see below). 

EEL Expressions are built using a fluent API; the entry point is `Eel.factory()`. In addition, there are
convenience methods in `Eel` that can be used to compile expressions with the Default EelContext.


#### Symbols Table

The Symbols Table is lookup mechanism that maps variable names in an expression to values that are provided at runtime.
These values can come from any, all or none of the following sources:
- The environment variables
- The JVM properties
- One or more `java.util.Map` objects 
- One or more lambda functions 
- A single hardcoded string which is associated with **all** variable names. This is usually used as a default value. 

EEL will read values from the Symbols Table as Text, however in most cases it will correctly convert the value to the 
type required by the operator that uses it. If this is insufficient then explicit conversion functions can be used to
ensure the correct type (see below)

If a key exists in multiple sources, then the first source added to the Symbols Table takes priority. Because
of this, no further data sources can be defined after a hardcoded string has been added.

Once defined, a Symbols Table can be used to evaluate multiple EEL Expressions. There is no requirement that
an EEL Expression has to use a Symbols Table when it is evaluated.      

Symbols Table objects are built using a fluent API; the entry point is `SymbolsTable.factory()`. In addition, there are 
convenience methods in:
- `SymbolsTable` - used to build a Symbols Table that reads from a single data source
- `Eel` - used to evaluate a compiled expression with a one-off Symbols Table that reads a single data source


#### Result

A Result object represents the output from an EEL Expression. Each Result contains:
- The type of the evaluated value (see below)
- The evaluated value which the client can read via a type specific getter method. If the getter method doesn't
match the Result type, then the Result will try to convert it using the rules described below. 

#### Exceptions 
The EEL compiler and runtime can throw the following Exceptions:

| Exception                 | Purpose                                            | Extends              |
|---------------------------|----------------------------------------------------|----------------------|
| EelException              | Base class for all EEL exceptions                  | RuntimeException     |
| EelInternalException      | The EEL core failed. This should never be seen     | EelException         |
|                           |
| EelCompileException       | Base class for all compile time exceptions         | EelException         |
| EelSourceException        | The expression source could not be read            | EelCompileException  |
| EelSyntaxException        | The source expression has a syntax error           | EelCompileException  |
|                           |
| EelRuntimeException       | Base class for all runtime exceptions              | EelException         |
| EelInterruptedException   | The evaluating thread was interrupted              | EelRuntimeException  | 
| EelTimeoutException       | The expression took too long to evaluate           | EelRuntimeException  | 
| EelUnknownSymbolException | The Symbols Table did not contain a required value | EelRuntimeException  |
| EelConvertException       | An EEL type conversion failed                      | EelRuntimeException  |
| EelFunctionException      | A function failed or could not be invoked          | EelRuntimeException  |
| EelFailException          | The expression executed the `fail()` function      | EelRuntimeException  | 

**Note:** All of these exceptions are unchecked.

#### Example Java Code

**Basic Usage**

In its simplest form, EEL can be invoked as:

    String result = Eel.compile(  ...some expression...  )
      .evaluate()
      .asText();

In this example the Default EelContext is used to compile the expression, it is then executed without reference to a 
Symbols Table and the result is returned as String. 
If this is enough for the client application then nothing more complex is required.

**Inline EelContext**

    String result = Eel.factory()
      .withPrecision(12)
      .compile(  ...some expression...  )
      .evaluate()
      .asText();

In this example the fluent API provided by `Eel.factory()` creates a new EelContext as the Expression is compiled;
in this case it is used to set the maths precision.

Inlining the Context makes for concise code but does not allow the EelContext to be reused.

**Explicit EelContext**

    EelContext context = EelContext.factory()
      .withPrecision(12)
      .build();
    String result = Eel.compile(context, ...some expression...)
      .evaluate()
      .asText();

This example uses the fluent API provided by `EelContext.factory()` to create an EelContext object, and then uses
it to compile an Expression. 
The EelContext object can be reused in as many calls to `Eel.compile()` as need.

**Inline Symbols Table**

    Map<String, String> symTab = ... 
    String result = Eel.compile(  ...some expression...  )
      .evaluate(symTab)
      .asText();
  
In this example the fluent API provided by `Eel.compile()` creates a new Symbols Table as the Expression is evaluated;
in this case the data comes from a map of values, but `evaluate` is overloaded to read from other data sources.

Inlining the Symbols Table makes for concise code but does not allow the Symbols Table to be reused.
It also means that the Symbols Table can only read data from a single source. 


**Explicit Symbols Table**

    Map<String, String> values = ...
    SymbolsTable symbols = SymbolsTable.factory()
      .withValues(values)
      .withProperties()
      .build();
    String result = Eel.compile(  ...some expression...  )
      .evaluate(symbols)
      .asText();

This example uses the fluent API provided by `SymbolsTable.factory()` to create a SymbolsTable object which is then
used to evaluate an Expression. 
There are several advantages to creating Symbols Table objects; the table can read multiple sources, 
provide a default value if a key is undefined, and it can be reused in as many calls to `Eel.evaluate()` as needed.
The disadvantage is the code is more verbose.


**Explicit EelContext and Symbols Table**

    EelContext context = EelContext.factory()
      .withPrecision(12)
      .build();
    SymbolsTable symbols = SymbolsTable.factory()
      .withProperties()
      .withEnvironment()
      .build();
    String result = Eel.compile(context, ...some expression...)
      .evaluate(symbols)
      .asText();   

This example combines the Explicit EelContext and Explicit Symbols Table examples above. This is EEL at its most 
flexible but also its most verbose.

**Using the result**

The previous examples all treated the Result as Text, however, EEL can evaluate other types of data as well. 
For example:

    Result result = Eel.compile(  ...some expression...  )
      .evaluate();
    
    if (result.getType() == Type.NUMBER) {
      System.out.println("As a number " +  result.asNumber());
    } else if (result.getType() == Type.LOGIC) {
      System.out.println("As logic " + result.asLogic());
    } else if (result.getType() == Type.DATE) {
      System.out.println("As a date " + result.asDate());
    }     

    System.out.println("As a string " + result.asText());

In this example the type of the data in the Result object is checked and then read using an appropriate getter
method. 

If the data does not match the type expected by the getter method, then the Result object will try to convert the
data to the required type. As all data can be represented as text it will always be possible read the result using
`asText()`. The rules for converting values are described below.


### EEL Syntax and Semantics

##### Source Characters
EEL Expressions accept only printable characters, spaces, and tabs as valid input. 
Other characters, including carriage-return, new-line, null and the control characters are invalid. 

**Escape sequences** 

Like most languages, EEL reserves certain characters for its own use. So that Text literals can contain these characters
the following 'C' style escape sequences are supported: 

| Escape sequence |Character represented  |
|-----------------|-----------------------|
| `\\`            | Backslash             |
| `\'`            | Single quotation mark |
| `\"`            | Double quotation mark |


#### Source Code
In its simplest form an EEL Expression is just a sequence of characters. The data that is passed to the EEL compiler 
will be returned as-is in the Result. On the face of it this isn't very useful, however it does provide a good
migration path for systems that are already configured with these strings.

Where EEL becomes more useful is with the two interpolation mechanisms it supports. These are substrings in 
the parsed expression. The interpolation mechanisms are:
1. **Variable Expansion** which is written in the form `${...}` and is used to read values from the Symbols Table
2. **Expression Expansion** which is written in the form `$(...)` and is used to perform calculations and/or call functions. 

The interpolation mechanisms can be nested. This is most commonly seen when an Expression Expansion uses a 
Variable Expansion to read a value from the Symbols Table.

The following sections describe the interpolation mechanisms in more detail. 


#### Variable Expansion - ${...}
Variables are written in a format that should be familiar to somebody who is familiar shell scripting - `${key}`. 
Unlike shell scripting, in EEL the braces are mandatory.
When EEL encounters a variable, it will replace the expansion with the associated value from the Symbols Table.

EEL variables support similar modifiers to bash:
- `${key}` - the text value associated with _key_
- `${#key}` - the length of the text value associated with _key_ 
- `${key^}` - the text value associated with _key_, but with the first character in upper case
- `${key^^}` - the text value associated with _key_, but with all the characters in upper case
- `${key,}` - the text value associated with _key_, but with the first character in lower case
- `${key,,}` - the text value associated with _key_, but with all the characters in lower case
- `${key~}` - the text value associated with _key_, but with the case of the first character toggled
- `${key~~}` - the text value associated with _key_, but with the case of the all the characters toggled
- `${key-default}` - if there is no value in the Symbols Table associated with the _key_ then the `default` is used

EEL allows these modifiers to be combined. For example:

- `${key^^-default}` - if there is a value associated with _key_ then use the value after converting it to uppercase.
  If there is no value associated with _key_ then use the `default`
- `${#key-default}` - if there is a value associated with _key_ then use its length.
  If there is no value associated with _key_ then use the `default`

Default values are EEL Expressions in their own right, so the following variable expansions are valid:

- `${undefined-defaultText}` - if _undefined_ is not in the Symbols Table then use the literal text `defaultText`
- `${undefined-}` - if _undefined_ is not in the Symbols Table then use empty text 
- `${first-${second}}` - if _first_ is not in the Symbols Table then use the value associated with _second_ instead 
- `${first-${second-defaultText}}` - if _first_ is not in the Symbols Table then try _second_. 
  If _second_ also not in the Symbols Table then use the literal text `defaultText`
- `${undefined-$( expression )}` - if _undefined_ is not in the Symbols Table then evaluate _expression_

**Undefined values**

There are several ways to handle keys do not have an associated value in the Symbols Table. These are:
1. Use defaults with all variable expansions 
1. Use the isDefined operator (`?`) to check the key is defined in the Symbols Table before reading it (see below)
1. Configure the Symbols Table to return a hardcoded value if the variable is undefined (e.g. empty text)
1. Have the client code handle the `EelUnknownSymbolException` exception


#### Expression Expansion  - $(...)
The full power of EEL is in the Expression Expansion. This a simple programming language that is used to evaluate
a value.

##### Reserved words and symbols

| Values  | Numeric Ops   | Logical Ops         | Relational Ops | Text Ops | Conversions | Quotes | Misc |
|---------|---------------|---------------------|----------------|----------|-------------|--------|------|
| `true`  | `+`           | `&`                 | `=`            | `~>`     | `TEXT`      | `'`    | `$` |
| `false` | `-`           | `&&`                | `<>` and `!=`  |          | `NUMBER`    | `"`    | `{` |
| `e`     | `*`           | vertical-bar        | `>`            |          | `LOGIC`     |        | `}`  |
| `pi`    | `/`           | double-vertical-bar | `>=`           |          | `DATE`      |        | `(`  |
| `c`     | `%`           | `!`                 | `<`            |          |             |        | `)`  |
|         | `^` and `**`  |                     | `<=`           |          |             |        | `,`  |
|         | `<<`          |                     |                |          |             |        | `?`  | 
|         | `>>`          |                     |                |          |             |        | `:`  |
|         | `AND`         |                     |                |          |             |        | `[`  |
|         | `OR`          |                     |                |          |             |        | `]`  |
|         | `XOR`         |                     |                |          |             |        |      |
|         | `NOT`         |                     |                |          |             |        |      |

##### Identifiers
Identifiers are used in Variable Expansions and as function names. The first letter of an identifier must be either: 
* an uppercase ASCII character
* a lowercase ASCII character
* an underscore (`_`)

Each of the (optional) subsequent characters must be either:
* an uppercase ASCII character
* a lowercase ASCII character
* a digit
* an underscore (`_`)
* a dot (`.`)
* open and closed square brackets (`[` and `]`)

EEL Identifiers are case-sensitive.

It should be noted that valid EEL identifiers do not always make valid Java identifiers and vise versa.

##### Naming conventions
- Constants are all lowercase. For example, `true`, and `pi`
- Other reserved words are all UPPERCASE. For example, `AND`, `OR`, `TEXT` and `LOGIC`
- The function names (see below) are in lowerCamelCase. Dots (`.`) are sometimes included in function names to create
  prefixes. These are used to logically group functions and avoid namespace clashes much as packages do in Java. 
  Otherwise, they have no special meaning. 
  Example function names are `count`, `cos`, `isEmpty`, `system.home`, `date.local`, `log.warn` and `format.hex`
- Variable names are typically in a format that is appropriate to the data source. For example, environment variables
  are usually in _UPPERCASE_WITH_UNDERSCORES_ while JVM properties are in _lower.case.with.dots_. The restrictions on
  variable names are detailed above.

##### Data types
EEL natively supports 4 data types. These are named differently from their Java equivalents, to distinguish between
data that is in the EEL and Java domains. These types are:
- **Text**

  Text literals can be written in the form `"..."` or `'...'`. The opening quote can be either a single or a double
  quote, but the associated closing quote must match the opening quote. This allows a double-quoted text literal
  to contain single quote characters and vise versa. Additionally, escape sequences can be used to embed special
  characters, including what would have been the closing quote.   

  Variable expansions (`${..}`) and expression expansions (`$(...)`) are not supported in Text literals. This is
  not usually a problem as Text concatenation can be used instead.

  Text values are returned from Result objects as Java Strings.


- **Logic**

  The two logic literals are `true` and `false`.

  Logic values are returned from Result objects as Java boolean primitives.


- **Number**

  The numeric literals can be expressed in any of the following ways:
  - Decimal integers (e.g. `1234`)
  - Hexadecimal integers (e.g. `0x89ab`)
  - Decimals with fractional parts (e.g. `123.456`)
  - Scientific format (e.g. `2.997e8`)

  Numbers are returned from Result objects as Java `BigDecimal`s


- **Date**

  EEL expressions support Date-Time stamps, to a precision of a second. There are no Date literals, but Dates can be 
  generated by: 
  - Calling a function that returns a date, such as `date.utc()`, `date.local()`, `date.at()` or `date.start()`
  - Converting another data type to a date

  Dates are returned from Result objects as Java `ZonedDateTime`s


##### Null Values

EEL is a Null Hostile language. As a consequence:
  - Variable Expansions will throw an `EelUnknownSymbolException` if a value cannot be read from the Symbols Table
  - If a function returns null then an `EelFunctionException` is thrown. 
  - UDF's (see below) do not have to check to see if `null` values are passed to them 
  - It is guaranteed that the Result getters will never return null.


##### Types conversions

Values in EEL Expressions are loosely typed; EEL will silently convert values as required. The conversion rules are:

* Text values are converted to Numbers if they can be parsed as either:
  - Decimal integer (e.g. `1234`)
  - Hexadecimal integer (e.g. `0x89ab`)
  - Decimal with a fractional part (e.g. `123.456`)
  - Scientific format (e.g. `2.997e8`)
  
  Leading and trailing spaces will be ignored.
* Text values `"true"` and `"1"` are converted to the Logic value `true`.
  Text values `"false"`, `"0"` and the empty string are converted to the Logic value `false`

  Leading spaces, trailing spaces and case will be ignored.
* Text values can be converted to Dates by parsing them as ISO 8601 formatted values (`yyyy-MM-dd'T'HH:mm:ssX`). 
  * The precision of these strings is flexible; they can contain as little as a 4-digit year or be specified to the second,
    but each period but be fully defined. For example, `2000-01` can be converted to a date, but `2001-1` cannot.  
  * If a time zone is not specified then UTC is assumed
  * The `T`, `-` and `:` separator characters can be replaced with a single space or omitted entirely.
  * Fractions of a second are not supported
  * Leading and trailing spaces will be ignored.
* Number values are converted to Text as their plain (non-scientific) decimal representation
* Positive numbers are converted to Logical `true`. Negative numbers and zero are converted to Logical `false`
* Numbers are converted to Dates as the number of elapsed seconds since 1970-01-01 00:00:00 in the UTC zone.  
* Logic values `true` and `false` are converted to Text values `"true"` and `"false"` respectively
* Logic values `true` and `false` are converted to numeric values `1` and `0` respectively
* Logic values `true` and `false` are converted to date values `1970-01-01 00:00:00Z` and `1970-01-01 00:00:01Z` respectively
* Dates are converted to Text in the format `yyyy-MM-dd'T'HH:mm:ssX`
* Dates are converted to Numbers by taking the number of elapsed seconds since `1970-01-01 00:00:00` in the UTC zone.
* Date values `1970-01-01 00:00:00` and earlier are converted to `false`. All other dates are converted to `true`

All other conversions are illegal and will cause EEL to throw an `EelConvertException`. 
For example the Text value `"true"` can be converted to a Logical value, but `"positive"` will throw an exception.

In general EEL will automatically convert values to the type required by the operator that uses it. If an expression
needs to ensure a specific type then the conversion functions `TEXT`, `NUMBER`, `LOGIC` and `DATE` are available. 
The syntax of these functions is exactly the same as any other function. For example `$( LOGIC("1") )` will return `true`

The rules have been defined this way because in the real world they generally do the right thing, however they are not
always Symmetric. For example:
 - `DATE( NUMBER( date.local() ) )` will convert a date to a number and then convert back to a date. The original date 
was in the local time zone but the result will be in UTC. The number of seconds elapsed since 
1970-01-01 00:00:00 in the UTC zone is maintained.
 - `TEXT( NUMBER( '0x1234' ) )` will convert text to a number to and back again, but the result will be in decimal rather 
than the original hex
 - `DATE( LOGIC( date.local() ) )` will convert a date to a logic value and then convert it back to a date. 
The conversion to a logic value will lose precision, so the resulting date is always `1970:01:01 00:00:01` in UTC
 - `TEXT( DATE( '2000-01-01' ) )` will convert text to a date and back to text again, but the result will contain time 
fields that were absent in the original text. The undefined fields default to the start of their respective periods.

There are also occasions when the rules are not Transitive. For example:
- `TEXT( NUMBER( true ) )` will return `1`, but `NUMBER( TEXT( true ) )` will throw an `EelConvertException`. 

##### Constants
EEL defines the following constants:
- **true** 
- **false**
- **pi**
- **e**
- **c**

Constant names are case-sensitive.

##### Operators
EEL supports the following operators:

| Precedence | Operators Symbols                                | Operators Name(s)                                                                |
|------------|--------------------------------------------------|----------------------------------------------------------------------------------|
| Highest    | `()`                                             | Parentheses                                                                      |
|            | `?`                                              | isDefined                                                                        |
|            | `-` `!` `NOT` conversions, function-call         | negation, logical-not, bitwise-not, type-conversions, function-calls             |
|            | `^` `**` `~>`                                    | power, string-concatenation                                                      |
|            | `*` `/` `%` `AND` `&` `&&`                       | multiply, divide, modulus, bitwise-and, logical-and, short-circuit-and           |
|            | `+` `-` `XOR`                                    | plus, minus, bitwise-xor                                                         |
|            | `<<` `>>`  `OR` vertical-bar double-vertical-bar | left-shift, right-shift, bitwise-or, logical-or, short-circuit-or                | 
|            | `=` `!=` `<>` `>` `>=` `<` `<=`                  | equal, not-equal, greater-than, greater-than-equals, less-than, less-than-equals |   
| Lowest     | `? :`                                            | condition                                                                        |


Most operators expect their operands to be a specific type and will automatically convert their operands using the standard rules defined above.
The expected types for the operators are: 

| Operator Group | Operators Symbols                                 | Operand type(s)                                                |
|----------------|---------------------------------------------------|----------------------------------------------------------------|
| Text           | `~>`                                              | Text                                                           |
| Logical        | `!`, `&` `&&`, vertical-bar, double-vertical-bar  | Logic                                                          |
| Bitwise        | `NOT` `AND` `XOR` `OR` `<<` `>>`                  | Number with their fractional parts truncated                   |
| Maths          | `+` `-` (minus and negation) `*` `/` `%` `^` `**` | Number                                                         |
| Inequality     | `>` `>=` `<` `<=`                                 | Number                                                         |
| Equality       | `=` `!=` `<>`                                     | see table below                                                | 
| Conditional    | `?  :`                                            | first operand is Logic, operands two and three can be any type |  
| Symbols        | `?`                                               | identifier                                                     |

The equals and not-equal operators compare their operands based on the following rules:

  | Left \ Right | Text                    | Number                    | Logic                          | Date                           |
  |--------------|-------------------------|---------------------------|--------------------------------|--------------------------------| 
  | **Text**     | compare two Text values | convert Number to Text    | convert Logic to Text          | convert Date to Text           |
  | **Number**   | convert Number to Text  | compare two Number values | convert Logic to Number        | convert Date to Number         |
  | **Logic**    | convert Logic to Text   | convert Logic to Number   | compare two Logic values       | convert both values to Numbers |
  | **Date**     | convert Date to Text    | convert Date to Number    | convert both values to Numbers | compare two instances in time  |

  Using these rules two dates will be considered equal if they represent the same instant of time, even if they are in 
  different zones. As a result the equality operators applied to Dates return values that are consistent with the inequality operators  
  
**Notes:**
- The bitwise operator names (`NOT`, `AND`, `XOR` and `OR`) are case-sensitive.
- The not-equal operator exists in two forms; `!=` and `<>`. 
  - The difference is purely cosmetic; they both behave in the same way and have the same priority.
  - The reason the operator exists in two forms is to match other languages    
- The power operator exists in two forms; `^` and `**`. 
  - The difference is purely cosmetic; they both behave in the same way and have the same priority.
  - The reason the operator exists in two forms is to match other languages    
- Just like other languages such as C and Java, the logic operators come in two varieties; long form and short-circuited. 
- The Logical operators (`!`, `&`, `&&`, `|` and `||`) are different from the Bitwise operators 
  (`NOT`, `AND`, `OR` and `XOR`) because they operate on different types.
- The `+` operator is purely a numeric operator; the `~>` operator is used for string concatenation. This removes
  the need for a conversion function when using values from the Symbols Table that are expected to be numeric.
  
  `${A} ~> {B}` will concatenate the Text operands whereas `${A} + {B}` will convert the values to numbers and then add them together.  
- The isDefined operator (`?`) is used to check if a key has an associated value in the Symbols Table. If the key is 
  defined the operator returns `true`, otherwise it returns `false`. 
  The operator is written after the name of the Symbols Table key (it has right associativity).

  For example, `myKey?` will return _true_ only if `${myKey}` returns a value rather than throwing an `EelUnknownSymbolException`.
- Fractional parts of numbers will be silently truncated by the Bitwise operators. 

  For example, `$( 3 << ( 28 / 10) )` will return `12`. This is because `28 / 10` evaluates to `2.8`, but the shift
  operator will truncate the fractional part, so 3 is shifted left by two bits which gives `12`. 
  A similar issue applies to the value being shifted.


### Standard Functions
EEL has a number of standard functions that are automatically made available to expressions without the client having
to explicitly import them.

Some of these functions can accept a variable number of arguments where the final argument can be passed zero, one or
more times. This is denoted in the list below by the `...` after the last argument. 
For example, `date.offset( date, offsets... )` requires a _date_ be passed, but will accept any number of _offsets_,
or no _offsets_ at all.

Some functions support default arguments; if the Expression does not explicitly pass a value, then EEL will silently
add a default value. This is denoted in the list below by surrounding the optional argument(s) with braces.
For example, `random( { minValue { , maxValue } } )` can be called as `random()`, `random( minValue )` 
or `random( minValue, maxValue )`.

#### Utility Functions
##### Text Processing
- **lower( text )** - returns the _text_ converted to lower case
- **upper( text )** - returns the _text_ converted to upper case
- **title( text )** - returns the _text_ converted to title case


- **left( text, count )** - returns up to _count_ characters from the start of the _text_
- **mid( text, offset, count )** - returns up to _count_ characters from the _text_ starting from the zero based _offset_
- **right( text, count )** - returns up to _count_ characters from the end of the _text_


- **beforeFirst( text, delimiter )** - returns all the text before the first occurrence of the _delimiter_
- **afterFirst( text, delimiter )** - returns all the text after the first occurrence of the _delimiter_
- **beforeLast( text, delimiter )** - returns all the text before the last occurrence of the _delimiter_
- **afterLast( text, delimiter )** - returns all the text after the last occurrence of the _delimiter_


- **extract( text, regEx )** - extracts some characters from the _text_ based on a regular expression that contains groups
- **matches( text, regEx )** - returns `true` only if the _text_ matches the regular expression
- **replace( text, from, to )** - returns the _text_ with all instances of the literal text _from_ replaced by _to_
- **replaceEx( text, regEx, to )** - returns the _text_ with all matches of the regular expression _regEx_ replaced by _to_
 

- **isEmpty( text )** - returns `true` only if the _text_ is empty. This is a more concise version of `len( text ) = 0`
- **isBlank( text )** - returns `true` only if the _text_ is empty or whitespace. This is a more concise version of `isEmpty( trim(text) )`
- **len( text )** - returns the length of the _text_, including leading and trailing whitespace
- **trim( text )** - returns the _text_ with all leading and trailing whitespaces removed
- **indexOf( text, subString )** - returns the zero based of the first occurrence of _subString_ in _text_, or -1 if _subString_ is not present
- **lastIndexOf( text, subString)** - returns the zero based of the last occurrence of _subString_ in _text_, or -1 if _subString_ is not present


- **char( codepoint )** - return a text value containing the single character given by the unicode _codepoint_
- **codepoint( text )** - returns the unicode codepoint of the first character in the _text_ 


##### File System Functions
- **baseName( path { , extension } )** - returns the _path_ with the leading directory components and the optional _extension_ removed
- **dirName( path )** - returns _path_ with its last non-slashed component and trailing slash removed
- **extension( path, { , max } )** - returns up to _max_ of the right most file extensions from the _path_. The default is to return all extensions
- **realPath( path )** - returns _path_ in a canonicalised format based on current operating system


- **exists( path )** - returns _true_ only if the file at the specified _path_ exists
- **fileCount( dir {, glob } )** - returns the number of files in the _dir_ that math the _glob_. 
  _glob_ defaults to _"*"_ so that all files are counted   
- **fileSize( path { , defaultSize } )** - returns the size of the file in bytes. 
  If the file does not exist then _defaultSize_ is returned or -1 if the  _defaultSize_ is not specified 
- **createAt( path { , defaultTime } )** - returns the local date on which file was created. 
  If the file does not exist then _defaultTime_ is returned or 1970-01-01 00:00:00Z if the  _defaultTime_ is not specified 
- **accessedAt( path { , defaultTime } )** - returns the local date on which file was last accessed. 
  If the file does not exist then _defaultTime_ is returned or 1970-01-01 00:00:00Z if the  _defaultTime_ is not specified 
- **modifiedAt( path { , defaultTime } )** - returns the local date on which file was last modified. 
  If the file does not exist then _defaultTime_ is returned or 1970-01-01 00:00:00Z if the _defaultTime_ is not specified 


- **firstCreated( dir {, glob } )** - returns the full path to the file that was created first in the _dir_ and matches 
  the _glob_ . An exception is thrown if a file could not be found. _glob_ defaults to _"*"_
- **lastCreated( dir {, glob } )** - returns the full path to the file that was created most recently in the _dir_ and
  matches the _glob_. An exception is thrown if a file could not be found. _glob_ defaults to _"*"_
- **firstAccessed( dir {, glob } )** - returns the full path to the file that was first accessed in the _dir_ and
  matches the _glob_. An exception is thrown if a file could not be found. _glob_ defaults to _"*"_
- **lastAccessed( dir {, glob } )** - returns the full path to the file that was last accessed in the _dir_ and
  matches the _glob_. An exception is thrown if a file could not be found. _glob_ defaults to _"*"_
- **firstModified( dir {, glob } )** - returns the full path to the file that was modified first in the _dir_ and 
  matches the _glob_. An exception is thrown if a file could not be found. _glob_ defaults to _"*"_ 
- **lastModified( dir {, glob } )** - returns the full path to the file that and was modified most recently in the _dir_ and
  matches the _glob_. An exception is thrown if a file could not be found. _glob_ defaults to _"*"_


##### Misc Utility Functions
- **count()** - returns the next value in a 1 based counter. To reset the counter recompile the expression with a new EelContext
- **random( { minValue { , maxValue } } )** - return a random non-fractional number in the range _minValue_ to _maxValue_ inclusive.
The default range is a number between 0 and 99 inclusive.
- **uuid()** - returns a new UUID
- **duration( from, to { , period } )** - returns the duration between two dates in the specified time period. The default period is seconds
- **fail( { message } )** - immediately terminates the expression by throwing an _EelFailException_

#### System Information Functions

- **system.fileSeparator()** - returns the Operating System path separator, usually either '\\' or '/'
- **system.home()** - returns the path to the users home directory
- **system.pwd()** - returns path to the applications current working directory.
- **system.temp()** - returns path to the system temporary directory.


#### Maths Functions
##### Trigonometric Functions
- **sin( value )** - returns the sine of the radian _value_
- **cos( value )** - returns the cosine of the radian _value_
- **tan( value )** - returns the tangens of the radian _value_  


- **asin( value )** - returns the arc sine (inverted sine) of _value_
- **acos( value )** - returns the arc cosine (inverted cosine) of _value_
- **atan( value )** - returns the arc tangens (inverted tangens) of _value_ 


##### Other Maths Functions

- **max( value, values... )** - returns the largest value of all the numbers passed. At least one number must be passed
- **min( value, values... )** - returns the smallest value of all the numbers passed. At least one number must be passed
- **avg( value, values... )** - returns the average value of all the numbers passed. At least one number must be passed
 
 
- **abs( value )** - returns the absolute value of a number
- **exp( value )** - returns the natural exponent of value (e<sup>value</sup>)
- **factorial( value )** - returns the factorial of _value_
- **ln( value )** - returns the natural log of _value_
- **log( value )** - returns the log in base 10 of _value_
- **root( value { , n } )** - returns the n'th root of _value_. The default value of _n_ is 2, which gives square roots
- **sgn( value )** - returns the sign of a numeric _value_; -1 for negative, 0 for zero and 1 for positive
 
 
- **number.round( number )** - returns the _number_ rounded to the closest non-fractional value.
- **number.truncate( number )** - returns the _number_ with its fractional part discarded

The precision of the returned numeric values is set in the EelContext.

#### Date Functions

- **date.start( { zone } )** - returns the date when the EelContext was created. _zone_ defaults to UTC
- **date.utc( offsets... )** - returns the current UTC date plus any optional offsets
- **date.local( offsets... )** - returns the current local date plus any optional offsets
- **date.at( zone, offsets... )** - returns the current date in the specified zone plus any optional offsets


- **date.offset( date, offsets... )** - returns the _date_ after adding one or more offsets
- **date.set( date, specifier... )** - returns the _date_ after setting one or more periods to an absolute value.
- **date.setZone( date, zone )** - returns the _date_ after setting the time zone
- **date.moveZone( date, zone )** - returns the _date_ after change the time zone and adjusting the time to maintain the same instant
- **date.truncate( date, period )** - returns a copy of the _date_ truncated to the start of the specified _period_. 

Most of the date functions support the ability to modify, set or offset a date with respect to a time period.
The supported time periods are: 

| Period      | Full Names           | Short Name | Notes        |
|-------------|----------------------|------------|--------------|
| **Years**   | `year`, `years`      | `y`        |              |
| **Months**  | `month`, `months`    | `M`        |              |
| **Weeks**   | `week`, `weeks`      | `w`        | Offsets only |
| **Days**    | `day`, `days`        | `d`        |              |
| **Hours**   | `hour`, `hours`      | `h`        |              |
| **Minutes** | `minute`, `minutes`  | `m`        |              |  
| **Seconds** | `second`, `seconds`  | `s`        |              |

Period names are case-sensitive. Time zones can be either:
- Fixed offsets - a fully resolved offset from UTC such as `+5`
- Geographical regions - an area where a specific set of rules for finding the offset from UTC apply such as `Europe/Paris` 

For example:
- **date.start( "Europe/Paris" )** - returns the time the EEL Context was created with respect to the Paris time zone
- **date.utc( "1day" )** - returns the current time in UTC plus 1 day
- **date.local( "+2months" )** - returns the current time in the local time zone plus 2 months
- **date.at( "-5", "-1w" )** - returns the current time in the _UTC -5_ time zone minus 1 week 
- **date.set( ${variable}, "12h", "0m", "0s" )** - returns the date given in the _variable_ with the time fields set to midday
- **date.offset( ${variable}, "15minutes" )** - returns the time given in the _variable_ plus 15 minutes 
- **date.truncate( date, "month" )** - reduce the accuracy of the _date_ to the start of the month

#### Logging Functions

These functions are used to perform logging as a side effect. The value returned by the function is the last argument 
converted to Text. 

In order of priority, from highest to lowest, the logging functions are:

- **log.error( { message, } args... )** - Log the optional message and the _args_ at error level
- **log.warn( { message, } args... )** - Log the optional message and the _args_ at warn level. 
- **log.info( { message, } args... )** - Log the optional message and the _args_ at info level. 
- **log.debug( { message, } args... )** - Log the optional message and the _args_ at debug level. 
- **log.trace( { message, } args... )** - Log the optional message and the _args_ at trace level. 

**Note:** The client's logging framework is responsible for enabling logging. EEL writes all its messages to the
`com.github.tymefly.eel.log` logger, so it is recommended that it is configured with **trace** level
logging enabled.

**Note:** Because expressions can be set after applications have been developed and deployed then there is a 
need to guard against log repudiation attacks. Consequently:
1. All logged messages are always prefixed by the literal text `Logged EEL Message: ` to make it obvious they are 
generated by EEL and do not come from any other source. This prefix can not be changed. 
2. All control characters, except for tabs, are filtered out of the logged message. This includes new-line and carriage
return characters to prevent EEL logging text that that might be mistaken for another logging message 

 
#### Data Formatting Functions

- **printf( format, arguments... )** - returns formatted text using the specified format string and arguments.
- **padLeft( text, width {, pad } )** - adds _pad_ characters to the start of the _text_ so that it is at least _width_ characters long.
   _pad_ defaults to a space
- **padRight( text, width {, pad } )** - adds _pad_ characters to the end of the _text_ so that it is at least _width_ characters long.
   _pad_ defaults to a space


- **format.octal( value )** - returns the _value_ as octal Text. 
- **format.hex( value )** - returns the _value_ as hexadecimal Text without a leading _"0x"_
- **format.number( value, radix )** - returns the number as Text in the given radix. Max _radix_ is 36 


- **format.date( format, date, offsets... )** - returns the _date_, plus optional offsets, as Text in a custom format.
- **format.utc( format, offsets... )** - Returns the current UTC time, plus optional offsets, as Text in a custom format.
- **format.local( format, offsets... )** - Returns the current local time, plus optional offsets, as Text in a custom format.
- **format.at( zone, format, offsets... )** - Returns the current time in the specified zone, plus optional offsets, as Text in a custom format.

Date formats are described in the 
[Java DateTimeFormatter](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html)
documentation

---
### Adding functions - UDFs
EEL can be extended by adding **U**ser **D**efined **F**unctions (UDFs). The Java code for a UDF could be something
as simple as:

    public class Half {
        @com.github.tymefly.eel.udf.EelFunction(name = "divide.by2")
        public int half(int value) {
            return value / 2;
        }
    }

After it's been registered, an EEL expression can call this function as:

    $( divide.by2( 1234 ) )

#### User Defined Function (UDF) Requirements
A valid UDF must meet the following requirements:
* The implementing class must have a public no argument constructor
* The implementing method must be annotated with `com.github.tymefly.eel.udf.EelFunction` 
* The implementing method must be public
* The implementing method must return one of the following types:
  * boolean
  * byte
  * short
  * int
  * long
  * float
  * double
  * char
  * a wrapped object for any of the previous types
  * String
  * BigInteger
  * BigDecimal
  * ZonedDateTime
  * com.github.tymefly.eel.EelValue
* The arguments passed to the implementing method must be:
  * one of the types that can be returned
  * VarArgs for one of the types that can be returned
  * the `EelContext`
* The implementing method must not return `null`
* The name given in the `EelFunction` annotation must be valid, as described below
* For security reasons:
  * The implementing function must return quickly to prevent potential DOS attacks - 
    The EelContext sets the maximum time for evaluating the complete expression, which will include the UDF.
  * Functions should execute without side effects.  

**Notes:** 
- `void` is not a supported return type
- The implementing class can define more than one UDF. 
- EEL does not support function overloading. Instead, functions can use default arguments or variable argument lists 
- Values are converted to chars by converting them to Text and taking the first character -
  attempting to convert empty text will throw an exception
- Because the language is null hostile it is guaranteed that none of the arguments passed to a function are `null` 
- Functions that might legitimately generate a 'no-value' should consider returning a value that can be converted to `false`.  
  * For Number values this is zero or a negative value, typically `-1`
  * For Date values this is at or before `1970-01-01 00:00:00Z`. For convenience the constant`EelContext.FALSE_DATE` can be used.

#### Function Names
The name EEL uses to refer to the UDF is given in the EelFunction annotation. So, in the example above `divide.by2` is the UDF name.  

The following prefixes have been reserved for the standard functions:

| Prefix    | Purpose                             | Example        |
|-----------|-------------------------------------|--------------- |
| < none >  | Utility functions                   | count()        |
| system    | System information functions        | system.home()  |
| format    | Data formatting functions           | format.local() | 
| log       | Logging functions                   | log.error()    |
| text      | Reserved for future Text functions  |                |
| logic     | Reserved for future Logic functions |                |
| number    | Number functions                    | number.round() |
| date      | Date functions                      | date.utc()     |
| eel       | Reserved for future use             |                |

EEL will throw a 'EelFunctionException' if a UDF has one of the reserved prefixes. 

Because UDFs must have at least one dot (`.`) delimited prefix UDF names are never valid Java identifiers.


#### Default Arguments
EEL allows functions to be called with default arguments. 
The default value is set by annotating the Java argument with `com.github.tymefly.eel.udf.DefaultArgument`.
For example:

    @EelFunction(name = "random")
    public String random(@DefaultArgument(of = "0") int min, 
                         @DefaultArgument(of = "99") int max) {
       // implement me
    }

Default arguments can only be specified after all the non-default arguments.
 
#### Exception Handling
If a UDF fails in an unexpected way then it can throw either a checked or an unchecked Exception. 

EEL will catch any exceptions thrown by the UDF and wrap them into an 'EelFunctionException' and add some additional 
context information. This exception will then be thrown back to the client application.


#### The Java API 
There are two ways to make EEL aware of a UDF; both are via the EelContext.

##### Registering UDF classes

The simplest way to add UDFs is to register them on a class-by-class basis. For example:

    EelContext context = EelContext.factory()
        .withUdfClass(MyClass1.class)
        .withUdfClass(MyClass2.class)
        .build();

##### Registering packages of UDFs  

Because it can get repetitive registering every new class with the context, EEL provides a way to register a
package of UDFs in one go. First annotate each UDF class with `com.github.tymefly.eel.udf.PackagedEelFunction`.
For example:

    @PackagedEelFunction
    public class MyFunctions {

Then create the context using:

    EelContext context = EelContext.factory()
        .withUdfPackage(MyFunctions.class.getPackage())    // Any of the classes in the package could have been used
        .build();

**Note:** Child packages are not automatically added. If a child package is also required then it must be added
with an additional call to `withUdfPackage()`   

---
	
## 'integration' - Using EEL with JVM applications

The integration module provides some convenience classes that can be used to integrate EEL Expressions into other
applications. 

### EelProperties

This is a simple utility class that allows a Java application to read a properties file that contains EEL Expressions.
These expressions will be evaluated as the properties file is read. For example, if there were a properties file that
contained:

    root=/some/path
    config=${root}/config
    log=${root}/log/$( format.local("yyyy/MM/dd/") )

Then:
* _'root'_ is a traditional hardcoded string. 
* _'config'_ and _'log'_ will both be prefixed with the value associated with _'root'_, in this case `/some/path`
* _'log'_ will have the current date appended.

To read this file, execute the following code:

    Reader reader = ... 
    Properties eelProps = new EelProperties().load(reader);

**Note:** Forward references are not supported. In this example _'root'_ has to be defined before the properties 
that reference it.

---

## 'evaluate' - Developing EEL Expressions

To help write and validate EEL Expressions users can use the **evaluate** application. This is a self-contained
JAR that wraps the EEL library in a CLI application. 

Evaluate can be invoked from the top-level directory as:

    java -jar evaluate/target/evaluate-<version>.jar --help

This will display a help page showing all the options and flags. To evaluate an expression, use:

    java -jar evaluate/target/evaluate-<version>.jar 'Hello'

which will write `Hello` to standard out. 

    java -jar evaluate/target/evaluate-<version>.jar --env 'Your HOME directory is ${HOME}'

will add all the environment variables to the Symbols Table and use one of them in the expression. 

To add UDF classes and packages of UDFs add the implementing classes to the classpath and execute evaluate 
using the `--udf-class` and `--udf-package` options.

---

## Sample EEL Expressions
### Text Pass Through

    this is an expression

The evaluated Result object will have a type of `TEXT` and value of `this is an expression`.  

### Reading Environment Variables

    Your HOME directory is ${HOME}

Assuming the EEL is running on a *nix operating system and that the Symbols Table contains the environment variables
then the Result object will have a type of `TEXT` and a value of that starts with `Your HOME directory is ` 
followed by the user's home directory.

On Windows systems this will expression throw a `EelUnknownSymbolException` because Windows uses the `HOMEDRIVE` and
`HOMEPATH` variables instead of `HOME`. Therefore, the correct expression under Windows would be:

     Your HOME directory is ${HOMEDRIVE}${HOMEPATH}

An alternative expression that works on both operating systems is: 

     Your HOME directory is ${HOME-${HOMEDRIVE}${HOMEPATH}}

However, because reading locating the users home directory is common requirement, a simpler and cleaner way to get this
information is via the standard EEL function:

     Your HOME directory is $( system.home() )


### Paths with a Common Root

Given the following two expressions:

    ${root}/config
    ${root}/template

If the value for `${root}` is in the symbols table then these expressions will evaluate paths that share a common root.
`${root}` might even be determined by a previously evaluated an EEL expression.


### Calling Functions

    Last week was $( date.local( "-7d" ) )

The Result object will have a type of `TEXT` and a value of that starts with `Last week was ` followed by last
week's date and time. 

Function calls can be nested. For example, the following expression will truncate last week's local date to the start of
the day:

    Last week is was $( date.truncate( date.local( "-7d" ), "d" ) )

If the requirement is to display the date without any time fields then use:

    Last week is was $( format.local( "yyyy-MM-dd", "-7d" ))


### Creating a sequence of files

If the EEL repeatedly evaluates

    $( system.temp() )/${myFilePrefix-}$( count() ).txt

**_with the same EelContext_** then each returned Result object will have a type of `TEXT` and a value that forms a
sequence of files in the system temp directory with an optional prefix.

To reset the sequence, recompile the expression with a new EelContext.


### Date-time Based Directories

    ${root-}/$( format.local("yyyy/MM/dd/HH/") )

The Result object will have a type of `TEXT` and a value that would make a valid directory name. Every
hour the expression will evaluate to a different directory. The optional `root` variable allows
the client to move the directory structure to a new location.

* If it is later determined that the system only needs one directory each day then change `yyyy/MM/dd/HH/` to `yyyy/MM/dd/` 
* If it is later determined that the system should have a flat directory structure then change `yyyy/MM/dd/HH/` to `yyyy-MM-dd-HH/`


### Converting Paths

To convert a path to *nix format use

    $( replace( ${root-}, "\\", "/") )

The Result object will have a type of `TEXT` and a value based on the optional _root_ value from the Symbols Table, 
but with forwards slashes instead of Windows style backslashes.

To force the path to the format used by the underlying operating system then use: 

    $( realPath( ${root-} ) )

This conversion could be used in the _date-time based Directory_ example above to ensure that the returned path
is formatted correctly.

    $( realPath( ${root-} ~> "/" ~> format.local("yyyy/MM/dd/HH/") ) )


### Logging

To log a single value at _info_ level use

    $( log.info( ${myVariable-Not Set} ) )

The Result object will have a type of `TEXT` and a value that either comes from the Symbols Table, or the literal
Text `Not Set`. The returned value will be written to the system log at INFO level. For example:

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Not Set

The text `Logged EEL Message:` can not be changed or removed as it is used to guard against log reputation attacks

To add some context to the logged message use:

    $( log.info( "The variable is {}", ${myVariable-Not Set} ) )

which might log

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: The variable is Not Set

Logging functions can write multiple values, but only the last one is returned by the expression. For example

    $( log.info( "Evaluating {} + {} = {}", 1, 2, ( 1 + 2 ) ) )

will log 

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Evaluating 1 + 2 = 3

and will return a Result object with a type of `TEXT` and a value of `'3'`

If the requirement to only log variables if they are not set then use:

    ${myVariable-$( log.warn( "myVariable is not set" ) ) }

This works because the Expression Expansion for the default value, which includes the logging, will only be executed if 
_myVariable_ is not in the Symbols Table.


### Failing Expressions

To fail an expression if a precondition is not met then use `fail` inside a condition. For example:

    $( isEmpty( ${myVariable-} ) ? fail() : ${myVariable} )

`isEmpty( ${myVariable-} )` will return `true` if the value associated with _myVariable_ is undefined or an empty string.
If this is the case then `fail()` will be executed which will terminate the expression with an `EelFailException`. 
If there is text then `${myVariable}` is read from the Symbols Table and returned to the client as Text

If the requirement is to fail an EEL expression with a custom message if a variable is not defined then use:

    ${myVariable-$( fail("Custom Message") )}

If the requirement is to fail an EEL expression if multiple variables are not defined then use: 

    $( ! myVar1? || ! myVar2? ? fail() : ${myVar1} ~> ${myVar2} )


### Dates Operations

Dates can be compared using the numeric operators. This works because as EEL will automatically convert the Dates
into the number of seconds elapsed since 1970-01-01 00:00:00 in the UTC time zone. 

The equality operators can be used to check if a date is before, after or at the same time as another date. For example:  

    $( DATE( ${dateValue} ) >= date.utc() )

Will return a Result object that will have a type of `LOGIC` and a value that is `true` only if `dateValue` is in
the future. The explicit conversion to DATE is required because values read from the symbols table are text,
which the `>=` operator would otherwise try to convert to a number.   

In the same way, the minus operator can be used to calculate time differences. For example:

    $( date.utc() - date.start() )

Will return a Result object that will have a type of `NUMBER` and a value that is the age, in seconds, of the EEL Context.

If seconds is too fine-grained then a time difference can be returned in minutes by using:

    $( duration( date.start(), date.utc(), "minutes" ) )

Building on this, an expression to check if a file is out of date could look something like:

    $( duration( modifiedAt("/path/to/my/file.txt"), date.local(), "months" ) > 6 )

In this case the Result object will have a type of `LOGIC` and a value that is `true` only if the referenced file is
more than 6 months old.

---
## External Libraries

EEL was implemented using as few third-party libraries as possible to prevent the client code from bloating. 
The dependant libraries are:

* **org.reflections:reflections** - used to scan for packages of EEL functions
* **ch.obermuhlner:big-math** - implementation for the maths operations and functions
* **org.slf4j:slf4j-api** - the logging facade
* **com.google.code.findbugs:jsr305** - used internally for tracking null/non-null object references
* **com.github.spotbugs:spotbugs-annotations** - used internally for bug checking annotations

Evaluate has one additional dependency:
* **args4j:args4j** - Command line argument parsing
