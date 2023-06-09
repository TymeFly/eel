# EEL
EEL is a small, compiled, **E**xtensible **E**xpression **L**anguage which can be used by a JVM application to evaluate 
expressions at run time. One particular use case for EEL is to configure applications in a dynamic and flexible way.

---
## Overview

There is a common requirement when developing systems to make certain values configurable. This is traditionally
achieved by reading values from the environment variables, JVM properties, databases, or properties files. 
These solutions work well if nothing more sophisticated than simple text substitution is required - the exact values
are known at deployment time and none of values are related.
However, sometimes something more sophisticated is needed. For example, an application might want to write data to a
file were the name or directory is based on the current date and/or time. On other occasions multiple settings may
need to be combined, for example to build up set of directories that have a common root. 

Dates in particular are often problematic as developers and/or end users can have different ideas
about how to format dates based on their own cultural preference - of course, the correct way is always ISO-8601 ;^)

To solve this problem a "_Pragmatic Programmer_" would consider using a Domain Specific Language to derive these
values at run time, and that's exactly what the **E**xtensible **E**xpression **L**anguage (EEL) is.
The strings that were previously used to configure an application can be treated as EEL Expressions that are
simple yet powerful and can be quickly evaluated at run time.

---
## EEL Requirements

The requirements for EEL are:
* Expressions that are just strings can be passed through as-is. This makes it possible to take settings
  that were previously hardcoded strings and use them as EEL Expressions without change.
* The language should be easy to use, especially by somebody who is familiar with Java programming and/or shell scripting 
* The language must include all the usual text, maths and logic operations that languages typically support
* The language must be able to manipulate date/time stamps
* The language must be _Null Hostile_ to prevent NullPointerExceptions  
* The language must support logging so clients can see what is being evaluated for security and/or debugging purposes
* The language must be secure:
  * It must be impossible to write an expression that will not complete at either compile time or runtime
  * It must not be possible to inject false information into the system logs. 
  * Except for logging, expressions are executed without side effects.
* The language must be extensible - additional functions can be added to the language without needing to update EEL
* The Java Programming API should be simple and concise

**Note:** There is no requirement that EEL be Turing complete. 
While there is support for conditionals, there is no inbuilt support for either iteration or recursion. 

---
## What's New
### 1.0.0
Everything

---
## Building EEL
EEL requires:
* Java 17+
* Maven 3.6+

The source code contains three modules:

* **lib** - the code that implements EEL
* **integration** - some helper classes that can be used to integrate EEL with JVM applications
* **evaluate** - a CLI wrapper for _lib_ that can be used to test EEL Expressions 

---
## 'lib' - The EEL Language and its Java API
### The EEL API
The API for EEL is based around four concepts. These are:

#### EEL Context

The EEL Context holds compile time information for an EEL Expression. A single EelContext can be used to compile
multiple EEL Expressions.

Currently, the client can use the Context to:
* Set the precision for maths operations
* Import **U**ser **D**efined **F**unctions (UDFs).
* Guard against rogue expressions causing Denial Of Service (DOS) attacks by:
  * Defining the maximum length of the expression in characters
  * Defining a timeout for evaluating the expression

In addition, the Context also:
* Allows EEL functions to read the time the Context was created 
* Provides a scope for the `count` function  

All the Context settings have defaults; if these are acceptable then the Default EelContext can be used.

EelContext objects are built using a fluent API; the entry point is `EelContext.factory()` 
 

#### EEL Expression

This is where the source expression is compiled. Like any other program, a compiled expression can be executed multiple
times. This could be handy if an expression makes use of functions that return different values each time 
the expression is evaluated (e.g. reading the current date/time, generating random numbers or reading a counter) 
Alternatively, the expression could be evaluated with a different Symbols Table (see below) which could also cause 
EEL to return a different Result (see below). 

EEL Expressions are built using a fluent API; the entry point is `Eel.factory()`. In addition, there are
convenience methods in `Eel` that can be used to compile expressions with the Default EelContext.


#### Symbols Table

The Symbols Table is lookup mechanism that maps variable names in an expression to values that are provided at runtime.
These values can come from any, all or none of the following sources:
- The environment variables
- The JVM properties
- Maps 
- Lambda functions 
- A hardcoded string which is associated with **All** variable names. This is usually used as a default value. 

EEL will treat values read from the Symbols Table as Text. In most cases EEL will correctly convert the value to the 
type required by the operator that uses it. If this is insufficient then explicit conversion functions can be used to
ensure the correct type (see below)

If a key exists in multiple sources, then the first source added to the Symbols Table takes priority. Because
of this, no further data sources can be defined after a hardcoded string has been added.

Once defined, a Symbols Table can be used to evaluate multiple EEL Expressions. However, there is no requirement that
an EEL Expression has to use a Symbols Table when it is evaluated.      

Symbols Table objects are built using a fluent API; the entry point is `SymbolsTable.factory()`. In addition, there are 
convenience methods in:
- `SymbolsTable` - used to build a Symbols Table that reads from a single data source
- `Eel` - used to evaluate a compiled expression with a one-off Symbols Table that reads a single data source


#### Result

A Result object represents the output from an EEL Expression. Each Result contains:
- The type of the evaluated value (see below)
- The evaluated value which the client can read the value via a type specific getter method. If the getter method doesn't
match the Result type, then the Result will try to convert it using the rules described below. 

#### Exceptions 
The EEL compiler and runtime can throw the following Exceptions:

| Exception                 | Purpose                                              | Extends              |
|---------------------------|------------------------------------------------------|----------------------|
| EelException              | Base class for all EEL exceptions                    | RuntimeException     |
| EelInternalException      | The EEL core failed. This should never be seen       | EelException         |
|                           |
| EelCompileException       | Base class for all compile time exceptions           | EelException         |
| EelIOException            | The expression source could not be read              | EelCompileException  |
| EelSyntaxException        | The source expression has a syntax error             | EelCompileException  |
|                           |
| EelRuntimeException       | Base class for all runtime exceptions                | EelException         |
| EelInterruptedException   | The evaluating thread was interrupted                | EelRuntimeException  | 
| EelTimeoutException       | EEL took too long to evaluate the expression         | EelRuntimeException | 
| EelUnknownSymbolException | EEL failed to look up a value in the Symbols Table   | EelRuntimeException  |
| EelConvertException       | An EEL type conversion failed                        | EelRuntimeException  |
| EelFunctionException      | A function failed or could not be invoked            | EelRuntimeException  |
| EelArithmeticException    | An Arithmetic operation failed                       | EelFunctionException |
| EelFailException          | EEL executed the `fail()` function                   | EelFunctionException | 

**Note:** EEL only throws unchecked exceptions.

#### Example Java Code

**Basic Usage**

In its simplest form, EEL can be invoked as:

    String result = Eel.compile(  ...some expression...  )
      .evaluate()
      .asText();

In this example the Default EelContext is used to compile the expression, it is then executed without reference to a 
Symbols Table and the evaluated value is returned as String. 
If this is enough for the client application then nothing more complex is required.

**Inline EelContext**

    String result = Eel.factory()
      .withPrecision(12)
      .compile(  ...some expression...  )
      .evaluate()
      .asText();

In this example the fluent API provided by `Eel.factory()` creates a new EelContext as the Expression is compiled;
in this case to set the maths precision.

Inlining the Context makes for concise code but does not allow the EelContext to be reused

**Explicit EelContext**

    EelContext context = EelContext.factory()
      .withPrecision(12)
      .build();
    String result = Eel.compile(context, ...some expression...)
      .evaluate()
      .asText();

This example uses the fluent API provided by `EelContext.factory()` to create an EelContext object. This context is
then used to compile an Expression. 
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
There are several advantages to creating Symbols Table objects; the table can read values from multiple sources, 
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
verbose.

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

If the data type does not match the type returned by the getter method, then the Result object will try to convert the
data to the required type. As all data can be represented as text it will always be possible read the result using
`asText()`. The rules for converting values are described below.


### EEL Syntax and Semantics

##### Source Characters
EEL Expressions accept only printable characters, spaces, and tabs as valid input. 
Other characters, including carriage-return, new-line, null and control characters are invalid. 

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
migration path for systems that are already configured with strings.

Where EEL becomes more useful is with the two interpolation mechanisms it supports. These are substrings in 
the parsed expression. The interpolation mechanisms are:
1. **Variable Expansion** which is written in the form `${...}` and is used to read values from the Symbols Table
2. **Expression Expansion** which is written in the form `$(...)` and is used to perform calculations and/or call functions. 

The interpolation mechanisms can be nested. This is most commonly seen when an Expression Expansion uses a 
Variable Expansion to read a value from the Symbols Table.

The following sections describe these interpolation mechanisms in more detail. 


#### Variable Expansion - ${...}
Variables are written in a format that should be familiar to somebody who is familiar shell scripting - `${key}`. 
Unlike shell scripting, in EEL the braces are mandatory.
When EEL encounters a variable, it will replace the expansion with the associated value from the Symbols Table.

EEL variables support similar modifiers to bash:
- `${key}` - the text value associated with _key_ unmodified
- `${#key}` - the length of the text value associated with _key_ 
- `${key^}` - the text value associated with _key_, but with the first character in upper case
- `${key^^}` - the text value associated with _key_, but with all the characters in upper case
- `${key,}` - the text value associated with _key_, but with the first character in lower case
- `${key,,}` - the text value associated with _key_, but with all the characters in lower case
- `${key~}` - the text value associated with _key_, but with the case of the first character toggled
- `${key~~}` - the text value associated with _key_, but with the case of the all the characters toggled
- `${key-default}` - if there is no value in the Symbols Table associated with the _key_ then `default` value is used

EEL allows these modifiers to be combined. For example:

- `${key^^-default}` - if there is a value associated with _key_ then use the value after converting it to uppercase.
  If there is no value associated with _key_ then use `default`
- `${#key-default}` - if there is a value associated with _key_ then use its length.
  If there is no value associated with _key_ then use `default`

As default is an EEL Expression in its own right, the following variable expansions are valid:

- `${undefined-defaultText}` - if _undefined_ is not in the Symbols Table then use the literal text `defaultText`
- `${undefined-}` - if _undefined_ is not in the Symbols Table then use empty text 
- `${first-${second}}` - if _first_ is not in the Symbols Table then use the value associated with _second_ instead 
- `${first-${second-defaultText}}` - if _first_ is not in the Symbols Table then try _second_. 
  If _second_ also not in the Symbols Table then use the literal text `defaultText`
- `${undefined-$( expression )}` - if _undefined_ is not in the Symbols Table then evaluate _expression_

**Undefined values**

There are several ways to handle keys do not have an associated value in the Symbols Table. These are:
1. Configure the Symbols Table to return a hardcoded value if the variable is undefined (e.g. empty text)
1. Use defaults with all variable expansions 
1. Use the isDefined operator (`?`) to check the key is defined in the Symbols Table before reading it (see below)
1. Have the client code handle the `EelUnknownSymbolException` exception


#### Expression Expansion  - $(...)
The full power of EEL is in the Expression Expansion. This a simple programming language that is used to evaluate
a single value.

##### Reserved words and symbols

| Values  | Number Ops   | Logic Ops           | Relational Ops | Text Ops | Conversions | Quotes | Misc |
|---------|--------------|---------------------|----------------|----------|-------------|--------|------|
| `true`  | `+`          | `&`                 | `=`            | `~>`     | `TEXT`      | `'`    | `$` |
| `false` | `-`          | `&&`                | `<>` and `!=`  |          | `NUMBER`    | `"`    | `{` |
| `e`     | `*`          | vertical-bar        | `>`            |          | `LOGIC`     |        | `}`  |
| `pi`    | `/`          | double-vertical-bar | `>=`           |          | `DATE`      |        | `(`  |
|         | `%`          | `!`                 | `<`            |          |             |        | `)`  |
|         | `^` and `**` |                     | `<=`           |          |             |        | `,`  |
|         | `<<`         |                     |                |          |             |        | `?`  | 
|         | `>>`         |                     |                |          |             |        | `:`  |
|         | `AND`        |                     |                |          |             |        |      |
|         | `OR`         |                     |                |          |             |        |      |
|         | `XOR`        |                     |                |          |             |        |      |
|         | `NOT`        |                     |                |          |             |        |      |

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

EEL Identifiers are case-sensitive.

It should be noted that valid EEL identifiers do not always make valid Java identifiers and vise versa.

##### Naming conventions
- Constants are all lowercase. For example, `true`, and `pi`
- Other reserved words are all UPPERCASE. For example, `AND`, `OR`, `TEXT` and `LOGIC`
- The function names (see below) are in lowerCamelCase. Dots (`.`) are sometimes included in function names to create
  prefixes. These are used to logically group certain functions and avoid namespace clashes much as packages do in
  Java. Otherwise, they have no special meaning. 
  Example function names are `count`, `cos`, `isEmpty`, `system.home`, `date.local`, `log.warn` and `format.hex`
- Variable names are typically in a format that is appropriate to the data source. For example, environment variables
  are usually in _UPPERCASE_WITH_UNDERSCORES_ while JVM properties are in _lower.case.with.dots_. The restrictions on
  variable names are detailed above.

##### Data types
EEL natively supports 4 data types. These are named differently from their Java equivalents, to distinguish between
data that is in the EEL and Java domains.

The EEL data types are:
- **Text**

  Text literals can be written in the form `"..."` or `'...'`. The opening quote can be either a single or a double
  quote, but the associated closing quote must match the opening quote. This allows a double-quoted text literal
  to contain single quote characters and vise versa. Additionally, escape sequences can be used to embed special
  characters, including what would have been the closing quote.   

  Variable expansions (`${..}`) and expression expansions (`$(...)`) are not supported in Text literals. This is
  not usually a problem as Text concatenation can be used instead.

  Text values are returned from a Result as a Java String.


- **Logic**

  The two logic literals are `true` and `false`.

  Logic values are returned from a Result as a Java boolean primitive.


- **Number**

  The numeric literals can be expressed in any of the following ways:
  - Decimal integer (e.g. `1234`)
  - Hexadecimal integer (e.g. `0x89ab`)
  - Decimal with a fractional part (e.g. `123.456`)
  - Scientific format (e.g. `2.997e8`)

  Numbers are returned from a Result as a Java BigDecimal


- **Date**

  EEL expressions support Date-Time stamps, to a precision of a second. As there are no date literals Dates are generated by: 
  - Calling a function that returns a date, such as `date.utc()`, `date.local()`, `date.at()` or `date.start()`
  - Converting ISO 8601 formatted Text
  - Converting the number of seconds that have elapsed seconds since 1970-01-01 00:00:00 in the UTC time zone

  Dates are returned from a Result as a Java ZonedDateTime


##### Null Values

EEL is a Null Hostile language. As a consequence:
  - Variable Expansions will throw an `EelUnknownSymbolException` if a value cannot be determined.
  - If a function returns null then an `EelFunctionException` is thrown. 
  - It is guaranteed that the Result getters will never return null.


##### Types conversions

Values in EEL Expressions are loosely typed; EEL will silently convert values as required. The conversion rules are:

* Text values are converted to Numbers if they can be parsed as either:
  - Decimal integer (e.g. `1234`)
  - Hexadecimal integer (e.g. `0x89ab`)
  - Decimal with a fractional part (e.g. `123.456`)
  - Scientific format (e.g. `2.997e8`)
  
  Leading and trailing spaces will be ignored.
* Text values `"true"` and `"false"` are converted to the Logic values `true` and `false` respectively

  Leading spaces, trailing spaces and case will be ignored.
* Text values can be converted to Dates by parsing them as ISO 8601 formatted values in the format `yyyy-MM-dd'T'HH:mm:ssX`. 
  * The precision of these strings is flexible; they can contain as little as a 4-digit year or be specified to the second,
    but each period but be fully defined. For example, `2000-01` can be converted to a date, but `2001-1` cannot.  
  * If a time zone is not specified then UTC is assumed
  * The `T`, `-` and `:` separator characters can be replaced with a single space or omitted entirely.
  * Fractions of a second are not supported
  * Leading and trailing spaces will be ignored.
* Number values are converted to Text as their plain (non-scientific) decimal representation
* Number values `1` and `0` are converted to the Logic values `true` and `false` respectively
* Numbers are converted to Dates as the number of elapsed seconds since 1970-01-01 00:00:00 in the UTC zone.  
* Logic values `true` and `false` are converted to Text values `"true"` and `"false"` respectively
* Logic values `true` and `false` are converted to numeric values `1` and `0` respectively
* Dates are converted to Text in the format `yyyy-MM-dd'T'HH:mm:ssX`
* Dates are converted to Numbers by taking the number of elapsed seconds since 1970-01-01 00:00:00 in the UTC zone.

All other conversions are illegal and will cause EEL to throw an `EelConvertException`. 
For example the Number `1` can be converted to a Logic value, but the `2` will throw an exception.

In general EEL will automatically convert values to the type required by the operator that uses it. If an expression
needs to ensure a specific type then the conversion functions `TEXT`, `NUMBER`, `LOGIC` and `DATE` are available. 
The syntax of these functions is exactly the same as any other function. For example `$( LOGIC("1") )` will return `true`

The rules have been defined this way because in the real world they generally do the right thing, however they are not
always Symmetric. For example:
 - `DATE( NUMBER( date.local() ) )` will convert a date to a number and then convert back to a date. The original date 
was in the local time zone but the result will be in the UTC zone. However, the number of seconds elapsed since 
1970-01-01 00:00:00 in the UTC zone is maintained. 
 - `TEXT( NUMBER('0x1234') )` will convert text to a number to and back again, but the result will be in decimal rather 
than the original hex
 - `TEXT( DATE('2000-01-01') )` will convert text to a date and back to text again, but the result will contain time 
fields that were absent in the original text. 

There are also occasions when the rules are not Transitive. For example:
- `TEXT( NUMBER( true) )` will return `1`, but `NUMBER( TEXT( true) )` will throw an `EelConvertException`. 

##### Constants
EEL defines the following constants:
- **true** 
- **false**
- **e**
- **pi**

Constant names are case-sensitive.

##### Operators
EEL supports the following operators:

| Priority | Operators Symbols                                | Operators Name(s)                                                                |
|----------|--------------------------------------------------|----------------------------------------------------------------------------------|
| Highest  | `-` `!` `NOT` `?` conversions, function-call     | negation, logical-not, bitwise-not, isDefined, type-conversion, function-calls   |
|          | `^` `~>`                                         | power, string-concatenation                                                      |
|          | `*` `/` `%` `AND` `&` `&&`                       | multiply, divide, modulus, bitwise-and, logical-and, short-circuit-and           |
|          | `+` `-` `XOR`                                    | plus, minus, bitwise-xor                                                         |
|          | `<<` `>>`  `OR` vertical-bar double-vertical-bar | left-shift, right-shift, bitwise-or, logical-or, short-circuit-or                | 
|          | `=` `!=` `>` `>=` `<` `<=`                       | equal, not-equal, greater-than, greater-than-equals, less-than, less-than-equals |   
| Lowest   | `?  :`                                           | condition                                                                        |

**Notes:**
- Operator names are case-sensitive.
- The not-equal operator exists in two forms; `!=` and `<>`. 
  - The difference is purely cosmetic; they both behave in the same way and have the same priority.
  - The reason the operator exists in two forms is to match other languages    
- The power operator exists in two forms; `^` and `**`. 
  - The difference is purely cosmetic; they both behave in the same way and have the same priority.
  - The reason the operator exists in two forms is to match other languages    
- Just like C and Java, the logical and operators come in two varieties; long form and short-circuited. 
- Operators expect their operands to be a particular type and will automatically convert values using the rules
  described above. The exceptions to this are the equals and not-equal operators which will try to compare values in 
  their native types and will only convert values if the types are not the same. 
- The logical operators (`!`, `&`, `&&`, `|` and `||`) are different from the numeric bitwise operators 
  (`NOT`, `AND`, `OR` and `XOR`) because they operate on different types.    
- The `+` operator is purely a numeric operator; the `~>` operator is used for string concatenation. This removes
  the need for a conversion function when reading values from the Symbols Table that are expected to be numeric.
  
  `${A} ~> {B}` will concatenate the Text operands whereas `${A} + {B}` will convert them to numbers and then add them together.  
- The isDefined operator (`?`) is used to check if a key has an associated value in the Symbols Table. If the key is 
  defined the operator returns `true`, otherwise it returns `false`. 
  The operator is written after the name of the Symbols Table key (it has right associativity).

  For example, `myKey?` will return _true_ only if `${myKey}` returns a value rather than throwing an `EelUnknownSymbolException`.
- Fractional parts of numbers will be silently truncated by the bit-manipulation operators. 

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

Some functions support optional arguments; if the Expression does not explicitly pass a value, then EEL will silently
add a default value. This is denoted in the list below by surrounding the optional argument(s) with braces.
For example, `random( { minValue { , maxValue } } )` can be called as `random()`, `random( minValue )` 
or `random( minValue, maxValue )`.

#### Utility Functions
##### Text Processing
- **lower( text )** - returns the _text_ converted to lower case
- **upper( text )** - returns the _text_ converted to upper case


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
- **len( text )** - returns the length of the _text_, including leading and trailing spaces
- **trim( text )** - returns the _text_ with all leading and trailing whitespaces removed
- **indexOf( text, subString )** - first occurrence of _subString_ in _text_, or -1 if _subString_ is not present
- **lastIndexOf( text, subString)** - last occurrence of _subString_ in _text_, or -1 if _subString_ is not present

##### File System Functions
- **baseName( path { , extension } )** - returns the _path_ with the leading directory components and the optional _extension_ removed
- **dirName( path )** - returns _path_ with its last non-slashed component and trailing slash removed
- **extension( path, { , max } )** - returns up to _max_ of the right most file extensions from the _path_. The default is to return all extensions
- **realPath( path )** - returns _path_ in a canonicalised format based on current operating system


- **exists( path )** - returns _true_ only if the file at the specified _path_ exists  
- **fileSize( path { , defaultSize } )** - returns the size of the file in bytes. 
If the file does not exist then _defaultSize_ is returned. This defaults to -1
- **createAt( path { , defaultTime } )** - returns the local date that the file was created. 
If the file does not exist then _defaultTime_ is returned. This defaults to 1970-01-01 00:00:00Z 
- **accessedAt( path { , defaultTime } )** - returns the local date that the file was last accessed. 
If the file does not exist then _defaultTime_ is returned. This defaults to 1970-01-01 00:00:00Z
- **modifiedAt( path { , defaultTime } )** - returns the local date that the file was last modified. 
If the file does not exist then _defaultTime_ is returned. This defaults to 1970-01-01 00:00:00Z 

##### Misc Utility Functions
- **count()** - returns the next value in a 1 based counter. To reset the counter recompile the expression with a new Context
- **random( { minValue { , maxValue } } )** - return a random non-fractional number in the range _minValue_ to _maxValue_ inclusive.
The default range is a number between 0 and 99 inclusive.
- **uuid()** - returns a new UUID
- **duration( from, to { , period } )** - returns the duration between two dates in the specified time period. The default period is seconds
- **fail( { message } )** - immediately terminates the expression by throwing an _EelFailException_

#### System Information Functions

- **system.fileSeparator()** - returns the Operating System path separator (usually either '\\' or '/')
- **system.home()** - returns the path to the users home directory
- **system.pwd()** - returns path to the applications current working directory.
- **system.temp()** - returns path to the system temporary directory.


#### Maths Functions
##### Trigonometric Functions
- **sin( value )** - returns the sine of the radian _value_
- **cos( value )** - returns the cosine of the radian _value_
- **tan( value )** - tangens of the radian _value_  


- **asin( value )** - returns the arc sine (inverted sine) of _value_
- **acos( value )** - returns the arc cosine (inverted cosine) of _value_
- **atan( value )** - returns the arc tangens (inverted tangens) of _value_ 


##### Other Maths Functions

- **max( values... )** - returns the largest value of the numbers passed
- **min( values... )** - returns the smallest value of the numbers passed
- **avg( values... )** - returns the average value of the numbers passed
 
 
- **abs( value )** - returns the absolute value of a number
- **exp( value )** - returns the natural exponent of value (e<sup>value</sup>)
- **factorial( value )** - returns the factorial of _value_
- **ln( value )** - returns the natural log for _value_
- **log( value )** - returns the log in base 10 for _value_
- **root( value { , n } )** - returns the n'th root of _value_. The default value of _n_ is 2, which returns square roots
- **sgn( value )** - returns the sign of a numeric _value_; -1 for negative, 0 for zero and 1 for positive

An `EelArithmeticException` is thrown if a maths function fails. 
The precision of the returned value is set in the EelContext.

#### Date Functions

- **date.start( { zone } )** - returns a time-date stamp indicating when the EelContext was created. _zone_ defaults to UTC
- **date.utc( offsets... )** - returns a time-date stamp based on the current UTC time plus any optional offsets
- **date.local( offsets... )** - returns a time-date stamp based on the current local time plus any optional offsets
- **date.at( zone, offsets... )** - returns a time-date stamp based on the current time in the specified zone plus any optional offsets


- **date.offset( date, offsets... )** - add one or more offsets to the _date_
- **date.set( date, specifier... )** - set more or more periods in the _date_ to an absolute value.
- **date.setZone( date, zone )** - change the _date_'s time zone without adjusting the time.  
- **date.moveZone( date, zone )** - change the _date_'s time zone and adjust the time to maintain the same instant
- **date.truncate( date, period )** - truncate the precision of the _date_ to the specified time _period_

Most of the date functions support the ability to modify, set or offset a date with respect to a time period.
The supported time periods are: 

| Period      | Full Name | Short Name | Notes        |
|-------------|-----------|------------|--------------|
| **Years**   | `year`    | `y`        |              |
| **Months**  | `month`   | `M`        |              |
| **Weeks**   | `week`    | `w`        | Offsets only |
| **Days**    | `day`     | `d`        |              |
| **Hours**   | `hour`    | `h`        |              |
| **Minutes** | `minute`  | `m`        |              |  
| **Seconds** | `second`  | `s`        |              |

Period names are case-sensitive. Time zones can be either:
- Fixed offsets - a fully resolved offset from UTC such as `+5`
- Geographical regions - an area where a specific set of rules for finding the offset from UTC apply such as `Europe/Paris` 

For example:
- **date.start( "Europe/Paris" )** - returns the time the EEL Context was created with respect to the Paris time zone
- **date.utc( "1day" )** - returns the current time in UTC plus 1 day
- **date.local( "+2month" )** - returns the current time in the local time zone plus 2 months
- **date.at( "-5", "-1w" )** - returns the current time in the _UTC -5_ time zone minus 1 week 
- **date.set( < date >, "12h", "0m", "0s" )** - set the _< date >_ to midday
- **date.offset( ${variable}, "15minutes" )** - returns the time given in the _variable_ plus 15 minutes 
- **date.truncate( date, "month" )** - reduce the accuracy of the _date_ to the start of the month

#### Logging Functions

These functions are used to perform logging as a side effect. The value returned by the function is the last argument 
converted to Text. 

In order of priority, from high to low, the logging functions are:

- **log.error( format, args... )** - Log the _args_ at error level. 
- **log.warn( format, args... )** - Log the _args_ at warn level. 
- **log.info( format, args... )** - Log the _args_ at info level. 
- **log.debug( format, args... )** - Log the _args_ at debug level. 
- **log.trace( format, args... )** - Log the _args_ at trace level. 

**Note:** The client's logging framework is responsible for enabling logging. EEL writes all its messages to the
`com.github.tymefly.eel.log` logger, so it is recommended that this is configured with **trace** level
logging enabled.

**Note:** Because expressions can be set after applications have been developed and deployed then there is a 
need to guard against log repudiation attacks. Consequently:
1. All logged messages are always prefixed by the literal text `Logged EEL Message: ` to make it obvious they are 
generated by EEL and not from any other source. This prefix can not be changed. 
2. All control characters, except for tabs, are filtered out of the logged message. The filter deliberately removes  
new-line and carriage return characters to prevent EEL logging text that that might be mistaken for another logging message 

 
#### Data Formatting Functions

- **format.octal( value )** - Converts the _value_ to octal Text. 
- **format.hex( value )** - Converts the _value_ to hexadecimal Text (without a leading _0x_)
- **format.number( value, radix )** - Converts a number to Text in the given radix 


- **format.date( format, date )** - Converts the _date_ to Text in a custom format.
- **format.utc( format, offsets... )** - Returns the current UTC time (plus optional offsets) in a custom format.
- **format.local( format, offsets... )** - Returns the current local time (plus optional offsets) in a custom format.
- **format.at( zone, format, offsets... )** - Returns the current time in the specified zone (plus optional offsets) in a custom format.

Date formats are described in the 
[Java DateTimeFormatter](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html)
documentation

---
### Adding functions
EEL can be extended by adding **U**ser **D**efined **F**unctions (UDFs). The Java code for a UDF could be something
as simple as:

    public class Half {
        @com.github.tymefly.eel.udf.EelFunction(name = "divideBy.2")
        public int half(int value) {
            return value / 2;
        }
    }

This could be called by EEL as:

    $( divideBy.2( 1234 ) )

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
  * a wrapped object for any of the previous types
  * BigInteger
  * BigDecimal
  * String
  * ZonedDateTime
* All arguments passed to the implementing method must be one of the following:
  * one of the types that can be returned
  * VarArgs for one of the types that can be returned
  * the `EelContext`
* The implementing method must not return `null`
* The name given in the `EelFunction` annotation must be valid (see below)
* The implementing function must return quickly to guard against DOS attacks
* Functions should not perform any side effects.  

**Notes:** 
- `void` is not a supported return type
- The implementing class can define more than one UDF. 
- It is recommended that failing UDFs should throw a `EelFunctionException`
- EEL does not support function overloading. Instead, functions can use default arguments and variable length
arguments lists.

#### Function Names
The name EEL uses to refer to the UDF is set in the EelFunction annotation. 
In the example above `divideBy.2` is the UDF name even though it is not a valid Java identifier. 

EEL reserves the following prefixes in function names:

| Prefix    | Purpose                            | Example        |
|-----------|------------------------------------|--------------- |
| < none >  | Utility functions                  | count()        |
| system    | System information functions       | system.home()  |
| date      | Date functions                     | date.utc()     |
| format    | Data formatting functions          | format.local() | 
| log       | Logging functions                  | log.error()    |
| eel       | Reserved for future EEL extensions |                |

EEL will throw a 'EelFunctionException' if a UDF has one of the reserve prefixes. 


#### Default Arguments
EEL allows functions to define default arguments. 
The default value is set by annotating the Java argument with `com.github.tymefly.eel.udf.DefaultArgument`.
For example:

    @EelFunction(name = "random")
    public String random(@DefaultArgument(of = "0") int min, 
                         @DefaultArgument(of = "99") int max) {
       // implement me
    }

Default arguments can only be specified after all the non-default arguments.
 
#### The Java API 
There are two ways to make EEL aware of UDFs; both are via the EelContext.

##### Adding UDF classes(s) 

The simplest way to add UDFs is to import them on a class-by-class basis. For example:

    EelContext context = EelContext.factory()
        .withUdfClass(MyClass1.class)
        .withUdfClass(MyClass2.class)
        .build();

##### Adding package(s) of UDFs  

Because it can get repetitive adding every new class to the context, EEL provides a way to add a complete
package of UDFs in one go. First annotate each UDF class with `com.github.tymefly.eel.udf.PackagedEelFunction`.
For example:

    @PackagedEelFunction
    public class MyFunction1 {

Then create the context using:

    EelContext context = EelContext.factory()
        .withUdfPackage(MyClass1.class.getPackage())    // Any of the classes in the package could have been used
        .build();

**Note:** Child packages are not added with their parent. If a child package is also required then it must be added
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
    log=${root}/log/$( format.date("yyyy/MM/dd/", date.local()) )

Then:
* _'root'_ is a traditional hardcoded string. 
* _'config'_ and _'log'_ will both be prefixed with the value associated _'root'_, in this case `/some/path`
* _'log'_ will have the current date appended.

To read this file, execute the following code:

    Reader reader = ... 
    Properties actual = new EelProperties().load(reader);

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

which will just write `Hello` to standard out. 

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

On Windows systems this will expression throw a `EelUnknownSymbolException` because Windows uses the `USERPROFILE` 
variable instead of `HOME`. Therefore, the correct expression under Windows would be:

     Your HOME directory is ${USERPROFILE}

An alternative expression that works on both operating systems is: 

     Your HOME directory is ${HOME-${USERPROFILE}}

If there is a possibility that neither environment variable is set, then:

     Your HOME directory is ${HOME-${USERPROFILE-undefined}}

will return `Your HOME directory is undefined` if neither variable is set.

However, because reading locating the users home directory is common requirement, a simpler and cleaner way to get this
information is via the standard EEL functions:

     Your HOME directory is $( system.home() )


### Paths with a Common Root

Given the following two expressions:

    ${root}/config
    ${root}/template

If the value for `${root}` is in the symbols table then these expressions will evaluate paths that share a common root.
`${root}` might even be determined by a previously evaluated an EEL expression.


### Calling Functions

    Last week was $( date.local( "-7d") )

The Result object will have a type of `TEXT` and a value of that starts with `Last week was` followed by last
week's date and time. 

Function calls can be nested. For example, the following expression will truncate the local date to the start of
the day:

    Last week is was $( date.truncate( date.local( "-7d"), "d" ) )


### Creating a sequence of files

If the EEL repeatedly evaluates

    $( system.temp() )/$( count() ).txt

_with the same EelContext_ then each returned Result object will have a type of `TEXT` and a value that form a sequence
of files in the system temp directory.

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

The Result object will have a type of `TEXT` and a value based on the _root_ value from the Symbols Table, but with
forwards slashes instead of Windows style backslashes.

To force the path to the format used by the underlying operating system then use: 

    $( realPath( ${root-} ) )

This conversion could be used in the _date-time based Directory_ example above to ensure that the returned path
is formatted correctly.

    $( realPath( ${root-} ~> "/" ~> format.local("yyyy/MM/dd/HH/") ) )


### Logging

To log a single value at _info_ level use

    $( log.info( ${myVariable-Not Set} ) )

The Result object will have a type of `TEXT` and a value that either comes from the Symbols Table, or the literal
Text `Not Set`. Either way, the returned value will be written to the system log at INFO level. For example:

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Not Set

The text `Logged EEL Message:` can not be changed or removed as it is used to guard against log reputation attacks

To add some context to the logged message use

    $( log.info( "The variable is {}", ${myVariable-Not Set} ) )

which will log

    [INFO ] com.github.tymefly.eel.log - The variable is Logged EEL Message: Not Set

Logging functions can write multiple values, but only the last one is returned. For example

    $( log.info( "Evaluating {} + {} = {}", 1, 2, ( 1 + 2 ) ) )

will log 

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Evaluating 1 + 2 = 3

and will return a Result object with a type of `TEXT` and a value of '3'

If the requirement to only log variables if they are not set then use:

    ${myVariable-$( log.warn( "Not Set" ) ) }

This works because the Expression Expansion for the default value, which includes the logging, will only be executed if 
_myVariable_ is not in the Symbols Table.


### Failing Expressions

To fail an expression if a precondition is not met then use `fail` inside a condition. For example:

    $( isEmpty( ${myVariable-} ) ? fail() : ${myVariable} )

`isEmpty( ${myVariable-} )` will return `true` if the value associated with _myVariable_ is undefined or an empty string.
If this is the case then `fail()` will be executed which will terminate the expression with an `EelFailException`. 
If there is text then `${myVariable}` is read from the Symbols Table and returned to the client.

If the requirement is to fail an EEL expression with a custom message if a variable is not defined then use:

    ${myVariable-$( fail("Custom Message") )}

If the requirement is to fail an EEL expression with a custom message if multiple variables are not defined then use: 

    $( ! myVar1? || ! myVar2? ? fail() : ${myVar1} ~> ${myVar2} )


### Dates Operations

Dates can be manipulated using the numeric operators. This works because as EEL will automatically convert the Dates
into the number of seconds elapsed since 1970-01-01 00:00:00 in the UTC time zone. 

The equality operators can be used to check if a date is before, after or at the same time as another date. For example:  

    $( ${dateValue} >= date.utc("-1day") )

Will return a Result object that will have a type of `LOGIC` and a value that is `true` only if `dateValue` is 
less than a day old. 
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
