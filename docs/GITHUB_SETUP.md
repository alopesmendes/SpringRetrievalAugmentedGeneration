# GitHub Environment Setup Guide

## Overview

This project uses **GitHub Environments** to manage secrets and variables per environment.
The `env.yml` workflow automatically selects the correct environment based on branch.

---

## Step 1: Create GitHub Environments

Go to: **Repository → Settings → Environments**

Create these 4 environments:

| Environment   | Branch                | Protection          |
|---------------|-----------------------|---------------------|
| `development` | PRs, feature branches | None                |
| `test`        | develop               | None                |
| `staging`     | staging               | Optional: reviewers |
| `prod`        | master                | Required: reviewers |

---

## Step 2: Add Secrets to Each Environment

For **each** environment, add these secrets:

| Secret           | Description                                  |
|------------------|----------------------------------------------|
| `OPENAI_API_KEY` | OpenAI API key (can differ per env)          |
| `JWT_SECRET`     | JWT signing secret (must be unique per env!) |

Generate JWT secrets:
```bash
openssl rand -base64 64
```

---

## Step 3: Add Variables to Each Environment

For **each** environment, add these variables:

| Variable                       | development | test      | staging   | prod      |
|--------------------------------|-------------|-----------|-----------|-----------|
| `APP_ENVIRONMENT`              | development | test      | staging   | prod      |
| `APP_NAME`                     | image-rag   | image-rag | image-rag | image-rag |
| `SERVER_PORT`                  | 8080        | 8080      | 8080      | 8080      |
| `DEVTOOLS_ENABLED`             | true        | false     | false     | false     |
| `LOG_LEVEL_ROOT`               | INFO        | WARN      | WARN      | ERROR     |
| `LOG_LEVEL_APP`                | DEBUG       | INFO      | INFO      | WARN      |
| `LOG_LEVEL_SPRING`             | DEBUG       | WARN      | WARN      | WARN      |
| `LOG_LEVEL_WEB`                | DEBUG       | WARN      | WARN      | WARN      |
| `LOG_LEVEL_SECURITY`           | DEBUG       | WARN      | WARN      | WARN      |
| `ERROR_INCLUDE_MESSAGE`        | always      | always    | always    | never     |
| `ERROR_INCLUDE_BINDING_ERRORS` | always      | always    | always    | never     |
| `ERROR_INCLUDE_STACKTRACE`     | always      | never     | never     | never     |
| `ERROR_INCLUDE_EXCEPTION`      | true        | false     | false     | false     |

---

## Automatic Environment Selection

The `env.yml` workflow auto-selects environment based on branch:

| Trigger | Branch    | Environment |
|---------|-----------|-------------|
| Push    | master    | prod        |
| Push    | staging   | staging     |
| Push    | develop   | test        |
| PR      | any       | development |
| Push    | feature/* | development |

---

## Manual Dispatch Restrictions

When running workflows manually, environment choices are restricted by branch:

| Branch    | Allowed Environments                 |
|-----------|--------------------------------------|
| master    | development, test, staging, **prod** |
| staging   | development, test, **staging**       |
| develop   | development, **test**                |
| feature/* | **development**, test                |

---

## Using env.yml in Your Workflows

```yaml
name: My Workflow

on:
  push:
    branches: [master, staging, develop]
  pull_request:
  workflow_dispatch:
    inputs:
      environment:
        type: choice
        options: [development, test, staging, prod]

jobs:
  # Load environment
  env:
    uses: ./.github/workflows/env.yml
    with:
      environment: ${{ inputs.environment || '' }}  # Auto-detect if empty
    secrets: inherit

  # Use environment
  build:
    needs: env
    runs-on: ubuntu-latest
    steps:
      - run: ./gradlew build
        env:
          # Secrets
          OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
          JWT_SECRET: ${{ secrets.JWT_SECRET }}

          # Variables from env.yml
          APP_ENVIRONMENT: ${{ needs.env.outputs.APP_ENVIRONMENT }}
          APP_NAME: ${{ needs.env.outputs.APP_NAME }}
          LOG_LEVEL_APP: ${{ needs.env.outputs.LOG_LEVEL_APP }}
          # ... other outputs
```
