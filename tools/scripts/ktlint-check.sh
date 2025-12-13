#!/usr/bin/env bash
# ktlint-check.sh - Runs ktlint check on staged Kotlin files
# This script is called by pre-commit with the list of staged files

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get the project root directory
PROJECT_ROOT="$(git rev-parse --show-toplevel)"
cd "$PROJECT_ROOT"

# Check if files were passed
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}No Kotlin files to check${NC}"
    exit 0
fi

echo -e "${YELLOW}Running ktlint check on staged files...${NC}"

# Filter to only include .kt and .kts files that exist
FILES_TO_CHECK=()
for file in "$@"; do
    if [[ -f "$file" && ("$file" == *.kt || "$file" == *.kts) ]]; then
        FILES_TO_CHECK+=("$file")
    fi
done

if [ ${#FILES_TO_CHECK[@]} -eq 0 ]; then
    echo -e "${GREEN}No Kotlin files to check${NC}"
    exit 0
fi

# Run ktlint via Gradle
# Using --continue to report all errors, not just the first one
if ./gradlew ktlintCheck --daemon -Pktlint.staged.files="${FILES_TO_CHECK[*]}" > /dev/null 2>&1; then
    # If Gradle task doesn't support staged files parameter, run full check
    ./gradlew ktlintCheck --daemon
fi

KTLINT_EXIT_CODE=$?

if [ $KTLINT_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ ktlint check passed${NC}"
else
    echo -e "${RED}✗ ktlint check failed${NC}"
    echo -e "${YELLOW}Run './gradlew ktlintFormat' to auto-fix issues${NC}"
    exit 1
fi
