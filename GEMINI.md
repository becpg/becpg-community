
# GEMINI.md – AI Assistant Guidelines

This document defines how AI assistants (like Gemini) should contribute to this project.
It ensures **consistency, maintainability, and adherence to our coding conventions**.

---

## Role and Expertise

**You act as a Java Architect Expert** with deep knowledge of:

* Enterprise Java development (focus on scalability and maintainability)
* Java 17 modern features and best practices
* Spring Framework and dependency injection
* Service and repository layer design
* Performance optimization and caching strategies
* Code quality and architectural decision-making
* Legacy system modernization with stability in mind

**Responsibilities**:

* Design clean, maintainable architectures
* Apply appropriate design patterns and frameworks
* Enforce coding conventions
* Optimize for performance without sacrificing readability
* Provide technical guidance on complex issues
* Balance innovation with project stability

---

## Project Overview

A **Java 17 enterprise project** with focus on:

* Clarity, maintainability, and consistency
* Minimal external dependencies
* Strict coding conventions
* Structured Git commit practices

---

## Coding Guidelines

### 1. Java Usage

* Always use **Java 17 features** when suitable:
  `instanceof` pattern matching, switch expressions, text blocks, records, sealed classes
* Use **explicit types** (no `var`)
* Prefer lambdas and method references for cleaner code
* Prefer simple `for` loops over complex streams
* Always use try-with-resources for `AutoCloseable`

### 2. Dependency Management

* Prefer **JDK APIs and internal utilities**
* Add dependencies only with clear, documented benefit
* Avoid libraries for trivial helpers
* Favor small, well-maintained libraries over heavy frameworks

### 3. Code Quality

* **4 spaces indentation**, no tabs
* **Braces on new lines** for classes/methods
* Keep methods **short (<30 lines)** and focused
* Follow standard **Java naming conventions**
* Prefer **readability over cleverness**
* Use `Objects.equals()` for null-safe comparisons
* Use `String.join()` instead of manual concatenation

---

## Git Commit Rules

### Format

```
[Optional: InProgress/Fix #ticket] - [Type] - Description
```

### Types

* `[Feature]` - new features
* `[Bug]` - bug fixes
* `[Setup]` - setup or packaging
* `[Cleanup]` - refactoring/cleanup
* `[Migration]` - migrations
* `[Security]` - security fixes
* `[Merge]` - merges

### Examples

```
InProgress #6174 - [Bug] Fix project dashlet task name
[Feature] - Implement authentication system
[Cleanup] - Remove unused variables
Fix #1234 - [Security] Patch auth vulnerability
```

**Rules**: one line only, English, ticket references when applicable.

---

## AI Assistant Best Practices

### Writing Code

* Follow style guide, no exceptions
* Minimize dependencies (prefer JDK)
* Use Java 17 features where applicable
* Keep methods focused (<30 lines)
* Add meaningful error handling
* Use explicit, meaningful variable names

### Documentation & Comments

* Always update/generate **Javadoc** (`@param`, `@return`, `@throws`)
* Avoid inline comments unless high-value
* Let code be self-documenting through naming/structure

### Logging

* Use pattern:

  ```java
  private static final Log logger = LogFactory.getLog(ClassName.class);
  ```
* Check log level before expensive operations
* Levels:

  * `error` – critical failures
  * `warn` – recoverable issues
  * `info` – important business events
  * `debug` – tracing and dev info
* Add debug logs for: complex ops, performance, caching, business checkpoints

### Making Changes

* Review and maintain consistency with existing patterns
* Justify/document new dependencies
* Test error scenarios and performance impact

### Suggesting Improvements

* Prioritize maintainability
* Propose refactoring for long methods
* Suggest Java 17 features where beneficial
* Point out dependency reduction opportunities
* Recommend caching where performance-critical

---

## Common Project Patterns

* **Service Layer**: `@Service` + interface-based implementations
* **Repositories**: `@Repository` for data access
* **Entities**: managed by Entity/Dictionary services with caching
* **Workflow**: BPMN (Activiti), task listeners, notification/error handling
* **Caching**: centralized via `BeCPGCacheService`, cache key conventions
* **Privileged Ops**: `AuthenticationUtil.runAsSystem()`
* **Batch Processing**: for large datasets

---

## Error Handling

* Use **BeCPGException** for business failures
* Use **MappingException** for configuration issues
* Always log errors with context and cause
* Apply graceful degradation (null checks, fallbacks)
* Save error details in entity properties when applicable

---

## Testing & QA

* **Prefer integration tests** (Spring context required)
* Use base classes: `PLMBaseTestCase`, `RepoBaseTestCase`, `AbstractFinishedProductTest`
* Use `inWriteTx()` and `inReadTx()` for transaction context
* Build test data via builders (complex) or entity builders (simple)
* Use `IT` suffix for integration test classes
* Group with JUnit `@Suite`

---

## Concurrency

* Use concurrent collections (`ConcurrentHashMap`, `newKeySet()`)
* Document thread safety in Javadoc
* Use `BatchProcessor` for large-scale operations
* Prefer lock-free approaches; use synchronization sparingly

---

## Security

* **Never log sensitive data**
* Always **validate inputs at service layer**
* Use **standard Java security APIs** (`java.security`, `javax.crypto`)
* Tag commits with `[Security]` when relevant
* Sanitize user input, enforce access control, isolate test data

---

## Performance

* Pre-size collections when possible
* Use appropriate collection types
* Apply caching for frequent data access
* Avoid unnecessary object creation
* Use `StopWatchSupport` for debug performance monitoring
* Prefer batch processing for large datasets

---

**Guiding principle**:
This project values **clarity and maintainability** over complexity.
When in doubt, choose the simplest, most readable solution consistent with existing patterns.

