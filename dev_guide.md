# Incremental Development Guidelines

## Core Principles

We follow a strict incremental approach to development with these core rules:

1. **Maximum 20 lines of code per change**
2. **Test after each change before proceeding**
3. **Maximum 2-3 files modified in one change session**

## Change Management Protocol

### Change Request Template

```
CHANGE REQUEST #[Number]
- Files to modify: [max 2-3 file paths]
- Purpose: [Brief description of the change]
- Expected outcome: [What should work after this change]
- Lines of code to add/modify: [Less than 20 lines]
```

### Validation Checklist

After each change, complete this validation before proceeding:

```
VALIDATION CHECKLIST
- UI renders correctly: [Yes/No]
- Navigation flow works: [Yes/No]
- Feature being tested: [Description]
- Test result: [Pass/Fail]
- Observations: [Any issues noticed]
```

## Implementation Rules

### The 20-Line Rule
Each change request must limit code changes to 20 lines or fewer across all files.

### The 3-File Maximum
Changes should affect no more than 3 files at once.

### Test-First Approach
For each feature:
- Define what specific test will validate the change
- Make the minimal change needed
- Run the test and document results
- Only proceed after confirming success

### Refactoring Pattern
When refactoring:
- Extract small components (single function/feature) one at a time
- Test the extracted component works identically to before
- Move to the next component only after validation

## Examples of Incremental Changes

### Example 1: Extracting a Function

**Change Request:**
```
CHANGE REQUEST #1
- Files to modify: index.html, js/quiz.js
- Purpose: Extract getRandomQuestions function to external file
- Expected outcome: Question randomization works identically
- Lines of code: ~15 lines (function definition + import)
```

**Validation:**
```
VALIDATION CHECKLIST
- UI renders correctly: Yes
- Navigation works: Yes
- Feature tested: Question randomization
- Test result: Pass
- Observations: None
```

### Example 2: Adding Test IDs

**Change Request:**
```
CHANGE REQUEST #2
- Files to modify: index.html
- Purpose: Add data-testid attributes to key elements
- Expected outcome: No visual changes, elements have test IDs
- Lines of code: ~10 lines (attribute additions)
```

### Example 3: Enhancing Error Handling

**Change Request:**
```
CHANGE REQUEST #3
- Files to modify: js/quiz.js
- Purpose: Improve error handling for quiz data loading
- Expected outcome: Better error messages when quiz fails to load
- Lines of code: ~12 lines (try/catch enhancement)
```

## Commit Guidelines

Each commit should:
1. Represent exactly one change request
2. Have a descriptive message following this format:
   ```
   [CR-##] Brief description of change
   
   - What was changed
   - Why it was changed
   - How to test the change
   ```
3. Include only the files relevant to that specific change

## Review Process

Before submitting any PR:
1. Verify all changes meet the 20-line rule
2. Confirm no more than 3 files were modified per change
3. Document test results for each change
4. Ensure commit messages follow the standard format

---

**IMPORTANT:** This document must be reviewed at the beginning of every development session to ensure strict adherence to the incremental development approach.
