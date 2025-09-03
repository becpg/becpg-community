# Git Commit Messages Convention

## 1. **Optional First Part: Ticket Reference**

* If the commit relates to a ticket, indicate its status and reference.
* Use:

  * `InProgress` → for tickets not yet resolved
  * `Fix` → for resolved tickets

**Format:**

```plaintext
InProgress/Fix #0000
```

---

## 2. **Required Second Part: Commit Type**

Choose one of the following tags to categorize your commit:

* `[Feature]` → new features or enhancements
* `[Bug]` → bug fixes
* `[Setup]` → project setup or packaging changes
* `[Cleanup]` → code cleanup and refactoring
* `[Migration]` → component or database migrations
* `[Security]` → fixes related to vulnerabilities
* `[Merge]` → merges between branches

**Format:**

```plaintext
[Tag] - Short description
```

---

## 3. **Required Third Part: Description**

* Write a **short, clear description in English**.
* **Commit messages must be one line only.**

---

## ✅ Examples

```plaintext
InProgress #6174 - [Bug] Fix project dashlet task name
[Bug] - Css disabled button fix
Fix #1234 - [Feature] Add security read/write option
[Cleanup] - Remove unused variable
[Setup] - Add hot class reloading for project and PLM
[Merge] - Merge develop into master
[Feature] - Implement new login page
InProgress #6174 - [Issue] Display wait message when form field is invalid
[Migration] - Update database schema for version 2.0
[Security] - Fix vulnerability in authentication module
```

---

## 🔧 Validation with Git Hook

To enforce this convention, you can create a `commit-msg` script in your repository’s `.git/hooks/` directory.
Make it executable (`chmod +x .git/hooks/commit-msg`) and add rules to check that:

* The message fits the format
* It is only **one line long**

