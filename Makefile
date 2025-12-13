# Makefile for Image RAG Project
# Common development tasks

.PHONY: help install-hooks lint format detekt check clean build test

# Default target
help:
	@echo "Available targets:"
	@echo "  install-hooks  - Install pre-commit hooks"
	@echo "  lint           - Run all linters (ktlint + detekt)"
	@echo "  format         - Auto-format code with ktlint"
	@echo "  detekt         - Run detekt static analysis"
	@echo "  check          - Run all checks (lint + test)"
	@echo "  test           - Run tests"
	@echo "  build          - Build the project"
	@echo "  clean          - Clean build artifacts"
	@echo "  baseline       - Generate detekt baseline"

# Install pre-commit hooks
install-hooks:
	@echo "Installing pre-commit hooks..."
	@if command -v pre-commit &> /dev/null; then \
		pre-commit install; \
		echo "✓ Pre-commit hooks installed"; \
	else \
		echo "⚠ pre-commit not found. Install it with: pip install pre-commit"; \
		exit 1; \
	fi

# Run all linters
lint:
	@echo "Running linters..."
	./gradlew lint --daemon

# Format code with ktlint
format:
	@echo "Formatting code..."
	./gradlew ktlintFormat --daemon

# Run detekt only
detekt:
	@echo "Running detekt..."
	./gradlew detekt --daemon

# Run all checks
check: lint test

# Run tests
test:
	@echo "Running tests..."
	./gradlew test --daemon

# Build project
build:
	@echo "Building project..."
	./gradlew build --daemon

# Clean build artifacts
clean:
	@echo "Cleaning..."
	./gradlew clean --daemon

# Generate detekt baseline
baseline:
	@echo "Generating detekt baseline..."
	./gradlew detektGenerateBaseline --daemon
	@echo "✓ Baseline generated at config/detekt/baseline.xml"
