# Git Conventions

This document describes the git workflow, branch naming conventions, and commit message format used in this project.

---

## Commit Message Guidelines

| Do ✅                                     | Don't ❌                                      |
|------------------------------------------|----------------------------------------------|
| Use imperative mood: "add feature"       | Use past tense: "added feature"              |
| Keep first line under 72 characters      | End subject line with a period               |
| Capitalize the message: "Add feature"    | Write vague messages: "fix bug", "update"    |
| Be specific: "add user login validation" | Include issue number manually (hook adds it) |

---

## Commit Message Format

### Structure

```
[gitmoji] prefix(#issue): message

[optional body]

[optional footer]
```

| Component  | Required | Description                         |
|------------|----------|-------------------------------------|
| `gitmoji`  | Optional | Emoji or `:shortcode:` at the start |
| `prefix`   | Yes      | Type of change (feat, fix, etc.)    |
| `(#issue)` | Yes      | Issue/ticket number from branch     |
| `message`  | Yes      | Short description (imperative mood) |
| `body`     | Optional | Detailed explanation                |
| `footer`   | Optional | Breaking changes, references        |

### Automatic Formatting

The `prepare-commit-msg` hook automatically formats your commits:

| You Write                 | Without Gitmoji                       | With Shortcode                       | With Unicode                   |
|---------------------------|---------------------------------------|--------------------------------------|--------------------------------|
| `add user authentication` | `feat(#123): add user authentication` | —                                    | —                              |
| `resolve null pointer`    | `fix(#456): resolve null pointer`     | —                                    | —                              |
| `:sparkles: add feature`  | —                                     | `:sparkles: feat(#123): add feature` | —                              |
| `:bug: fix validation`    | —                                     | `:bug: fix(#456): fix validation`    | —                              |
| `✨ add feature`           | —                                     | —                                    | `✨ feat(#123): add feature`    |
| `🐛 fix validation`       | —                                     | —                                    | `🐛 fix(#456): fix validation` |

**Override Branch Prefix:** Use explicit prefix like `fix: correct typo` → `fix(#123): correct typo`

### Multi-line Commits

```bash
git commit -m ":sparkles: add user authentication" -m "
- Implement JWT token generation
- Add password hashing with bcrypt

Closes #123
"
```

---

## Branch Strategy

### Main Branches

| Branch    | Purpose                         | Protected |
|-----------|---------------------------------|-----------|
| `master`  | Production-ready code           | ✅ Yes     |
| `develop` | Integration branch for features | ✅ Yes     |
| `staging` | Pre-production testing          | ✅ Yes     |

### Working Branches

Format: `prefix/issue-number-short-description`

| Prefix     | Purpose                 | Example                            |
|------------|-------------------------|------------------------------------|
| `feat`     | New feature             | `feat/123-add-user-authentication` |
| `fix`      | Bug fix                 | `fix/456-resolve-login-error`      |
| `hotfix`   | Critical production fix | `hotfix/789-patch-security-vuln`   |
| `chore`    | Maintenance tasks       | `chore/101-update-dependencies`    |
| `docs`     | Documentation changes   | `docs/202-update-readme`           |
| `refactor` | Code refactoring        | `refactor/404-extract-service`     |

### Branch Naming Rules

| Rule                  | Good ✅                   | Bad ❌                                     |
|-----------------------|--------------------------|-------------------------------------------|
| Lowercase only        | `feat/123-add-login`     | `Feat/123-Add-Login`                      |
| Hyphens for spaces    | `feat/123-add-user-auth` | `feat/123_add_user_auth`                  |
| Issue number required | `feat/123-description`   | `feat/description`                        |
| Short and descriptive | `feat/123-add-login`     | `feat/123-add-the-new-user-login-feature` |

---

## Hook Installation

```bash
# Already calls the scripts to setup lint and prepare-commit-msg
./setup.sh
```

### Verify Installation

```bash
ls -la .git/hooks/prepare-commit-msg

# Test on a feature branch
git checkout -b feat/999-test
git commit --allow-empty -m "test commit"
git log -1 --oneline  # Should show: feat(#999): test commit

# Cleanup
git checkout develop && git branch -D feat/999-test
```
