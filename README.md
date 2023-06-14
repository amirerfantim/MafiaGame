
# MafiaGame

MafiaGame is an interactive multiplayer game inspired by the popular party game "Mafia." It provides a platform for players to engage in a thrilling social deduction experience online. This README will guide you through the setup, rules, and features of MafiaGame.

## Table of Contents

- [Installation](#installation)
- [Technologies](#Technologies)
- [Game Rules](#game-rules)
- [Features](#features)
- [Contributing](#contributing)
- [License](#license)

## Installation

To run MafiaGame locally, please ensure that you have the following prerequisites:

- Node.js (v12.0.0 or higher)
- npm (v6.0.0 or higher)

Follow these steps to set up MafiaGame:

1. Clone the repository:

   ```shell
   git clone https://github.com/amirerfantim/MafiaGame.git
   ```

2. Navigate to the project directory:

   ```shell
   cd MafiaGame
   ```

3. compile server and client:

   ```shell
   javac server.java
   javac client.java
   ```

4. run server & then client:

   ```shell
   java server
   java client
   ```
## Technologies:
- Java: Core programming language used for the game logic and networking.
- Socket programming: Utilized for establishing communication between the server and clients.

## Game Rules

MafiaGame follows the classic rules of the Mafia party game. The objective of the game is for the two factions, the Mafia and the Townspeople, to eliminate each other and seize control of the town. The game consists of two main phases: Day and Night.

### Day Phase

During the Day phase, players engage in discussions and debates to identify the Mafia members within the group. Players can vote to eliminate a suspected Mafia member. The player with the most votes against them will be eliminated. If the Townspeople successfully eliminate all the Mafia members, they win the game.

### Night Phase

During the Night phase, the Mafia secretly chooses a player to eliminate. The Mafia can communicate privately to strategize and select their target. Additionally, some players may have special roles with unique abilities that can influence the outcome of the game. The Night phase ends when the Mafia has made their decision.

The game continues alternating between Day and Night phases until one faction achieves their victory condition.

## Features

MafiaGame offers several exciting features to enhance your gaming experience:

- **Real-time multiplayer:** Play with friends and other players from around the world in real-time.
- **Customizable game settings:** Modify the number of players, roles, and other game parameters to suit your preferences.
- **Role-based gameplay:** Experience the unique abilities and challenges of different roles within the game.
- **In-game chat:** Communicate with other players through the built-in chat feature.


## License

MafiaGame is licensed under the [MIT License](https://github.com/amirerfantim/MafiaGame/blob/main/LICENSE). You are free to use, modify, and distribute the codebase following the terms of this license.

---

Thank you for your interest in MafiaGame
