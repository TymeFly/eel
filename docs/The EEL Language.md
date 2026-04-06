# EEL Language
Probably the easiest way to imagine an EEL expression is as a text literal that can contain:
1. [Escaped characters](#escaped-characters) which are prefixed with a backslash character (`\`) 
2. [Interpolation sequence](#interpolation-sequences) which are prefixed with a dollar character (`$`)

If the source expression contains doesn't contain any of these sequences then the data consumed by the EEL compiler 
will be returned as-is in the Result. On the face of it this isn't very useful; however, it does provide a good
migration path for systems that use simple strings.


### Source characters
EEL Expressions can contain printable characters, spaces, tabs, line-feed and form-feed characters. 
Other characters, including null and the control characters are invalid. 


### Escaped characters
EEL supports the following 'C' style character escape sequences  

| Escape sequence | Character represented                                           |
|-----------------|-----------------------------------------------------------------|
| `\uxxxx`        | Unicode character, where `xxxx` is a character codepoint in hex |
| `\f`            | Form Feed                                                       |
| `\t`            | Tab                                                             |
| `\n`            | New Line                                                        |
| `\r`            | Carriage Return                                                 |
| `\b`            | Backspace                                                       |
| `\\`            | Backslash                                                       |
| `\$`            | Dollar character                                                |  
| `\'`            | Single quotation mark                                           |
| `\"`            | Double quotation mark                                           |

## Interpolation Sequences
EEL Supports the following types of interpolation sequences:
1. **[Value interpolation](#value-interpolation)** which is written in the form `${...}` and is used to read values from the SymbolsTable
2. **[Function interpolation](#function-interpolation)** which is written in the form `$functionName(...)` and is used to call a [function](#eel-functions)
3. **[Expression interpolation](#expression-interpolation)** which is written in the form `$(...)` and is used to perform operations which 
 may include [operators](#operators) [function](function-reference/index.html) calls, [compound expressions](#compound-expressions)

These interpolation sequences can be nested. This is most commonly seen when an Expression interpolation uses a 
Value interpolation to read a value from the SymbolsTable.

`\$` is used to include `$` in an expression without invoking an interpolation sequences. For example

    \$CA 1.23

will evaluate to the Text value `$CA 1.23`

## Identifiers
Identifiers are used in Value interpolations to look up data from the SymbolsTable. Function interpolation and 
Expression interpolation use identifiers as function names. 

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
* an opening square bracket (`[`) or a closing square bracket (`]`)

EEL Identifiers are case-sensitive.

It should be noted that valid EEL identifiers are usually not valid Java identifiers and vice versa.

## Data types

EEL supports four data types. Although similar, they are named differently from their Java equivalents to distinguish 
between data that is in the EEL and Java domains. The EEL data types are:

### Text

Text literals are written in the form `"..."` or `'...'`; the opening quote can be either a single or a double
quote, but the associated closing quote must match the opening quote. This allows a double-quoted text literal to 
contain single quote characters and vice versa.  

Text literals behave exactly the same way as full EEL expressions; they can contain 
[Value Interpolations](#value-interpolation), [Function Interpolations](#function-interpolation) and 
[Expression Interpolations](#expression-interpolation). They can also contain [Escape Sequences](#source-characters) to
embed special characters, including what would have been the closing quote. If a Text literal is nested inside an
expression interpolation then [Look Backs](#compound-expressions) are also supported.

Text values are returned from [Result](Using%20EEL.md#result) objects as Java Strings.

### Logic

The two logic literals are `true` and `false`.

Logic values are returned from [Result](Using%20EEL.md#result) objects as Java boolean primitives.


### Number

The numeric literals can be expressed in any of the following ways:
  - Decimal integers (e.g. `1234`)
  - Binary integers (e.g. `0b1010`). Binary numbers are prefixed with `0b`
  - Octal integers (e.g. `0c1234567`). Octal numbers are prefixed with `0c`
  - Hexadecimal integers (e.g. `0x89ab`). Hex numbers are prefixed with `0x`
  - Decimals with fractional parts (e.g. `123.456789`)
  - Scientific format (e.g. `2.99792e8`) where the exponent maybe signed
 
Letters in numeric literals are case-insensitive.

The underscore character (`_`) may appear between digits in a numerical literal for grouping purposes. 
This is to make numeric literals more readable, but otherwise they have no effect. For example:
  - Decimal integers (e.g. `1_234`)
  - Binary integers (e.g. `0b10_1010`)
  - Octal integers (e.g. `0c1_234_567`)
  - Hexadecimal integers (e.g. `0x89_ab`)
  - Decimals with fractional parts (e.g. `123.456_789`)
  - Scientific format (e.g. `2.997_92e8`)
   
Numbers can be returned from [Result](Using%20EEL.md#result) objects as `BigDecimal`, `BigInteger`, `double`, `long` or `int` values

#### Integral values

EEL does not have an integral data type; all numeric values are Numbers, regardless of whether they 
contain a fractional part. When an integral value is required, EEL automatically and silently truncates any
fractional portion of the number. This truncation can occur in the following contexts:

- [Bitwise operators](#operators)
- Substring operations in [Value interpolation](#value-interpolation) sequences
- Indexes used in Lookback for [compound expressions](#compound-expressions)
- Certain [standard functions](function-reference/index.html), such as `left(text, length)`, `right(text, length)`, and `mid(text, position, length)`

For instance, the expression `(28 / 10) << 3` will evaluate to `16`. This occurs because `28 / 10` is `2.8`,
which then is truncated to `2` before left-shifting 3-bits.



### Date

EEL expressions support Date-Time values, to the precision of a nanosecond. There are no Date-Time literals,
but they can be generated by: 
  - Calling a function that returns a date, such as `date.utc()`, `date.local()`, `date.at()`, `date.start()` or
    `date.parse()`
  - [Converting](#types-conversions) another data type to a date

Dates are returned from [Result](Using%20EEL.md#result) objects as Java `ZonedDateTime` objects


### Null

EEL is a Null Hostile language. As a consequence:
- [Value interpolations](#value-interpolation) will throw an `EelUnknownSymbolException` if a value cannot be read from the SymbolsTable
- If a [UDF](User%20Defined%20Functions.md) returns null then an `EelFunctionException` is thrown 
- It is guaranteed that [UDF](User%20Defined%20Functions.md)'s will never be passed `null` values 
- It is guaranteed that the [Result](Using%20EEL.md#result) getters will never return `null`


## Types conversions

Values in EEL Expressions are loosely typed; EEL will silently convert values as required. The conversion rules are:

* Text values are converted to Numbers as if they were numeric literals. Leading and trailing spaces are ignored.
* Text values `"true"` and `"1"` are converted to the Logic value `true`.
  Text values `"false"`, `"0"` and empty text are converted to the Logic value `false`

  Leading spaces, trailing spaces and case are ignored.
* Text values are converted to Dates by parsing them as ISO 8601 formatted values (`[-]yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]X`). 
  * The precision of the text is flexible; it can contain as little as a 4-digit year or be specified to the nanosecond. 
  * The optional leading `-` sign is used to indicate the BCE (aka BC) era. 
  * The `T`, `-` and `:` separator characters can be replaced with a single space or omitted entirely.
  * To prevent ambiguity when separator characters are omitted, years are always 4-digit numbers. Months, days,  
    hours, minutes, and seconds are always 2-digit numbers. Fractions of a second can be anywhere from 1 to 9 characters.
    Fields must be padded with leading `0`'s to make up the required width.
  
    For example, `20000909` can be converted to a Date, but `200099` can not be converted to a Date.  
  * If a time zone is not given then UTC is assumed
  * Leading and trailing spaces are ignored.
* Number values are converted to Text in their plain (non-scientific) decimal format
* Positive numbers are converted to Logical `true`. Negative numbers and zero are converted to Logical `false`
* Number values are converted to Dates as the number of elapsed seconds since 1970-01-01 00:00:00 in the UTC zone.  
* Logic values `true` and `false` are converted to Text values `"true"` and `"false"` respectively
* Logic values `true` and `false` are converted to numeric values `1` and `0` respectively
* Logic values `true` and `false` are converted to date values `1970-01-01 00:00:01Z` and `1970-01-01 00:00:00Z` 
  respectively
* Date values are converted to Text in the format `[-]yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]X` where a negative year represents 
  a date in the BCE (aka BC) era.
* Date values are converted to Numbers by taking the number of elapsed seconds since `1970-01-01 00:00:00` in the 
  UTC zone.
* Date values `1970-01-01 00:00:00` and earlier are converted to logic value `false`. All other dates are converted to 
  logic `true`

All other conversions are illegal and will cause EEL to throw an `EelConvertException`. 
For example, the Text value `"true"` can be converted to a Logical value, but `"positive"` will throw an exception.

In general EEL will automatically convert values to the type required by the operator that uses it. If an expression
needs to ensure a specific type then the conversion functions `text`, `number`, `logic` and `date` are available. 
The syntax of these functions is exactly the same as any other function call. For example `$logic( "1" )` will return `true`

The rules have been defined this way because in the real world they generally do the right thing, however, they are not
always Symmetric. For example:

 - `date( number( date.local() ) )` will convert a date to a number and then convert back to a date. The original date 
was in the local time zone but the result will be in UTC, however the number of seconds elapsed since 
1970-01-01 00:00:00 in the UTC zone is maintained.
 - `text( number( '0x1234' ) )` will convert text to a number to and back again, but the result will be in decimal 
rather than the original hex
 - `date( logic( date.local() ) )` will convert the current date to a logic value and back to a date again. 
As the conversion to a logic value will lose precision, the resulting date is always `1970:01:01 00:00:01` in UTC
 - `text( date( '2000-01-01' ) )` will convert text to a date and back to text again, but the result will contain time 
fields that were absent in the original text. The undefined fields default to the start of their respective periods.

There are also occasions when the rules are not transitive. For example:
- `text( number( true ) )` will return `1`, but `number( text( true ) )` will throw an `EelConvertException`. 


## Value interpolation

Value interpolation is written in the form **_${key}_** which should be familiar to anybody who is familiar
with shell scripting. Unlike shell scripting, the braces in EEL are mandatory.

The purpose of value interpolation is used to read values from the SymbolsTable and apply some optional modifiers. 

For an unscoped SymbolsTable, the _key_ is the name looked up by the backing data source. If the SymbolsTable is scoped 
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
- `${key-default}` - if there is no value in the SymbolsTable associated with the _key_ then the `default` is used
- `${key:-default}` - if there is no value in the SymbolsTable associated with the _key_ or the value is an empty sting 
  then the `default` is used
- `${key:offset}` - a substring of the text value associated with _key_ where `offset` is a numeric EEL expressions.
- `${key:offset:count}` - a substring of the text value associated with _key_ where `offset` and `count` are numeric EEL  
  expressions. 
  If _offset_ is negative then the offset is taken from the end of the text. A space before the `-` sign can be used to  
  prevent the `-` sign becoming bound to the proceeding `:` 

EEL allows the modifiers to be combined. For example:

- `${key,,^}` - the value associated with _key_ with the first character in upper case and subsequent 
characters in lowercase.  
- `${key^^-default}` - if there is a value associated with _key_ then use its value in uppercase.
  If there is no value associated with _key_ then use the literal `default`
- `${key:0:3,,}` - the first three characters of the value associated with _key_ in lower case
- `${key:2:3^}` - 3 characters from the middle of the value associated with _key_. The first character returned will be
  in upper case
- `${#key-default}` - if there is a value associated with _key_ then use its length.
  If there is no value associated with _key_ then use the literal `default`
- `${key:(-3):3:-default}` - if the value associated with _key_ is defined and not empty then return the last three 
  characters otherwise return the literal `default`

The order the modifiers are specified is:

| Order | Modifier                   | Function                                                               |
|-------|----------------------------|------------------------------------------------------------------------| 
| First | `#`                        | Take the length of text after applying any substring or case modifiers |
|       | `:`                        | Text substring                                                         |
|       | `^` `^^` `,` `,,` `~` `~~` | Case change                                                            |
| last  | `-` `:-`                   | Default value modifiers                                                |

Default values, indexes and lengths of substrings are EEL Expressions in their own right. Therefore, the following 
Value Interpolations are valid:

- `${undefined-defaultText}` - if _undefined_ is not in the SymbolsTable then use the literal text `defaultText`
- `${undefined-}` - if _undefined_ is not in the SymbolsTable then use empty text 
- `${first-${second}}` - if _first_ is not in the SymbolsTable then use the value associated with _second_ instead 
- `${first-${second-defaultText}}` - if _first_ is not in the SymbolsTable then try _second_. 
  If _second_ is also not in the SymbolsTable then use the literal text `defaultText`
- `${undefined-$myFunction()}` - if _undefined_ is not in the SymbolsTable then call _myFunction_ 
- `${undefined-$( expression )}` - if _undefined_ is not in the SymbolsTable then evaluate _expression_
- `${text: -3:3}` - the last three characters from _text_. Note the space in between the `:` and the `-`  
- `${text:$(-3):3}` - the last three characters from _text_. Expression interpolation has been used to calculate the offset
- `${text:$(indexOf(${text}, '~', fail()) + 1):1}` - return the character in _text_ that immediately follows the first `~`.
  If there are no `~` characters then fail the expression.

**Undefined values**

There are several ways to handle keys do not have an associated value in the SymbolsTable. These are:
1. Use defaults with all Value interpolations 
2. Use the [isDefined operator](#operators) (`?`) to check the key is defined in the SymbolsTable before reading it 
3. [Configure](Using%20EEL.md#symbols-table) the SymbolsTable to return a hardcoded value if the key is undefined (e.g. empty text)
4. Have the client code handle the [EelUnknownSymbolException](Using%20EEL.md#exceptions)
5. Using [Function interpolation](#function-interpolation) to fail the expression with a custom message. 
   For example, `${undefined-$fail('custom error message')}` 


## Function interpolation
Function interpolation is written in the form **_$functionName(...)_**. This is the equivalent of:

    $( functionName(...) )

This is an Expression interpolation that only calls a function. The arguments passed to the function can contain 
Value interpolations, literals, nested functions and other expressions.


## Expression interpolation

Function interpolation is written in the form **_$(...)_**, where `...` is a complex expression that can contain 
literal [constants](#constants), [Value interpolations](#value-interpolation), [function calls](#eel-functions) and 
[operators](#operators). Expression interpolations cannot be empty


The full power of EEL is in the Expression interpolation. This is a simple programming language used to evaluate
a value.

### Reserved words and symbols

| Values  | Numeric Ops | Logical Ops |     Bitwise Ops     | Relational Ops | Equality Ops  | Text Ops | Conversions | Quotes | [Values](#value-interpolation) | Misc |
|:-------:|:-----------:|:-----------:|:-------------------:|:--------------:|:-------------:|:--------:|:-----------:|:------:|:------------------------------:|:----:|
| `true`  |     `+`     |    `and`    |         `&`         |      `<`       |      `=`      |   `~>`   |   `text`    |  `'`   |              `#`               | `${` |
| `false` |     `-`     |    `or`     | <code>&#124;</code> |      `<=`      | `<>` and `!=` |          |  `number`   |  `"`   |              `^`               | `$(` |
|         |     `*`     |    `not`    |         `~`         |      `>`       |               |          |   `logic`   |        |              `^^`              | `$[` |
|         |     `/`     |    `xor`    |         `^`         |      `>=`      |               |          |   `date`    |        |              `,`               | `(`  |
|         |    `//`     |             |        `<<`         |      `in`      |               |          |             |        |              `,,`              | `)`  | 
|         |    `-/`     |             |        `>>`         |   `isBefore`   |               |          |             |        |              `~`               | `[`  |
|         |     `%`     |             |                     |   `isAfter`    |               |          |             |        |              `~~`              | `]`  | 
|         |    `**`     |             |                     |                |               |          |             |        |              `-`               | `{`  |
|         |             |             |                     |                |               |          |             |        |              `:-`              | `}`  |
|         |             |             |                     |                |               |          |             |        |              `:`               | `,`  |
|         |             |             |                     |                |               |          |             |        |                                | `?`  |
|         |             |             |                     |                |               |          |             |        |                                | `;`  |
|         |             |             |                     |                |               |          |             |        |                                | `:`  |
|         |             |             |                     |                |               |          |             |        |                                | `##` |

#### Naming conventions
- Reserved words are lowerCamelCase. For example `text` and `logic`, `and`, `true` and `isAfter`,  
- [Function names](#eel-functions) are lowerCamelCase. Dots (`.`) are sometimes included in function names to create
  prefixes. These are used to logically group functions and avoid namespace clashes much as packages do in Java. 
  Otherwise, they have no special meaning. 
  Example function names are `count`, `cos`, `isEmpty`, `system.home`, `date.local`, `log.warn` and `format.hex`
- SymbolsTable keys are typically in a format that is appropriate to the data source. For example, environment variables
  are usually in _UPPERCASE_WITH_UNDERSCORES_ while JVM properties are in _lower.case.with.dots_. 
- The [identifiers](#identifiers) section describes for format of an identifier

### Comments
Expression Interpolations can contain comments. A comment starts with two hash characters (`##`) and is terminated
by either a newline, a carriage return or two more hashes. For example:

    $random( 10, ## upper-limit ## 99 )

or  

    $( random(1, 10);        ## first random number
       random(20, 30);       ## second random number
       ($[1] + $[2]) / 2     ## average of the random numbers
    )

### Constants
EEL defines the following constants:
- **true** 
- **false**

Constant names are case-sensitive.

### Operators
EEL supports the following operators:

| Precedence  | Operators Symbols                              | Operators Name(s)                                                                     |
|-------------|------------------------------------------------|---------------------------------------------------------------------------------------|
| 1 (Highest) | `()` `?`                                       | parentheses, isDefined                                                                |
| 2           | `+` `-` `~` type-conversion, function-call     | unary-plus, unary-minus, bitwise-not, type-conversion, function-call                  |
| 3           | `**` `~>`                                      | exponentiation, string-concatenation                                                  |
| 4           | `*` `/` `//` `-/` `%`                          | multiply, divide, divide-floor, divide-truncate, modulus                              |
| 5           | `+` `-`                                        | addition, subtraction                                                                 |
| 6           | `<<` `>>`                                      | left-shift, right-shift                                                               | 
| 7           | `&`                                            | bitwise-and                                                                           |
| 8           | `^`                                            | bitwise-xor                                                                           |
| 9           | <code>&#124;</code>                            | bitwise-or                                                                            | 
| 10          | `<` `<=` `>` `>=`, `in`, `isBefore`, `isAfter` | less-than, less-than-equals, greater-than, greater-than-equals, in, isBefore, isAfter |   
| 11          | `=` `!=` `<>`                                  | equal, not-equal[1]                                                                   |   
| 12          | `not`                                          | logical-not                                                                           |
| 13          | `and`                                          | logical-and                                                                           |
| 14          | `xor`                                          | logical-xor                                                                           |
| 15          | `or`                                           | logical-or                                                                            | 
| 16          | `? :`                                          | conditional                                                                           |
| 17 (Lowest) | ;                                              | [compound expression](#compound-expressions)                                          |

[1] To match other languages, the not-equal operator exists in two forms; `!=` and `<>`. The difference is purely cosmetic

Because EEL is a weakly typed language values, values are automatically converted by the operators using the standard 
[type conversion rules](#types-conversions). The expected types for the operators are follows: 

| Operator Group   | Operators Symbols                                       | Operand type(s)                                                |
|------------------|---------------------------------------------------------|----------------------------------------------------------------|
| Text             | `~>`                                                    | Text                                                           |
| Logical          | `not` `and` `or` `xor`                                  | Logic                                                          |
| Bitwise          | `~` `&` <code>&#124;</code> `^` `<<` `>>`               | Number with any fractional part truncated                      |
| Maths            | `+` `-` (minus and negation) `*` `/` `//` `-/` `%` `**` | Number with any optional fractional part                       |
| Numeric relation | `<` `<=` `>` `>=`                                       | Number with any optional fractional part                       |
| Date relation    | `isBefore`, `isAfter`                                   | Date                                                           |
| SymbolsTable     | `?`                                                     | identifier                                                     |
| Conditional      | `?  :`                                                  | first operand is Logic, operands two and three can be any type |  
| Equality         | `=` `!=` `<>` `in`                                      | see table below                                                | 

The equals, not-equal and `in` operators compare their operands based on the following rules:

| Left \ Right | Text                       | Number                    | Logic                          | Date                             |
|--------------|----------------------------|---------------------------|--------------------------------|----------------------------------| 
| **Text**     | Compare two Text values[1] | Convert Number to Text    | Convert Logic to Text          | Convert Date to Text             |
| **Number**   | Convert Number to Text     | Compare two Number values | Convert Logic to Number        | Convert Date to Number           |
| **Logic**    | Convert Logic to Text      | Convert Logic to Number   | Compare two Logic values       | Convert both values to Numbers   |
| **Date**     | Convert Date to Text       | Convert Date to Number    | Convert both values to Numbers | Compare two instances in time[2] |

[1] Text comparisons are case-sensitive  
[2] Two dates will be considered equal if they represent the same instant of time, even if they are in different zones. 
As a result the `=`, `<>`, `<`, `<=`, `=>` and `>` operators applied to dates will return self-consistent values.

These rules are defined this way so that it is always possible to compare the values. 
  
#### Operators Notes

- Operator names (`not`, `and`, `or`, `xor`, `isBefore`, `isAfter` and `in`) are case-sensitive.
- The `+` operator is purely a numeric operator; the `~>` operator is used for string concatenation.
- The isDefined operator (`?`) is used to check if an identifier has an associated value in the SymbolsTable. 
  If the identifier is defined the operator returns `true`, otherwise it returns `false`. 
  The operator is written after the name of the SymbolsTable identifier (it has right associativity).

  For example, `myIdentifier?` will return _true_ only if `${myIdentifier}` returns a value rather than throwing an 
  `EelUnknownSymbolException`.
- The `in` operator is used to test for equality against multiple values. For example, 
  
  `(${myValue} = 1) or (${myValue} = 2) or (${myValue} = 3 )` 
  
  can be simplified to 

  `${myValue} in { 1, 2, 3 }` 
    
- The logic operators (`not`, `and`, `xor` and `or`) operate on [Logic](#logic) values whereas the Bitwise operators 
  (`~`, `&`, `^` and `|`) operate on [Number](#number) values.
- The logic operators `and` and `or` are short-circuited. `xor` can not be short-circuited because the 
  result always depends on both operands
- The shift operators (`<<` and `>>`) can shift numbers by a negative number of bits. This is the equivalent of shifting
  the number in the other direction by a positive number of bits. 
- The right shift operator (`>>`) performs sign extension.

- There are 3 division operators:
  - divide (`/`) - the quotient could contain fractional parts
  - divide-floor (`//`) - the integer quotient is rounded towards negative infinity
  - divide-truncate (`-/`) - the integer quotient is rounded towards 0 discarding any fractional parts
  
  The difference between the (`//`) and (`-/`) becomes apparent with negative numbers. For example:  
   
  |      divide (`/`)       |  divide-floor (`//`)   | divide-truncate (`-/`) |
  |:-----------------------:|:----------------------:|:----------------------:| 
  |  `( 12.0 / 1 )` = 12.0  |  `( 12.0 // 1 )` = 12  |  `( 12.0 -/ 1 )` = 12  |
  |  `( 12.3 / 1 )` = 12.3  |  `( 12.3 // 1 )` = 12  |  `( 12.3 -/ 1 )` = 12  |
  |  `( 12.5 / 1 )` = 12.5  |  `( 12.5 // 1 )` = 12  |  `( 12.5 -/ 1 )` = 12  |
  |  `( 12.7 / 1 )` = 12.7  |  `( 12.7 // 1 )` = 12  |  `( 12.7 -/ 1 )` = 12  | 
  |                         | 
  | `( -12.0 / 1 )` = -12.0 | `( -12.0 // 1 )` = -12 | `( -12.0 -/ 1 )` = -12 |
  | `( -12.3 / 1 )` = -12.3 | `( -12.3 // 1 )` = -13 | `( -12.3 -/ 1 )` = -12 |
  | `( -12.5 / 1 )` = -12.5 | `( -12.5 // 1 )` = -13 | `( -12.5 -/ 1 )` = -12 |
  | `( -12.7 / 1 )` = -12.7 | `( -12.7 // 1 )` = -13 | `( -12.7 -/ 1 )` = -12 | 


### Standard functions

EEL has a number of standard functions that are automatically made available to expressions. These are defined in 
the [EEL Function Reference Manual ](function-reference/index.html)

The standard functions have prefixes that describe their purpose and prevent name space clashes.

| Prefix   | Group                                                  | Example        |
|----------|--------------------------------------------------------|----------------|
| < none > | General utilities                                      | count()        |
| eel      | Functions that interact directly with the EEL runtime. | eel.version()  |
| system   | Functions that are host-specific.                      | system.home()  |
| text     | Functions that manipulate or generate text.            | text.random()  |
| number   | Functions that manipulate or generate numbers.         | number.pi()    |
| logic    | Functions that manipulate or generate logical values.  | logic.index()  |
| date     | Functions that manipulate or generate dates.           | date.utc()     |
| log      | Writes messages to the system logs                     | log.error()    |
| format   | Functions for data formatting.                         | format.local() | 
| io       | Functions that read data from local files.             | format.local() | 


### Compound expressions

Compound expressions are written in the form:

    $( <expression-1> ; <expression-2> ; <expression-3> )

Where `expression-1`, `expression-2` and `expression-3` are all non-empty expressions that are separated from each 
other by a semicolon (`;`). The value returned by the compound expression is the last expression in the sequence.
An expression can refer to a previously defined expression by using a _Lookback_, which is written in the 
form `$[index]`, where _index_ is a 1-based number. For example:

    $( ${Key,,^-} ; isEmpty( $[1] ) ? fail('no text') : $[1] )

The first expression is `${Key,,^-}` which will attempt to read _Key_ from the SymbolsTable, capitalise the first letter
and convert the rest in lower case. The second expression in the chain references the first expression twice
via Lookback `$[1]`. This use of compound expressions can be used to eliminate DRY code.


Backwards references are supported; every expression in the chain can reference any of the previous expressions. 
For example, 

    $( 'a' ; $[1] ~> 'b' ; $[2] ~> 'c' )

will evaluate to `abc`. To guard against accidental recursion, forward references are not supported. As a result 

    $( $[2] + $[3] ; 2 ; 3 ; $[1] )

will throw an [EelSemanticException](Using%20EEL.md#exceptions).  

 
Each expression in the chain is evaluated at **_most_** once. This can be used to call functions like `count()`, 
`random()` and `text.random()`, which generate different values each time they are called, exactly once. 
Through the Lookback, the same generated value can be used multiple times. 

The scope of a Look Back is limited to the Expression interpolation in which they are defined. For example:

    First = $( 'a' ; 'b' ; '<$[1]~$[2]>' ) and Second = $( 'c' ; 'd' ; '<$[1]~$[2]>' )

will evaluate to `First = <a~b> and Second = <c~d>`. To prevent ambiguity, compound expressions cannot be nested. So

    $( count() ; $[1] + $( 2 ; $[1] + 3 ) )

will throw an [EelSyntaxException](Using%20EEL.md#exceptions). 


Lookbacks are full EEL expressions. For example:

    $( 'First'; 'Second'; 'Third' ; $[($count() + 1)] )

will return `First` the first time the expression is evaluated, followed by `Second` and `Third` on subsequent
evaluations.

Just like [Value Interpolation](#value-interpolation), lookback indexes support default values which are used if the value is out of 
range. Like indexes, default values are EEL expressions in their own right. Extending the previous example, 

    $( 'First'; 'Second'; 'Third' ; $[($count() + 1)-$log.info('Not found', '3+')] )

With this change subsequent evaluations of the expression will return `3+` and, as a side effect, log an info message. 


### Caveats 
1. Because EEL is a [Lazy language](#lazy-processing), unreferenced expressions will not be evaluated. For example:


    $( fail('not called') ; 200 ; $[2] + $[2] )

  evaluates to 200. Because the first expression in the sequence is not referenced, it will never be evaluated and the
  expression will not fail.


2. EEL does **NOT** guarantee lookback expressions will be evaluated in any particular order, and specifically they
might not be evaluated in the order they are defined. So for example:


    $( count() ; count() ; count(); ($[2] ** $[3]) + $[1] )

might be interpreted as `(0 ** 1) + 2`, `(1 ** 2) + 0` or any other combination of values returned by the counter. 
 


## Lazy processing
EEL is a lazy language; if parts of the expression do not need to be evaluated, they won't be. This leads to some
interesting effects that can be put to good use, particularly where there is the possibility of a side effect. 

**1. Value interpolations might not evaluate their defaults**  

Consider the following expression:

    ${myVariable-$log.error("Undefined")}

If `myVariable` is in the SymbolsTable it will be returned without writing anything to the system logs.


**2. The conditional operator will only evaluate one path**

Consider the following expression:

    $( myVariable? ? ${myVariable} : log.error("Undefined") )

Like the previous example, if `myVariable` is in the SymbolsTable it will be returned without writing anything to the 
system logs.


**3. The logical 'and' and 'or' operators are short-circuited**

Consider the following expression:

    $( true or (count() = 10) )

Because the first operand to the `or` operator is true there is no need to evaluate the second operand to determine
the eventual outcome. Consequently, the counter will not be incremented.

In a similar way:

    $( false and (count() = 10) )

Because the first operand to the `ànd` operator is false there is no need to evaluate the second operand to determine
the eventual outcome, and again the counter is not incremented.


The logical xor operator cannot be short-circuited as both operands are always required to determine the value. 


**4. Function arguments might not be evaluated**

Expressions can be passed to functions without them being evaluated first. These are known as ["Thunks"](https://en.wikipedia.org/wiki/Thunk).

Many [Eel functions](#eel-functions) use thunks to pass a default value that is returned if a function
cannot determine what value it should return. For example: 

    $indexOf( ${myVariable-}, "x" )

will return -1 if `x` can not be found in the input string. However, there is an optional third argument that can be 
passed:

    $indexOf( ${myVariable-}, "x", log.error("Undefined", 99) )

If `x` is found in `${myVariable-}` then its index is returned. If it's not found then, _and only then_, will the 
third argument be evaluated at which point a message will be logged and 99 returned.


**5. Lookbacks in compound expressions might not be evaluated**

Consider the following [compound expression](#compound-expressions):

    $( 1 ; fail("Not evaluated") ; $[1] )

Because there is no reference to the second lookback, it will not be evaluated and the expression will not be failed


---
# Eel functions

The [EEL Function Reference Manual](function-reference/index.html) outlines the functions that are available by default. 
Additional functions can be added through User-Defined Functions ([UDF](User%20Defined%20Functions.md)s).

Functions are organised into groups based on their purpose. The group name is separated from the function by a 
dot (`.`). This grouping serves two purposes: it describes the function's intent and prevents naming conflicts, 
similar to how packages work in Java.

EEL functions can have default arguments. If an argument is not explicitly passed then EEL will automatically insert
the default value.

EEL functions can also accept "varargs", with the final argument being able to be passed zero, one, or more times.


---
# Sample EEL Expressions
## Text pass-through

    this is an expression

Because there are no interpolated sequences or escaped characters, the expression is passed through unchanged.
As a result, the evaluated [Result](Using%20EEL.md#result) object has a type of `Text` and a value of
`this is an expression`.

The expression

    Hello\nWorld

returns two lines of text in the evaluated [Result](Using%20EEL.md#result) object.


## Reading environment variables

    Your HOME directory is ${HOME}

Assuming EEL is running on a Unix-like operating system and that the [SymbolsTable](Using%20EEL.md#symbols-table) contains
the environment variables, the evaluated [Result](Using%20EEL.md#result) object has a type of `Text` and a value that
starts with `Your HOME directory is `, followed by the user’s home directory.

On Windows systems, this expression throws an
[EelUnknownSymbolException](Using%20EEL.md#exceptions) because Windows uses the `HOMEDRIVE` and `HOMEPATH` environment
variables instead. The equivalent expression on Windows is:

    Your HOME directory is ${HOMEDRIVE}${HOMEPATH}

An expression that works on both operating systems is:

    Your HOME directory is ${HOME-${HOMEDRIVE}${HOMEPATH}}

However, because locating the user’s home directory is a common requirement, a simpler and cleaner approach is to use
the [standard EEL function](#eel-functions)


    Your HOME directory is $system.home()


## Calling functions

[Function interpolation](#function-interpolation) can be used to call a function. For example:

    Next week is $date.local("7d")

The evaluated [Result](Using%20EEL.md#result) object has a type of `Text` and a value that starts with
`Next week is `, followed by the local date and time for the following week.

Function calls can be nested. For example, the following expression truncates the local date for the following week to
the start of the day:

    Next week is $date.set(date.local("7d"), "@d")

Because the nested function call is already part of the function interpolation, there is no need to prefix
`date.local` with a `$`. The same result can be achieved more simply as:

    Next week is $date.local("7d", "@d")

If the date should be displayed without any time components, the following expression can be used:

    Next week is $format.local("yyyy-MM-dd", "7d")


## Functions that return default values

Some [EEL functions](#eel-functions) allow a default value to be provided, which is returned if the function is unable
to perform its normal operation.

For example, `indexOf(text, subString)` returns `-1` if the search string is not present in the text.
Therefore,

    $indexOf('abcdef', 'z')

returns `-1` because `z` does not occur in `abcdef`. Reading the evaluated [Result](Using%20EEL.md#result) object as a
logical value returns `false`, indicating that the search text was not found.

If a different value is required when the search string is not found, a default can be provided as an additional
function argument. For example:

    $indexOf('abcdef', 'z', 0)

returns `0` instead. If no acceptable default value exists, the `fail()` function can be passed as the default argument.
This function is evaluated only if the default is required. For example:

    $indexOf('abcdef', 'z', fail())

This causes an [EelFailException](Using%20EEL.md#exceptions) to be thrown.

By default, the message generated by `fail()` provides little context, so a more informative example might be:

    $indexOf('abcdef', 'z', fail('There is no z'))

A less disruptive alternative is to use one of the logging functions to warn of the condition while still returning a
default value. For example:

    $indexOf('abcdef', 'z', log.warn('There is no z, returning {}', 0))

Finally, note that:

    $indexOf('abcdef', 'd', fail())

returns `3`, which is the index of `d`. In this case, `fail()` is not evaluated and no exception is thrown.


## Changing the result type

Function interpolation can be used to call a conversion function and explicitly set the type of the evaluated
[Result](Using%20EEL.md#result) object. For example, default values for
[Value Interpolation](#value-interpolation) are text values, which may not always be appropriate. Conversion function
could fix this:

    $number(${#myValue--1})

In this expression, EEL attempts to return the number of characters associated with `myValue`. If `myValue` is not
defined, `-1` is returned. The result type is guaranteed to be `Number`.


## Choosing values

EEL expressions often need to return one of several possible values. If the choice is between just two values, 
the simplest approach is to use the [conditional operator](#operators). This works in the same way as in languages like
C or Java. For example:

    $( ${logicExpression} ? 'trueValue' : 'falseValue' )

If an expression needs to return more than two values, the conditional operator can be nested, but this quickly becomes
hard to read. Fortunately, EEL can use [compound expressions](#compound-expressions) to provide a much more 
readable alternative.

If the choice is between several numeric values, it can be written like this:

    $( 'value1'; 'value2'; 'value3'; $[ ${numericExpression} ] )

The numeric expression is effectively used as a **1-based index** into the list of expressions (in this case, text 
literals) to determine which value to return.

If there is a possibility that `numericExpression` might return a value outside the valid range, a default value
can be specified. For example:

    $( 'value1'; 'value2'; 'value3'; $[ ${numericExpression}-defaultValue] )

One thing to note is that if `numericExpression` is anything more complex than a numeric literal or a symbol table
lookup, it must be enclosed in brackets. This avoids ambiguity between the `-` character when used as a negative sign
and when used to separate the default value.

The `text.index` function can be used to select one of several text values. This function returns the 1-based index of 
the first occurrence of a given text value within a list of text values. For example:

    $( 'value1'; 'value2'; 'value3'; $[text.index( ${search}, 'test1', 'test2', 'test3' )-defaultValue] )

In this case:
- If `${search}` is equal to `test1`, the result is `value1`.
- If `${search}` is equal to `test2`, the result is `value2`.
- If `${search}` is equal to `test3`, the result is `value3`.
- If `${search}` matches none of these values, `defaultValue` is returned.

The most flexible variant uses the `logic.index` function. This function takes a list of logical expressions and 
returns the 1-based index of the first expression that evaluates to `true`. For example:

    $( 'value1'; 'value2'; 'value3'; $[logic.index( ${test1}, ${test2}, ${test3} )-defaultValue] )

These tests are not limited to symbol table lookups; they can be any expressions that return a logical value.

These constructs can perform similar functions to `if...else`, `if...else if...else` and `switch`/`case` constructs
in other languages.


## Counters

    $count()

returns a [Result](Using%20EEL.md#result) object with a type of `Number` and a value from an anonymous zero-based counter.
If the expression is reevaluated _with the same Context_, the next value from the counter is returned.

Named counters can be used when expressions using the [Context](Using%20EEL.md#eel-context) require multiple independent
counters. For example:

    First: $count("first"), Second: $count("second")

returns a [Result](Using%20EEL.md#result) object with a type of `Text` containing the values from both counters.
If the expression is reevaluated _with the same Context_, the next value from each counter is returned.


## Creating a sequence of file names

If EEL repeatedly evaluates

    $system.temp()/${myFilePrefix-}$count().txt

_with the same [Context](Using%20EEL.md#eel-context)_, each returned [Result](Using%20EEL.md#result) object has a type of `Text`
and a value that forms a sequence of file names in the system temp directory, optionally prefixed with a common value.

EEL does not create the files; the client application is responsible for that.

To reset the sequence, the expression must be recompiled with a new Context.


## Paths with a common root

Given the following two expressions:

    ${root}/config
    ${root}/template

If `${root}` is defined in a common [SymbolsTable](Using%20EEL.md#symbols-table), these expressions return paths that
share a common root. The value of `${root}` can also be determined by a previously evaluated EEL expression.


## Directories with time stamps

Given the expression

    ${root-}/$format.local("yyyy/MM/dd/HH/")

the evaluated [Result](Using%20EEL.md#result) object has a type of `Text` and a value that forms a valid *nix directory
name. The expression produces a different directory each hour. The optional `root` value allows the directory structure
to be moved to a new location in the file system.

* To create one directory per day instead of per hour, change `"yyyy/MM/dd/HH/"` to `"yyyy/MM/dd/"`.
* To use a flat directory structure, change `"yyyy/MM/dd/HH/"` to `"yyyy-MM-dd-HH/"`.


## Separating randomly named files into directories

To create a large number of files with random names and organise them into subdirectories, use:

    $(text.random(10, '0-9'); $left($[1], 4); '${root-}/$[2]/$[1].txt')

The evaluated [Result](Using%20EEL.md#result) object has a type of `Text` and a value that forms a valid *nix directory path.
The root of the directory is the optional `root` value from the [SymbolsTable](Using%20EEL.md#symbols-table).  
The subdirectory is determined by the first four digits of a randomly generated 10-digit number. Inside that directory, a
`.txt` file is created with the same 10-digit number as its name.


## Converting paths

To convert path separator characters in text to *nix format, use:

    $replace(${root-}, "\\", "/")

The evaluated [Result](Using%20EEL.md#result) object has a type of `Text` and a value based on the optional `root` value
from the [SymbolsTable](Using%20EEL.md#symbols-table), but with forward slashes instead of Windows-style backslashes.

To convert a path to the format used by the underlying operating system, use:

    $realPath(${root-})

This conversion can be applied in the _[Directories with time stamps](#directories-with-time-stamps)_ example to ensure
that the returned path is correctly formatted:

    $realPath(${root-} ~> "/" ~> format.local("yyyy/MM/dd/HH/"))


## Directory listings

Assuming `${myPath}` is defined in the [SymbolsTable](Using%20EEL.md#symbols-table) and references a valid directory:

    $firstModified(${myPath})

returns a [Result](Using%20EEL.md#result) object with a type of `Text` containing the name of the most recently modified
file in that directory.

An optional second argument can be provided as a glob pattern to filter files. By default, it is `"*"`, which matches
all files. For example:

    $firstModified(${myPath}, "*.txt")

returns the name of the most recently modified text file in the directory.

An optional third argument specifies a zero-based index of the file to return. The default is `0`, but it can be set to
return files modified later. For example:

    $firstModified(${myPath}, "*.txt", 1)

returns the name of the second most recently modified text file.

With a new [Context](Using%20EEL.md#eel-context), repeatedly evaluating:

    $(firstModified(${myPath}, "*.txt", count()))

returns all files in the directory until none remain. By default, the function throws an exception if no file is found,
but this can be overridden with a fourth optional argument. For example:

    $(firstModified(${myPath}, "*", count(), ""))

returns an empty string when no more files are available.

Other functions operate in the same way but return files in different orders. These are:
`firstCreated`, `lastCreated`, `firstAccessed`, `lastAccessed`, `firstModified`, and `lastModified`.


## Logging

To log a single value at _info_ level, use:

    $log.info(${myValue-Not Set})

The evaluated [Result](Using%20EEL.md#result) object has a type of `Text` and a value that either comes from the
[SymbolsTable](Using%20EEL.md#symbols-table) or the literal text `'Not Set'`. The returned value is written to the system
log at INFO level. For example:

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Not Set

The text `'Logged EEL Message:'` cannot be changed or removed, as it is used to guard against log reputation attacks.

To add context to the logged message, use:

    $log.info("The value is {}", ${myValue-not set})

which might log:

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: The value is not set

Logging functions can accept multiple values, but only the last one is returned by the expression. For example:

    $log.info("Evaluating {} + {} = {}", 1, 2, (1 + 2))

will log:

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Evaluating 1 + 2 = 3

and return a [Result](Using%20EEL.md#result) object with a type of `Number` and a value of `3`.

To log a message only when a value is not in the [SymbolsTable](Using%20EEL.md#symbols-table), use:

    ${myValue-$log.warn("myValue is not set")}

The function interpolation for the default value, including the logging, is executed only if `myValue` is not present.

Finally, the last value passed to a logging function does not need to be logged. For example:

    $log.info("{} {}", "Hello", "World", 99)

will log:

    [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Hello World

and return a [Result](Using%20EEL.md#result) object with a type of `Number` and a value of `99`.


## Manipulating dates

Dates can be manipulated using numeric operators because EEL automatically converts dates into the number of seconds
elapsed since `1970-01-01 00:00:00` in the UTC time zone.

For example:

    $(date.utc() + 5)

adds 5 seconds to the current UTC date. Similarly, the minus operator can be used to calculate time differences:

    $(date.utc() - date.start())

returns a [Result](Using%20EEL.md#result) object with a type of `Number` and a value representing the age, in seconds,
of the current [Context](Using%20EEL.md#eel-context).

If seconds are too fine-grained, the difference can be expressed in minutes using:

    $duration(date.start(), date.utc(), "minutes")

Building on this, an expression to check whether a file is out of date could be written as:

    $(duration(modifiedAt(${myFile}), date.local(), "months") > 6)

In this case, the [Result](Using%20EEL.md#result) object has a type of `Logic` and a value that is `true` only if the
referenced file is more than 6 months old.


## Failing expressions

To fail an expression if a condition is not met, use `fail` within a conditional expression. For example:

    $( ${myValue-} ; isEmpty($[1]) ? fail() : $[1] )

The first expression in a [compound expression](#compound-expressions) is used to read a value from the 
[SymbolsTable](Using%20EEL.md#symbols-table). If the value is undefined or an empty string, `isEmpty($[1])` returns 
`true`, and `fail()` is executed to terminate the expression with an [EelFailException](Using%20EEL.md#exceptions). 
If a value exists, the look-back value is returned to the client as `Text`.

To fail an expression with a custom message when a value is not defined, use:

    ${myValue-$fail("Custom Message")}

To fail an expression if multiple values are not defined, use:

    $( not myValue1? or not myValue2? ? fail() : ${myValue1} ~> ${myValue2} )

It is also possible to fail an expression if an unsupported version of EEL is being used:

    $( eel.version() < 2 ? 0 : fail("Invalid EEL Version") )

