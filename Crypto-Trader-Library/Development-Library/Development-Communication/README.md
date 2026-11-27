# Development-Communication

Development-Communication defines the information passed between development automation and GitHub project operations. It gives the integration a consistent way to describe an existing work item that should become part of a project.

## What it enables

After an issue has been created, project automation needs to identify that issue and describe the kind of item being added. This module carries that information between the development services and the GitHub integration.

Keeping this exchange consistent helps issue creation and project organization work together without requiring each caller to invent its own request format.

## Place in the platform

Development-Models supplies the shared vocabulary for work items. Development-Services uses the communication contracts when organizing those items in GitHub, while Development-Config supplies the connection and authentication settings. This module describes the exchange; the surrounding services perform the remote operations.
