# Console-Repositories

Console-Repositories provides access to stored console command execution records. It supports the history needed to inspect how the console has been used.

## What it enables

The console's surrounding services can store execution records and retrieve them later for review or operational investigation. Access to those records gives the application a basis for execution-history features without placing persistence concerns inside command handling.

The module currently provides general record access rather than specialized history searches or reporting workflows.

## Place in the platform

Console-Models defines the information represented by an execution record, while console services decide when records are created and how they are used. The host application supplies the database environment. Command parsing, authorization, and execution belong to the console's other layers.
