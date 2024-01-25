# Git commits messages

1. **Optional First Part:**
   - If a ticket is associated with the commit, indicate the status and reference of the ticket.
   - Use "InProgress" for tickets not yet resolved and "Fix" for resolved tickets.

   Example:
   ```
   InProgress/Fix #0000
   ```

2. **Required Second Part:**
   - Use one of the following tags to categorize the type of commit:
     - [Feature] - For new features or enhancements
     - [Bug] - For bug fixes
     - [Setup] - For changes related to project packaging
     - [Cleanup] - For code cleanup
     - [Migration] - For migration of a component
     - [Security] - For addressing security issues
     - [Merge] - For merge between branches

   Example:
   ```
   [Feature] - Add new functionality
   ```

3. **Required Third Part:**
   - Provide a brief English description of the commit.

   Example:
   ```
   [Feature] - Add new functionality
   ```

Example commit messages following the convention:

```plaintext
InProgress #6174 - [Bug] Fix project dashlet task name
[Bug] - Css disabled button fix
Fix #1234 - [Feature] Add security readandwrite option
[Cleanup] - Remove unused code variable
[Setup] - Add project and plm to hot class reloading
[Merge] - Merge develop into master
[Feature] - Implement new login page
InProgress/Fix #6174 - [Issue] Wait message display when form field is invalid
[Migration] - Update database schema for version 2.0
[Security] - Fix security vulnerability in authentication module
```

You can use this template when writing your commit messages. Additionally, you mentioned a local Git configuration for commit message validation. To implement this, you need to create a `commit-msg` script in the `.git/hooks` directory of your project. Make sure to make it executable. If you need further assistance with this, let me know!