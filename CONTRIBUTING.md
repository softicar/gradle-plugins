# How to Contribute to the SoftiCAR Gradle Plugins

First of all, thank you for taking the time to contribute!

## Bug Report

Before reporting a bug, please check that the bug was not reported before, by searching under [GitHub Issues](../../issues).

When filing a bug report, try to describe the easiest way to reproduce the problem, e.g. screenshots are often helpful.
The easier to understand and reproduce the bug, the higher the changes that someone will take a look at it and fix it.

Also describe what you expected in contrast to what actually happend. You can skip that if the bug is obvious, e.g. some internal error.

## Feature Request

Before asking for new features, please check that the feature was not already requested before, by searching under [GitHub Issues](../../issues).

To request new features or changes to existing features, create an issue in the form of a user story, that is, specify who wants to do what and why?

## Pull Request

Every pull request shall refer to an issue. The issue number shall be given as a prefix to the title.

We are currently using [JIRA](https://www.atlassian.com/software/jira) for issue management internally. The prefix `PLAT-` plus the respective issue number shall be used, e.g. `PLAT-123`, `PLAT-345`. 

For pull requests referencing a [GitHub Issue](../../issues), the prefix shall be `i` plus the respective issue number, e.g. `i123`, `i345`.

After the prefix, a short title shall be given in [title case](https://en.wikipedia.org/wiki/Title_case), e.g. `PLAT-123 Cool New Feature` or `i345 Fixed Login Bug`. We aim for a clean *Git* history that speaks for itself, and the commit titles shall be given with that in mind. Meaningless titles like `Updates` or `Changes` shall not be used.

Furthermore, a pull request shall describe how the changes were validated, i.e. a test plan shall be given. A test plan is not required for purely mechanical refactorings (e.g. automated renaming, moving, etc.) or changes to non-code files. For changes sufficiently covered by unit tests (or similar automated tests), it suffices to refer to those tests in the description, e.g. `Test Plan: see new unit tests`.

## Documentation

Please consult the [README](README.md) file, checkout our [Wiki pages](../../wiki) or start a [Discussion](../../discussions).
