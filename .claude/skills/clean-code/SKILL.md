---
name: clean-code
description: Clean code principles for this project. Covers single responsibility, no god services, rule of three before abstraction, no premature abstractions, no unnecessary error handling, and naming clarity. Trigger when reviewing code structure, noticing a class doing too much, deciding whether to extract a helper, or when a method is growing too large.
---

# Clean Code Skill

One reason to change. Names reveal intent. No speculative code.

## Single Responsibility

One class, one reason to change. Signs of violation:

- Class name contains "And", "Manager", "Handler", "Helper", "Utils"
- Method count > 7 (rough signal, not hard rule)
- Constructor takes > 4 dependencies
- Multiple unrelated field groups

Fix: split by responsibility. Each piece should be nameable without "and".

```kotlin
// bad — two responsibilities
class UserService(repo: IUserRepository, mailer: IMailerService) {
    fun create(...) { ... }
    fun sendWelcomeEmail(...) { ... }
}

// good — separate use cases
class CreateUserUseCase(repo: IUserRepository) : ICreateUserUseCase { ... }
class SendWelcomeEmailUseCase(mailer: IMailerService) : ISendWelcomeEmailUseCase { ... }
```

## Rule of Three — no premature abstraction

First time: write it inline.
Second time: duplicate is OK.
Third time: extract.

```kotlin
// two similar mappers — fine
fun UserResult.toResponseDto(): UserResponseDto = ...
fun AdminResult.toResponseDto(): AdminResponseDto = ...

// third one appears — now consider a shared pattern
```

Never create a base class or generic helper for one or two use cases.

## No speculative code

Write for what exists now, not hypothetical futures.

- No `// future: add X` comments
- No `type` discriminator fields for a single type
- No strategy pattern for one implementation
- No feature flags unless the flag ships in the same PR

```kotlin
// bad — solving tomorrow's problem today
interface Notifier { fun notify(event: Event) }
class EmailNotifier : Notifier  // only one impl exists

// good — just the function
fun sendWelcomeEmail(email: Email) { ... }
```

## No unnecessary error handling

Only validate at system boundaries (user input, external APIs). Trust internal code.

```kotlin
// bad — domain trusts nothing
class CreateUserUseCase(...) {
    override fun invoke(cmd: CreateUserCommand): Result<UserResult> = runCatching {
        if (cmd.email == null) throw IllegalArgumentException("null email") // Email VO already prevents this
        ...
    }
}

// good — VO constructor handles shape; use case handles business rules only
override fun invoke(cmd: CreateUserCommand): Result<UserResult> = runCatching {
    repo.findByEmail(cmd.email)?.let { throw UserException.EmailAlreadyExists(it.email) }
    repo.save(User.create(cmd.email, cmd.name, cmd.hash, cmd.age)).toUserResult()
}
```

## Naming

Names should be self-documenting. No comments needed if name is good.

| Bad          | Good                   |
|--------------|------------------------|
| `data`       | `userDocument`         |
| `process()`  | `hashPassword()`       |
| `handleIt()` | `translateToCommand()` |
| `flag`       | `isEmailVerified`      |
| `list`       | `activeUsers`          |

Booleans: `is`, `has`, `can` prefix. Never `flag`, `status`, `check`.

## Method length

Methods > 20 lines are a smell. Extract when:
- A block has a conceptual name you'd put in a comment
- It can be tested independently
- It's reused in two other places (rule of three)

```kotlin
// long
override fun invoke(cmd: CreateUserCommand): Result<UserResult> = runCatching {
    val emailRegex = Regex("...")
    if (!cmd.email.value.matches(emailRegex)) throw ...
    val existingUser = repo.findByEmail(cmd.email)
    if (existingUser != null) throw UserException.EmailAlreadyExists(cmd.email)
    val hash = hasher.hash(cmd.password)
    val user = User.create(cmd.email, cmd.name, hash, cmd.age)
    repo.save(user).toUserResult()
}

// extracted — email check belongs in Email VO init; rest is clear
override fun invoke(cmd: CreateUserCommand): Result<UserResult> = runCatching {
    repo.findByEmail(cmd.email)?.let { throw UserException.EmailAlreadyExists(cmd.email) }
    repo.save(User.create(cmd.email, cmd.name, hasher.hash(cmd.password), cmd.age)).toUserResult()
}
```

## No dead code

Remove unused: functions, parameters, imports, commented-out blocks, `_` variables.
Git history holds the past. No `// removed` comments needed.

## Comments — when to write

Write only when WHY is non-obvious: hidden constraint, workaround for external bug, subtle invariant.

```kotlin
// bad — explains what, not why
// increment counter
counter++

// good — explains why (non-obvious constraint)
// Mongo ObjectId requires 24-char hex; prefix with zero-pad if shorter
val paddedId = id.padStart(24, '0')
```

Never: task references, caller names, "added for X feature" — these rot.
