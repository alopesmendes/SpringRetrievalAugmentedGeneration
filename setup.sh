#!/usr/bin/env bash
# setup.sh - Initial project setup for new developers
# Run this script after cloning the repository

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Image RAG Project Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check for required tools
echo -e "${YELLOW}Checking required tools...${NC}"

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo -e "${GREEN}✓ Java found: $JAVA_VERSION${NC}"
else
    echo -e "${RED}✗ Java not found. Please install JDK 21+${NC}"
    exit 1
fi

# Check Gradle wrapper
if [ -f "./gradlew" ]; then
    echo -e "${GREEN}✓ Gradle wrapper found${NC}"
else
    echo -e "${YELLOW}⚠ Gradle wrapper not found. Generating...${NC}"
    if command -v gradle &> /dev/null; then
        gradle wrapper --gradle-version 8.14
        echo -e "${GREEN}✓ Gradle wrapper generated${NC}"
    else
        echo -e "${RED}✗ Gradle not found. Please install Gradle or generate wrapper manually${NC}"
        exit 1
    fi
fi

# Check Python (for pre-commit)
if command -v python3 &> /dev/null; then
    PYTHON_VERSION=$(python3 --version)
    echo -e "${GREEN}✓ Python found: $PYTHON_VERSION${NC}"
else
    echo -e "${YELLOW}⚠ Python not found. Pre-commit hooks will not be available${NC}"
fi

# Install pre-commit if available
echo ""
echo -e "${YELLOW}Setting up pre-commit hooks...${NC}"

if command -v pre-commit &> /dev/null; then
    echo -e "${GREEN}✓ pre-commit found${NC}"
else
    echo -e "${YELLOW}Installing pre-commit...${NC}"

    # Prefer brew on macOS (avoids externally-managed-environment error)
    if command -v brew &> /dev/null; then
        echo -e "${YELLOW}Installing via Homebrew...${NC}"
        brew install pre-commit
        echo -e "${GREEN}✓ pre-commit installed via Homebrew${NC}"
    # Try pipx (recommended for Python CLI tools)
    elif command -v pipx &> /dev/null; then
        echo -e "${YELLOW}Installing via pipx...${NC}"
        pipx install pre-commit
        echo -e "${GREEN}✓ pre-commit installed via pipx${NC}"
    # Fallback to pip with --user flag
    elif command -v pip3 &> /dev/null; then
        echo -e "${YELLOW}Installing via pip3 --user...${NC}"
        pip3 install --user pre-commit 2>/dev/null || {
            echo -e "${YELLOW}pip3 --user failed, trying pipx installation...${NC}"
            if command -v brew &> /dev/null; then
                brew install pipx
                pipx ensurepath
                pipx install pre-commit
                echo -e "${GREEN}✓ pre-commit installed via pipx${NC}"
            else
                echo -e "${RED}✗ Could not install pre-commit automatically${NC}"
                echo -e "${RED}  Please install manually using one of:${NC}"
                echo -e "${RED}    brew install pre-commit${NC}"
                echo -e "${RED}    pipx install pre-commit${NC}"
            fi
        }
        echo -e "${GREEN}✓ pre-commit installed${NC}"
    else
        echo -e "${RED}✗ Could not install pre-commit. Please install it manually:${NC}"
        echo -e "${RED}  brew install pre-commit${NC}"
        echo -e "${RED}  or: pipx install pre-commit${NC}"
    fi
fi

# Install pre-commit hooks
if command -v pre-commit &> /dev/null; then
    pre-commit install
    echo -e "${GREEN}✓ Pre-commit hooks installed${NC}"
else
    echo -e "${YELLOW}⚠ Skipping pre-commit hooks installation${NC}"
fi

# Make scripts executable
echo ""
echo -e "${YELLOW}Setting up scripts...${NC}"
chmod +x tools/scripts/*.sh 2>/dev/null || true
echo -e "${GREEN}✓ Scripts are executable${NC}"

# Build project to download dependencies
echo ""
echo -e "${YELLOW}Downloading dependencies...${NC}"
./gradlew dependencies --daemon > /dev/null 2>&1 || true
echo -e "${GREEN}✓ Dependencies downloaded${NC}"

# Run initial lint check
echo ""
echo -e "${YELLOW}Running initial lint check...${NC}"
if ./gradlew ktlintCheck --daemon > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Code style check passed${NC}"
else
    echo -e "${YELLOW}⚠ Some code style issues found. Run './gradlew ktlintFormat' to fix${NC}"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}  Setup Complete!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Available commands:"
echo -e "  ${YELLOW}make lint${NC}          - Run all linters"
echo -e "  ${YELLOW}make format${NC}        - Auto-format code"
echo -e "  ${YELLOW}make test${NC}          - Run tests"
echo -e "  ${YELLOW}make build${NC}         - Build project"
echo -e "  ${YELLOW}./gradlew tasks${NC}    - List all Gradle tasks"
echo ""
echo -e "Pre-commit hooks will automatically run on each commit."
echo -e "To manually run all hooks: ${YELLOW}pre-commit run --all-files${NC}"
echo ""
