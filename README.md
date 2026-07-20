# Vestal

A desktop app that keeps a folder of plain-text notes in sync across machines via Git, with optional local encryption for sensitive files.

## What it does

- **Watches** a local folder for changes to your `.txt` notes
- **Pushes** changes to a GitHub repository
- **Pulls** a GitHub repository down to a local folder
- **Encrypts/decrypts** notes locally, if you'd rather not store sensitive content in plain text — even in a private repo

The goal is a single source of truth for your notes, kept consistent across different machines and operating systems, using Git as the sync mechanism.

## Status

🚧 Early development — not yet functional.

## Tech stack

- Java + JavaFX (desktop UI)
- JGit (Git operations, no external `git` binary required)
- JDK's built-in `WatchService` (file monitoring)

## Getting started

_Coming soon — build and run instructions will go here once the project is runnable._

## License

_TBD_