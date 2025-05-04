# User Defined Functions (UDFs) 

**U**ser **D**efined **F**unctions (UDFs) are the way that EEL is extended. While there are no particular limits on
what the UDF can do, it is very strongly recommended that the implementations follow the [EEL design requirements](../README.md#design-requirements).
Specifically:
* Expressions, including function calls, must evaluate quickly
* The language, and its functions must be secure; must always complete, avoid injecting misleading messages into the logs, 
block access sensitive parts of the file system or not cause external side effects.
* The language must be [lazy](The%20EEL%20Language.md#lazy-processing)

# UDF Requirements
A valid UDF must meet the following requirements:
* The implementing class must have a public no argument constructor
* Each implementing method must be public
* Each implementing method must be annotated with `com.github.tymefly.eel.udf.EelFunction` 
* The name given in the `EelFunction` annotation must be a valid [function name](#function-names)
* Each implementing method must return one of the following types:
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
  * `com.github.tymefly.eel.Value`
* `void` is not a supported return type
* Each of the arguments passed to the implementing method must be one of:
  * the types that can be returned
  * `java.io.File`
  * VarArgs for one of the previous types 
  * `com.github.tymefly.eel.EelContext`
  * `com.github.tymefly.eel.udf.FunctionalResource`
* The implementing method must not return `null`
* The implementing class must not maintain state. (See [Stateful functions](#stateful-functions) for an alternative) 

## Function names

The value in the `EelFunction` annotation provides the name EEL uses to call the functions. Names are written in the form
`prefix.functionName`. The rules for function names are:

1. The name of the function must be a value [EEL Identifier](The%20EEL%20Language.md#identifiers)
2. The function name must have at least one dot (`.`) delimited prefix.
3. The prefix must not be one of the reserved prefixes. See [Standard Functions](The%20EEL%20Language.md#function-prefixes) 
4. The function name must be unique. EEL functions cannot be overloaded or overridden.  

Because UDFs must have at least one dot (`.`) delimited prefix UDF names are never valid Java identifiers.


## Security
The implementing function must return quickly to prevent potential DOS attacks; the [EelContext](Using%20EEL.md#eel-context) determines the
maximum time for evaluating the complete expression, which includes the time taken executing the UDF. 

Before calling the UDF, EEL will check `File` parameters to ensure they do not reference sensitive parts of the local
file system. It is therefore recommended that UDF functions accept `File` parameters instead of file names. If the 
parameter also needs to be [lazy](The%20EEL%20Language.md#lazy-processing), then declare it as a `Value` and
the access the File by calling `Value.asFile()`

The [EelContext](Using%20EEL.md#eel-context) determines the maximum number of bytes that can be read from an external source such as the
file system. Functions that read more than this limit must fail with an IOException. This is most easily implemented
by using `com.github.tymefly.eel.udf.LimitedInputStream` to read files.


## Laziness
As EEL is a [lazy](The%20EEL%20Language.md#lazy-processing) language, the runtime will avoid evaluating terms in the 
expression until they are actually needed. Unfortunately, in most datatypes require EEL to evaluate the parameters 
before they can be passed to the UDF. This could cause a problem if:

* The parameter is a complex expression or a look back
* The parameter is a function that has a side effected, such as `count()`,`fail()` or one of the logging functions.

It is therefore recommended that:

* parameters that are guaranteed to be used by the UDF can be of any type 
* parameters that might not be used by the UDF are of type `com.github.tymefly.eel.Value`, as the parameter will
not be evaluated until the first accessor method is called.

One common use for `com.github.tymefly.eel.Value` parameters is implementing functions like `indexOf()`, which 
have a parameter that is used to pass a value that is returned if the function fails. 
If there is no acceptable value to return then the EEL function might pass `fail()` to terminate the expression. It is
therefore vital that this parameter is not evaluated until the function fails.  


# Implementing a UDF
## General notes
- The implementing class can define more than one UDF. 
- EEL functions cannot be overloaded. However, functions can use default arguments and variable length argument lists 
  to achieve similar results.
- Because the language is null hostile it is guaranteed that none of the arguments passed to a UDF are `null`. However, 
it is also required that the returned value must not be `null`
- Functions that return a 'non-value' should default to something that can be [converted to 'false'](The%20EEL%20Language.md#types-conversions).
  * For Text values this is the empty string
  * For Number values this is zero or a negative value, usually `-1`
  * For Date values this is a time at or before `1970-01-01 00:00:00Z`. For convenience the constant`EelContext.FALSE_DATE` 
    can be used.
- Functions that return a 'non-value' should consider allowing the caller to pass a custom value, a logging function,  
`fail()` or some other term that will be evaluated when the non-value is required. This parameter should be [Lazy](#laziness)
and is usually [defaulted](#default-arguments) to a ['false' value](The%20EEL%20Language.md#types-conversions).


## Default arguments
EEL allows functions to be called with default arguments. 
The default value is set by annotating the Java argument with `com.github.tymefly.eel.udf.DefaultArgument`.
For example:

    @EelFunction("my.random")
    public int random(int min, @DefaultArgument("99") int max) {
       // implement me
    }

Default arguments can only be defined after all the non-default arguments.
 

## Exception handling
If a UDF fails it can throw either a checked or an unchecked Exception. 

EEL will catch all exceptions thrown by the UDF and wrap them into an [EelFunctionException](Using%20EEL.md#exceptions) that contains
some additional context information. This exception will then be thrown back to the client application.


## Stateful functions
Because the EEL runtime may reuse instances of UDFs, they **must** be stateless. 

In the very rare occasions that a function has to be stateful then EEL can manage the state for the function.
This is achieved by declaring the UDF function with an additional parameter of type `FunctionalResource`. This is an 
EEL aware object used to manage shared state. For example:

    class DTO {
        DTO(String name) {
        }

        // Define fields and accessor methods.
    }


    @EelFunction(name = "my.stateful")
    public String stateful(FunctionalResource functionalResource) {
        DTO myDto = functionalResource.getResource("myName", DTO::new);

       // use DTO
    }

The parameters passed to `getResource` are:
1. **_name_**: The name of the resource. UDFs can more than one stateful object, so long as they have unique names.
2. **_supplier-function_**: A function that is used to return a new instance of a stateful object. This is usually a 
constructor or a factory method. Eel will pass the name of the resource to this function as a String

The first time `getResource` is called for the named resource the `supplier-function` will be called to create the 
stateful object. For subsequent invocations the existing object will be returned.

**Important Note:** As the returned object is a shared resource, the UDF is responsible for synchronising access to it
if it is going to be used in a multithreaded environment.  

The FunctionalResource is associated with both the [EelContext](Using%20EEL.md#eel-context) and the implementing class. Consequently:
 * If the EEL expression is recompiled with a new Context then new stateful objects will be allocated. 
 * If another EEL expression is compiled with the same Context then the stateful object will be shared.
 * If the implementing class implements more than one UDF, the stateful object can be shared between the UDFs
 * UDFs that are in different implementing classes cannot interfere with each other's stateful objects, even if they 
have the same type and/or name 

    
## Sample UDF 

The Java code for a UDF could be as simple as:

    import com.github.tymefly.eel.udf.EelFunction; 

    public class Half {
        @EelFunction("divide.by2")
        public int half(int value) {
            return value / 2;
        }
    }

This function meets all the requirements for a UDF; 'divide.by2' is a valid EEL name, the parameters and return type
are valid types, it executes quickly, and it meets the security requirements. 

After it's been [registered](#the-client-java-api), an EEL expression can call this function as:

    $divide.by2( 1234 )

  

# The client Java API
There are two ways to make EEL aware of a UDF; both are via the [EelContext](Using%20EEL.md#eel-context).

## Registering UDF classes

The simplest way to register UDFs is to add them on a class-by-class basis. For example:

    EelContext context = EelContext.factory()
        .withUdfClass(MyClass1.class)
        .withUdfClass(MyClass2.class)
        .build();

## Registering UDF packages

Because it can get repetitive registering every class, EEL provides a way to register all the UDF classes in a Java
package in one go. First annotate each UDF class with 
`com.github.tymefly.eel.udf.PackagedEelFunction`.

For example:

    import com.github.tymefly.eel.udf.PackagedEelFunction; 

    @PackagedEelFunction
    public class MyClass1 {

Then create the Context using:

    EelContext context = EelContext.factory()
        .withUdfPackage(MyClass1.class.getPackage())    // Any of the classes in the package could have been used
        .build();

**Note:** Child packages are not automatically added. If a child package is also required then it must be added
with an additional call to `withUdfPackage()`   

