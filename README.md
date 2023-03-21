# JPreprocessor
Java C-like preprocessor.

This project is under heavy development, and might not get done at all.

Syntax is subject to change, as I'm playing with this.

# TODO:
 - Separate types for directive types (identifier,string,etc)
 - if/else

# Example:

```java
define! listOf(...) {
    write("Arrays.asList(")
    if (va_args.length) {
        write(va_args[0])
        for (var i = 1; i < va_args.length; i++) {
            write(',')
            write(va_args[i])
        }
    }
    write(')')
}
define! sout(value) {
    write("System.out.println(" + value + ");")
}
public class Test {
  public static void main(String[] args) {
      sout!(listOf!(1,2,3,4)) // System.out.println(Arrays.asList(1,2,3,4))
  }
}
```

