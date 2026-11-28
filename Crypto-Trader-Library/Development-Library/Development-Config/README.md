# Development-Config

Development-Config connects development automation to the GitHub repository, organization, and project it serves. It establishes the authenticated integration environment used to publish issues and maintain their project information.

## What it enables

A deployment can direct its development workflows to the intended GitHub workspace using a shared set of credentials and project settings. Issue, organization, and project operations then use a consistent connection rather than separate connection arrangements for each workflow.

This configuration supports both repository-level work and the project metadata used to organize a team's backlog. Access depends on the supplied account credentials and the permissions available for the target resources.

## Place in the platform

Development-Services performs the issue and project operations; this module provides its GitHub connections. The application supplies environment-specific settings. The module also includes an optional live publishing check that creates a real issue, so that check requires an intentionally selected target workspace.
