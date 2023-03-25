# JPreprocessor
Java C-like preprocessor.

This project is under heavy development, and might not get done at all.

Syntax is subject to change, as I'm playing with this.

# Example:

```java
define! listOf(...) {
    write(`Arrays.asList(${va_args.join(',')})`)
}
define! sout(value) {
    write(`System.out.println(${value});`)
}
public class Test {
  public static void main(String[] args) {
      sout!(listOf!(1,2,3,4)) // System.out.println(Arrays.asList(1,2,3,4))
  }
}
```

