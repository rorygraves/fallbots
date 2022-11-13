
# Welcome to Fallbots

## What is fallbots?

Fallbots is a game, specifically a game for software developers at hackathons.

Combining idea from:
   * [Fall Guys](https://www.fallguys.com/) - an amazing interactive race game on thw Switch.
   * [Roborally](https://en.wikipedia.org/wiki/RoboRally) - a fun board game where you program robots to avoid obsticles and win the race. 


Each player writes a bot on their machine, connecting it to a shared game server.
The game is played on a digital board.  Each turn each bot is sent information about the current state, the bot then has a limited amount of time
to decide on an action (move, turn, rest).  Once all moves are recieved the game turn is playe dout.
Each bot performs its action, but is also affected by:
   * Other bots (it can be pushed)
   * conveyor belts.
   * Crushers
   * etc.

Can you write a bot to outwit your opponents and win the race?


## Current state

This project has only just started development.  Nothing works yet (at all).
If you are excited to get this going - come and help :)

## Basic Design

There are several key components.

### A game server 

The server manages the game state.  Playing a series of games.  During each game
the server runs each turn, sharing information with the players and then actioning the turn.

The server exposes a websocket for bots to connect to and uses a defined JSON protocol for 
the messages.  This means that bots can be written in any language.

### A Game display

So that everybody can see the big picture, we have a gui (webpage?) that will display
the current game and other relevant infomation.

### Bots

The bots are written by the players.  We hope to provide a number of templates for different
languages so that players can focus on their bots magic rather than worrying about the protocol etc.

## Building

You will need:
   * Java 11  (we recommend using https://github.com/FelixSelter/JEnv-for-Windows / https://www.jenv.be/  )
   * SBT (https://www.scala-sbt.org/)

Pick your favorite IDE :)

The main server class FallBotServer current starts a test bot.
Bots are implemented using BotInterface - see SampleBotApp for examples.
