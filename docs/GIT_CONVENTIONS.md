# Git Conventions

This document describes the git workflow, branch naming conventions, and commit message format used in this project.

---

## Branch Strategy

### Main Branches

| Branch            | Purpose                         | Protected |
|-------------------|---------------------------------|-----------|
| `main` / `master` | Production-ready code           | ✅ Yes     |
| `develop`         | Integration branch for features | ✅ Yes     |
| `staging`         | Pre-production testing          | ✅ Yes     |

### Working Branches

All working branches **must** follow the naming convention:

```
prefix/issue-number-short-description
```

#### Supported Prefixes

| Prefix     | Purpose                         | Example                                   |
|------------|---------------------------------|-------------------------------------------|
| `feat`     | New feature                     | `feat/123-add-user-authentication`        |
| `fix`      | Bug fix                         | `fix/456-resolve-login-error`             |
| `hotfix`   | Critical production fix         | `hotfix/789-patch-security-vulnerability` |
| `chore`    | Maintenance tasks               | `chore/101-update-dependencies`           |
| `docs`     | Documentation changes           | `docs/202-update-readme`                  |
| `style`    | Code style changes (formatting) | `style/303-fix-indentation`               |
| `refactor` | Code refactoring                | `refactor/404-extract-service-layer`      |
| `test`     | Adding or updating tests        | `test/505-add-unit-tests`                 |
| `perf`     | Performance improvements        | `perf/606-optimize-query`                 |
| `ci`       | CI/CD changes                   | `ci/707-add-github-actions`               |
| `build`    | Build system changes            | `build/808-update-gradle-config`          |
| `revert`   | Revert previous changes         | `revert/909-undo-breaking-change`         |

### Branch Naming Rules

- **Lowercase only**: `feat/123-add-login` ✅ | `Feat/123-Add-Login` ❌
- **Hyphens for spaces**: `feat/123-add-user-auth` ✅ | `feat/123_add_user_auth` ❌
- **Issue number required**: `feat/123-description` ✅ | `feat/description` ❌
- **Short and descriptive**: `feat/123-add-login` ✅ | `feat/123-add-the-new-user-login-feature-to-the-app` ❌

---

## Commit Message Format

### Structure

```
[gitmoji] prefix(#issue): message

[optional body]

[optional footer]
```

### Components

| Component  | Required   | Description                         |
|------------|------------|-------------------------------------|
| `gitmoji`  | ❌ Optional | Emoji or `:shortcode:` at the start |
| `prefix`   | ✅ Yes      | Type of change (feat, fix, etc.)    |
| `(#issue)` | ✅ Yes      | Issue/ticket number from branch     |
| `message`  | ✅ Yes      | Short description (imperative mood) |
| `body`     | ❌ Optional | Detailed explanation                |
| `footer`   | ❌ Optional | Breaking changes, references        |

### Automatic Formatting

The `prepare-commit-msg` hook automatically formats your commits:

#### Without Gitmoji

| You Write                 | Git Saves                             |
|---------------------------|---------------------------------------|
| `add user authentication` | `feat(#123): add user authentication` |
| `resolve null pointer`    | `fix(#456): resolve null pointer`     |
| `update dependencies`     | `chore(#789): update dependencies`    |

#### With Gitmoji (Shortcode)

| You Write                | Git Saves                             |
|--------------------------|---------------------------------------|
| `:sparkles: add feature` | `:sparkles: feat(#123): add feature`  |
| `:bug: fix validation`   | `:bug: fix(#456): fix validation`     |
| `:wrench: update config` | `:wrench: chore(#789): update config` |

#### With Gitmoji (Unicode)

| You Write           | Git Saves                       |
|---------------------|---------------------------------|
| `✨ add feature`     | `✨ feat(#123): add feature`     |
| `🐛 fix validation` | `🐛 fix(#456): fix validation`  |
| `🔧 update config`  | `🔧 chore(#789): update config` |

#### Override Branch Prefix

If you need a different prefix than your branch suggests:

| Branch             | You Write                 | Git Saves                       |
|--------------------|---------------------------|---------------------------------|
| `feat/123-feature` | `fix: correct typo`       | `fix(#123): correct typo`       |
| `feat/123-feature` | `:bug: fix: correct typo` | `:bug: fix(#123): correct typo` |

---

## Gitmoji Reference

Common gitmojis used in this project:

| Emoji | Shortcode                     | When to Use              |
|-------|-------------------------------|--------------------------|
| ✨     | `:sparkles:`                  | New feature              |
| 🐛    | `:bug:`                       | Bug fix                  |
| 🔥    | `:fire:`                      | Remove code/files        |
| 📝    | `:memo:`                      | Documentation            |
| 🎨    | `:art:`                       | Improve structure/format |
| ⚡     | `:zap:`                       | Performance improvement  |
| 🔧    | `:wrench:`                    | Configuration changes    |
| 🔨    | `:hammer:`                    | Development scripts      |
| ♻️    | `:recycle:`                   | Refactor code            |
| ✅     | `:white_check_mark:`          | Add/update tests         |
| 🔒    | `:lock:`                      | Security fix             |
| ⬆️    | `:arrow_up:`                  | Upgrade dependencies     |
| ⬇️    | `:arrow_down:`                | Downgrade dependencies   |
| 🚀    | `:rocket:`                    | Deploy                   |
| 💄    | `:lipstick:`                  | UI/style updates         |
| 🎉    | `:tada:`                      | Initial commit           |
| 🚧    | `:construction:`              | Work in progress         |
| 💚    | `:green_heart:`               | Fix CI build             |
| 👷    | `:construction_worker:`       | CI changes               |
| 📦    | `:package:`                   | Build changes            |
| 🔀    | `:twisted_rightwards_arrows:` | Merge branches           |
| ⏪     | `:rewind:`                    | Revert changes           |
| 🗑️   | `:wastebasket:`               | Deprecate code           |

Full list: [gitmoji.dev](https://gitmoji.dev/)

---

## Commit Message Guidelines

### Do's ✅

- **Use imperative mood**: "add feature" not "added feature"
- **Keep first line under 72 characters**
- **Capitalize the message**: "Add feature" not "add feature"
- **Be specific**: "add user login validation" not "update code"

### Don'ts ❌

- Don't end the subject line with a period
- Don't use past tense: "added", "fixed", "updated"
- Don't write vague messages: "fix bug", "update", "changes"
- Don't include issue number manually (hook adds it)

### Examples

**Good commits:**

```bash
git commit -m ":sparkles: add JWT authentication"
# → :sparkles: feat(#123): add JWT authentication

git commit -m "fix: resolve race condition in cache"
# → fix(#123): resolve race condition in cache

git commit -m ":memo: document API endpoints"
# → :memo: docs(#456): document API endpoints
```

**Bad commits:**

```bash
# Too vague
git commit -m "fix bug"

# Past tense
git commit -m "added new feature"

# Manual issue number (hook handles this)
git commit -m "feat(#123): add feature"
```

---

## Multi-line Commits

For complex changes, add a body:

```bash
git commit -m ":sparkles: add user authentication" -m "
- Implement JWT token generation
- Add password hashing with bcrypt
- Create login/logout endpoints

Closes #123
"
```

Result:

```
:sparkles: feat(#123): add user authentication

- Implement JWT token generation
- Add password hashing with bcrypt
- Create login/logout endpoints

Closes #123
```

---

## Workflow Example

```bash
# 1. Create branch from develop
git checkout develop
git pull origin develop
git checkout -b feat/123-add-user-login

# 2. Make changes and commit
git add .
git commit -m ":sparkles: add login form"
# → :sparkles: feat(#123): add login form

git commit -m ":white_check_mark: add login tests"
# → :white_check_mark: feat(#123): add login tests

# 3. Push and create PR
git push origin feat/123-add-user-login

# 4. After PR approval, merge to develop
git checkout develop
git merge feat/123-add-user-login
git push origin develop

# 5. Delete feature branch
git branch -d feat/123-add-user-login
git push origin --delete feat/123-add-user-login
```

---

## Hook Installation

The `prepare-commit-msg` hook must be installed for automatic formatting:

```bash
# Option 1: Use installer script
./tools/scripts/install-prepare-commit-msg.sh

# Option 2: Manual installation
cp tools/scripts/prepare-commit-msg.sh .git/hooks/prepare-commit-msg
chmod +x .git/hooks/prepare-commit-msg
```

### Verify Installation

```bash
# Check hook exists
ls -la .git/hooks/prepare-commit-msg

# Test on a feature branch
git checkout -b feat/999-test
git commit --allow-empty -m "test commit"
git log -1 --oneline
# Should show: feat(#999): test commit

# Cleanup
git checkout develop
git branch -D feat/999-test
```

---

## Skipped Branches

The hook does **not** modify commits on these branches:

- `main` / `master`
- `develop`
- `staging`
- `release/*`

This allows direct commits without formatting on protected branches.
