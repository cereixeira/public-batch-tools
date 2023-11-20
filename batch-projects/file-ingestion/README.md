# Module "batch-ingestion"

## Description
This project tries to provide a template for executing bulk ingestion from CSV documents.
The target is to split the process in several jobs.
Each job must check if its part is completed or not, if any part is uncompleted
that one has to continue from the last step.

### executeblock-job
Saves the ingestion job name and the input path in "Execution" table, also split the whole ingestion
in "Blocks" in order to manage with threads the payload and doesn't overload the Java memory.
Each "Block" belongs to only one "Execution".

### entry-job
Saves each "Entry" from the input file.
Each "Entry" belongs to only one "Block".

### report-job
Generates a CSV report from database by execution name
