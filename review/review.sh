#!/bin/sh
set -eu

SCRIPT_DIR="$(dirname "$0")"
RESULT_FILE="$SCRIPT_DIR/review-result.md"
ERRORS_FILE="$SCRIPT_DIR/review-errors.txt"

MODE="${1:-}"
COMMIT=""
REDMINE_TICKET=""
FIRST_INSTRUCTIONS=""

# Validate mode parameter
if [ -z "$MODE" ]; then
    echo "Error: Mode must be provided (commit|ticket)" >&2
    echo "Usage: ./run.sh review <mode> [commit_hash|ticket_number]" >&2
    exit 1
fi

case "$MODE" in
    commit)
        COMMIT="${2:-HEAD}"
        FIRST_INSTRUCTIONS="1. Review git commit: ${COMMIT}\n2. Extract Redmine ticket from commit message"
        ;;
    ticket)
        if [ -z "${2:-}" ]; then
            echo "Error: Redmine ticket number must be provided in 'ticket' mode." >&2
            exit 1
        fi
        REDMINE_TICKET="${2:-}"
        FIRST_INSTRUCTIONS="1. Review git staged changes \n2. Use Redmine ticket: ${REDMINE_TICKET}"
        ;;
    *)
        echo "Error: Invalid mode '$MODE'. Use 'commit' or 'ticket'." >&2
        exit 1
        ;;
esac

# Clear output files
: > "$RESULT_FILE"
: > "$ERRORS_FILE"

echo "Reviewing mode: $MODE, Commit: ${COMMIT:-N/A}, Redmine Ticket: ${REDMINE_TICKET:-N/A}"

# Build prompt with proper indentation handling
PROMPT="You are a senior software engineer performing a code review.
Your task is to review the code changes introduced in a specific commit
and ensure they align with the requirements specified in the associated Redmine ticket.
Do not access any files outside of the git diff and Redmine ticket details.
Provide only the review results without any explanations.

Instructions:
$FIRST_INSTRUCTIONS
3. Fetch the ticket details from Redmine using Redmine tool
4. Review the code changes using ONLY the git command - do not read or access any other files
5. Compare the implementation against ticket specifications based solely on the diff
6. Perform security review using only the diff (check for vulnerabilities, injection risks, authentication/authorization issues)
7. Perform performance review using only the diff (identify bottlenecks, inefficient queries, resource usage)
8. If issues found: categorize by severity (critical/major/minor) and provide specific fixes
9. Output format:
   - Issues found (if any) with suggested fixes
   - Suggested commit message (if not provided)
   - Overall assessment (approve/needs changes)
10. Provide only the review results—no explanations of your process

IMPORTANT: Base your entire review exclusively on the git diff. Do not read, view, or reference any other files in the repository.

Commit hash: ${COMMIT:-N/A}
Redmine ticket: ${REDMINE_TICKET:-Will be extracted from commit}"

# Execute review with error handling

if ! echo "$PROMPT" | gemini \
    --allowed-tools redmine_request,run_shell_command \
    2>"$ERRORS_FILE" >> "$RESULT_FILE"; then
    echo "Error: Review failed. Check $ERRORS_FILE for details." >&2
    exit 1
fi

# Check if errors occurred
if [ -s "$ERRORS_FILE" ]; then
    echo "Warning: Review completed with errors. Check $ERRORS_FILE" >&2
fi

echo "✓ Review complete! Results written to $RESULT_FILE"
