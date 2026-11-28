# Development-Services

Development-Services turns structured development work into GitHub issues and organizes those issues within a project. It connects the platform's description of a task with the repository and backlog where people will act on it.

## What it enables

Work can be prepared for review with consistent descriptions, checklists, estimates, labels, and planning information. When published, the issue is added to the configured project and enriched with the available ownership, priority, effort, size, and type information.

The services also retrieve repository labels and organization metadata so automated workflows can use the vocabulary already present in the team's workspace. They support preparation and presentation as well as publication, allowing callers to incorporate a review step into their process.

## Operational context

GitHub credentials, repository selection, and project configuration come from the surrounding application. Publication and project updates are separate remote operations; an issue may exist even if a later project update fails. Some metadata mappings are specific to the configured organization and need review when moving the integration to another workspace.
