#!/usr/bin/env bash
# claude-local.sh — Connect Claude Code directly to LM Studio
# Usage: ./claude-local.sh [optional-model-name] [other-claude-flags]

set -euo pipefail

# Check if the first argument exists and is NOT a flag (doesn't start with '-')
if [[ $# -gt 0 && ! "$1" == -* ]]; then
  MODEL_NAME="$1"
  shift
else
  # Updated to match the exact "Model" string from your LM Studio screenshot
  MODEL_NAME="google/gemma-4-e4b"
fi

echo "Starting Claude directly through LM Studio..."
echo "Model: ${MODEL_NAME}"
echo "---------------------------------------"

# Removed /v1 from the URL and removed the API_KEY variable
ANTHROPIC_BASE_URL="http://localhost:1234" \
ANTHROPIC_AUTH_TOKEN="lmstudio" \
claude --model "${MODEL_NAME}" "$@"
