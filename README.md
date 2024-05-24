# EEL
EEL is a small, compiled, **E**xtensible **E**xpression **L**anguage which can be used by a JVM application to evaluate 
expressions at run time. EEL can be used to configure applications in a dynamic and flexible way.

---
## Overview

There is a common requirement when developing systems to make certain values configurable. This is traditionally
achieved by reading values as-is from the environment variables, JVM properties, databases, or properties files. 
These solutions work well if nothing more sophisticated than simple text substitution is required, the exact values
are known at deployment time and none of values are guaranteed to be related. However, sometimes something more
sophisticated is needed. 
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
* The language must be easy to use by somebody who is familiar with Java programming and/or shell scripting. 
* The Java Programming API should be simple and concise.
* The language must include all the usual text, maths and logic operations that languages typically support.
* The language must be able to manipulate date/time stamps.
* The language must support logging so clients can see what is being evaluated for security and/or debugging purposes.
* The language must be _Null Hostile_ to avoid NullPointerExceptions.  
* The language must be secure:
  * It must be impossible to write an expression that will not complete at either compile time or runtime.
  * It must not be possible to inject false information into the system logs. 
  * Except for logging, expressions are executed without external side effects.
* The language must be extensible - additional functions can be added without needing to update EEL.

**Note:** There is no requirement that EEL be Turing complete. 
While there is support for conditions, neither iteration nor recursion is supported 

---
## What's new
See [What's new](WhatsNew.md)

---
## Building EEL
EEL requires:
* Java 17+
* Maven 3.6+

The source code contains three modules:

* **lib** - the EEL compiler and runtime
* **integration** - helper classes that can be used to integrate EEL with JVM applications
* **evaluate** - CLI wrapper for _lib_ that executes EEL Expressions 

---
## 'lib' - The EEL Language and its Java API

### The Java API
The API for EEL is based around four components. These are:

#### EEL Context

The EEL Context manages the compile time settings and any state information. An EelContext can be used by many 
EEL Expressions.

The EEL Context can:
* Return information about the current version of EEL 
* Set the precision for maths operations
* Import **U**ser **D**efined **F**unctions (UDFs).
* Guard against rogue expressions causing Denial Of Service (DOS) attacks by:
  * setting the maximum length of the expression in characters
  * setting a timeout for evaluating the expression

In addition, the Context also manages EEL state that can be shared across invocations. These are
* the time the Context was created 
* the values returned by the `count()` function  

All the Context settings have defaults; if these are acceptable then the Default EelContext can be used.

EelContext objects are built using a fluent API; the entry point is `EelContext.factory()` 
 

#### EEL Expression

This is where the source expression is compiled. Like any other program, a compiled expression can be executed multiple
times. This could be useful if an expression makes use of functions that return different values each time 
the expression is evaluated, for example reading the current date/time, examining the file system, generating random 
numbers or reading a counter. 
Alternatively, the expression could be evaluated with a different SymbolsTable (see below) which could also cause 
EEL to return a different Result (see below). 

EEL Expressions are built using a fluent API; the entry point is `Eel.factory()`. In addition, there are
convenience methods in `Eel` that can be used to compile expressions with the default Eel Context.


#### Symbols Table

The SymbolsTable is lookup mechanism that maps a name in an expression to a value that is provided at runtime.
These values can come from any, all or none of the following sources:
- The environment variables
- The JVM properties
- One or more `java.util.Map` objects 
- One or more Java lambda functions 
- A single hardcoded string which is associated with **all** names. This is usually used to set a default value. 

If the client needs to read a key from multiple sources without risking a name clash the SymbolsTable can be 
created with named _"scopes"_. When each data source set a unique scope name is also provided. 
When the data is read the EEL expression prefixes the key with the scope name and a delimiter.

If there are no scopes defined and a key exists in multiple sources, then the first source added to the SymbolsTable
takes priority. Because of this, no further data sources can be defined after a hardcoded string has been added.

Once defined, a SymbolsTable can be used to evaluate multiple EEL Expressions. However, there is no requirement that
an EEL Expression has to use a SymbolsTable when it is evaluated.      

SymbolsTable objects are built using a fluent API; the entry point is `SymbolsTable.factory()`. In addition, there are 
convenience methods in:
- `SymbolsTable` - used to build a SymbolsTable that reads from a single data source
- `Eel` - used to evaluate a compiled expression with a one-off SymbolsTable that reads from a single data source

EEL always reads values from the SymbolsTable as Text, however in most cases it will correctly convert the value to the 
type required by the operator that uses it. If this is insufficient then explicit conversion functions can be used to
ensure the correct type (see [Data types](#data-types-))

#### Result

A Result object represents the output from an EEL Expression. Each Result contains:
- The type of the evaluated value (see [Data types](#data-types-))
- The evaluated value which the client can read via a type specific getter method. If the getter method doesn't
match the Result type, then the Result will try to convert it using the rules [described below](#types-conversions). 


#### Exceptions
The EEL compiler and runtime can throw the following Exceptions:

| Exception                 | Purpose                                             | Extends              |
|---------------------------|-----------------------------------------------------|----------------------|
| EelException              | Base class for all EEL exceptions                   | RuntimeException     |
| EelInternalException      | The EEL core failed. This should never be seen      | EelException         |
|                           |
| EelCompileException       | Base class for all compile time exceptions          | EelException         |
| EelSourceException        | The expression source could not be read             | EelCompileException  |
| EelSyntaxException        | The source expression has a syntax error            | EelCompileException  |
| EelSymbolsTableException  | The SymbolsTable is misconfigured                   | EelCompileException  |
|                           |
| EelRuntimeException       | Base class for all runtime exceptions               | EelException         |
| EelInterruptedException   | The evaluating thread was interrupted               | EelRuntimeException  | 
| EelTimeoutException       | The expression took too long to evaluate            | EelRuntimeException  | 
| EelUnknownSymbolException | The SymbolsTable did not contain a required value   | EelRuntimeException  |
| EelConvertException       | An EEL type conversion failed                       | EelRuntimeException  |
| EelFunctionException      | A function failed or could not be invoked           | EelRuntimeException  |
| EelFailException          | The expression executed the `fail()` function       | EelRuntimeException  | 

**Note:** All of these exceptions are unchecked.

#### Example Java code

**Basic Usage**

In its simplest form, EEL can be invoked as:

    String result = Eel.compile(  ...some expression...  )
      .evaluate()
      .asText();

In this example the default EelContext is used to compile the expression, it is then executed without reference to a 
SymbolsTable and the result is returned as String. 
If this is enough for the client application then nothing more complex is required.

**Inline EelContext**

    String result = Eel.factory()
      .withPrecision(12)
      .compile(  ...some expression...  )
      .evaluate()
      .asText();

In this example the fluent API provided by `Eel.factory()` creates a new EelContext as the Expression is compiled;
in this case to set the maths precision.

Inlining the Context makes for concise code but does not allow the EelContext to be reused by other expressions.

**Explicit EelContext**

    EelContext context = EelContext.factory()
      .withPrecision(12)
      .build();
    String result = Eel.compile(context, ...some expression...)
      .evaluate()
      .asText();

This example uses the fluent API provided by `EelContext.factory()` to create an EelContext, and then uses
it to compile an Expression. 
The EelContext object can be reused in as many calls to `Eel.compile()` as need.

**Inline Symbols Table**

    Map<String, String> symTable = ... 
    String result = Eel.compile(  ...some expression...  )
      .evaluate(symTable)
      .asText();
  
In this example the fluent API provided by `Eel.compile()` creates a new SymbolsTable as the Expression is evaluated;
in this case the data comes from a map of values, but `evaluate` is overloaded to read from all the other supported sources.

Inlining the SymbolsTable makes for concise code but does not allow the SymbolsTable to be reused.


**Explicit Symbols Table**

    Map<String, String> values = ...
    SymbolsTable symbols = SymbolsTable.factory(".")
      .withValues("val", values)
      .withProperties("prop")
      .build();
    String result = Eel.compile(  ...some expression...  )
      .evaluate(symbols)
      .asText();

This example uses the fluent API provided by `SymbolsTable.factory()` to create a SymbolsTable object which is then
used to evaluate an Expression. 
There are several advantages to creating SymbolsTable objects; the table can read multiple sources while also
providing a default value if an identifier is undefined, the scope delimiter can be set, and it can be reused in as
many calls to `Eel.evaluate()` as needed.
The disadvantage is the code is more verbose.


**Explicit EelContext and Symbols Table**

    EelContext context = EelContext.factory()
      .withPrecision(12)
      .build();
    SymbolsTable symbols = SymbolsTable.factory(".")
      .withProperties("props")
      .withEnvironment("env")
      .build();
    String result = Eel.compile(context, ...some expression...)
      .evaluate(symbols)
      .asText();   

This example shows how to use an explicit EelContext and an explicit SymbolsTable with scopes.
This is EEL at its most flexible but also its most verbose.

**Using the result**

The previous examples all treated the Result as Text, however, EEL can evaluate other types of data as well. 
For example:

    Result result = Eel.compile(  ...some expression...  )
      .evaluate();
    
    if (result.getType() == Type.NUMBER) {
      System.out.println("As a number " + result.asNumber());
    } else if (result.getType() == Type.LOGIC) {
      System.out.println("As logic " + result.asLogic());
    } else if (result.getType() == Type.DATE) {
      System.out.println("As a date " + result.asDate());
    }     

    System.out.println("As a string " + result.asText());

In this example the type of the data in the Result object is checked and then read using an appropriate getter
method. 

If the data does not match the type expected by the getter method the Result object will try to convert the
data to the required type. As all data can be represented as text it will always be possible read the result using
`asText()`. The rules for converting values are described in the [Data types](#data-types-) section of this document.


### EEL syntax and semantics

##### Source characters
EEL Expressions accept only printable characters, spaces, and tabs as valid input. 
Other characters, including carriage-return, new-line, null and the control characters are invalid. 

**Escape sequences** 

Like most languages, EEL reserves certain characters for its own use. So that Text literals can contain these characters
the following 'C' style escape sequences are supported: 

| Escape sequence | Character represented |
|-----------------|-----------------------|
| `\\`            | Backslash             |
| `\'`            | Single quotation mark |
| `\"`            | Double quotation mark |


#### Source code
In its simplest form an EEL Expression is just a sequence of characters. The data that is passed to the EEL compiler 
will be returned as-is in the Result. On the face of it this isn't very useful, however it does provide a good
migration path for systems that are already configured with these strings.

Where EEL becomes more useful is with the interpolation mechanisms it supports. These are substrings in 
the parsed expression. The interpolation mechanisms are:
1. **[Value interpolation](#value-interpolation)** which is written in the form `${...}` and is used to read values from the SymbolsTable
2. **[Function interpolation](#function-interpolation)** which is written in the form `$functionName(...)` and is used to call a [function](#Standard-functions)
3. **[Expression interpolation](#expression-interpolation)** which is written in the form `$(...)` and is used to perform calculations which may include function calls 

These interpolation mechanisms can be nested. This is most commonly seen when an Expression interpolation uses a 
Value interpolation to read a value from the SymbolsTable.


#### Identifiers
Identifiers are used in Value interpolations to look up data from the SymbolsTable. Function interpolation and Expression
interpolation use identifiers as function names. 

The first letter of an identifier must be either: 
* an uppercase ASCII character
* a lowercase ASCII character
* an underscore (`_`)

Each of the (optional) subsequent characters must be either:
* an uppercase ASCII character
* a lowercase ASCII character
* a digit
* an underscore (`_`)
* a dot (`.`)
* a square bracket (either `[` or `]`)

EEL Identifiers are case-sensitive.

It should be noted that valid EEL identifiers are usually not valid Java identifiers and vice versa.

#### Data types 

EEL natively supports 4 data types. They are named differently from their Java equivalents to distinguish between
data that is in the EEL and Java domains. The EEL data types are:
##### Text

  Text literals can be written in the form `"..."` or `'...'`; the opening quote can be either a single or a double
  quote, but the associated closing quote must match the opening quote. This allows a double-quoted text literal
  to contain single quote characters and vice versa. Additionally, escape sequences can be used to embed special
  characters, including what would have been the closing quote.   

  Interpolation is not supported in Text literals. This is not usually a problem as Text concatenation can be used instead.

  Text values are returned from [Result](#result) objects as Java Strings.


##### Logic

  The two logic literals are `true` and `false`.

  Logic values are returned from [Result](#result) objects as Java boolean primitives.


##### Number

  The numeric literals can be expressed in any of the following ways:
  - Decimal integers (e.g. `1234`)
  - Binary integers (e.g. `0b1010`). Binary numbers are prefixed with `0b`
  - Octal integers (e.g. `0c1234567`). Octal numbers are prefixed with `0c`
  - Hexadecimal integers (e.g. `0x89ab`). Hex numbers are prefixed with `0x`
  - Decimals with fractional parts (e.g. `123.456789`)
  - Scientific format (e.g. `2.99792e8`) where exponents may be negative  
 
  Letters in numeric literals are case-insensitive.

  The underscore character (`_`) may appear between digits in a numerical literal for grouping purposes. This is to make
  numbers more readable, but otherwise they have no effect. For example:
  - Decimal integers (e.g. `1_234`)
  - Binary integers (e.g. `0b10_10`)
  - Octal integers (e.g. `0c123_4567`)
  - Hexadecimal integers (e.g. `0x89_ab`)
  - Decimals with fractional parts (e.g. `123.456_789`)
  - Scientific format (e.g. `2.997_92e8`)
   
  Numbers are returned from [Result](#result) objects as Java `BigDecimal` objects.


##### Date

  EEL expressions support Date-Time values, to a precision of a second. There are no Date-Time literals, but they can be 
  generated by: 
  - Calling a function that returns a date, such as `date.utc()`, `date.local()`, `date.at()` or `date.start()`
  - [Converting](#types-conversions) another data type to a date

  Dates are returned from [Result](#result) objects as Java `ZonedDateTime` objects


##### Null

EEL is a Null Hostile language. As a consequence:
  - [Value interpolations](#value-interpolation) will throw an `EelUnknownSymbolException` if a value cannot be read from the SymbolsTable
  - If a [UDF](#user-defined-functions-udfs) returns null then an `EelFunctionException` is thrown 
  - It is guaranteed that [UDF](#user-defined-functions-udfs)'s will never be passed `null` values 
  - It is guaranteed that the [Result](#result) getters will never return `null`


#### Types conversions

Values in EEL Expressions are loosely typed; EEL will silently convert values as required. The conversion rules are:

* Text values are converted to Numbers if they are in the same form as any of the numeric literals. 

  Leading spaces, trailing spaces and case will be ignored.
* Text values `"true"` and `"1"` are converted to the Logic value `true`.
  Text values `"false"`, `"0"` and empty text are converted to the Logic value `false`

  Leading spaces, trailing spaces and case will be ignored.
* Text values can be converted to Dates by parsing them as ISO 8601 formatted values (`yyyy-MM-dd'T'HH:mm:ssX`). 
  * The precision of these strings is flexible; they can contain as little as a 4-digit year or be specified to the second,
    but each period but be fully defined. For example, `2000-01` can be converted to a Date, but `2001-1` cannot.  
  * If a time zone is not specified then UTC is assumed
  * The `T`, `-` and `:` separator characters can be replaced with a single space or omitted entirely.
  * Fractions of a second are not supported
  * Leading and trailing spaces will be ignored.
* Number values are converted to Text as their plain (non-scientific) decimal representation
* Positive numbers are converted to Logical `true`. Negative numbers and zero are converted to Logical `false`
* Number values are converted to Dates as the number of elapsed seconds since 1970-01-01 00:00:00 in the UTC zone.  
* Logic values `true` and `false` are converted to Text values `"true"` and `"false"` respectively
* Logic values `true` and `false` are converted to numeric values `1` and `0` respectively
* Logic values `true` and `false` are converted to date values `1970-01-01 00:00:01Z` and `1970-01-01 00:00:00Z` respectively
* Date values are converted to Text in the format `yyyy-MM-dd'T'HH:mm:ssX`
* Date values are converted to Numbers by taking the number of elapsed seconds since `1970-01-01 00:00:00` in the UTC zone.
* Date values `1970-01-01 00:00:00` and earlier are converted to logic value `false`. All other dates are converted to logic `true`

All other conversions are illegal and will cause EEL to throw an `EelConvertException`. 
For example the Text value `"true"` can be converted to a Logical value, but `"positive"` will throw an exception.

In general EEL will automatically convert values to the type required by the operator that uses it. If an expression
needs to ensure a specific type then the conversion functions `text`, `number`, `logic` and `date` are available. 
The syntax of these functions is exactly the same as any other function. For example `$logic( "1" )` will return `true`

The rules have been defined this way because in the real world they generally do the right thing, however they are not
always Symmetric. For example:
 - `date( number( date.local() ) )` will convert a date to a number and then convert back to a date. The original date 
was in the local time zone but the result will be in UTC. The number of seconds elapsed since 
1970-01-01 00:00:00 in the UTC zone is maintained.
 - `text( number( '0x1234' ) )` will convert text to a number to and back again, but the result will be in decimal rather 
than the original hex
 - `date( logic( date.local() ) )` will convert the current date to a logic value and back to a date again. 
The conversion to a logic value will lose precision, so the resulting date is always `1970:01:01 00:00:01` in UTC
 - `text( date( '2000-01-01' ) )` will convert text to a date and back to text again, but the result will contain time 
fields that were absent in the original text. The undefined fields default to the start of their respective periods.

There are also occasions when the rules are not transitive. For example:
- `text( number( true ) )` will return `1`, but `number( text( true ) )` will throw an `EelConvertException`. 



#### Value interpolation

Value interpolation is written in the form **_${key}_** which should be familiar to somebody who is familiar
with shell scripting. Unlike shell scripting, the braces in EEL are mandatory.

The purpose of value interpolation is used to read values from the SymbolsTable and apply some optional modifiers. 

For an unscoped SymbolsTable, the _key_ is the name used by the backing data source. If the SymbolsTable is scoped 
then the _key_ is prefixed by the scope name and the scope delimiter. For example, given:

    SymbolsTable symbols = SymbolsTable.factory(".")
        .withValues("m1", Map.ofEntries(Map.entry("a", "Map1 value a"), Map.entry("b", "Map1 value b")))
        .withValues("m2", Map.ofEntries(Map.entry("a", "Map2 value a"), Map.entry("b", "Map2 value b")))
        .build();

then:
* **${a}** - is not defined
* **${m1.a}** - is "_Map1 value a_"
* **${m2.b}** - is "_Map2 value b_"


Value interpolation support similar modifiers to bash:
- `${key}` - the text value associated with _key_ with no changes
- `${#key}` - the length of the text value associated with _key_. This is returned as text
- `${key^}` - the text value associated with _key_, but with the first character in upper case
- `${key^^}` - the text value associated with _key_, but with all the characters in upper case
- `${key,}` - the text value associated with _key_, but with the first character in lower case
- `${key,,}` - the text value associated with _key_, but with all the characters in lower case
- `${key~}` - the text value associated with _key_, but with the case of the first character toggled
- `${key~~}` - the text value associated with _key_, but with the case of the all the characters toggled
- `${key:offset:count}` - a substring of the text value associated with _key_ where `offset` and `count` are EEL expressions 
  that generate numeric values
- `${key-default}` - if there is no value in the SymbolsTable associated with the _key_ then the `default` is used

EEL allows these modifiers to be combined. For example:

- `${key,,^}` - the value associated with _key_ with the first character in upper case and subsequent 
characters in lowercase.  
- `${key^^-default}` - if there is a value associated with _key_ then use its value in uppercase.
  If there is no value associated with _key_ then use the literal `default`
- `${key:0:3,,}` - the first 3 characters of the value associated with _key_ in lower case
- `${key:2:3^}` - 3 characters from the middle of the value associated with _key_. The first character returned will be in upper case
- `${#key-default}` - if there is a value associated with _key_ then use its length.
  If there is no value associated with _key_ then use the literal `default`

The order the modifiers are specified is:

| Order | Modifier                    | Function                                                                  |
|-------|-----------------------------|---------------------------------------------------------------------------| 
| First | `#`                         | Take the length of text after applying all modifiers (except the default) |
|       | `:`                         | Substring                                                                 |
|       | `^` `^^` `,` `,,` `~` `~~`  | Case change                                                               |
| last  | `-`                         | Default values                                                            |

Because default values and indexes/lengths for substrings are EEL Expressions in their own right, the following 
Value Interpolations are valid:

- `${undefined-defaultText}` - if _undefined_ is not in the SymbolsTable then use the literal text `defaultText`
- `${undefined-}` - if _undefined_ is not in the SymbolsTable then use empty text 
- `${first-${second}}` - if _first_ is not in the SymbolsTable then use the value associated with _second_ instead 
- `${first-${second-defaultText}}` - if _first_ is not in the SymbolsTable then try _second_. 
  If _second_ is also not in the SymbolsTable then use the literal text `defaultText`
- `${undefined-$myFunction()}` - if _undefined_ is not in the SymbolsTable then call _myFunction_ 
- `${undefined-$( expression )}` - if _undefined_ is not in the SymbolsTable then evaluate _expression_
- `${STR:$(indexOf(${STR}, '~', fail()) + 1):1}` - return the character in _STR_ that immediately follows the first `~`.
  If there are no `~` characters then fail the expression.

**Undefined values**

There are several ways to handle keys do not have an associated value in the SymbolsTable. These are:
1. Use defaults with all Value interpolations 
2. Use the [isDefined operator](#operators) (`?`) to check the key is defined in the SymbolsTable before reading it 
3. Configure the SymbolsTable to return a hardcoded value if the key is undefined (e.g. empty text)
4. Have the client code handle the [EelUnknownSymbolException](#exceptions)
5. Have the interpolation throw an exception with a custom message. This is achieved by using
   [Function interpolation](#function-interpolation) to fail the expression. For example, `${undefined-$fail('custom error message')}` 


#### Function interpolation
Function interpolation is written in the form **_$functionName(...)_**. This is the equivalent of:

    $( functionName(...) )

that is, an Expression interpolation that only calls a function. The arguments passed to the function
can contain all the Value interpolations, literals, functions or expressions that an Expression interpolation could use.


#### Expression interpolation

Function interpolation is written in the form **_$(...)_**

The full power of EEL is in the Expression interpolation. This a simple programming language that is used to evaluate
a value.

##### Reserved words and symbols

| Values  | Numeric Ops | Logical Ops |     Bitwise Ops     | Relational Ops | Text Ops | Conversions | Quotes | Misc |
|:-------:|:-----------:|:-----------:|:-------------------:|:--------------:|:--------:|:-----------:|:------:|:----:|
| `true`  |     `+`     |    `and`    |         `&`         |      `=`       |   `~>`   |   `text`    |  `'`   | `$`  |
| `false` |     `-`     |    `or`     | <code>&#124;</code> | `<>` and `!=`  |          |  `number`   |  `"`   | `{`  |
|         |     `*`     |    `not`    |         `^`         |      `>`       |          |   `logic`   |        | `}`  |
|         |     `/`     |             |         `~`         |      `>=`      |          |   `date`    |        | `(`  |
|         |    `//`     |             |        `<<`         |      `<`       |          |             |        | `)`  | 
|         |    `-/`     |             |        `>>`         |      `<=`      |          |             |        | `,`  |
|         |     `%`     |             |                     |   `isBefore`   |          |             |        | `?`  | 
|         |    `**`     |             |                     |   `isAfter`    |          |             |        | `:`  |
|         |             |             |                     |                |          |             |        | `[`  |
|         |             |             |                     |                |          |             |        | `]`  |

###### Naming conventions
- Reserved words are all lowerCamelCase. For example `text` and `logic`, `and`, `or`, `true` and `isAfter`,  
- [Function names](#standard-functions) are also in lowerCamelCase. Dots (`.`) are sometimes included in function names to create
  prefixes. These are used to logically group functions and avoid namespace clashes much as packages do in Java. 
  Otherwise, they have no special meaning. 
  Example function names are `count`, `cos`, `isEmpty`, `system.home`, `date.local`, `log.warn` and `format.hex`
- SymbolsTable keys are typically in a format that is appropriate to the data source. For example, environment variables
  are usually in _UPPERCASE_WITH_UNDERSCORES_ while JVM properties are in _lower.case.with.dots_. 
- The [identifiers](#identifiers) section describes for format of an identifier

##### Constants
EEL defines the following constants:
- **true** 
- **false**

Constant names are case-sensitive.

##### Operators
EEL supports the following operators:

| Precedence | Operators Symbols                                      | Operators Name(s)                                                                                   |
|------------|--------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| Highest    | `()`, `?`                                              | Parentheses, isDefined                                                                              |
|            | `-` `not` `~` conversions, function-call               | negation, logical-not, bitwise-not, type-conversions, function-calls                                |
|            | `**` `~>`                                              | exponentiation, string-concatenation                                                                |
|            | `*` `/` `//` `-/` `%` `and` `&`                        | multiply, divide, divide-floor, divide-truncate, modulus, logical-and, bitwise-and                  |
|            | `+` `-` `^`                                            | plus, minus, bitwise-xor                                                                            |
|            | `or` <code>&#124;</code> `<<` `>>`                     | logical-or, bitwise-or, left-shift, right-shift                                                     | 
|            | `=` `!=` `<>` `>` `>=` `<` `<=`, `isBefore`, `isAfter` | equal, not-equal, greater-than, greater-than-equals, less-than, less-than-equals, isBefore, isAfter |   
| Lowest     | `? :`                                                  | conditional                                                                                         |


Most operators require their operands to be a particular type, so EEL will automatically convert their operands using the [type conversion rules](#types-conversions).
The expected types for the operators are: 

| Operator Group   | Operators Symbols                                       | Operand type(s)                                                |
|------------------|---------------------------------------------------------|----------------------------------------------------------------|
| Text             | `~>`                                                    | Text                                                           |
| Logical          | `not`, `and`, `or`                                      | Logic                                                          |
| Bitwise          | `~` `&` <code>&#124;</code> `^` `<<` `>>`               | Number (with any fractional part truncated)                    |
| Maths            | `+` `-` (minus and negation) `*` `/` `//` `-/` `%` `**` | Number (complete with any optional fractional part)            |
| Numeric relation | `>` `>=` `<` `<=`                                       | Number (complete with any optional fractional part)            |
| Date relation    | `isBefore`, `isAfter`                                   | Date                                                           |
| Symbols          | `?`                                                     | identifier                                                     |
| Conditional      | `?  :`                                                  | first operand is Logic, operands two and three can be any type |  
| Equality         | `=` `!=` `<>`                                           | see table below                                                | 

The equals and not-equal operators compare their operands based on the following rules:

| Left \ Right | Text                    | Number                    | Logic                          | Date                             |
|--------------|-------------------------|---------------------------|--------------------------------|----------------------------------| 
| **Text**     | Compare two Text values | Convert Number to Text    | Convert Logic to Text          | Convert Date to Text             |
| **Number**   | Convert Number to Text  | Compare two Number values | Convert Logic to Number        | Convert Date to Number           |
| **Logic**    | Convert Logic to Text   | Convert Logic to Number   | Compare two Logic values       | Convert both values to Numbers   |
| **Date**     | Convert Date to Text    | Convert Date to Number    | Convert both values to Numbers | Compare two instances in time[1] |

[1] Two dates will be considered equal if they represent the same instant of time, even if they are in different zones.

These rules are defined this way so that it is always possible convert the values. 
  
##### Operators Details

- Operator names (`not`, `and`, `or`, `isBefore` and `isAfter`) are case-sensitive.
- The `+` operator is purely a numeric operator; the `~>` operator is used for string concatenation.
  This removes the need to call a conversion function when adding values from the SymbolsTable that are expected to be numeric.
- The isDefined operator (`?`) is used to check if an identifier has an associated value in the SymbolsTable. 
  If the identifier is defined the operator returns `true`, otherwise it returns `false`. 
  The operator is written after the name of the SymbolsTable identifier (it has right associativity).

  For example, `myIdentifier?` will return _true_ only if `${myIdentifier}` returns a value rather than throwing an `EelUnknownSymbolException`.
- To match other languages, the not-equal operator exists in two forms; `!=` and `<>`. The difference is purely cosmetic - they both
  behave in the same way and have the same priority.
- The logic operators (`not`, `and`, and `or`) and the Bitwise operators (`~`, `&`, `|` and `^`) operate on different data types.
- The logic operators are always short-circuited. 
- The Bitwise operators will silently truncate any fractional parts of their operands. 

  For example, `$( 3 << ( 28 / 10) )` will return `12`. This is because `28 / 10` evaluates to `2.8`, but the shift
  operator will truncate the fractional part, so 3 is shifted left by two bits which gives `12`. 
  
- The shift operators (`<<` and `>>`) can shift numbers by a negative number of bits. This is the equivalent of shifting
  the number in the other direction by a positive number of bits. 
- The right shift operator (`>>`) will perform sign extension.

- There are 3 division operators:
  - divide (`/`) - the quotient can contain fractional parts
  - divide-floor (`//`) - the quotient is rounded towards negative infinity
  - divide-truncate (`-/`) - the quotient is rounded towards 0 discarding any fractional parts
  
  The difference between the (`//`) and (`-/`) becomes apparent with negative numbers. For example:  
   
  |          divide          |      divide-floor       |     divide-truncate     |
  |:------------------------:|:-----------------------:|:-----------------------:| 
  |  `$( 12.0 / 1 )` = 12.0  |  `$( 12.0 // 1 )` = 12  |  `$( 12.0 -/ 1 )` = 12  |
  |  `$( 12.3 / 1 )` = 12.3  |  `$( 12.3 // 1 )` = 12  |  `$( 12.3 -/ 1 )` = 12  |
  |  `$( 12.5 / 1 )` = 12.5  |  `$( 12.5 // 1 )` = 12  |  `$( 12.5 -/ 1 )` = 12  |
  |  `$( 12.7 / 1 )` = 12.7  |  `$( 12.7 // 1 )` = 12  |  `$( 12.7 -/ 1 )` = 12  | 
  |                          | 
  | `$( -12.0 / 1 )` = -12.0 | `$( -12.0 // 1 )` = -12 | `$( -12.0 -/ 1 )` = -12 |
  | `$( -12.3 / 1 )` = -12.3 | `$( -12.3 // 1 )` = -13 | `$( -12.3 -/ 1 )` = -12 |
  | `$( -12.5 / 1 )` = -12.5 | `$( -12.5 // 1 )` = -13 | `$( -12.5 -/ 1 )` = -12 |
  | `$( -12.7 / 1 )` = -12.7 | `$( -12.7 // 1 )` = -13 | `$( -12.7 -/ 1 )` = -12 | 

---

### Standard functions

EEL has a number of standard functions that are automatically made available to expressions.

Some of these functions can accept a variable number of arguments where the final argument can be passed zero, one or
more times. This is denoted in the list below by the `...` after the last argument. 
For example, `date.offset( date, offsets... )` requires a _date_ be passed, but will accept any number of _offsets_,
including no _offsets_.

Some functions support default arguments; if the expression does not explicitly pass an argument, then EEL will silently
add a default value. This is denoted in the list below by surrounding the optional argument(s) with braces.
For example, `random( { minValue { , maxValue } } )` can be called as `random()`, `random( minValue )` 
or `random( minValue, maxValue )`.


#### Function prefixes
The standard functions have prefixes that describe their purpose and prevent name space clashes.

| Prefix    | Group                               | Example        |
|-----------|-------------------------------------|----------------|
| < none >  | General utility functions           | count()        |
| eel       | Eel system functions                | eel.version()  |
| system    | Host system information functions   | system.home()  |
| format    | Data formatting functions           | format.local() | 
| log       | Logging functions                   | log.error()    |
| text      | Reserved for future Text functions  |                |
| logic     | Reserved for future Logic functions |                |
| number    | Number functions                    | number.pi()    |
| date      | Date functions                      | date.utc()     |


#### EEL system functions
- **eel.version()** - returns the EEL version number.
- **eel.buildDate()** - returns the date and time the EEL compiler was built


#### Text processing
##### Case conversions
- **lower( text )** - returns the _text_ as lower case
- **upper( text )** - returns the _text_ as upper case
- **title( text )** - returns the _text_ as title case


##### Querying text
- **len( text )** - returns the length of the _text_, including leading and trailing whitespace
- **isEmpty( text )** - returns `true` only if the _text_ is empty. This is a more concise version of `len( text ) = 0`
- **isBlank( text )** - returns `true` only if the _text_ is empty or whitespace. This is a more concise version of `isEmpty( trim(text) )`
- **matches( text, regEx )** - returns `true` only if the _text_ matches the regular expression _regEx_
- **indexOf( text, subString { , defaultFunc } )** - 
    returns the zero based index of the first occurrence of _subString_ in _text_, or the result of _defaultFunc_ if _subString_ is not present.
    If not specified, _defaultFunc_ returns _-1_ 
- **lastIndexOf( text, subString { , defaultFunc } )** - 
    returns the zero based of the last occurrence of _subString_ in _text_, or the result of _defaultFunc_ if _subString_ is not present.
    If not specified, _defaultFunc_ returns _-1_ 
- **contains( text, subtext )** - returns the number of times that the _subtext_ occurs in _text_


##### Splitting text on indexes
- **left( text, count )** - returns up to _count_ characters from the start of the _text_
- **mid( text, offset, count )** - returns up to _count_ characters from the _text_ starting from the zero based _offset_
- **right( text, count )** - returns up to _count_ characters from the end of the _text_


##### Splitting text on delimiters
- **before( text, delimiter, count )** - returns all the text before the _count_'th occurrence of the _delimiter_
- **between( between, delimiter, start, end )** - returns all the text between the _start_'th and the _end_'th occurrence of the _delimiter_
- **after( text, delimiter, count )** - returns all the text after the _count_'th occurrence of the _delimiter_- 


- **beforeFirst( text, delimiter )** - returns all the text before the first occurrence of the _delimiter_
- **afterFirst( text, delimiter )** - returns all the text after the first occurrence of the _delimiter_
- **beforeLast( text, delimiter )** - returns all the text before the last occurrence of the _delimiter_
- **afterLast( text, delimiter )** - returns all the text after the last occurrence of the _delimiter_

##### Extracting text
- **trim( text )** - returns the _text_ with all leading and trailing whitespaces removed
- **extract( text, regEx )** - returns the grouped characters from the _text_ based on a regular expression
- **replace( text, from, to )** - returns the _text_ with all instances of the literal text _from_ replaced by _to_
- **replaceEx( text, regEx, to )** - returns the _text_ with all matches of the regular expression _regEx_ replaced by _to_


##### Text to Unicode codepoints
- **char( codepoint )** - return a text value containing the single character given by the unicode _codepoint_
- **codepoint( text )** - returns the unicode codepoint of the first character in the _text_ 


#### Maths functions
##### Constants
- **number.pi()** - returns an approximate value for _pi_
- **number.e()** - returns an approximate value for _e_
- **number.c()** - returns the value for _c_, the speed of light in meters per second.  

##### Rounding and conversion functions
- **number.round( number )** - returns the _number_ rounded to the closest non-fractional value.
- **number.truncate( number )** - returns the _number_ with its fractional part discarded 
- **number.ceil( number )** - returns the nearest value that is greater than or equal to _number_ and is non-fractional
- **number.floor( number )** - returns the nearest value that is less than or equal to _number_ and is non-fractional


- **toDegrees( radians )** - returns the _radians_ value expressed in degrees
- **toRadians( degrees )** - returns the _degrees_ value expressed in radians


##### Trigonometric functions
- **sin( value )** - returns the sine of the radian _value_
- **cos( value )** - returns the cosine of the radian _value_
- **tan( value )** - returns the tangens of the radian _value_  


- **asin( value )** - returns the arc sine (inverted sine) of _value_
- **acos( value )** - returns the arc cosine (inverted cosine) of _value_
- **atan( value )** - returns the arc tangens (inverted tangens) of _value_ 

##### Statistics functions
- **max( value, values... )** - returns the largest value of all the numbers passed
- **min( value, values... )** - returns the smallest value of all the numbers passed
- **avg( value, values... )** - returns the average value of all the numbers passed

##### Other maths functions
- **abs( value )** - returns the absolute value of a number
- **exp( value )** - returns the natural exponent of value (e<sup>value</sup>)
- **factorial( value )** - returns the factorial of _value_
- **ln( value )** - returns the natural log of _value_
- **log( value )** - returns the log in base 10 of _value_
- **root( value { , n } )** - returns the n'th root of _value_. The default value of _n_ is 2, which gives square roots
- **sgn( value )** - returns the sign of a numeric _value_; -1 for negative, 0 for zero and 1 for positive
 

The precision of the numeric functions is set in the [EelContext](#eel-context).

#### Date functions
##### Reading dates 
- **date.start( { zone {, offsets... } } )** - returns the date-time when the EelContext was created plus any optional 
    offsets. _zone_ defaults to UTC
- **date.utc( offsets... )** - returns the current UTC date-time plus any optional offsets
- **date.local( offsets... )** - returns the current local date-time plus any optional offsets
- **date.at( zone, offsets... )** - returns the current date-time in the specified zone plus any optional offsets

##### Modifying  dates 
- **date.offset( date, offsets... )** - returns the _date_ after adding one or more offsets
- **date.set( date, specifier... )** - returns the _date_ after setting one or more periods
- **date.setZone( date, zone )** - returns the _date_ after setting the time zone
- **date.moveZone( date, zone )** - returns the _date_ after change the time zone and adjusting the time to maintain the same instant
- **date.truncate( date, period )** - returns the _date_ truncated to the start of the specified _period_. 

Most of the date functions support the ability to modify, set or offset a date with respect to a time period.
The supported time periods are: 

|   Period    |      Full Names      | Short Name | Notes        |
|:-----------:|:--------------------:|:----------:|:-------------|
|  **Years**  |   `year`, `years`    |    `y`     |              |
| **Months**  |  `month`, `months`   |    `M`     |              |
|  **Weeks**  |   `week`, `weeks`    |    `w`     | Offsets only |
|  **Days**   |    `day`, `days`     |    `d`     |              |
|  **Hours**  |   `hour`, `hours`    |    `h`     |              |
| **Minutes** | `minute`, `minutes`  |    `m`     |              |  
| **Seconds** | `second`, `seconds`  |    `s`     |              |

Period names are case-sensitive. Time zones can be either:
- Fixed offsets - a fully resolved offset from UTC such as `+5`
- Geographical regions - an area where a specific set of rules for finding the offset from UTC apply such as `Europe/Paris` 

For example:
- **date.start( "Europe/Paris" )** - the instant the EEL Context was created with respect to the Paris time zone
- **date.utc( "1day" )** - the current date-time in UTC plus 1 day
- **date.local( "+2months" )** - the current date-time in the local time zone plus 2 months
- **date.at( "-5", "-1w" )** - the current date-time in the _UTC -5_ time zone minus 1 week 
- **date.set( ${value}, "12h", "0m", "0s" )** - the date given in the _value_ with the time fields set to midday
- **date.offset( ${value}, "15minutes" )** - the time given in the _value_ plus 15 minutes 
- **date.truncate( date, "month" )** - reduce the accuracy of the _date_ to the start of the month

#### Logging functions
These functions are used to perform logging as a side effect. The value returned by the function is the last argument 
passed. 

In order of priority, from highest to lowest, the logging functions are:

- **log.error( { message, } arg, args... )** - log the optional message and the _args_ at error level
- **log.warn( { message, } arg, args... )** - log the optional message and the _args_ at warn level. 
- **log.info( { message, } arg, args... )** - log the optional message and the _args_ at info level. 
- **log.debug( { message, } arg, args... )** - log the optional message and the _args_ at debug level. 
- **log.trace( { message, } arg, args... )** - log the optional message and the _args_ at trace level. 


**Note:** The EEL expression must pass at least one argument to the logging function.

**Note:** The client's logging framework is responsible for enabling logging. EEL writes all its messages to the
`com.github.tymefly.eel.log` logger, so it is recommended that it is configured with **trace** level
logging enabled.

**Note:** Because expressions can be set after applications have been developed and deployed then there is a 
need to guard against log repudiation attacks. Consequently:
1. All logged messages are always prefixed by the literal text `Logged EEL Message: ` to make it obvious they are 
generated by EEL and do not come from any other source. This prefix cannot be changed or removed. 
2. All control characters, except for tabs, are filtered out of the logged message. This includes new-line and carriage
return characters to prevent EEL logging text that that might be mistaken for another logging message 

 
#### Data formatting functions
##### Formatting text
- **printf( format, arguments... )** - returns formatted text using the specified [format string](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Formatter.html) and arguments.
- **padLeft( text, width {, pad } )** - adds _pad_ characters to the start of the _text_ so that it is at least _width_ characters long.
   _pad_ defaults to a space
- **padRight( text, width {, pad } )** - adds _pad_ characters to the end of the _text_ so that it is at least _width_ characters long.
   _pad_ defaults to a space

##### Formatting numbers
- **format.binary( value )** - returns the _value_ as binary Text without a leading _"0b"_
- **format.octal( value )** - returns the _value_ as octal Text without a leading _"0c"_
- **format.hex( value )** - returns the _value_ as hexadecimal Text without a leading _"0x"_
- **format.number( value, radix )** - returns the number as Text in the given radix. The maximum _radix_ is 36 

##### Formatting dates
- **format.date( format, date, offsets... )** - returns the _date_, plus optional offsets, as Text in a custom format.
- **format.start( format { , zone { , offsets... } } )** - returns instant the EEL Context was created, plus optional offsets, 
      as Text in a custom format. _zone_ defaults to UTC.
- **format.utc( format, offsets... )** - returns the current UTC time, plus optional offsets, as Text in a custom format.
- **format.local( format, offsets... )** - returns the current local time, plus optional offsets, as Text in a custom format.
- **format.at( zone, format, offsets... )** - returns the current time in the specified zone, plus optional offsets, as Text in a custom format.

Date formats are described in the 
[Java DateTimeFormatter](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html)
documentation


#### File system functions
##### Filename and path manipulation
- **baseName( path { , extension } )** - returns the _path_ with the leading directory components and the optional _extension_ removed
- **dirName( path )** - returns _path_ with its last non-slashed component and trailing slash removed
- **extension( path, { , max } )** - returns up to _max_ of the right most file extensions from the _path_. The default is to return all extensions
- **realPath( path )** - returns _path_ in a canonicalised format based on current operating system

##### File information
- **exists( path )** - returns _true_ only if the file at the specified _path_ exists
- **fileSize( path { , defaultSizeFunc } )** - returns the size of the file in bytes. 
  If the file does not exist then _defaultSizeFunc_ is evaluated. -1 is returned if the  _defaultSizeFunc_ is not specified 
- **createAt( path {, defaultTimeFunc } )** - returns the local date on which file was created. 
  If the file does not exist then _defaultTimeFunc_ is evaluated. 1970-01-01 00:00:00Z is returned if the  _defaultTimeFunc_ is not specified 
- **accessedAt( path {, defaultTimeFunc } )** - returns the local date on which file was last accessed. 
  If the file does not exist then _defaultTimeFunc_ is evaluated. 1970-01-01 00:00:00Z is returned if the  _defaultTimeFunc_ is not specified 
- **modifiedAt( path {, defaultTimeFunc } )** - returns the local date on which file was last modified. 
  If the file does not exist then _defaultTimeFunc_ is evaluated. 1970-01-01 00:00:00Z is returned if the  _defaultTimeFunc_ is not specified 

##### Directory information
- **fileCount( dir {, glob } )** - returns the number of files in the _dir_ that match the _glob_ expression. 
  _glob_ defaults to `*` so that all files are counted   
- **firstCreated( dir {, glob, {, index {, defaultFunc }}} )** - returns the full path to the _index_'th (0 based) file that matches the 
  _glob_ expression in the _dir_ where the files are ordered from the first created to the last created. 
  _glob_ defaults to `*`. _index_ defaults to 0. _defaultFunc_ is evaluated if a file could not be found and defaults to throwing an IOException 
- **lastCreated( dir {, glob, {, index {, defaultFunc }}} )** - returns the full path to the _index_'th (0 based) file that matches the 
  _glob_ expression in the _dir_ where the files are ordered from the last created to the first created. 
  _glob_ defaults to `*`. _index_ defaults to 0. _defaultFunc_ is evaluated if a file could not be found and defaults to throwing an IOException 
- **firstAccessed( dir {, glob, {, index {, defaultFunc }}} )** - returns the full path to the _index_'th (0 based) file that matches the 
  _glob_ expression in the _dir_ where the files are ordered from the first accessed to the last accessed. 
  _glob_ defaults to `*`. _index_ defaults to 0. _defaultFunc_ is evaluated if a file could not be found and defaults to throwing an IOException  
- **lastAccessed( dir {, glob, {, index {, defaultFunc }}} )** - returns the full path to the _index_'th (0 based) file that matches the 
  _glob_ expression in the _dir_ where the files are ordered from the last accessed to the first accessed. 
  _glob_ defaults to `*`. _index_ defaults to 0. _defaultFunc_ is evaluated if a file could not be found and defaults to throwing an IOException 
- **firstModified( dir {, glob, {, index {, defaultFunc }}} )** - returns the full path to the _index_'th (0 based) file that matches the 
  _glob_ expression in the _dir_ where the files are ordered from the first modified to the last modified. 
  _glob_ defaults to `*`. _index_ defaults to 0. _defaultFunc_ is evaluated if a file could not be found and defaults to throwing an IOException  
- **lastModified( dir {, glob, {, index {, defaultFunc }}} )** - returns the full path to the _index_'th (0 based) file that matches the 
  _glob_ expression in the _dir_ where the files are ordered from the last modified to the first modified. 
  _glob_ defaults to `*`. _index_ defaults to 0. _defaultFunc_ is evaluated if a file could not be found and defaults to throwing an IOException 


#### Operating System information functions
- **system.fileSeparator()** - returns the Operating System path separator, usually either '\\' or '/'
- **system.home()** - returns the path to the users home directory
- **system.pwd()** - returns path to the applications current working directory.
- **system.temp()** - returns path to the system temporary directory.


#### Miscellaneous utility functions
- **count( { name } )** - returns the next value in the zero based named a counter. _name_ defaults to an anonymous counter. 
  To reset the counters recompile the expression with a new EelContext
- **random( { minValue { , maxValue } } )** - return a random non-fractional number in the range _minValue_ to _maxValue_ inclusive.
The default range is a number between 0 and 99 inclusive.
- **uuid()** - returns a new **U**niversally **U**nique **I**dentifier (UUID)
- **duration( from, to { , period } )** - returns the duration between two dates in the specified time period. The default period is seconds
- **fail( { message } )** - immediately terminates the expression by throwing an _EelFailException_

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

To read this file, the client would execute the following code:

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

will add all the environment variables to the SymbolsTable and use one of them in the expression. 

To add [UDF](#user-defined-functions-udfs) classes and packages ensure the implementing classes are on the classpath
and execute **evaluate** using the `--udf-class` and `--udf-package` options.

---
## Querying the EEL version
As of version 2.0, it is possible to query the version of EEL that's running.  

- **EEL expressions** - The function `eel.version()` will return the version as Text. 
- **Evaluate** - launch the evaluate JAR with the command line argument `--version`
- **Java client API** - call `Eel.metadata().version()` 
- **UDF implementers** - ensure that one of the parameters passed to the function is of type `EelContext` and 
 then call `context.metadata().version()`

The version is always a pair of numbers separated by a decimal point (`.`), which means that the version text can
be converted to a number.

---
## User Defined Functions (UDFs)
EEL can be extended by adding **U**ser **D**efined **F**unctions (UDFs). The Java code for a UDF could be something
as simple as:

    public class Half {
        @com.github.tymefly.eel.udf.EelFunction(name = "divide.by2")
        public int half(int value) {
            return value / 2;
        }
    }

After it's been registered, an EEL expression can call this function as:

    $divide.by2( 1234 )

#### UDF requirements
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
  * `EelValue`
* `void` is not a supported return type
* Each of the arguments passed to the implementing method must be one of:
  * any of the types that can be returned
  * an `EelLambda`
  * VarArgs for one of the previous types 
  * the `EelContext`
  * a `FunctionalResource`
* The implementing method must not return `null`
* The name given in the `EelFunction` annotation must be a valid [function name](#function-names)
* The implementing class must not maintain state. (See [Stateful functions](#stateful-functions) for an alternative) 
* The UDFs must be secure.
  * The implementing function must return quickly to prevent potential DOS attacks - 
    The [EelContext](#eel-context) sets the maximum time for evaluating the complete expression, which includes the time taken executing the UDF.
  * Functions must return without side effects.  

### UDF implementation notes
- The implementing class can define more than one UDF. 
- EEL functions can not be overloaded. However, functions can use default arguments or variable length argument lists 
- Values are converted to characters by converting them to Text and taking the first character -
  attempting to convert empty text will cause EEL to throw an [EelConvertException](#exceptions)
- Because the language is null hostile it is guaranteed that none of the arguments passed to a UDF are `null`
- Some functions have a default value that is returned if no value can be determined. 
  It is recommended that this argument is of type `EelLambda` so that the EEL expression can pass the `fail()`
  function if it's not acceptable to default the function
- Functions that can legitimately generate a 'no-value' should return a value that can be converted to `false`.
  * For Text values this is the empty string
  * For Number values this is zero or a negative value, typically `-1`
  * For Date values this is at or before `1970-01-01 00:00:00Z`. For convenience the constant`EelContext.FALSE_DATE` can be used.

### Function names
All the prefixes used by the [Standard Functions](#function-prefixes) are reserved. EEL will throw a [EelFunctionException](#exceptions)
if a UDF uses one of the reserved prefixes. 

Because UDFs must have at least one dot (`.`) delimited prefix UDF names are never valid Java identifiers.


### Default arguments
EEL allows functions to be called with default arguments. 
The default value is set by annotating the Java argument with `com.github.tymefly.eel.udf.DefaultArgument`.
For example:

    @com.github.tymefly.eel.udf.EelFunction(name = "my.random")
    public int random(@com.github.tymefly.eel.udf.DefaultArgument("0") int min, 
                      @com.github.tymefly.eel.udf.DefaultArgument("99") int max) {
       // implement me
    }

Default arguments can only be defined after all the non-default arguments have been defined
 
### Exception handling
If a UDF fails in an unexpected way then it can throw either a checked or an unchecked Exception. 

EEL will catch any exceptions thrown by the UDF and wrap them into an [EelFunctionException](#exceptions) and add some
additional context information. This exception will then be thrown back to the client application.


### Stateful functions
Because EEL may reuse instances of the implementing UDF classes they **must** always be stateless. 

In the very rare occasions that a function has to be stateful then the solution is to get EEL to manage the state
for the function.
This is achieved by declaring the UDF function with an additional parameter of type `FunctionalResource`. This is an 
EEL aware object that is used to manage shared data objects. For example:

    @com.github.tymefly.eel.udf.EelFunction(name = "my.stateful")
    public String stateful(FunctionalResource functionalResource) {
        DTO myDto = functionalResource.getResource("myName", DTO::new);

       // use DTO
    }

The parameters passed to `getResource` are:
1. **_name_**: The name of the resource. UDFs can have multiple managed resource so long as they have unique names.
2. **_supplier-function_**: A function that is used to return a new instance of a stateful object, typically a constructor. 
Eel will pass the name of the resource to this function as a String

The first time `getResource` is called for the named resource the supplier-function will be called to create the 
resource. For subsequent invocations the existing resource will be returned.

**Important Note:** As the returned object is a shared resource, the UDF is responsible for synchronizing access to it
in multi-threaded environments.  

The FunctionalResource is associated with both the [EelContext](#eel-context) and the implementing class. Consequently:
 * If the EEL expression is recompiled with a new Context then new resources will be allocated. 
 * If another EEL expression is compiled with the same Context then the resources will be shared.
 * If the implementing class supports more than one UDF then the resources can be shared between the UDFs
 * UDFs that are in different implementing classes can not interfere with each other's resources, even if they 
request resources with the same name
  

### The client Java API 
There are two ways to make EEL aware of a UDF; both are via the [EelContext](#eel-context).

#### Registering UDF classes

The simplest way to add UDFs is to register them on a class-by-class basis. For example:

    EelContext context = EelContext.factory()
        .withUdfClass(MyClass1.class)
        .withUdfClass(MyClass2.class)
        .build();

#### Registering packages of UDFs  

Because it can get repetitive registering every class with the Context, EEL provides a way to register a
package of UDFs classes. First annotate each UDF class with `com.github.tymefly.eel.udf.PackagedEelFunction`.
For example:

    @com.github.tymefly.eel.udf.PackagedEelFunction
    public class MyClass1 { {

Then create the Context using:

    EelContext context = EelContext.factory()
        .withUdfPackage(MyClass1.class.getPackage())    // Any of the classes in the package could have been used
        .build();

**Note:** Child packages are not automatically added. If a child package is also required then it must be added
with an additional call to `withUdfPackage()`   

---

## Sample EEL Expressions
### Text pass through

    this is an expression

As there are no interpolated sequences the expression will be passed through as-is. Consequently, the evaluated 
[Result](#result) object will have a type of `text` and value of `this is an expression`.  

### Reading environment variables

    Your HOME directory is ${HOME}

Assuming the EEL is running on a *nix operating system and that the [SymbolsTable](#symbols-table) contains the environment 
variables then the [Result](#result) object will have a type of `text` and a value of that starts with `Your HOME directory is ` 
followed by the user's home directory.

On Windows systems this will throw a [EelUnknownSymbolException](#exceptions) because Windows uses the `HOMEDRIVE` and
`HOMEPATH` environment variables instead. The correct expression under Windows would be:

     Your HOME directory is ${HOMEDRIVE}${HOMEPATH}

An alternative expression that works on both operating systems is: 

     Your HOME directory is ${HOME-${HOMEDRIVE}${HOMEPATH}}

However, because reading locating the users home directory is common requirement, a simpler and cleaner way to get this
information is via the [standard EEL function](#operating-system-information-functions):

     Your HOME directory is $system.home()

### Forcing the result type

Using Function interpolation to call a conversion function can be used to guarantee the [Result](#result) object has the
required type. For example: 

    $number( ${#myValue--1} )

In this expression EEL will try to return the number of characters associated with `myValue`. If it is not defined then
-1 will be returned. The return type will always be a Number


### Paths with a common root

Given the following two expressions:

    ${root}/config
    ${root}/template

If the value for `${root}` is in a common [SymbolsTable](#symbols-table) then these expressions will return paths that
share a common root. `${root}` might even be determined by a previously evaluated an EEL expression.


### Calling functions

[Function interpolation](#function-interpolation) can be used to call a function. For example: 

    Last week was $date.local( "-7d" )

The [Result](#result) object will have a type of `text` and a value of that starts with `Last week was ` followed by last
week's date and time. 

Function calls can be nested. For example, the following expression will truncate last week's local date to the start of
the day:

    Last week was $date.truncate( date.local( "-7d" ), "d" )

It is worth noting that because the nested function is already part of the function interpolation, it is not prefixed with
a `$`. 

If the requirement is to display the date without any time fields then use:

    Last week was $format.local( "yyyy-MM-dd", "-7d" )


### Counters

    $count()

will return a [Result](#result) object that will have a type of `number` and value from an anonymous zero based counter. 
If the expression is reevaluated _with the same Context_ then the next value from the counter is returned.

Named counters can be used if the expressions that use the [Context](#eel-context) require multiple, independent, counters. 
For example:   

    First: $count( "first" ), Second: $count( "second" )

will return a [Result](#result) object that will have a type of `text` and the values from both counters 
If the expression is reevaluated _with the same Context_ then the next value from each counter is returned.


### Creating a sequence of file name

If the EEL repeatedly evaluates

    $system.temp()/${myFilePrefix-}$count().txt

**_with the same [Context](#eel-context)_** then each returned [Result](#result) object will have a type of `text`
and a value that forms a sequence of files in the system temp directory with a common optional prefix.

EEL will not create the files, but the client application could.

To reset the sequence, recompile the expression with a new Context.


### Date-time based directories

    ${root-}/$format.local("yyyy/MM/dd/HH/")

The [Result](#result) object will have a type of `text` and a value that would make a valid *nix directory name. Every
hour the expression will evaluate to a different directory. The optional `root` value allows the client to move the
directory structure to a new location.

* If it is later determined that the system only needs one directory each day then change `yyyy/MM/dd/HH/` to `yyyy/MM/dd/` 
* If it is later determined that the system should have a flat directory structure then change `yyyy/MM/dd/HH/` to `yyyy-MM-dd-HH/`


### Converting paths

To convert the path separator characters in some text to *nix format use

    $replace( ${root-}, "\\", "/")

The [Result](#result) object will have a type of `text` and a value based on the optional _root_ value from the 
[SymbolsTable](#symbols-table), but with forwards slashes instead of Windows style backslashes.

To force the path to the format used by the underlying operating system then use: 

    $realPath( ${root-} )

This conversion could be used in the _[date-time based Directory](#date-time-based-directories)_ example above to 
ensure that the returned path is formatted correctly.

    $realPath( ${root-} ~> "/" ~> format.local("yyyy/MM/dd/HH/") )


### Directories listings 

Assuming that `${myPath}` is in the [SymbolsTable](#symbols-table) and references a valid directory then

    $firstModified( ${myPath} )

will return a [Result](#result) object that has a type of `text` and a value that is the name of the most recently modified
file in that directory.

There is an optional second argument that is a globing expression that can be used to filter the files. 
This defaults to `*` so it considers all files, but this be overridden to search for specific files. For example:

    $firstModified( ${myPath}, "*.txt" )

will return the name of the most recently modified text file in the directory.

There is an optional third argument is a zero based index of the file to find. This defaults to 0, but it can be set to
some other value to return the names of files that were modified later. For example:

    $firstModified( ${myPath}, "*.txt", 1 )

will return the name of the second most recently modified text file in the directory. Given a new [Context](#eel-context) then
repeatedly evaluating 

    $( firstModified( ${myPath}, "*.txt", count() ) )

will return all the files in the directory until there are no more files. By default, the function will throw an 
exception if it can not find a file to return, however this can be fixed with the final optional argument. 
If we call:

    $( firstModified( ${myPath}, "*", count(), "" ) )

Then EEL will return an empty string to indicate when there are no more files to return. 

Finally, it is worth noting that there are other functions which operate in exactly the same way but return files in
different orders. These are `firstCreated`, `lastCreated`, `firstAccessed`, `lastAccessed`, `firstModified` and
`lastModified`.


### Logging

To log a single value at _info_ level use

    $log.info( ${myValue-Not Set} )

The [Result](#result) object will have a type of `text` and a value that either comes from the SymbolsTable, or the literal
Text _'Not Set'_. The returned value will be written to the system log at INFO level. For example:

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Not Set

The text _'Logged EEL Message:'_ cannot be changed or removed as it is used to guard against log reputation attacks

To add some context to the logged message use:

    $log.info( "The value is {}", ${myValue-not set} )

which might log

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: The value is not set

Logging functions can write multiple values, but only the last one is returned by the expression. For example

    $log.info( "Evaluating {} + {} = {}", 1, 2, ( 1 + 2 ) )

will log 

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Evaluating 1 + 2 = 3

and will return a [Result](#result) object with a type of number and a value of 3

If the requirement to only log values if they are not set then use:

    ${myValue-$log.warn( "myValue is not set" )}

This works because the Function interpolation for the default value, which includes the logging, will only be executed if 
_myValue_ is not in the [SymbolsTable](#symbols-table).

Finally, it is worth noting that the final value passed to a logging function doesn't have to be logged. For example:

    $log.info( "{} {}", "Hello", "World", 99 )

will log 

    [INFO ] [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Hello World

and will return a Result object with a type of number and a value of 99


### Failing expressions

To fail an expression if a precondition is not met then use `fail` inside a condition. For example:

    $( isEmpty( ${myValue-} ) ? fail() : ${myValue} )

`isEmpty( ${myValue-} )` will return `true` if the value associated with _myValue_ is undefined or an empty string.
If this is the case then `fail()` will be executed which will terminate the expression with an [EelFailException](#exceptions). 
If there is text then `${myValue}` is read from the [SymbolsTable](#symbols-table) and returned to the client as Text

If the requirement is to fail an EEL expression with a custom message if a value is not defined then use:

    ${myValue-$fail("Custom Message")}

If the requirement is to fail an EEL expression if multiple values are not defined then use: 

    $( not myValue1? or not myValue2? ? fail() : ${myValue1} ~> ${myValue2} )

It is also possible to fail an expression if an old version of EEL is being used

    $( eel.version() >= 99.9 ? 0 : fail("Invalid EEL Version") )


### Functions that return default values

Some functions return default values if they are unable to perform their normal operation. Often the default value
is determined by a function that is only evaluated when the default value is required. See [Standard functions](#Standard-functions)

For example `indexOf( text, subString { , defaultFunc } )` will return -1 if the search string is not present in the text. 
So

    $indexOf( 'abcdef', 'z' ) 

will return -1 because _z_ is not in  `abcdef`. Reading the [Result](#result) object as a logic value will return _false_ 
to indicate that the search text was not found.

If the client requires a different value if the file can not be found then this can be set by providing the default as
a function argument. For example:

    $indexOf( 'abcdef', 'z', 0 ) 

will return 0 instead. If there is no acceptable default value then it is also possible to pass the `fail()` function
as an argument. For example:

    $indexOf( 'abcdef', 'z', fail() )

This will cause an [EelFailException](#exceptions) to be thrown. 
By default, the message generated by `fail()` doesn't provide much context, so a more helpful example could be: 

    $indexOf( 'abcdef', 'z', fail('There is no z') )

A less dramatic version of the expression could use the logging functions to warn the client of an issue, but return 
a default value. For example:

    $indexOf('abcdef', 'z', log.warn('There is no z, returning {}', 0) )

Finally, it's worth noting that:

    $indexOf( 'abcdef', 'd', fail() )

will return _3_, which is the index of 'd'. `fail()` is not evaluated and no exception is thrown. 


### Dates operations

Dates can be manipulated using the numeric operators. This works because as EEL will automatically convert the Dates
into the number of seconds elapsed since 1970-01-01 00:00:00 in the UTC time zone. 

For example:

    $( date.utc() + 5 )

will add 5 seconds to the current UTC date. In the same way, the minus operator can be used to calculate time differences.
For example:

    $( date.utc() - date.start() )

Will return a [Result](#result) object that will have a type of number and a value that is the age, in seconds, of the
[Context](#eel-context).

If seconds is too fine-grained then a time difference can be returned in minutes by using:

    $duration( date.start(), date.utc(), "minutes" )

Building on this, an expression to check if a file is out of date could look something like:

    $( duration( modifiedAt( ${myFile} ), date.local(), "months" ) > 6 )

In this case the Result object will have a type of logic and a value that is `true` only if the referenced file is
more than 6 months old.

---
## External Libraries

EEL was implemented using as few third-party libraries as possible to prevent the client code from bloating. 
The dependant libraries are:

* **org.reflections:reflections** - used to scan for packages of UDF functions
* **ch.obermuhlner:big-math** - implementation for the maths operations and functions
* **org.slf4j:slf4j-api** - the logging facade
* **com.google.code.findbugs:jsr305** - used internally for tracking null/non-null object references
* **com.github.spotbugs:spotbugs-annotations** - used internally for bug checking annotations

Evaluate has one additional dependency:
* **args4j:args4j** - Command line argument parsing
