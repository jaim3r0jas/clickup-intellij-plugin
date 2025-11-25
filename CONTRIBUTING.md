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

If you find a bug, please report it using our standardized bug report template:

1. Navigate to the [Issues page](https://github.com/rojas-safenow/clickup-integration/issues/new/choose)
2. Select **"Bug Report"** from the available templates
3. Fill out all required fields, including:
    - Detailed description of the bug
    - Steps to reproduce
    - Expected vs. actual behavior
    - Your IDE version (IntelliJ IDEA, PhpStorm, WebStorm, etc.)
    - Plugin version
    - Operating system and Java version
    - Relevant logs and stack traces
4. Submit the issue

**Tip:** The more detailed information you provide, the faster we can identify and fix the issue.

### Suggesting Enhancements

Have an idea to improve the plugin? We'd love to hear it! Submit a feature request using our template:

1. Navigate to the [Issues page](https://github.com/rojas-safenow/clickup-integration/issues/new/choose)
2. Select **"Feature Request"** from the available templates
3. Provide detailed information about:
    - What problem the feature would solve
    - Your proposed solution
    - Specific use cases
    - Expected benefits
    - Which IDEs should support this feature
4. Submit the issue

Feature requests with clear use cases and benefits are more likely to be prioritized and implemented.

### Submitting Pull Requests

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature-name`).
3. Make your changes.
4. Commit your changes (`git commit -m 'Add some feature'`).
5. Push to the branch (`git push origin feature/your-feature-name`).
6. Open a pull request.

Please ensure your pull request adheres to the following guidelines:

- Include a clear description of the changes.
- Reference any related issues (e.g., "Fixes #123" or "Addresses #456").
- Ensure your code follows the project's style guides.
- Add or update tests as necessary.
- Ensure all tests pass and code coverage requirements are met.
- Verify code quality with Qodana checks.

## Development Setup

1. Clone the repository: `git clone https://github.com/rojas-safenow/clickup-integration.git`
2. Open the project in IntelliJ IDEA.
3. Ensure you have the required plugins installed:
    - Gradle
    - Java
    - Kotlin
4. Build the project using Gradle: `./gradlew build`
5. Run tests with coverage: `./gradlew koverHtmlReport`
6. Run the IDE with the plugin: `./gradlew runIde`

## Style Guides

### Git Commit Messages

- Use the present tense ("Add feature" not "Added feature").
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...").
- Limit the first line to 72 characters or less.
- Reference issues and pull requests liberally.

### Java and Kotlin Style Guide

- Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Code quality is validated using Qodana in our CI pipeline.

## Additional Resources

- **Discussions:** For questions and community discussion, visit our [Discussions page](https://github.com/rojas-safenow/clickup-integration/discussions)
- **Documentation:** Read the [README](README.md) for setup and usage instructions
- **Security Issues:** Report security vulnerabilities privately through [GitHub Security Advisories](https://github.com/rojas-safenow/clickup-integration/security/advisories/new)

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).
