#!/usr/bin/env bash
# install-prepare-commit-msg.sh - Installs the prepare-commit-msg hook
#
# Usage: ./install-prepare-commit-msg.sh
#
# This script installs the hook directly to .git/hooks/ which is the
# recommended approach when NOT using pre-commit framework for this hook.

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Prepare-Commit-Msg Hook Installer${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if we're in a git repository
if [ ! -d ".git" ]; then
    echo -e "${RED}Error: Not in a git repository.${NC}"
    echo -e "Please run this script from the root of your git repository."
    exit 1
fi

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HOOK_SOURCE="$SCRIPT_DIR/prepare-commit-msg.sh"
HOOK_DEST=".git/hooks/prepare-commit-msg"

# Check if source hook exists
if [ ! -f "$HOOK_SOURCE" ]; then
    echo -e "${RED}Error: Hook source not found at $HOOK_SOURCE${NC}"
    exit 1
fi

# Create hooks directory if it doesn't exist
mkdir -p .git/hooks

# Check if hook already exists
if [ -f "$HOOK_DEST" ]; then
    echo -e "${YELLOW}Warning: Hook already exists at $HOOK_DEST${NC}"
    read -p "Do you want to overwrite it? (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Installation cancelled.${NC}"
        exit 0
    fi
fi

# Copy the hook
cp "$HOOK_SOURCE" "$HOOK_DEST"

# Make it executable
chmod +x "$HOOK_DEST"

echo -e "${GREEN}✓ Hook installed successfully!${NC}"
echo ""
echo -e "${BLUE}Hook location:${NC} $HOOK_DEST"
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  How It Works${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Branch naming convention: ${YELLOW}prefix/number-description${NC}"
echo -e "Supported prefixes: feat, fix, hotfix, chore, docs, style, refactor, test, perf, ci, build, revert"
echo ""
echo -e "${BLUE}Examples:${NC}"
echo ""
echo -e "  Branch: ${YELLOW}feat/123-add-login${NC}"
echo -e "  Input:  ${YELLOW}git commit -m 'add authentication'${NC}"
echo -e "  Output: ${GREEN}feat(#123): add authentication${NC}"
echo ""
echo -e "  Branch: ${YELLOW}feat/123-add-login${NC}"
echo -e "  Input:  ${YELLOW}git commit -m ':sparkles: add authentication'${NC}"
echo -e "  Output: ${GREEN}:sparkles: feat(#123): add authentication${NC}"
echo ""
echo -e "  Branch: ${YELLOW}feat/123-add-login${NC}"
echo -e "  Input:  ${YELLOW}git commit -m ':bug: fix: correct validation'${NC}"
echo -e "  Output: ${GREEN}:bug: fix(#123): correct validation${NC}"
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Troubleshooting${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "If the hook doesn't work, check:"
echo -e "  1. Branch name matches pattern: ${YELLOW}prefix/number-description${NC}"
echo -e "  2. Hook is executable: ${YELLOW}ls -la .git/hooks/prepare-commit-msg${NC}"
echo -e "  3. No syntax errors: ${YELLOW}bash -n .git/hooks/prepare-commit-msg${NC}"
echo -e "  4. Test manually: ${YELLOW}echo 'test message' | .git/hooks/prepare-commit-msg /dev/stdin${NC}"
echo ""
