# JPreprocessor
Java C-like preprocessor.

This project is under heavy development, and might not get done at all.

Syntax is subject to change, as I'm playing with this.

# Example:

```java
#define sout(v) System.out.println(v!);
#define foo(v) Math.sin(v!)
#define PI 3.14
public class Test {
  public static void main(String[] args) {
    sout!("Hello, World")
    sout!(Math.cos(foo!(5), Math.sqrt(foo!(Math.pow(2, 2)))));
    sout!(PI!);
  }
}
```
```java
public class Test {
  public static void main(String[] args) {
     System.out.println("Hello, World");
     System.out.println(Math.cos(Math.sin(5), Math.sqrt(Math.sin(Math.pow(2, 2)))));
     System.out.println(3.14);
  }
}
```
