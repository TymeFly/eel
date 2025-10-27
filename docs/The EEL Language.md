# EEL Language
Probably the easiest way to imagine an EEL expression is as a text literal that can contain:
1. [Escaped characters](#escaped-characters). These are all prefixed with a backslash character (`\`) 
2. [Interpolation sequence](#interpolation-sequences) and [look backs](#compound-expressions). These are all prefixed with a dollar character (`$`)

If the source expression contains doesn't contain any of these sequences then the data consumed by the EEL compiler 
will be returned as-is in the Result. On the face of it this isn't very useful; however, it does provide a good
migration path for systems that are already configured with simple strings.


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
3. **[Expression interpolation](#expression-interpolation)** which is written in the form `$(...)` and is used to perform calculations which 
 may include function calls

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

EEL supports 4 data types. They are named differently from their Java equivalents to distinguish between
data that is in the EEL and Java domains. The EEL data types are:

### Text

Text literals are written in the form `"..."` or `'...'`; the opening quote can be either a single or a double
quote, but the associated closing quote must match the opening quote. This allows a double-quoted text literal to 
contain single quote characters and vice versa.  

Text literals behave exactly the same way as full EEL expressions; they can contain 
[Value Interpolations](#value-interpolation), [Function Interpolations](#function-interpolation) and 
[Expression Interpolations](#expression-interpolation). They can also contain [Escape Sequences](#source-characters) to
embed special characters, including what would have been the closing quote. If the Text literal is nested inside an
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
  - Scientific format (e.g. `2.99792e8`) where the exponent may include a sign
 
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


### Date

EEL expressions support Date-Time values, to the precision of a nanosecond. There are no Date-Time literals,
but they can be generated by: 
  - Calling a function that returns a date, such as `date.utc()`, `date.local()`, `date.at()` or `date.start()`
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

* Text values are converted to Numbers as if they were numeric literals. Leading spaces, trailing spaces are ignored.
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
* Number values are converted to Text as their plain (non-scientific) decimal representation
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
- `${key:0:3,,}` - the first 3 characters of the value associated with _key_ in lower case
- `${key:2:3^}` - 3 characters from the middle of the value associated with _key_. The first character returned will be
  in upper case
- `${#key-default}` - if there is a value associated with _key_ then use its length.
  If there is no value associated with _key_ then use the literal `default`
- `${key:(-3):3:-default}` - if the value associated with _key_ is defined and not empty then return the last 3 
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
- `${text: -3:3}` - the last 3 characters from _text_. Note the space in between the `:` and the `-`  
- `${text:$(-3):3}` - the last 3 characters from _text_. Expression interpolation has been used to calculate the offset
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
Expression Interpolations can contain comments. A comment starts with 2 hash characters (`##`) and is terminated
by either a newline, a carriage return or 2 more hashes. For example:

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
| 1 (Highest) | `()` `?`                                       | Parentheses, isDefined                                                                |
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
| 16 (Lowest) | `? :`                                          | conditional                                                                           |

[1] To match other languages, the not-equal operator exists in two forms; `!=` and `<>`. The difference is purely cosmetic

As most operators require their operands to be a particular type, EEL will automatically convert their operands using the
[type conversion rules](#types-conversions). The expected types for the operators are: 

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
- The `in` operator is used to test for equality against multiple values to simplify expressions. For example, 
  
  `(${myValue} = 1) or (${myValue} = 2) or (${myValue} = 3 )` 
  
  can be simplified to 

  `${myValue} in { 1, 2, 3 }` 
    
- The logic operators (`not`, `and`, `xor` and `or`) and the Bitwise operators (`~`, `&`, `^` and `|`) operate on
  different data types.
- The logic operators `and` and `or` are short-circuited. `xor` can not be short-circuited because the 
  result always depends on both operands
- The Bitwise operators will silently truncate any fractional parts of their operands. 

  For example, `3 << (28 / 10)` will return `12`. This is because `28 / 10` evaluates to `2.8`, but the shift
  operator will truncate the fractional part, so 3 is shifted left by two bits which gives `12`. 
  
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


### Compound expressions

Compound expressions are written in the form:

    $( <expression-1> ; <expression-2> ; <expression-3> )

Where `expression-1`, `expression-2` and `expression-3` are all non-empty expressions that are separated from each 
other by a semicolon (`;`). An expression can refer to a previously defined expression by using a _Look Back_, which 
is written in the form `$[index]`, where _index_ is a 1-based number. For example:

    $( ${Key,,^-} ; isEmpty( $[1] ) ? fail('no text') : $[1] )

The first expression in the chain is `${Key,,^-}` which will attempt to read _Key_ from the SymbolsTable with the first
letter capitalised and the rest in lower case. The second expression in the chain references the first expression twice
via Look Back `$[1]`. This use of compound expressions can be used to eliminate DRY code.


Backwards references are supported; every expression in the chain can reference any of the previous expressions. 
For example, 

    $( 'a' ; $[1] ~> 'b' ; $[2] ~> 'c' )

will evaluate to `abc`. To guard against recursion, forward references are not supported. As a result 

    $( $[2] + $[3] ; 2 ; 3 ; $[1] * 10 )

will throw an [EelSemanticException](Using%20EEL.md#exceptions).  

 
Each expression in the chain is evaluated at **_most_** once. This can be used to call functions like `count()`, 
`random()` and `text.random()`, which generate different values each time they are called, exactly once. 
Through the Look Back, the same generated value can be used multiple times. However, care should be taken with
expressions like:

    $( count() ; count() ; $[2] + $[2] )

Because EEL is a [Lazy language](#lazy-processing) and the first expression in the chain is not referenced, it will
never be evaluated. Consequently `$[2]` will return 0, and therefore the overall expression will be `0 + 0` and 
not `1 + 1` as might be expected.

The scope of a Look Back is limited to the Expression interpolation which they are defined. 
Consequently, `$1` always refers to the first expression in the current Expression interpolation. For example:

    First = $( 'a' ; 'b' ; '<$[1]~$[2]>' ) and Second = $( 'c' ; 'd' ; '<$[1]~$[2]>' )

will evaluate to `First = <a~b> and Second = <c~d>`. To prevent ambiguity, compound expressions cannot be nested. So

    $( count() ; $[1] + $( 2 ; $[1] + 3 ) )

will throw an [EelSyntaxException](Using%20EEL.md#exceptions). 



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
## Text pass through

    this is an expression

As there are no interpolated sequences or escaped characters, the expression will be passed through as-is. 
Consequently, the evaluated [Result](Using%20EEL.md#result) object will have a type of Text and value of 
`this is an expression`.  

The expression 

    Hello\nWorld

will return two lines of text in the evaluated [Result](Using%20EEL.md#result) object


## Reading environment variables

    Your HOME directory is ${HOME}

Assuming the EEL is running on a *nix operating system and that the [SymbolsTable](Using%20EEL.md#symbols-table) contains the environment 
variables, then the [Result](Using%20EEL.md#result) object will have a type of Text and a value of that starts with 
`Your HOME directory is ` followed by the user's home directory.

On Windows systems this will throw a [EelUnknownSymbolException](Using%20EEL.md#exceptions) because Windows uses the `HOMEDRIVE` and
`HOMEPATH` environment variables instead. The correct expression under Windows would be:

     Your HOME directory is ${HOMEDRIVE}${HOMEPATH}

An expression that works on both operating systems is: 

     Your HOME directory is ${HOME-${HOMEDRIVE}${HOMEPATH}}

However, because reading locating the users home directory is common requirement, a simpler and cleaner way to get this
information is via the [standard EEL function](#file-system-functions-1):

     Your HOME directory is $system.home()

## Forcing the result type

Using Function interpolation to call a conversion function can be used to set the [Result](Using%20EEL.md#result) object to the
required type. For example: 

    $number( ${#myValue--1} )

In this expression EEL will try to return the number of characters associated with `myValue`. If it is not defined then
-1 will be returned. The return type is guaranteed be a Number


## Paths with a common root

Given the following two expressions:

    ${root}/config
    ${root}/template

If the value for `${root}` is in a common [SymbolsTable](Using%20EEL.md#symbols-table) then these expressions will return paths that
share a common root. `${root}` might even be determined by a previously evaluated an EEL expression.


## Calling functions

[Function interpolation](#function-interpolation) can be used to call a function. For example: 

    Next week is $date.local( "7d" )

The [Result](Using%20EEL.md#result) object will have a type of Text and a value of that starts with `Next week is ` followed by next
week's date and time. 

Function calls can be nested. For example, the following expression will truncate last week's local date to the start of
the day:

    Next week is $date.set( date.local("7d"), "@d" )

It is worth noting that because the nested function is already part of the function interpolation, there is no need
to prefix _date.local_ with a `$`. However, this could be written more simply as:

    Next week is $date.local( "7d", "@d" )

If the requirement is to display the date without any time fields then use:

    Next week is $format.local( "yyyy-MM-dd", "7d" )


## Counters

    $count()

will return a [Result](Using%20EEL.md#result) object that will have a type of `number` and value from an anonymous zero-based counter. 
If the expression is reevaluated _with the same Context_ then the next value from the counter is returned.

Named counters can be used if the expressions that use the [Context](Using%20EEL.md#eel-context) requires multiple, independent, counters. 
For example:   

    First: $count( "first" ), Second: $count( "second" )

will return a [Result](Using%20EEL.md#result) object that will have a type of Text and the values from both counters. 
If the expression is reevaluated _with the same Context_ then the next value from each counter is returned.


## Creating a sequence of file name

If the EEL repeatedly evaluates

    $system.temp()/${myFilePrefix-}$count().txt

_with the same [Context](Using%20EEL.md#eel-context)_ then each returned [Result](Using%20EEL.md#result) object will have a type of Text
and a value that forms a sequence of files in the system temp directory with a common optional prefix.

EEL will not create the files, but the client application could.

To reset the sequence, recompile the expression with a new Context.


## Directories with time stamps

    ${root-}/$format.local("yyyy/MM/dd/HH/")

The [Result](Using%20EEL.md#result) object will have a type of Text and a value that would make a valid *nix directory name. Every
hour the expression will evaluate to a different directory. The optional `root` value allows the client to move the
directory structure to a new location.

* If it is later determined that the system only needs one directory each day then change `yyyy/MM/dd/HH/` to `yyyy/MM/dd/` 
* If it is later determined that the system should have a flat directory structure then change `yyyy/MM/dd/HH/` to `yyyy-MM-dd-HH/`


## Separating randomly named files into directories

To create a large number of files with random names and put them in subdirectories where they will be easy to find use

    $( text.random(10, '0-9') ; $left( $[1], 4 ); '${root-}/$[2]/$[1].txt' )

The [Result](Using%20EEL.md#result) object will have a type of Text and a value that would make a valid *nix directory name.
The root of the directory is the optional _root_ value from the [SymbolsTable](Using%20EEL.md#symbols-table). 
The subdirectory is the first 4 digits of a randomly generated 10-digit number. Inside that directory will be a txt file
with the same 10-digit number. 


## Converting paths

To convert the path separator characters in some text to *nix format use

    $replace( ${root-}, "\\", "/")

The [Result](Using%20EEL.md#result) object will have a type of Text and a value based on the optional _root_ value from the 
[SymbolsTable](Using%20EEL.md#symbols-table), but with forwards slashes instead of Windows style backslashes.

To force the path to the format used by the underlying operating system then use: 

    $realPath( ${root-} )

This conversion could be used in the _[Directories with time stamps](#directories-with-time-stamps)_ example above to 
ensure that the returned path is formatted correctly.

    $realPath( ${root-} ~> "/" ~> format.local("yyyy/MM/dd/HH/") )


## Directory listings 

Assuming that `${myPath}` is in the [SymbolsTable](Using%20EEL.md#symbols-table) and references a valid directory then

    $firstModified( ${myPath} )

will return a [Result](Using%20EEL.md#result) object that has a type of Text and a value that is the name of the most recently 
modified file in that directory.

There is an optional second argument that is a globing expression that is used to filter the files. 
This defaults to `*` so it considers all files, but this can be set to search for specific files. For example:

    $firstModified( ${myPath}, "*.txt" )

will return the name of the most recently modified text file in the directory.

There is an optional third argument is a zero-based index of the file to find. This defaults to 0, but it can be set to
some other value to return the names of files that were modified later. For example:

    $firstModified( ${myPath}, "*.txt", 1 )

will return the name of the second most recently modified text file in the directory. Given a new 
[Context](Using%20EEL.md#eel-context) then repeatedly evaluating 

    $( firstModified( ${myPath}, "*.txt", count() ) )

will return all the files in the directory until there are no more files. By default, the function will throw an 
exception if it cannot find a file to return, however, this can be fixed with the final optional argument. 
If we call:

    $( firstModified( ${myPath}, "*", count(), "" ) )

Then EEL will return an empty string to indicate when there are no more files.

Finally, it is worth noting that there are other functions which operate in exactly the same way but return files in
different orders. These are `firstCreated`, `lastCreated`, `firstAccessed`, `lastAccessed`, `firstModified` and
`lastModified`.


## Logging

To log a single value at _info_ level use

    $log.info( ${myValue-Not Set} )

The [Result](Using%20EEL.md#result) object will have a type of Text and a value that either comes from the SymbolsTable, or the 
literal Text _'Not Set'_. The returned value will be written to the system log at INFO level. For example:

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

and will return a [Result](Using%20EEL.md#result) object with a type of number and a value of 3

If the requirement to only log values if they are not set then use:

    ${myValue-$log.warn( "myValue is not set" )}

This works because the Function interpolation for the default value, which includes the logging, will only be executed  
if _myValue_ is not in the [SymbolsTable](Using%20EEL.md#symbols-table).

Finally, it is worth noting that the final value passed to a logging function doesn't have to be logged. For example:

    $log.info( "{} {}", "Hello", "World", 99 )

will log 

    [INFO ] [INFO ] com.github.tymefly.eel.log - Logged EEL Message: Hello World

and will return a Result object with a type of number and a value of 99


## Failing expressions

To fail an expression if a condition is not met then use `fail` inside a condition. For example:

    $( ${myValue-} ; isEmpty( $[1] ) ? fail() : $[1] )

A look back is used to read a value from the [SymbolsTable](Using%20EEL.md#symbols-table). If this is undefined or an
empty string then `isEmpty( $[1] )` will return `true`, in which case `fail()` will be executed to terminate the
expression with an [EelFailException](Using%20EEL.md#exceptions). 
If there is text, then the value in the look back` is returned to the client as Text

If the requirement is to fail an EEL expression with a custom message if a value is not defined then use:

    ${myValue-$fail("Custom Message")}

If the requirement is to fail an EEL expression if multiple values are not defined then use: 

    $( not myValue1? or not myValue2? ? fail() : ${myValue1} ~> ${myValue2} )

It is also possible to fail an expression if an old version of EEL is being used

    $( eel.version() >= 99.9 ? 0 : fail("Invalid EEL Version") )


## Functions that return default values

Some of the [Eel functions](#eel-functions) allow the expression to pass a default values that is returned
if the function is unable to perform its normal operation.

For example `indexOf( text, subString { , defaultFunc } )` will return -1 if the search string is not present in the 
text. So

    $indexOf( 'abcdef', 'z' ) 

will return -1 because _z_ is not in  `abcdef`. Reading the [Result](Using%20EEL.md#result) object as a logic value will return _false_ 
to indicate that the search text was not found.

If the client requires a different value if the file cannot be found, then this can be set by providing the default as
a function argument. For example:

    $indexOf( 'abcdef', 'z', 0 ) 

will return 0 instead. If there is no acceptable default value, then it is also possible to pass the `fail()` function
as an argument. The function will only be evaluated if the default is required. For example:

    $indexOf( 'abcdef', 'z', fail() )

This will cause an [EelFailException](Using%20EEL.md#exceptions) to be thrown. 
By default, the message generated by `fail()` doesn't provide much context, so a more helpful example could be: 

    $indexOf( 'abcdef', 'z', fail('There is no z') )

A less dramatic version of the expression could use the logging functions to warn the client of an issue, but still 
return a default value. For example:

    $indexOf('abcdef', 'z', log.warn('There is no z, returning {}', 0) )

Finally, it's worth noting that:

    $indexOf( 'abcdef', 'd', fail() )

will return _3_, which is the index of 'd'. `fail()` will not be evaluated and no exception is thrown. 


## Dates operations

Dates can be manipulated using the numeric operators. This works because as EEL will automatically convert the Dates
into the number of seconds elapsed since 1970-01-01 00:00:00 in the UTC time zone. 

For example:

    $( date.utc() + 5 )

will add 5 seconds to the current UTC date. In the same way, the minus operator can be used to calculate time 
differences. For example:

    $( date.utc() - date.start() )

Will return a [Result](Using%20EEL.md#result) object that will have a type of number and a value that is the age, in seconds, of the
[Context](Using%20EEL.md#eel-context).

If seconds are too fine-grained, then a time difference can be returned in minutes by using:

    $duration( date.start(), date.utc(), "minutes" )

Building on this, an expression to check if a file is out of date could look something like:

    $( duration( modifiedAt( ${myFile} ), date.local(), "months" ) > 6 )

In this case the Result object will have a type of logic and a value that is `true` only if the referenced file is
more than 6 months old.



