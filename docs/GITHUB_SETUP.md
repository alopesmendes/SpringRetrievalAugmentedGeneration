# GitHub Environment Setup Guide

## Overview

This project uses **GitHub Environments** for secrets/variables and **Render Free Plan** with 2 services mapped to 4 environments.

```
development ──┐                       staging ──┐
              ├──→ api-image-rag-dev            ├──→ api-image-rag
test ─────────┘                       prod ─────┘
```

## Deployment Flow

```
┌──────────┐
│ Security │ (Independent)
└──────────┘

┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│   Lint   │────▶│  Build   │────▶│   Test   │────▶│ Coverage │────▶│  Deploy  │
└──────────┘     └──────────┘     └──────────┘     └──────────┘     └──────────┘
                      │                                              (Push only)
                      ▼
                 ┌──────────┐
                 │ Analysis │
                 └──────────┘
```

---

## Step 1: Create GitHub Environments

Go to: **Repository → Settings → Environments**

- [ ] Create `development` environment
- [ ] Create `test` environment
- [ ] Create `staging` environment
- [ ] Create `prod` environment

---

## Step 2: Add Repository Secrets

Go to: **Repository → Settings → Secrets and variables → Actions → Secrets**

- [ ] `RENDER_API_KEY` - Generate at [Render Dashboard](https://dashboard.render.com/u/settings#api-keys)
- [ ] `CODECOV_TOKEN` - From Codecov dashboard

---

## Step 3: Add Environment Secrets

For **each** environment, add these secrets:

| Secret           | Description                               |
|------------------|-------------------------------------------|
| `JWT_SECRET`     | Unique per env: `openssl rand -base64 64` |
| `PROJECT_TOKEN`  | GitHub PAT for deployment tracking        |
| `RENDER_API_KEY` | Render API key                            |

### Checklist per Environment

| Secret           | development | test | staging | prod |
|------------------|:-----------:|:----:|:-------:|:----:|
| `JWT_SECRET`     |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `PROJECT_TOKEN`  |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `RENDER_API_KEY` |     [ ]     | [ ]  |   [ ]   | [ ]  |

---

## Step 4: Add Environment Variables

For **each** environment, add these variables:

| Variable                     | development                              | test            | staging                              | prod        |
|------------------------------|------------------------------------------|-----------------|--------------------------------------|-------------|
| `ACTUATOR_ENDPOINTS`         | `*`                                      | `health`        | `health`                             | `health`    |
| `ACTUATOR_HEALTH_COMPONENTS` | `always`                                 | `always`        | `always`                             | `never`     |
| `ACTUATOR_HEALTH_DB`         | `false`                                  | `false`         | `false`                              | `false`     |
| `ACTUATOR_HEALTH_DETAILS`    | `always`                                 | `always`        | `when_authorized`                    | `never`     |
| `ACTUATOR_HEALTH_MONGO`      | `false`                                  | `false`         | `false`                              | `false`     |
| `APP_ENVIRONMENT`            | `development`                            | `test`          | `staging`                            | `prod`      |
| `APP_NAME`                   | `image-rag-dev`                          | `image-rag-dev` | `image-rag`                          | `image-rag` |
| `DEVTOOLS_ENABLED`           | `true`                                   | `false`         | `false`                              | `false`     |
| `ERROR_INCLUDE_EXCEPTION`    | `true`                                   | `false`         | `false`                              | `false`     |
| `ERROR_INCLUDE_STACKTRACE`   | `on-param`                               | `never`         | `never`                              | `never`     |
| `LOG_LEVEL_APP`              | `DEBUG`                                  | `INFO`          | `INFO`                               | `WARN`      |
| `LOG_LEVEL_ROOT`             | `INFO`                                   | `WARN`          | `WARN`                               | `ERROR`     |
| `LOG_LEVEL_SECURITY`         | `DEBUG`                                  | `WARN`          | `WARN`                               | `ERROR`     |
| `LOG_LEVEL_SPRING`           | `DEBUG`                                  | `WARN`          | `WARN`                               | `ERROR`     |
| `LOG_LEVEL_WEB`              | `DEBUG`                                  | `WARN`          | `WARN`                               | `ERROR`     |
| `RENDER_DOMAIN`              | `https://api-image-rag-dev.onrender.com` | (same)          | `https://api-image-rag.onrender.com` | (same)      |
| `RENDER_SERVICE_ID`          | `srv-xxx (dev)`                          | (same)          | `srv-yyy (prod)`                     | (same)      |
| `SERVER_PORT`                | `8080`                                   | `8080`          | `8080`                               | `8080`      |

### Checklist per Environment

| Variable                     | development | test | staging | prod |
|------------------------------|:-----------:|:----:|:-------:|:----:|
| `ACTUATOR_ENDPOINTS`         |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `ACTUATOR_HEALTH_COMPONENTS` |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `ACTUATOR_HEALTH_DB`         |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `ACTUATOR_HEALTH_DETAILS`    |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `ACTUATOR_HEALTH_MONGO`      |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `APP_ENVIRONMENT`            |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `APP_NAME`                   |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `DEVTOOLS_ENABLED`           |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `ERROR_INCLUDE_EXCEPTION`    |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `ERROR_INCLUDE_STACKTRACE`   |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `LOG_LEVEL_APP`              |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `LOG_LEVEL_ROOT`             |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `LOG_LEVEL_SECURITY`         |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `LOG_LEVEL_SPRING`           |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `LOG_LEVEL_WEB`              |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `RENDER_DOMAIN`              |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `RENDER_SERVICE_ID`          |     [ ]     | [ ]  |   [ ]   | [ ]  |
| `SERVER_PORT`                |     [ ]     | [ ]  |   [ ]   | [ ]  |

---

## Step 5: Render Setup

### 5.1 Create Web Services

| Service Name        | Branch  | Runtime | Plan |
|---------------------|---------|---------|------|
| `api-image-rag-dev` | develop | Docker  | Free |
| `api-image-rag`     | master  | Docker  | Free |

- [ ] Create `api-image-rag-dev` service
- [ ] Create `api-image-rag` service
- [ ] Disable **Auto-Deploy** on both services

### 5.2 Get Service IDs

1. Open service in Render Dashboard
2. Copy ID from URL: `https://dashboard.render.com/web/srv-XXXXXXXXXX`
3. Add to GitHub environment variables

- [ ] Copy dev service ID → `RENDER_SERVICE_ID` in `development` and `test`
- [ ] Copy prod service ID → `RENDER_SERVICE_ID` in `staging` and `prod`

### 5.3 Add Render Environment Variables

For **each** Render service:

```bash
SERVER_PORT=10000
SPRING_PROFILES_ACTIVE=dev  # or 'prod'
JWT_SECRET=<from-github>
SPRING_DATA_MONGODB_URI=<mongodb-atlas-uri>
```

- [ ] Configure `api-image-rag-dev` environment
- [ ] Configure `api-image-rag` environment

---

## Environment Mapping Summary

| GitHub Environment | Render Service      | Branch    | Deploy Trigger |
|--------------------|---------------------|-----------|----------------|
| `development`      | api-image-rag-dev   | feature/* | Manual         |
| `test`             | api-image-rag-dev   | develop   | On push        |
| `staging`          | api-image-rag       | staging   | On push        |
| `prod`             | api-image-rag       | master    | On push        |

---

## Troubleshooting

| Issue                       | Solution                                                    |
|-----------------------------|-------------------------------------------------------------|
| `RENDER_SERVICE_ID not set` | Add as environment variable in GitHub Environment           |
| `RENDER_API_KEY not set`    | Add as repository secret                                    |
| Health check fails          | Free tier cold start ~30s, verify `/actuator/health` exists |
| Workflow not triggering     | Check `paths` filter and branch names                       |
