API Design Choices
==================

1. To the extent possible, member variables should be final. Immutability is also desired, although
in many cases performance dictates that only limited immutability is reasonable. An example of this 
is that an image's dimensions will not change, the data reference will not change, but the content
of the data is still mutable.
1. Data and functionality should be separated as cleanly and reasonably as possible, although it is
not required to go so far as POJOs and static functions. This tends to simplify the type hierarchy
and encourages functional interfaces for complex logic that can then be incorporated into more 
conventional OO-types by using has-a relationships instead of is-a.
1. Colors and pixels would ideally be value types but because Java does not yet support them, 
compromises are made in the API. Namely, these value instances often are included as a result
argument for a function instead of the return type. Alternatively, a reused instance is used
internally by some longer-lived processor, effectively using the instances in the flyweight pattern.
1. Arguments are validated when the values' correctness is not immediately checked or tested by 
another function or implicit aspect of Java. Implicit validation can occur when an argument is 
required to be non-null, and a side-effect free member is accessed; it can occur with array access 
or creation as well where the index is validated. The important thing is that all arguments must
be explicitly or implicitly validated before any side effects of the function can occur. 
1. Argument validation for internal and low-level classes can be eschewed for performance reasons if
unit tests assure they are invoked properly by higher-level APIs that do validate arguments. This is
primarily for performance reasons and to avoid redundant checks and assertions.
1. For complex type creation, the builder pattern is preferred so that a fluent API can be used.
Internal, or very low-level, types do not require the builder pattern unless it happens to greatly
improve code readability.
1. Utility functions that operate on objects should be grouped into related utility classes that
are final, have a private constructor, and only expose static methods. Since they are utilities, 
there is no need to develop an interface and type hierarchy around them. This is in contrast to,
as an example, the design of the I/O package that does provide a set of interfaces that different
image formats implement.
1. Object arguments should be assumed not nullable unless appropriately annotated or documented.
1. For simplicity in validation, it can be assumed that enum arguments are never null.

Code Style
==========

1. Use spaces instead of tabs. Indentation width is set to 2 spaces.
1. IntelliJ's autoformatting and organizing is required for final code release.