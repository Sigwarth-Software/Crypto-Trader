# Contributing

Crypto Trader welcomes authorized contributions. At Sigwarth Software, we have
conventions and standards required for contributions.

## Branch Names

All branches besides `main` and `development` should be based on an issue
created in the `Crypto Trader Development` project. This issue number will be
used as a suffix. The prefix should be the type of change being made.

`hotfix-{issue number}`
`issue-{issue number}`

This will allow us to easily track the issue and what the purpose for
branching was.

## Commit Messages

Commit messages at Sigwarth Software are granular. We do this to make changes
easier to track, and to make any rollbacks easier and more reliable.

1. Commits should generally be one file
  - If the commit is a very large change, selecting all relevant files is
    permitted. An example of this would be bumping versions of dependencies
    before release.
2. Commits should include the file name
3. Commits, in general, should include what changed, and why

Examples:

>Restrict usage of `User` model for type checking only to prevent module initialization errors in paycheck.py

>Change the return type to `String` to be more generalized for the pie chart mapping in GraphController.java

>Bump project versions accross all pom.xml files to prepare for release

## Issues

There documents in the repository will guide you in particular on how to
format your issues. In general, Crypto Trader issues are user stories. Refer
to the following templates for issue formatting:

- [User Story](./USER_STORY_ISSUE_TEMPLATE.md)
- [Generic Issue](./ISSUE_TEMPLATE.md)

## Pull Requests

Pull request titles should be all capitals of the issue type with the number.
Then, it should be followed by a colon and a brief description of the change.

`ISSUE-31: Full account deletion`
`ISSUE-53: Add support for Binance`
`HOTFIX-12: Patch security vulnerability in Dokka`

Within the body should be a relevant context for the reviewers to understand
your changes. Try your best to preempt any obvious questions but adding it to
the description. Then, there should be a bullet point list of the changes
made.