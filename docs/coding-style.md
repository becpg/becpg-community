# Coding Style Guide (Java 17)

This document describes the **coding conventions** for this project.  
The goals are **clarity**, **maintainability**, and **consistency** across the codebase.


## 1. Java Version
- Always use **Java 17**.
- Leverage Java 17 features:
  - Pattern matching for `instanceof`
  - Switch expressions
  - Text blocks
  - Records, sealed classes (when appropriate)

✅ Example:

```java
if (object instanceof Integer i) {
    return i.intValue();
}

int i = switch (j) {
    case 1 -> 3;
    case 2 -> 4;
    default -> 0;
};
```

## 2. Variable Declaration

* **Do not use `var`**.
* Explicitly declare types for readability.

✅ Example:

```java
int number = 0;
ArrayList<String> list = new ArrayList<String>();
HashMap<Integer, String> map = new HashMap<>();
```

## 3. Lambdas and Functional Interfaces

* Use **lambdas** instead of anonymous classes when possible.
* Prefer **method references** when they improve readability.

✅ Examples:

```java
IntConsumer c = System.out::println;
Runnable r = () -> { /* do something */ };

Comparator<Date> comparator =
    Comparator.nullsFirst(Comparator.comparing(Date::toString));
```


## 4. Streams

* Use Streams **sparingly**:

  * Allowed for **simple, readable pipelines**.
  * Avoid nested or overly complex stream logic.
* Prefer **`for` loops** when logic is complex.

✅ Example:

```java
for (int id : ids) {
    double value = id / 2;
    System.out.println(value);
}

for (int i = 0; i < ids.length; i++) {
    System.out.println("here");
}
```

## 5. Strings

* Use **`String.join`** instead of manual concatenation in loops.
* Use **text blocks** for multi-line strings.

✅ Examples:

```java
String concatenation = String.join(", ", texts);

String buf = """
    public class A {
        public void foo() {
        }
    }
    """;
```

## 6. Exception Handling

* Use **multi-catch** when it makes the code cleaner.
* Keep exception blocks short and meaningful.

✅ Example:

```java
try {
    obj.throwingMethod();
} catch (IllegalArgumentException | IOException ioe) {
    ioe.printStackTrace();
}
```

## 7. Resource Management

* Always use **try-with-resources** when working with `AutoCloseable`.

✅ Example:

```java
final FileInputStream inputStream = new FileInputStream("out.txt");
try (inputStream) {
    System.out.println(inputStream.read());
}
```

## 8. Objects and Comparisons

* Use `Objects.equals` for null-safe comparisons.
* Use `Objects.hash` when implementing `hashCode`.

✅ Examples:

```java
return Objects.hash(aShort);

if (!Objects.equals(aText, other.aText)) {
    return false;
}
```

## 9. Wrapper Types

* Avoid unnecessary boxing/unboxing.
* Prefer constants instead of manual `valueOf`.

✅ Example:

```java
Integer integerObject = Integer.MAX_VALUE;
Character cObject = Character.MAX_VALUE;

int i = integerObject.intValue();
char c = cObject.charValue();
```


## 10. Pattern Matching

* Use **pattern matching** to simplify type checks and casts.

✅ Example:

```java
if (x instanceof Integer xInt) {
    i = xInt.intValue();
} else if (x instanceof Double xDouble) {
    d = xDouble.doubleValue();
} else if (x instanceof Boolean xBoolean) {
    b = xBoolean.booleanValue();
} else {
    i = 0;
    d = 0.0D;
    b = false;
}
```


## 11. General Style Rules

* Follow standard **Java naming conventions** (CamelCase, PascalCase for classes, UPPER\_CASE for constants).
* **Braces on a new line** for classes and methods.
* **4 spaces indentation**, no tabs.
* Keep methods **short and focused** (≤ 30 lines recommended).
* Prioritize **readability over cleverness**.
* Minimize external libraries:
  - Prefer JDK APIs and existing internal utilities first.
  - Add a dependency only with a clear, documented benefit (correctness, performance, or significant productivity).
  - Avoid bringing in libraries for trivial helpers (strings, collections, simple I/O).
  - Choose small, well-maintained libraries; avoid heavy frameworks unless strongly justified.

## 12. Source Code Formatting and Tooling

### Java Formatting

Before being committed, a Java class must be cleaned up:

1. Right-click > Source > Clean Up
2. No comments in French, remove Eclipse warnings, no `System.out` or unnecessary logs. Follow the guidelines of SonarLint.

#### Configuration Settings

* **Preferences > Java > Code Style > Import :** `becpg_cleanup.xml`
* **Preferences > Java > Formatter > Import :** `becpg_formatter.xml`

#### Naming Conventions

Variable and method names should adhere to the conventions outlined in the [Google Java Style Guide - Naming](https://google.github.io/styleguide/javaguide.html#s5-naming).

### XML Formatting

Use the configured XML formatting options in your IDE.

### JavaScript Formatting

**Web -> Client-side JavaScript -> Formatter -> beCPG [built-in]**

### Internationalization (I18n)

Follow the configured I18n formatting standards for consistent localization file formatting.

---

✦ All contributors are expected to follow this guide when writing or reviewing code.
