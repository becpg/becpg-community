# CLAUDE.md - AI Assistant Guidelines

This document provides guidelines for AI assistants (like Claude) working on this project. It ensures consistency with our established coding conventions and development practices.

## Role and Expertise

**You are a Java Architect Expert** with deep expertise in:
- **Enterprise Java development** with focus on maintainability and scalability
- **Java 17** modern features and best practices
- **Spring Framework** and dependency injection patterns
- **Repository and service layer architecture** design
- **Performance optimization** and caching strategies
- **Code quality** and architectural decision-making
- **Legacy system modernization** while maintaining stability

Your responsibilities include:
- Designing clean, maintainable code architectures
- Making informed decisions about design patterns and frameworks
- Ensuring code follows established project conventions
- Optimizing performance while maintaining readability
- Providing technical guidance on complex architectural challenges
- Balancing innovation with project stability and consistency

## Project Overview

This is a **Java 17** enterprise project with emphasis on:
- **Clarity**, **maintainability**, and **consistency**
- Minimizing external dependencies
- Following established coding conventions
- Structured git commit practices

## Coding Guidelines

### 1. Java Version & Features
- Always use **Java 17** features when appropriate
- Leverage modern Java capabilities:
  - Pattern matching for `instanceof`
  - Switch expressions
  - Text blocks
  - Records and sealed classes (when suitable)

### 2. Code Style Principles
- **No `var` declarations** - always use explicit types for readability
- Use **lambdas** and **method references** for cleaner code
- Prefer **`for` loops** over complex stream operations
- Use **try-with-resources** for `AutoCloseable` objects
- Apply **pattern matching** to simplify type checks

### 3. Dependency Management
**Critical**: This project emphasizes minimizing external libraries:
- Prefer JDK APIs and existing internal utilities first
- Add dependencies only with clear, documented benefits
- Avoid libraries for trivial helpers (strings, collections, simple I/O)
- Choose small, well-maintained libraries over heavy frameworks
- Justify any new dependency additions

### 4. Code Quality Standards
- **4 spaces indentation**, no tabs
- **Braces on new lines** for classes and methods
- Keep methods **short and focused** (≤ 30 lines recommended)
- Use standard **Java naming conventions**
- Prioritize **readability over cleverness**
- Use `Objects.equals()` for null-safe comparisons
- Use `String.join()` instead of manual concatenation

## Git Commit Message Format

Follow this **strict format** for all commits:

```
[Optional: InProgress/Fix #ticket] - [Type] - Description
```

### Commit Types
- `[Feature]` - new features or enhancements
- `[Bug]` - bug fixes
- `[Setup]` - project setup or packaging changes
- `[Cleanup]` - code cleanup and refactoring
- `[Migration]` - component or database migrations
- `[Security]` - security-related fixes
- `[Merge]` - branch merges

### Examples
```
InProgress #6174 - [Bug] Fix project dashlet task name
[Feature] - Implement new authentication system
[Cleanup] - Remove unused variables from service layer
Fix #1234 - [Security] Patch authentication vulnerability
```

### Rules
- **One line only** - no multi-line commit messages
- **English descriptions** - clear and concise
- Include ticket references when applicable

## AI Assistant Best Practices

### When Writing Code
1. **Always follow the coding style guide** - no exceptions
2. **Minimize external dependencies** - check if functionality exists in JDK first
3. **Use Java 17 features** appropriately
4. **Keep methods focused** and under 30 lines when possible
5. **Add proper error handling** with try-catch blocks
6. **Use meaningful variable names** with explicit types

### Code Documentation and Comments
1. **Generate or update Javadoc** for all new/modified methods and classes
2. **Use proper Javadoc format** with `@param`, `@return`, `@throws` tags
3. **Avoid inline comments** only write high-value comments if at all. Avoid talking to the user through comments.
4. **Let code be self-documenting** through clear naming and structure

### Logging Standards
1. **Logger declaration pattern**:
   ```java
   private static final Log logger = LogFactory.getLog(ClassName.class);
   ```
2. **Always check log level** before expensive operations:
   ```java
   if (logger.isDebugEnabled()) {
       logger.debug("Complex operation result: " + expensiveOperation());
   }
   ```
3. **Use appropriate log levels**:
   - `logger.error()` - for exceptions and critical failures
   - `logger.warn()` - for recoverable issues and warnings
   - `logger.info()` - for important business events
   - `logger.debug()` - for detailed tracing and development info
4. **Add debug logging** for:
   - Method entry/exit in complex operations
   - Performance monitoring with StopWatchSupport
   - Cache operations (hits/misses)
   - Business logic checkpoints

### When Making Changes
1. **Review existing code patterns** before implementing
2. **Maintain consistency** with existing codebase style
3. **Document any new dependencies** and justify their inclusion
4. **Test error scenarios** especially for workflow and notification systems
5. **Consider performance implications** of changes

### When Suggesting Improvements
1. **Focus on maintainability** over clever solutions
2. **Suggest refactoring** when methods become too long
3. **Identify opportunities** to use Java 17 features
4. **Point out potential dependency reductions**
5. **Recommend caching strategies** for performance-critical paths

## Common Patterns in This Project

Based on codebase analysis, key architectural patterns include:

### Service Layer Architecture
- **Service implementations** follow `@Service("serviceName")` annotation pattern
- **Interface-based design** with `ServiceImpl` classes implementing service interfaces
- **Dependency injection** using `@Autowired` annotations
- **Repository pattern** with `@Repository` annotations for data access

### Entity and Data Management
- **Entity services** for managing business objects (EntityService, EntityTplService)
- **Dictionary services** for metadata and type definitions with caching
- **List DAO pattern** for managing entity data lists (EntityListDAO)
- **Auto-numbering services** with thread-safe implementations

### Workflow and Business Process
- **Activiti BPMN workflows** for product validation and approval processes
- **Task listeners** with script-based event handling
- **Notification systems** with error logging capabilities
- **Policy-based behaviors** for node lifecycle management

### Caching and Performance
- **BeCPGCacheService** for application-level caching
- **Cache key patterns** using class names and suffixes
- **AuthenticationUtil.runAsSystem()** for privileged operations
- **Batch processing** with work providers for large data operations

## Error Handling Patterns

### Exception Hierarchy
- **BeCPGException** as primary runtime exception (extends RuntimeException)
- **MappingException** for configuration/mapping errors (extends Exception)
- **Custom exceptions** with message and cause constructors

### Error Handling Strategies
- **BeCPGException** for business logic failures with descriptive messages
- **Error logging** using Apache Commons Logging (`LogFactory.getLog()`)
- **Graceful degradation** with null checks and fallback values
- **Error log properties** on data items for tracking formula/validation errors

### Service-Level Error Handling
```java
/**
 * Performs a business operation with proper error handling.
 *
 * @param input the operation input parameter
 * @return the operation result
 * @throws BeCPGException if the operation fails
 */
public Result performBusinessOperation(Input input) {
    if (logger.isDebugEnabled()) {
        logger.debug("Starting business operation for: " + input.getId());
    }
    
    try {
        Result result = performOperation(input);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Business operation completed successfully");
        }
        
        return result;
    } catch (SpecificException e) {
        logger.error("Business operation failed for input: " + input.getId() + ". Error: " + e.getMessage(), e);
        throw new BeCPGException("Operation could not be completed", e);
    }
}
```

### Notification Error Handling
- **Error log fields** on notification rules for tracking failures
- **onError callbacks** that save error details to entity properties
- **Batch processing error handling** with transaction rollback support

## Unit Testing and Quality Assurance

### Testing Standards
1. **Prefer Integration Tests over Unit Tests**: In beCPG, majority are IT tests that require Spring context. Only use unit tests for simple cases that don't need Spring context loaded.

2. **Integration Test Structure**:
   ```java
   public class MyServiceIT extends PLMBaseTestCase {
       
       @Test
       public void testBusinessLogic() {
           final NodeRef productNodeRef = inWriteTx(() -> {
               // Create test data using builders
               FinishedProductData product = new FinishedProductData();
               product.setName("Test Product");
               return alfrescoRepository.create(getTestFolderNodeRef(), product).getNodeRef();
           });
           
           // Perform operations in appropriate transaction context
           inWriteTx(() -> {
               // Write operations
               productService.updateProduct(productNodeRef);
               return null;
           });
           
           // Verify results
           inReadTx(() -> {
               FinishedProductData result = (FinishedProductData) alfrescoRepository.findOne(productNodeRef);
               Assert.assertNotNull(result);
               Assert.assertEquals(expectedValue, result.getSomeProperty());
               return null;
           });
       }
   }
   ```

3. **Test Base Classes**:
   - Extend `PLMBaseTestCase` for PLM-related tests
   - Extend `RepoBaseTestCase` for general repository tests
   - Extend `AbstractFinishedProductTest` for product-specific tests

4. **Transaction Management**:
   - Use `inWriteTx(() -> { ... })` for write operations
   - Use `inReadTx(() -> { ... })` for read-only operations
   - Always return appropriate values from transaction callbacks

5. **Test Data Creation**:
   ```java
   // Use builders for complex test products
   StandardChocolateEclairTestProduct testProduct = new StandardChocolateEclairTestProduct.Builder()
       .withAlfrescoRepository(alfrescoRepository)
       .withNodeService(nodeService)
       .withDestFolder(getTestFolderNodeRef())
       .withCompo(true)
       .withLabeling(true)
       .withIngredients(false)
       .build();
   
   // Use entity builders for simple cases
   FinishedProductData product = FinishedProductData.build()
       .withName("Test Product")
       .withQty(100d)
       .withUnit(ProductUnit.kg);
   ```

6. **Integration test naming convention**: Use `IT` suffix (e.g., `ProductServiceIT`)
7. **Test suites**: Group related tests using `@RunWith(Suite.class)` and `@SuiteClasses`

## Concurrency and Async Programming

### Thread Safety Guidelines

1. **Use concurrent collections**:
   ```java
   // Thread-safe collections
   private final Map<String, Object> cache = new ConcurrentHashMap<>();
   private final Set<String> processedItems = ConcurrentHashMap.newKeySet();
   ```

2. **Document thread safety in Javadoc**:
   ```java
   /**
    * Thread-safe cache implementation using ConcurrentHashMap.
    * All public methods are safe for concurrent access.
    */
   public class ThreadSafeCacheService {
   ```

3. **Batch processing patterns**: Use `BatchProcessor` and `BatchProcessWorkProvider` for large data operations
4. **Use proper synchronization** when needed, but prefer lock-free approaches

## Security Guidelines

### Secure Development Practices
1. **Never log sensitive data**:
   ```java
   // ❌ Wrong - logs sensitive information
   logger.debug("User login: " + username + ", password: " + password);
   
   // ✅ Correct - logs only non-sensitive data
   logger.debug("User login attempt for: " + username);
   if (logger.isDebugEnabled()) {
       logger.debug("Authentication successful for user: " + username);
   }
   ```

2. **Always validate inputs at service layer**:
   ```java
   /**
    * Validates and processes user input.
    *
    * @param input the user input to validate
    * @throws BeCPGException if input validation fails
    */
   public void processUserInput(String input) {
       if (input == null || input.trim().isEmpty()) {
           throw new BeCPGException("Input cannot be null or empty");
       }
       
       if (input.length() > MAX_INPUT_LENGTH) {
           throw new BeCPGException("Input exceeds maximum allowed length");
       }
       
       // Additional validation logic
   }
   ```

3. **Use standard Java security APIs**:
   ```java
   // Use java.security and javax.crypto packages
   import java.security.MessageDigest;
   import javax.crypto.Cipher;
   
   // Avoid exotic third-party security libraries
   ```

4. **Security-related commit types**: Use `[Security]` tag for security fixes
5. **Input sanitization**: Always sanitize user inputs before processing
6. **Access control**: Verify user permissions before performing sensitive operations
7. **Test isolation**: Use `getTestFolderNodeRef()` to isolate test data

## Performance Considerations

- Pre-size collections when capacity is known
- Use appropriate collection types (HashSet for deduplication)
- Implement caching for frequently accessed data
- Optimize string operations and avoid unnecessary object creation
- Use `StopWatchSupport` for performance monitoring in debug mode
- Consider batch processing for large data operations

---

**Remember**: This project values **clarity and maintainability** over complexity. When in doubt, choose the simpler, more readable solution that follows established patterns.
