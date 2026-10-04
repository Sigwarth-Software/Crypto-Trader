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

### Drafting Issues/Tickets (including via AI agents/assistants)

Any request to create, write, draft, or propose a GitHub issue or ticket
(whether asked of a person or of an AI coding assistant, e.g. through
MCP/agent ticket tools such as `createIssue`, `confirmIssue`, `publishIssue`)
must be treated strictly as read-only ticket drafting, never as an
implementation task:

- Do not create, edit, or delete any source files (code, config,
  `module-info`, tests, etc.) while drafting a ticket, even if the user story
  describing the requested feature reads like an actionable spec (e.g. "I
  want a service that does X with method Y"). That text is content to put
  into the ticket body/checklist, not a to-do list to execute.
- Map every clause of the raw request into the ticket template's checklist
  (see [User Story](./USER_STORY_ISSUE_TEMPLATE.md)) as a concrete, testable
  task, and provide a justified hours-based point estimate instead of a
  generic one-line summary.
- After drafting the ticket, show the full proposed ticket (title, user
  story, checklist, estimate) to the requester and wait for explicit
  confirmation before confirming or publishing it. Never chain
  draft -> confirm -> publish automatically; publishing a public issue is an
  irreversible external action and requires explicit consent, similar to a
  `git push`.
- Once a ticket-drafting tool has been invoked for a request, do not call any
  file-editing tools for the remainder of that request — only
  ticket-management tools or user-facing communication are allowed.
- If interrupted or corrected during this flow (e.g. "you are implementing
  this instead of just drafting a ticket"), stop all exploratory and edit
  actions immediately, revert any files already touched, and confirm the
  working tree is clean before proceeding with the ticket-only flow.
- The drafted title, user story, checklist, and estimate must use proper
  Markdown (heading/bold text, a `- [ ]` checklist, code spans for file and
  class names, etc.) matching the structure of
  [User Story](./USER_STORY_ISSUE_TEMPLATE.md) or
  [Generic Issue](./ISSUE_TEMPLATE.md) — never a single unformatted paragraph
  or plain prose dump.
- Labels must always be set on the draft: at minimum the template's default
  labels (e.g. `needs triage, user story`). Before choosing labels, read the
  repository's full list of available labels (e.g. via the
  `getIssueCreationPrompt` tool output) rather than guessing from a few
  familiar names, and apply every label from that list that is relevant to
  the request (e.g. `security`, `bug`, `feature`, `tech-debt`, area labels,
  etc.) — not just the first one that comes to mind. Never publish or
  confirm a ticket with an empty label set.
- Never state or imply that the draft was already "shown", "presented", or
  "displayed" to the requester unless the full ticket text (title, user
  story, checklist, labels, estimate) was literally included, verbatim, in
  that same reply. A summary or description of the draft is not equivalent to
  showing it. If the draft was not actually shown yet, the next action must
  be to show it in full before asking for or acting on any confirmation.

This rule lives in the tracked codebase (not in any local/untracked
configuration) so that it applies to every contributor and every AI agent
session working on Crypto Trader, regardless of local setup.

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