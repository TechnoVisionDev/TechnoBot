# Contributing to TechnoBot

We love your input! We want to make contributing to this project as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features

## We develop with GitHub

We use GitHub to host code, track issues and feature requests, and accept pull requests. To discuss development less
formally, please [join the TechnoBot Discord server](https://discord.gg/SfE4vYfJ8w).

## We use [GitHub Flow](https://guides.github.com/introduction/flow/index.html), so all code changes happen through pull requests

Pull requests are the best way to propose changes to the codebase (we
use [GitHub Flow](https://guides.github.com/introduction/flow/index.html)). We actively welcome your pull requests:

1. Fork the repo and create your branch from `master`.
2. Comment your code and commit your changes.
3. If you've added code that should be tested, **test it!** For help running the bot on your computer,
   see [the instructions here](#instructions-for-building-and-running-technobot-on-your-computer).
4. Issue the pull request!

## Any contributions you make will be under the Attribution-NonCommercial-ShareAlike 4.0 International License

When you submit code changes, your submissions are understood to be under the
same [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/) that covers the project. Feel free to contact
the maintainers if that's a concern.

## Report bugs using GitHub's [issues](https://github.com/TechnoVisionDev/TechnoBot/issues)

We use GitHub issues to track public bugs. Report a bug
by [opening a new issue](https://github.com/TechnoVisionDev/TechnoBot/issues/new/choose); it's that easy!

## Write bug reports with detail, background, and sample code

[This is an example](http://stackoverflow.com/q/12488905/180626) of a well-written bug report. A bug report for
TechnoBot will not look exactly the same, but the important parts in the example are the expected behavior and the
actual behavior explained completely and concisely so anyone can reproduce the issue.

**Great Bug Reports** tend to have:

- A quick summary and/or background
- Steps to reproduce
    - Be specific!
    - Give sample code if you can. (This may not apply to TechnoBot because it is not an API)
- What you expected would happen
- What actually happens
- Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)

## Use a consistent coding style

<details>
<summary>Java Style Guidelines</summary>
1. The order of members in a class **must** be as follows:
- Static Fields
- Static Initializers
- Non-Static Fields
- Non-Static Initializers
- Constructors
- Static Methods
- Non-Static Methods
- Types (Inner Classes)

2. The order within each of these groups of members **must** be as follows:

- Public
- Protected
- Package
- Private

3. You **must** use spaces in place of tabs and the tab width will be 4 spaces.

4. You **must** follow these braces rules:

- Braces should be on the end of the current line instead of a new line
- Braces do not need to be used if a jump keyword is used, and it is the only line after the statement.
- `else`, `catch`, `finally`, `while` and `do...while` loops must go on the same line as the closing brace.

5. The default file encoding **must** be UTF-8.
6. You **must** use Java. **Not** Kotlin, Groovy, Scala or any other Java Bytecode based language (for now).

7. You **must** follow the Oracle Java Naming Conventions which are as follows:

- Packages: lower_snake_case
- Classes: UpperCamelCase (Avoid abbreviations unless the acronym or abbreviation is more widely used than the long
  form. For example HTML or URI/URL/URN)
- Interfaces: UpperCamelCase (Should describe what the interface does; do not prefix with I)
- Methods: camelCase (Should be verbs and well describe what the method does)
- Constants: UPPER_SNAKE_CASE - Sometimes known as SCREAMING_SNAKE_CASE (ANSI Constants should be avoided)
- Variables: camelCase (Should not start with `\_` or `$` and should not be a singular letter unless the letter makes
  sense. For example `int x = getPosX()`. An example of a violation would be `int j = getFooBar() * WIBBLE_WOBBLE`)

8. Java Identifiers **must** follow the following order:

- public/private/protected
- abstract
- static
- final
- transient
- volatile
- default
- synchronized
- native
- strictfp

9. Imports **must** be formatted as follows:

- Sorted:
    - Non-Static Imports
    - Static Imports
    - Package Origin according to the following order:
        - `java` packages
        - `javax` packages
        - external packages (e.g. `org.xml`)
        - internal packages (e.g. `com.sun`)
- Imports should not be line-wrapped, no matter the length.
- No unused imports should be present.
- Wildcard imports should be avoided unless a very large number of classes(dozens) are imported from that package.
- No more than 1 wildcard import per file should exist.

10. Case lines **must** be indented with a (4 space) tab.
11. Square brackets for arrays **must** be at the type and not at the variable.
12. Annotations **must** be on a separate line from the declaration unless it is annotating a parameter.
13. Lambda Expressions **must** not contain a body if the body is only 1 line.
14. Method References **must** be used in replacement for a lambda expression when possible.
15. Parameter Types in lambda expressions **must** be omitted unless they increase readability.
16. Dedent Parenthesis **must** be removed unless they are clearly increasing readability.
17. Long literals **must** use uppercase `L`.
18. Hexadecimal literals **must** use uppercase `A-F`.
19. All other literals **must** use lowercase letters.

For any other niche cases, refer to https://cr.openjdk.java.net/~alundblad/styleguide/index-v6.html
</details>

## Instructions for building and running TechnoBot on your computer

**!!!INSERT GOOD INSTRUCTIONS HERE!!!**

### Creating the bot

### Configuring the environment variables

### Running the bot

#### Using Docker

#### Manually configuring MongoDB

## References

This document is based on [this template](https://gist.github.com/briandk/3d2e8b3ec8daf5a27a62).
That template was adapted from the open-source contribution guidelines
for [Facebook's Draft](https://github.com/facebook/draft-js/blob/a9316a723f9e918afde44dea68b5f9f39b7d9b00/CONTRIBUTING.md)
.