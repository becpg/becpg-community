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
        FIRST_INSTRUCTIONS="1. Review git commit: ${COMMIT}\n2. Extract Redmine ticket from commit message."
        ;;
    ticket)
        if [ -z "${2:-}" ]; then
            echo "Error: Redmine ticket number must be provided in 'ticket' mode." >&2
            exit 1
        fi
        REDMINE_TICKET="${2:-}"
        FIRST_INSTRUCTIONS="1. Use Redmine ticket: ${REDMINE_TICKET}\n2. Review all staged changes."
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

Instructions:
$FIRST_INSTRUCTIONS
3. Fetch the ticket details from Redmine.
4. Review the code changes.
5. Compare the implementation against the ticket specifications.
6. Perform a security review of the changes.
7. Perform a performance review of the changes.
8. Provide a summary of the review.
9. If any issues are found, suggest improvements or corrections.

Commit hash: ${COMMIT:-N/A}
Redmine ticket: ${REDMINE_TICKET:-Extract from commit}"

# Execute review with error handling
if ! echo "$PROMPT" | gemini \
    --allowed-tools redmine_request,run_shell_command,read_file,search_file_content \
    2>"$ERRORS_FILE" >> "$RESULT_FILE"; then
    echo "Error: Review failed. Check $ERRORS_FILE for details." >&2
    exit 1
fi

# Check if errors occurred
if [ -s "$ERRORS_FILE" ]; then
    echo "Warning: Review completed with errors. Check $ERRORS_FILE" >&2
fi

echo "✓ Review complete! Results written to $RESULT_FILE"
