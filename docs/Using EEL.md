# Using EEL

The source code contains three modules. These are:

* **lib** - the EEL compiler and runtime. This describes how to compile and execute EEL expressions from Java
* **integration** - helper classes that can be used to integrate EEL with JVM applications
* **evaluate** - EEL Command Line Interface 

As of version 2.0, it is possible to query the version of EEL that's running.  

- **EEL expressions** - The function `eel.version()` will return the version as Text. 
- **Evaluate** - run the _evaluate_ JAR with the command line argument `--version`
- **Java client API** - call `Eel.metadata().version()` 
- **UDF implementers** - ensure that one of the parameters passed to the function is of type `EelContext` and 
 then call `context.metadata().version()`

The version is always a pair of numbers separated by a decimal point (`.`), which means that the version text can
be converted to a number.

---

# 'lib' - The EEL Language and its Java API

The API for EEL is based around four components. These are:

## EEL Context

The EEL Context manages the compile time settings and any state information. An EelContext can be used in multiple 
EEL Expressions.

The EEL Context can:
* Return information about the current version of EEL 
* Set the precision for maths operations
* Set the first day of a week and the minimum number of days in the first week of a year for use in date-time operations
* Import **U**ser **D**efined **F**unctions ([UDFs](User%20Defined%20Functions.md)).
* Guard against rogue expressions causing Denial Of Service (DOS) attacks by:
  * setting the maximum length of the expression in characters
  * setting a timeout for evaluating the expression
  * set a limit on the amount of data that can be read from the filesystem by each function

In addition, the Context also manages state shared across invocations. This includes
* the time the Context was created 
* any EEL function-specific data, such as the values returned by `count()` 

All the Context settings have defaults; if these are acceptable then the Default EelContext can be used.

EelContext objects are built using a fluent API; the entry point is `EelContext.factory()` 
 

## EEL Expression

This is where the source expression is compiled. Like any other program, a compiled expression can be executed multiple
times. This could be useful if an expression makes use of functions that return different values each time 
the expression is evaluated, for example, reading the current date/time, examining the file system, generating random 
numbers/text or reading a counter. 
Alternatively, the expression could be evaluated with a different SymbolsTable (see below) which could also cause 
EEL to return a different [Result](#result)

EEL Expressions are built using a fluent API; the entry point is `Eel.factory()`. In addition, there are
convenience methods in `Eel` that can be used to compile expressions with the default Eel Context.


## Symbols Table

The SymbolsTable is a lookup mechanism that maps a name in an expression to a value that is provided at runtime.
These values can come from any, all or none of the following sources:
- The environment variables
- The JVM properties
- One or more `java.util.Map` objects 
- One or more Java lambda functions 
- A single hardcoded string which is associated with **all** names. This is usually used to set a default value. This
 has to be the last data source added to the SymbolsTable.

If the client needs to read a key from multiple sources without risking a name clash the SymbolsTable can be 
created with named _"scopes"_. When the SymbolsTable is created each data source is defined with unique scope name. 
To read the data, the EEL expression prefixes the key with the scope name and a delimiter.

If there are no scopes defined and a key exists in multiple sources, then the first source added to the SymbolsTable
takes priority. 

Once defined, a SymbolsTable can be used to evaluate multiple EEL Expressions. However, there is no requirement that
an EEL Expression has to use a SymbolsTable when it is evaluated.      

SymbolsTable objects are built using a fluent API; the entry point is `SymbolsTable.factory()`. In addition, there are 
convenience methods in:
- `SymbolsTable` - used to build a SymbolsTable that reads from a single data source
- `Eel` - used to evaluate a compiled expression with a one-off SymbolsTable that reads from a single data source

EEL always reads values from the SymbolsTable as Text; however, in most cases it will correctly convert the value to the 
type required by the operator that uses it. If this is insufficient then explicit conversion functions can be used to
ensure the correct [Data type](The%20EEL%20Language.md#data-types)

## Result

A Result object represents the output from an EEL Expression. Each Result contains:
- The type of the evaluated value (see [Data types](The%20EEL%20Language.md#data-types)
- The evaluated value which the client can read via a type-specific getter method. If the getter method doesn't
match the Result type, then the Result will [convert it](The%20EEL%20Language.md#types-conversions). 


## Exceptions
The EEL compiler and runtime can throw the following Exceptions:

| Exception                   | Purpose                                           | Extends              |
|-----------------------------|---------------------------------------------------|----------------------|
| EelException                | Base class for all EEL exceptions                 | RuntimeException     |
| EelInternalException        | The EEL core failed. This should never be seen    | EelException         |
|                             |                                                   |                      |
| EelCompileException         | Base class for all compile time exceptions        | EelException         |
| EelSourceException          | The expression source could not be read           | EelCompileException  |
| EelSyntaxException          | The source expression has a syntax error          | EelCompileException  |
| EelSemanticException        | The source expression has a semantic error        | EelCompileException  |
| EelSymbolsTableException    | The SymbolsTable is misconfigured                 | EelCompileException  |
| EelUnknownFunctionException | The expression calls an unknown function          | EelCompileException  |
|                             |                                                   |                      |
| EelRuntimeException         | Base class for all runtime exceptions             | EelException         |
| EelInterruptedException     | The evaluating thread was interrupted             | EelRuntimeException  | 
| EelTimeoutException         | The expression took too long to evaluate          | EelRuntimeException  | 
| EelUnknownSymbolException   | The SymbolsTable did not contain a required value | EelRuntimeException  |
| EelConvertException         | An EEL type conversion failed                     | EelRuntimeException  |
| EelFunctionException        | A function failed or could not be invoked         | EelRuntimeException  |
| EelFailException            | The expression executed the `fail()` function     | EelRuntimeException  | 

**Note:** All of these exceptions are unchecked.

## Example Java code

**Basic Usage**

In its simplest form, EEL can be invoked as:

    String result = Eel.compile(  ...some expression...  )
      .evaluate()
      .asText();

In this example the default EelContext is used to compile the expression, it is executed without a reference to a 
SymbolsTable, and the result is returned as String. 
If this is enough for the client application, then nothing more complex is required.

**Inline EelContext**

    String result = Eel.factory()
      .withPrecision(12)
      .compile(  ...some expression...  )
      .evaluate()
      .asText();

In this example the fluent API provided by `Eel.factory()` creates a new EelContext as the Expression is compiled;
in this case to set the maths precision.

Inlining the Context makes for concise code but does not allow the EelContext to be reused.

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
in this case the data comes from a map of values, however `evaluate` is overloaded to read from all the other supported 
sources.

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
providing a default value if an identifier is undefined, the scope delimiter can be set, and the SymbolsTable can be
reused in as many calls to `Eel.evaluate()` as needed.
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
data to the required type. As all data can be represented as text, it will always be possible to read the result using
`asText()`. The rules for converting values are described in the [Data types](The%20EEL%20Language.md#data-types)
section of this document.



---	
# 'integration' - Using EEL with JVM applications

The integration module provides some convenience classes that can be used to integrate EEL Expressions into other
applications. 

## EelProperties

This is a simple utility class that allows a Java application to read a properties file that contains EEL Expressions.
These expressions will be evaluated as the properties file is read. For example, if there were a properties file that
contained:

    root=/some/path
    config=${root}/config
    log=${root}/log/$format.local("yyyy/MM/dd/")

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

# 'evaluate' - Developing EEL Expressions

To help write and validate EEL Expressions users can use the **evaluate** application. This is a self-contained
JAR that wraps the EEL library in a CLI application. 

Evaluate can be invoked from the top-level directory as:

    java -jar evaluate/target/evaluate-<version>.jar --help

This will display a help page showing all the options and flags. To evaluate an expression, use:

    java -jar evaluate/target/evaluate-<version>.jar 'Hello'

which will write `Hello` to standard out. 

    java -jar evaluate/target/evaluate-<version>.jar --env 'Your HOME directory is ${HOME}'

will add all the environment variables to the SymbolsTable and use one of them in the expression. 

To add [UDF](User%20Defined%20Functions.md) classes and packages ensure the implementing classes are on the classpath
and execute **evaluate** using the `--udf-class` and `--udf-package` options.


---
# External Libraries

EEL was implemented using as few third-party libraries as possible to prevent the client code from bloating. 
The dependant libraries are:

* **org.reflections:reflections** - used to scan for packages of [UDF](User%20Defined%20Functions.md) functions
* **ch.obermuhlner:big-math** - implementation for the maths operations and functions
* **org.slf4j:slf4j-api** - the logging facade
* **com.google.code.findbugs:jsr305** - used internally for tracking null/non-null object references
* **com.github.spotbugs:spotbugs-annotations** - used internally for bug checking annotations

Evaluate has one additional dependency:
* **args4j:args4j** - Command line argument parsing
