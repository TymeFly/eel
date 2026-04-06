# EEL
EEL is a small, compiled, **E**xtensible **E**xpression **L**anguage for JVM applications.  
It enables dynamic, flexible configuration by evaluating expressions at run time.

---
# Overview

Many systems need configurable values. Traditionally, these come from environment variables, JVM properties, databases, 
or property files. These solutions work well when values are known at deployment and unrelated, but sometimes, 
more sophisticated logic is needed.  

For example, an application might need to generate file paths based on the current date and/or time. On other occasions,
it might need to combine multiple settings, such as building several directories that share a common root.  

Dates are especially tricky: developers/users can have different formatting preferences based on personal or
cultural preferences - of course they are all wrong, the correct way is always ISO-8601 ;^)

A "_Pragmatic Programmer_" would solve this with a Domain-Specific Language, and that's exactly what the 
**E**xtensible **E**xpression **L**anguage (EEL) is. 


EEL allows configuration strings to be treated as expressions that are simple, powerful, and evaluated quickly at run time.

---
# Design Requirements

The requirements for EEL are: 
* Simple strings can be passed through without change, enabling existing hardcoded settings to become EEL expressions.  
* The Programming API must be simple and intuitive.
* The EEL language must be easy to use by somebody with a programming background
* The EEL language must support usual text, maths, and logic operations.  
* Date/time manipulation must also be supported.  
* Logging must be available for security, to monitor evaluations, and debugging.  
* Expressions, including function calls, must evaluate quickly.  
* The language must be [lazy](docs/The%20EEL%20Language.md#lazy-processing) - if parts of the expression do not need to be evaluated, they won't be.
* The language must be extensible, allowing additional functions to be added without changing EEL itself.
* EEL is _Null Hostile_ to prevent NullPointerExceptions.  
* The language and its functions must be secure:
  * EEL must guard against DOS attacks
  * Log injection attacks must be impossible.  
  * Expressions must not be able to access to sensitive parts of the local file system. 
  * Expressions execute without _external_ side effects, except for logging.  

**Note:** EEL is not Turing complete; conditions are supported, but iteration and recursion are not.

---
# Documentation
* [Using EEL](docs/Using%20EEL.md) — Integration guide and CLI usage  
* [EEL Language](docs/The%20EEL%20Language.md) — EEL Language specification  
* [EEL Function Reference Manual](docs/function-reference/index.html) — Standard EEL functions  
* [User Defined Functions](docs/User%20Defined%20Functions.md) — Extending EEL with custom functions  
* [What's New](docs/WhatsNew.md) — Release highlights  

---
# Building EEL

Requirements:  
* Java 17+  
* Maven 3.6+