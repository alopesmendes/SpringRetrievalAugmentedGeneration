# GitHub Environment Setup Guide

## Overview

This project uses **GitHub Environments** to manage secrets and variables per environment.
The `env.yml` and `deploy.yml` workflows automatically select the correct environment based on branch.

**Deployment Platform**: [Render](https://render.com) (Free Tier)

---

## Render Free Tier Limitations

| Feature       | Limit                                        |
|---------------|----------------------------------------------|
| Web Services  | 750 hours/month (shared across all services) |
| Spin-down     | After 15 min inactivity                      |
| Cold Start    | ~30 seconds                                  |
| RAM           | 512 MB                                       |
| Bandwidth     | 100 GB/month                                 |
| Build Minutes | Unlimited                                    |

> **Tip**: For development/test environments, cold starts are acceptable. For production, consider upgrading to the Starter plan ($7/month) for always-on services.

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

Add these **repository-level** secrets (shared across all environments):

| Secret           | Description                                      |
|------------------|--------------------------------------------------|
| `RENDER_API_KEY` | Render API key (shared across all environments)  |

### Generate Render API Key

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click your profile → **Account Settings**
3. Navigate to **API Keys**
4. Click **Create API Key**
5. Copy and save as `RENDER_API_KEY` in GitHub

---

## Step 3: Add Environment Secrets

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

## Step 4: Add Environment Variables

For **each** GitHub environment, add these variables:

### Render Variables (Required for Deployment)

| Variable              | Description                                    | Example                                      |
|-----------------------|------------------------------------------------|----------------------------------------------|
| `RENDER_SERVICE_ID`   | Service ID from Render dashboard               | `srv-abc123def456...`                        |
| `RENDER_DOMAIN`       | Public URL for health checks (optional)        | `https://image-rag-prod.onrender.com`        |

### Application Variables

| Variable                       | development | test      | staging   | prod      |
|--------------------------------|-------------|-----------|-----------|-----------|
| `APP_ENVIRONMENT`              | development | test      | staging   | prod      |
| `APP_NAME`                     | image-rag   | image-rag | image-rag | image-rag |
| `SERVER_PORT`                  | 10000       | 10000     | 10000     | 10000     |
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

## Step 5: Render Setup

### 5.1 Create Render Services

For each environment, create a separate web service:

1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click **New** → **Web Service**
3. Connect your GitHub repository
4. Configure the service:

| Setting         | Value                                             |
|-----------------|---------------------------------------------------|
| Name            | `image-rag-{environment}` (e.g., `image-rag-dev`) |
| Region          | Oregon (or closest to your users)                 |
| Branch          | Environment-specific branch (see table below)     |
| Runtime         | Docker                                            |
| Dockerfile Path | `./Dockerfile`                                    |
| Plan            | Free (or Starter for production)                  |

### 5.2 Branch Configuration per Environment

| GitHub Environment | Render Service Name | Branch   |
|--------------------|---------------------|----------|
| `development`      | image-rag-dev       | develop  |
| `test`             | image-rag-test      | develop  |
| `staging`          | image-rag-staging   | staging  |
| `prod`             | image-rag-prod      | master   |

### 5.3 Disable Auto-Deploy

Since we're using GitHub Actions for deployments:

1. Go to your Render service
2. Navigate to **Settings** → **Build & Deploy**
3. Set **Auto-Deploy** to **No**

### 5.4 Get Service ID

For each service:

1. Go to the service in Render Dashboard
2. Look at the URL: `https://dashboard.render.com/web/srv-XXXXXXXXXX`
3. Copy the `srv-XXXXXXXXXX` part
4. Add as `RENDER_SERVICE_ID` in the corresponding GitHub Environment

### 5.5 Configure Environment Variables in Render

For each Render service, add these environment variables in **Environment** tab:

```
# Application
SERVER_PORT=10000
SPRING_PROFILES_ACTIVE=<environment>  # dev, test, staging, prod

# Database (when ready)
SPRING_DATA_MONGODB_URI=<your-mongo-atlas-uri>

# AI (when ready)
OPENAI_API_KEY=<your-key>

# Security
JWT_SECRET=<your-secret>
```

### 5.6 MongoDB Setup (MongoDB Atlas - Free)

Since Render doesn't offer free MongoDB, use MongoDB Atlas:

1. Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create a free M0 cluster (512MB)
3. Create database user
4. Whitelist IP: `0.0.0.0/0` (for Render's dynamic IPs)
5. Get connection string and add to Render service as `SPRING_DATA_MONGODB_URI`

---

## Environment Mapping Summary

| GitHub Environment | Render Service    | Branch    | Trigger     |
|--------------------|-------------------|-----------|-------------|
| `development`      | image-rag-dev     | feature/* | Manual only |
| `test`             | image-rag-test    | develop   | On push     |
| `staging`          | image-rag-staging | staging   | On push     |
| `prod`             | image-rag-prod    | master    | On push     |

---

## Automatic Environment Selection

The `deploy.yml` workflow auto-selects environment based on branch:

| Trigger | Branch    | Environment |
|---------|-----------|-------------|
| Push    | master    | prod        |
| Push    | staging   | staging     |
| Push    | develop   | test        |
| PR      | any       | development |
| Push    | feature/* | development |

---

## Deployment Workflow

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DEPLOYMENT FLOW                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐   │
│   │  Lint    │────▶│  Build   │────▶│  Test    │────▶│  Deploy  │   │
│   └──────────┘     └──────────┘     └──────────┘     └──────────┘   │
│                                                            │        │
│                                                            ▼        │
│                                                     ┌──────────┐    │
│                                                     │  Verify  │    │
│                                                     └──────────┘    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Triggers

| Event             | Lint | Build | Test | Deploy       |
|-------------------|------|-------|------|--------------|
| PR opened/updated | ✅    | ✅     | ✅    | ❌            |
| Push to develop   | ✅    | ✅     | ✅    | ✅ test       |
| Push to staging   | ✅    | ✅     | ✅    | ✅ staging    |
| Push to master    | ✅    | ✅     | ✅    | ✅ prod       |
| Manual dispatch   | ✅    | ✅     | ✅    | ✅ (selected) |

---

## Complete Setup Checklist

### GitHub Setup

- [ ] Create 4 environments: `development`, `test`, `staging`, `prod`
- [ ] Add `RENDER_API_KEY` to repository secrets
- [ ] Add `OPENAI_API_KEY` to each environment (secrets)
- [ ] Add `JWT_SECRET` to each environment (secrets, unique per env!)
- [ ] Add `RENDER_SERVICE_ID` to each environment (variables)
- [ ] Add `RENDER_DOMAIN` to each environment (variables, optional)
- [ ] Add application variables to each environment
- [ ] Configure branch protection for `prod` environment

### Render Setup

- [ ] Create Render account
- [ ] Generate API key and save to GitHub
- [ ] Create 4 web services (one per environment)
- [ ] Disable auto-deploy on all services
- [ ] Copy service IDs to GitHub environments
- [ ] Configure environment variables in each service
- [ ] Set up MongoDB Atlas (free tier) for database

---

## Troubleshooting

### Deployment fails with "RENDER_API_KEY not set"

1. Verify the API key is added to **repository** secrets (not environment secrets)
2. Check the API key is valid (regenerate if needed)
3. Ensure the key has not expired

### Deployment fails with "RENDER_SERVICE_ID not set"

1. Verify the service ID is added to the correct GitHub **Environment**
2. Check the service ID format (should start with `srv-`)
3. Ensure the service exists in Render

### Health check fails

1. Render free tier services spin down after 15 min inactivity
2. First request after spin-down takes ~30 seconds
3. Verify the application exposes `/actuator/health` endpoint
4. Check `RENDER_DOMAIN` is set correctly (include `https://`)

### Build fails in Render

1. Check build logs in Render Dashboard
2. Ensure Dockerfile exists in repository root
3. Verify Java version compatibility (21)
4. Check Gradle wrapper is committed to repository

### Service shows "Suspended"

1. Free tier services may be suspended due to inactivity
2. Manually trigger a deploy to wake the service
3. Consider upgrading to Starter plan for production

### Cold start too slow

1. Free tier cold starts take ~30 seconds
2. Optimize JVM startup (already configured in Dockerfile)
3. Consider Starter plan ($7/month) for always-on services

---

## Cost Optimization Tips

1. **Use free tier wisely**: 750 hours/month shared across services
    - 4 services × 24 hours × 31 days = 2,976 hours needed
    - Free tier only covers ~25% of always-on for 4 services
    - Solution: Only keep staging/prod running continuously

2. **Database**: Use MongoDB Atlas free tier (512MB) instead of Render's paid databases

3. **Upgrade strategically**: Only upgrade production to Starter ($7/month)

4. **Monitor usage**: Check Render Dashboard → Usage regularly
