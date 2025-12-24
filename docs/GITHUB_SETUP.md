# GitHub Environment Setup Guide

## Overview

This project uses **GitHub Environments** to manage secrets and variables.
Deployment uses **Render Free Plan** with 2 services mapped to 4 GitHub environments.

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
| `APP_ENVIRONMENT`          | development                        | test          | staging                        | prod              |
| `APP_NAME`                 | image-rag                          | image-rag     | image-rag                      | image-rag         |
| `LOG_LEVEL_APP`            | DEBUG                              | INFO          | INFO                           | WARN              |
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
4. Add to GitHub:
    - Dev service → `RENDER_SERVICE_ID_DEV`
    - Prod service → `RENDER_SERVICE_ID_PROD`

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

| GitHub Environment | Render Service | Branch    | Trigger |
|--------------------|----------------|-----------|---------|
| `development`      | image-rag-dev  | feature/* | Manual  |
| `test`             | image-rag-dev  | develop   | On push |
| `staging`          | image-rag-prod | staging   | On push |
| `prod`             | image-rag-prod | master    | On push |

---

## Deployment Flow

```
┌───────────────────────────────────────────────────────────────────────────┐
│                        DEPLOYMENT FLOW                                    │
├───────────────────────────────────────────────────────────────────────────┤
│                                                                           │
│   ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐         │
│   │  Lint    │────▶│  Build   │────▶│  Test    │────▶│  Deploy  │         │
│   └──────────┘     └──────────┘     └──────────┘     └──────────┘         │
│                                                            │              │
│                                            ┌───────────────┴──────┐       │
│                                            ▼                      ▼       │
│                                     image-rag-dev          image-rag-prod │
│                                     (dev/test)            (staging/prod)  │
└───────────────────────────────────────────────────────────────────────────┘
```

---

## Complete Setup Checklist

### GitHub Repository Level
- [ ] Add `RENDER_API_KEY` to repository secrets
- [ ] Add `RENDER_SERVICE_ID_DEV` to repository variables
- [ ] Add `RENDER_SERVICE_ID_PROD` to repository variables

### GitHub Environments (for each of 4 environments)
- [ ] Create environment: `development`, `test`, `staging`, `prod`
- [ ] Add `OPENAI_API_KEY` secret
- [ ] Add `JWT_SECRET` secret (unique per environment!)
- [ ] Add `RENDER_DOMAIN` variable
- [ ] Add `APP_ENVIRONMENT` variable
- [ ] Configure branch protection for `prod`

### Render (2 services)
- [ ] Create `image-rag-dev` service (branch: develop)
- [ ] Create `image-rag-prod` service (branch: master)
- [ ] Disable auto-deploy on both
- [ ] Copy service IDs to GitHub
- [ ] Add environment variables to each service

---

## Troubleshooting

### "RENDER_SERVICE_ID_DEV/PROD not set"
- Add as **repository variable** (not environment variable)
- Go to: Settings → Secrets and variables → Actions → Variables

### "RENDER_API_KEY not set"
- Add as **repository secret**
- Generate at: https://dashboard.render.com/u/settings#api-keys

### Health check fails
- Render free tier spins down after 15 min
- Cold start takes ~30 seconds
- Verify `/actuator/health` endpoint exists

### Wrong service deployed
- Check the environment-to-service mapping in Deploy.yml
- Verify branch triggers in workflow
