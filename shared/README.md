# Shared module

This module contains logic and definitions shared between client and server.  It has a number of key pieces:

## The base share definitions

The definitions of such things as `Board`,`Direction` and `Position` that both the client and server need to understand.

## The interprocess messages

The messages sent between client and server (these are encoded with upickle for transmission across the wire)

## The game definitions

Clients can run games locally using the `LocalGameRunner` so the logic for games must be shared.
`LocalGameRunner` and `FallbotServer` run exactly the same game logic, the fundamental difference is that
`FallbotsServer` is remote and supports multiple connected bots to compete.


