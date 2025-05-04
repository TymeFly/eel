# EEL
EEL is a small, compiled, **E**xtensible **E**xpression **L**anguage which can be used by a JVM application to evaluate 
expressions at run time. EEL can be used to configure applications in a dynamic and flexible way.

---
# Overview

There is a common requirement when developing systems to make certain values configurable. This is traditionally
achieved by reading values as-is from the environment variables, JVM properties, databases, or properties files. 
These solutions work well if nothing more sophisticated than simple text substitution is required, the exact values
are known at deployment time, and none of the values are guaranteed to be related. However, sometimes something more
sophisticated is needed. 
For example, an application might want to write data to a file were the path is based on the current date or time. 
On other occasions, multiple settings may need to be combined, for example, to build up a set of directories that have
a common root. 

Dates in particular are often problematic as developers and/or end users can have different ideas
about how to format dates based on their own cultural preference - of course they are all wrong, the correct
way is always ISO-8601 ;^)

To solve this problem, a "_Pragmatic Programmer_" would consider using a Domain-Specific Language to derive these
values at run time, and that's exactly what the **E**xtensible **E**xpression **L**anguage (EEL) is.
The strings previously used to configure an application can be treated as EEL Expressions that are simple yet powerful
and are quickly evaluated at run time.

---
# Design Requirements

The requirements for EEL are:
* Expressions that are just strings can be passed through as-is. This makes it possible to take settings
  that were previously hardcoded strings and use them as EEL Expressions without change.
* Expressions, including function calls, must evaluate quickly
* EEL must be easy to use, especially by somebody with a programming background
* The Java Programming API should be simple and concise.
* The language must include all the usual text, maths and logic operations that languages typically support.
* The language must be able to manipulate date/time stamps.
* The language must support logging so clients can see what is being evaluated for security and/or debugging purposes.
* The language must be _Null Hostile_ to avoid NullPointerExceptions.  
* The language and its functions must be secure:
  * It must be impossible to write an expression that will not complete at compile time or run time.
  * It must not be possible to inject misleading messages into the system logs. 
  * It must not access sensitive parts of the local file system.
  * Except for logging, expressions are executed without _external_ side effects.
* The language must be extensible - it should be easy to add additional functions without needing to update EEL.
* The language must be [lazy](docs/The%20EEL%20Language.md#lazy-processing) - if parts of the expression do not need to be evaluated, they won't be.

**Note:** There is no requirement that EEL be Turing complete; while there is support for conditions, neither iteration
nor recursion is supported 

---
# Documentation
* [Using EEL](docs/Using%20EEL.md) - Describes how to integrate EEL into an application and the EEL CLI  
* [EEL Language](docs/The%20EEL%20Language.md) - Describes the EEL language
* [User Defined Functions](docs/User%20Defined%20Functions.md) - Describes how to extend EEL with additional functions
* [What's new](docs/WhatsNew.md) - Describes what is new in each release
 
---
# Building EEL

EEL requires:
* Java 17+
* Maven 3.6+
