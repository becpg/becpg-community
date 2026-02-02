Given the Commit diff, verify it matches the specifications in its associated Redmine ticket:

1. Extract the Redmine ticket number from the commit message.

2. Fetch the ticket details from Redmine (including description, acceptance criteria, requirements, and any non-functional requirements).

3. Review the code changes in the commit.

4. Compare the implementation against the ticket specifications and check for:
   - All required features/changes are implemented
   - Code follows the specified functional and non-functional requirements
   - Edge cases mentioned in the ticket are handled
   - All acceptance criteria are met

5. Perform a security review of the changes, including but not limited to:
   - Input validation and sanitization
   - Authentication and authorization concerns
   - Exposure of sensitive data (secrets, tokens, PII, credentials)
   - Common vulnerabilities (e.g., injection, XSS, CSRF)
   - Secure usage of third-party libraries

6. Perform a performance review of the changes, including but not limited to:
   - Time and space complexity
   - Database query efficiency
   - Network and I/O efficiency
   - Caching considerations
   - Scalability concerns

7. Provide a summary of:
   - What matches the specifications
   - Any discrepancies or missing implementations
   - Identified security concerns
   - Identified performance concerns
   - Suggestions for improvement
