#!/bin/bash
# wizard.sh - Interactive wizard for review.sh using whiptail

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REVIEW_SH="$SCRIPT_DIR/review.sh"

# Check for whiptail
if ! command -v whiptail >/dev/null 2>&1; then
    echo "Error: whiptail is required for this wizard."
    echo "Install it with: sudo apt-get install whiptail   or   sudo yum install newt"
    exit 1
fi

# Show main menu
CHOICE=$(whiptail --title "Review Wizard" \
    --menu "What do you want to review?" 15 60 2 \
    "1." "Git commit" \
    "2." "Staged changes" 3>&1 1>&2 2>&3)

if [ $? -ne 0 ]; then
  echo "User cancelled."
  exit 1
fi

if [[ "$CHOICE" == "1." ]]; then
    # Get last 10 commits
    mapfile -t COMMITS < <(git log --oneline -n 10)
    if [[ ${#COMMITS[@]} -eq 0 ]]; then
        whiptail --msgbox "No git commits found." 10 50
        exit 1
    fi
    MENU_ITEMS=()
    # Prepare in tag item pairs
    for i in "${!COMMITS[@]}"; do
        MENU_ITEMS+=("$i" "${COMMITS[$i]}")
    done
    IDX=$(whiptail --title "Select Commit" --menu "Pick a commit to review:" 20 100 10 "${MENU_ITEMS[@]}" 3>&1 1>&2 2>&3)
    if [ $? -ne 0 ]; then
        echo "User cancelled."
        exit 1
    fi
    CHOSEN_HASH=$(echo "${COMMITS[$IDX]}" | awk '{print $1}')
    whiptail --msgbox "Starting review of commit: $CHOSEN_HASH" 10 60
    bash "$REVIEW_SH" commit "$CHOSEN_HASH"
    exit $?

elif [[ "$CHOICE" == "2." ]]; then
    TICKET=$(whiptail --title "Redmine Ticket" --inputbox "Enter Redmine ticket number:" 10 60 3>&1 1>&2 2>&3)
    if [ $? -ne 0 ]; then
        echo "User cancelled."
        exit 1
    fi
    whiptail --msgbox "Starting review for Redmine ticket: $TICKET" 10 60
    bash "$REVIEW_SH" ticket "$TICKET"
    exit $?
else
    whiptail --msgbox "Unknown error in choice." 10 60
    exit 2
fi
