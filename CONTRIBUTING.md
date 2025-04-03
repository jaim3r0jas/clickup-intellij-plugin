# Contributing to ClickUp Integration for JetBrains IDEs

Thank you for considering contributing to the ClickUp Integration for JetBrains IDEs! Your contributions are highly
appreciated. Below are the guidelines to help you get started.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How to Contribute](#how-to-contribute)
    - [Reporting Bugs](#reporting-bugs)
    - [Suggesting Enhancements](#suggesting-enhancements)
    - [Submitting Pull Requests](#submitting-pull-requests)
- [Development Setup](#development-setup)
- [Style Guides](#style-guides)
    - [Git Commit Messages](#git-commit-messages)
    - [Java and Kotlin Style Guide](#java-and-kotlin-style-guide)
- [License](#license)

## Code of Conduct

This project adheres to the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are
expected to uphold this code. Please report unacceptable behavior
to [jaimitorojas@gmail.com](mailto:jaimitorojas@gmail.com).

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue on
the [GitHub Issues](https://github.com/rojas-safenow/clickup-integration/issues) page. Provide detailed information
about the bug, including steps to reproduce, expected behavior, and screenshots if applicable.

### Suggesting Enhancements

If you have an idea for an enhancement, please create an issue on
the [GitHub Issues](https://github.com/rojas-safenow/clickup-integration/issues) page. Describe the enhancement in
detail and explain why it would be beneficial.

### Submitting Pull Requests

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature-name`).
3. Make your changes.
4. Commit your changes (`git commit -m 'Add some feature'`).
5. Push to the branch (`git push origin feature/your-feature-name`).
6. Open a pull request.

Please ensure your pull request adheres to the following guidelines:

- Include a clear description of the changes.
- Reference any related issues.
- Ensure your code follows the project's style guides.

## Development Setup

1. Clone the repository: `git clone https://github.com/rojas-safenow/clickup-integration.git`
2. Open the project in IntelliJ IDEA.
3. Ensure you have the required plugins installed:
    - Gradle
    - Java
    - Kotlin
4. Build the project using Gradle: `./gradlew build`
5. Run the IDE with the plugin: `./gradlew runIde`

## Style Guides

### Git Commit Messages

- Use the present tense ("Add feature" not "Added feature").
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...").
- Limit the first line to 72 characters or less.
- Reference issues and pull requests liberally.

### Java and Kotlin Style Guide

- Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).