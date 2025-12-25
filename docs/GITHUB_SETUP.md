# GitHub Environment Setup Guide

## Overview

This project uses **GitHub Environments** to manage secrets and variables.
Deployment uses **Render Free Plan** with 2 services mapped to 4 GitHub environments.

All workflows trigger on both **PR** and **Push** events for consistent CI/CD behavior.

```
┌─────────────────────────────────────────────────────────────────┐
│              GitHub Environments → Render Services              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   development ──┐                                               │
│                 ├──→ image-rag-dev (Render)                     │
│   test ─────────┘                                               │
│                                                                 │
│   staging ──────┐                                               │
│                 ├──→ image-rag-prod (Render)                    │
│   prod ─────────┘                                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Deployment Flow

All workflows trigger on **PR** and **Push** to master/develop/staging branches.
Chain ordering is maintained via `workflow_run` triggers.

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                            DEPLOYMENT FLOW                                   │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────┐                                                                │
│  │ Security │ (Independent - PR & Push)                                      │
│  └──────────┘                                                                │
│                                                                              │
│                    ┌──────────┐                                              │
│               ┌───▶│ Analysis │ (PR & Push + after Lint)                     │
│               │    └──────────┘                                              │
│  ┌──────────┐ │                                                              │
│  │   Lint   │─┤    (Entry Point - PR & Push)                                 │
│  └──────────┘ │                                                              │
│               │    ┌──────────┐     ┌──────────┐     ┌──────────┐            │
│               └───▶│  Build   │────▶│   Test   │────▶│ Coverage │            │
│                    └──────────┘     └──────────┘     └──────────┘            │
│                    (PR & Push)      (PR & Push)      (PR & Push)             │
│                                                           │                  │
│                                                           ▼                  │
│                                                     ┌──────────┐             │
│                                                     │  Deploy  │             │
│                                                     └──────────┘             │
│                                                     (Push only)              │
│                                                          │                   │
│                                          ┌───────────────┴───────┐           │
│                                          ▼                       ▼           │
│                                   image-rag-dev           image-rag-prod     │
│                                     (dev/test)            (staging/prod)     │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Workflow Triggers

| Workflow | PR Trigger | Push Trigger | Chain Trigger  |
|----------|------------|--------------|----------------|
| Lint     | ✅          | ✅            | Entry point    |
| Build    | ✅          | ✅            | After Lint     |
| Test     | ✅          | ✅            | After Build    |
| Coverage | ✅          | ✅            | After Test     |
| Deploy   | ❌          | ✅            | After Coverage |
| Analysis | ✅          | ✅            | After Lint     |
| Security | ✅          | ✅            | Independent    |

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

## Step 2: Add Repository Secrets

Go to: **Repository → Settings → Secrets and variables → Actions → Secrets**

| Secret           | Description                                      |
|------------------|--------------------------------------------------|
| `RENDER_API_KEY` | Render API key (shared across all environments)  |
| `CODECOV_TOKEN`  | Codecov token for coverage reporting             |

### Generate Render API Key

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click your profile → **Account Settings** → **API Keys**
3. Click **Create API Key**
4. Copy and save as `RENDER_API_KEY` in GitHub

---

## Step 3: Add Repository Variables

Go to: **Repository → Settings → Secrets and variables → Actions → Variables**

| Variable                 | Description                        | Example         |
|--------------------------|------------------------------------|-----------------|
| `RENDER_SERVICE_ID_DEV`  | Service ID for development service | `srv-abc123...` |
| `RENDER_SERVICE_ID_PROD` | Service ID for production service  | `srv-xyz789...` |

---

## Step 4: Add Environment Secrets

For **each** GitHub environment, add these secrets:

| Secret           | Description                                  |
|------------------|----------------------------------------------|
| `OPENAI_API_KEY` | OpenAI API key (can differ per env)          |
| `JWT_SECRET`     | JWT signing secret (must be unique per env!) |

Generate JWT secrets:
```bash
openssl rand -base64 64
```

---

## Step 5: Add Environment Variables

For **each** GitHub environment, add these variables:

| Variable                   | development                        | test          | staging                        | prod              |
|----------------------------|------------------------------------|---------------|--------------------------------|-------------------|
| `RENDER_DOMAIN`            | https://image-rag-dev.onrender.com | (same as dev) | https://image-rag.onrender.com | (same as staging) |
| `RENDER_SERVICE_ID`        | srv-xxx (dev service)              | srv-xxx (dev) | srv-yyy (prod service)         | srv-yyy (prod)    |
| `APP_ENVIRONMENT`          | development                        | test          | staging                        | prod              |
| `APP_NAME`                 | image-rag                          | image-rag     | image-rag                      | image-rag         |
| `LOG_LEVEL_APP`            | DEBUG                              | INFO          | INFO                           | WARN              |
| `LOG_LEVEL_ROOT`           | INFO                               | WARN          | WARN                           | ERROR             |
| `ERROR_INCLUDE_STACKTRACE` | always                             | never         | never                          | never             |

> **Note**: `development` and `test` share the same `RENDER_DOMAIN` (dev service).
> `staging` and `prod` share the same `RENDER_DOMAIN` (prod service).

---

## Step 6: Render Setup

### 6.1 Create 2 Web Services

| Service Name     | Branch  | Purpose                          |
|------------------|---------|----------------------------------|
| `image-rag-dev`  | develop | Development & Test deployments   |
| `image-rag-prod` | master  | Staging & Production deployments |

For each service:
1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click **New** → **Web Service**
3. Connect your GitHub repository
4. Configure:

| Setting         | Development     | Production       |
|-----------------|-----------------|------------------|
| Name            | `image-rag-dev` | `image-rag-prod` |
| Branch          | `develop`       | `master`         |
| Runtime         | Docker          | Docker           |
| Dockerfile Path | `./Dockerfile`  | `./Dockerfile`   |
| Plan            | Free            | Free             |

### 6.2 Disable Auto-Deploy

For each service:
1. Go to **Settings** → **Build & Deploy**
2. Set **Auto-Deploy** to **No**

### 6.3 Get Service IDs

1. Open each service in Render Dashboard
2. Look at the URL: `https://dashboard.render.com/web/srv-XXXXXXXXXX`
3. Copy `srv-XXXXXXXXXX`
4. Add to GitHub Environment variables:
    - Dev service → Add to `development` and `test` environments
    - Prod service → Add to `staging` and `prod` environments

### 6.4 Add Environment Variables in Render

For **each** Render service, add in **Environment** tab:

```bash
# Required
SERVER_PORT=10000
SPRING_PROFILES_ACTIVE=dev  # or 'prod' for production service

# Secrets (copy from GitHub Environment secrets)
JWT_SECRET=<your-jwt-secret>
OPENAI_API_KEY=<your-openai-key>

# Database (when ready)
SPRING_DATA_MONGODB_URI=<mongodb-atlas-uri>
```

---

## Environment Mapping

| GitHub Environment | Render Service | Branch    | Trigger      |
|--------------------|----------------|-----------|--------------|
| `development`      | image-rag-dev  | feature/* | Manual only  |
| `test`             | image-rag-dev  | develop   | On push      |
| `staging`          | image-rag-prod | staging   | On push      |
| `prod`             | image-rag-prod | master    | On push      |

---

## Using Env.yml in Your Workflows

The Env.yml workflow provides environment resolution. Each workflow now includes
its own environment resolution for consistency, but you can still use Env.yml
for custom workflows:

```yaml
name: My Workflow

on:
  push:
    branches: [master, staging, develop]
  pull_request:
  workflow_dispatch:

jobs:
  # Option 1: Use Env.yml for resolution only
  env:
    uses: ./.github/workflows/Env.yml
    secrets: inherit

  build:
    needs: env
    runs-on: ubuntu-latest
    environment: ${{ needs.env.outputs.environment }}
    steps:
      - run: echo "Building for ${{ needs.env.outputs.environment }}"

  # Option 2: Use Env.yml with command execution
  test-with-env:
    uses: ./.github/workflows/Env.yml
    with:
      command: ./gradlew test
    secrets: inherit
```

---

## Complete Setup Checklist

### GitHub Repository Level
- [ ] Add `RENDER_API_KEY` to repository secrets
- [ ] Add `CODECOV_TOKEN` to repository secrets
- [ ] Add `RENDER_SERVICE_ID_DEV` to repository variables
- [ ] Add `RENDER_SERVICE_ID_PROD` to repository variables

### GitHub Environments (for each of 4 environments)
- [ ] Create environment: `development`, `test`, `staging`, `prod`
- [ ] Add `OPENAI_API_KEY` secret
- [ ] Add `JWT_SECRET` secret (unique per environment!)
- [ ] Add `RENDER_DOMAIN` variable
- [ ] Add `RENDER_SERVICE_ID` variable
- [ ] Add `APP_ENVIRONMENT` variable
- [ ] Add `APP_NAME` variable
- [ ] Add `LOG_LEVEL_APP` variable
- [ ] Configure branch protection for `prod`

### Render (2 services)
- [ ] Create `image-rag-dev` service (branch: develop)
- [ ] Create `image-rag-prod` service (branch: master)
- [ ] Disable auto-deploy on both
- [ ] Copy service IDs to GitHub
- [ ] Add environment variables to each service

---

## Troubleshooting

### "RENDER_SERVICE_ID not set"
- Add as **environment variable** in the GitHub Environment
- Each environment needs its own `RENDER_SERVICE_ID`

### "RENDER_API_KEY not set"
- Add as **repository secret**
- Generate at: https://dashboard.render.com/u/settings#api-keys

### Health check fails
- Render free tier spins down after 15 min
- Cold start takes ~30 seconds
- Verify `/actuator/health` endpoint exists

### Workflow not triggering
- Check the `paths` filter in the workflow
- Ensure branch names match (master/develop/staging)
- For chain triggers, verify the previous workflow succeeded

### Environment not resolved correctly
- Check the branch name pattern in the resolve step
- PRs use `GITHUB_HEAD_REF`, pushes use `GITHUB_REF_NAME`
- For `workflow_run`, use `github.event.workflow_run.head_branch`
