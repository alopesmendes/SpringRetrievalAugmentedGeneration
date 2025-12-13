#!/usr/bin/env bash
# detekt-check.sh - Runs detekt static analysis on staged Kotlin files
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
    echo -e "${YELLOW}No Kotlin files to analyze${NC}"
    exit 0
fi

echo -e "${YELLOW}Running detekt analysis on staged files...${NC}"

# Filter to only include .kt files that exist
FILES_TO_CHECK=()
for file in "$@"; do
    if [[ -f "$file" && "$file" == *.kt ]]; then
        FILES_TO_CHECK+=("$file")
    fi
done

if [ ${#FILES_TO_CHECK[@]} -eq 0 ]; then
    echo -e "${GREEN}No Kotlin files to analyze${NC}"
    exit 0
fi

# Run detekt via Gradle
./gradlew detekt --daemon

DETEKT_EXIT_CODE=$?

if [ $DETEKT_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ detekt analysis passed${NC}"
else
    echo -e "${RED}✗ detekt analysis found issues${NC}"
    echo -e "${YELLOW}Check build/reports/detekt/ for detailed report${NC}"
    exit 1
fi
