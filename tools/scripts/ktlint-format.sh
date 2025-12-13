#!/usr/bin/env bash
# ktlint-format.sh - Auto-formats Kotlin files with ktlint
# This script is called by pre-commit with the list of staged files

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
    echo -e "${YELLOW}No Kotlin files to format${NC}"
    exit 0
fi

# Filter to only include .kt and .kts files that exist
FILES_TO_FORMAT=()
for file in "$@"; do
    if [[ -f "$file" && ("$file" == *.kt || "$file" == *.kts) ]]; then
        FILES_TO_FORMAT+=("$file")
    fi
done

if [ ${#FILES_TO_FORMAT[@]} -eq 0 ]; then
    echo -e "${GREEN}No Kotlin files to format${NC}"
    exit 0
fi

echo -e "${YELLOW}Running ktlint format on ${#FILES_TO_FORMAT[@]} file(s)...${NC}"

# Run ktlint format via Gradle
./gradlew ktlintFormat --daemon
KTLINT_EXIT_CODE=$?

if [ $KTLINT_EXIT_CODE -eq 0 ]; then
    # Re-stage the formatted files
    for file in "${FILES_TO_FORMAT[@]}"; do
        if [[ -f "$file" ]]; then
            git add "$file"
        fi
    done
    echo -e "${GREEN}✓ ktlint format completed and files re-staged${NC}"
    exit 0
else
    echo -e "${RED}✗ ktlint format failed with exit code $KTLINT_EXIT_CODE${NC}"
    echo -e "${YELLOW}Check the output above for details${NC}"
    exit 1
fi
