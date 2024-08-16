# [Stage 1/5](https://hyperskill.org/projects/366/stages/2166/implement) : Console game | [main README.md](../readme.md)
Everyone knows the [rules](https://en.wikipedia.org/wiki/Tic-tac-toe) of tic-tac-toe. In this stage, implement a console version of tic-tac-toe to allow two players to play on the same computer. In addition, players should be able to set an arbitrary game field size.
![Tic Tac Toe Board or Grid](images/tic-tac-toe-grid.webp "Tic-Tac-Toe Grid")
### Objectives
You need to implement a console game of tic-tac-toe. Interaction with the program should look as follows:

1. Upon starting, the program asks for the name of the first and second players. If the user pressed Enter and did not enter a name, set the names to Player1 and Player2.
2. After that, the program asks for the game field size. If the user does not enter anything (or enters the wrong size), set it to 3x3.
   What's the wrong size? A wrong size is a size that does not match the AxB pattern, where A and B are positive integers. Second, the map size cannot be less than 3 in two directions simultaneously. In other words, at least one size must be greater than or equal to 3: A>=3 || B>=3. For example, 1x10 is the correct size, and 2x2 is the incorrect one.
3. Regardless of map size, a player wins with three symbols aligned (horizontally, vertically, or diagonally).
4. The program outputs an empty playing field with the corresponding sizes.
5. After that, the game starts, where players are prompted to enter their moves in turn.
6. After each move, the program displays the updated playing field.
7. If the user has entered an incorrect move, the program displays Wrong move entered and prompts to enter the move again.
8. As soon as one of the players wins, the program must output <name> wins!, where <name> is the name of the player who won.
9. If the game ends with a draw, output Draw!

The input/output of your program should be done according to the following examples.

### Examples
The greater-than symbol followed by a space (> ) represents the user input. Note that it's not part of the input.

#### Example 1:
```
Enter the first player's name (Player1 by default)
>Bob
First player's name: Bob
Enter the second player's name (Player2 by default)
>John
Second player's name: John

Enter the field size (3x3 by default)
>sdfs
Field size: 3x3

|---|---|---|-y
|   |   |   |
|---|---|---|
|   |   |   |
|---|---|---|
|   |   |   |
|---|---|---|
|
x

Enter Bob's move as (x,y)
>(1,1)
|---|---|---|
| X |   |   |
|---|---|---|
|   |   |   |
|---|---|---|
|   |   |   |
|---|---|---|

Enter John's move as (x,y)
>(2,3)
|---|---|---|
| X |   |   |
|---|---|---|
|   |   | O |
|---|---|---|
|   |   |   |
|---|---|---|

Enter Bob's move as (x,y)
>sdfsdf
Wrong move entered
Enter Bob's move as (x,y)
>(5,5)
Wrong move entered
Enter Bob's move as (x,y)
>(2,3)
Wrong move entered
Enter Bob's move as (x,y)
>(2,2)
|---|---|---|
| X |   |   |
|---|---|---|
|   | X | O |
|---|---|---|
|   |   |   |
|---|---|---|

Enter John's move as (x,y)
>(3,1)
|---|---|---|
| X |   |   |
|---|---|---|
|   | X | O |
|---|---|---|
| O |   |   |
|---|---|---|

Enter Bob's move as (x,y)
>(3,3)
|---|---|---|
| X |   |   |
|---|---|---|
|   | X | O |
|---|---|---|
| O |   | X |
|---|---|---|

Bob wins!
```
#### Example 2:
```
Enter the first player's name (Player1 by default)
>
First player's name: Player1
Enter the second player's name (Player2 by default)
>
Second player's name: Player2

Enter the field size (3x3 by default)
>3x4
Field size: 3x4

|---|---|---|---|-y
|   |   |   |   |
|---|---|---|---|
|   |   |   |   |
|---|---|---|---|
|   |   |   |   |
|---|---|---|---|
|
x

Enter Player1's move as (x,y)
>(2,2)
|---|---|---|---|
|   |   |   |   |
|---|---|---|---|
|   | X |   |   |
|---|---|---|---|
|   |   |   |   |
|---|---|---|---|

Enter Player2's move as (x,y)
>(2,2)
Wrong move entered
Enter Player2's move as (x,y)
>(1,2)
|---|---|---|---|
|   | O |   |   |
|---|---|---|---|
|   | X |   |   |
|---|---|---|---|
|   |   |   |   |
|---|---|---|---|

Enter Player1's move as (x,y)
>(2,3)
|---|---|---|---|
|   | O |   |   |
|---|---|---|---|
|   | X | X |   |
|---|---|---|---|
|   |   |   |   |
|---|---|---|---|

Enter Player2's move as (x,y)
>(1,3)
|---|---|---|---|
|   | O | O |   |
|---|---|---|---|
|   | X | X |   |
|---|---|---|---|
|   |   |   |   |
|---|---|---|---|

Enter Player1's move as (x,y)
>(3,2)
|---|---|---|---|
|   | O | O |   |
|---|---|---|---|
|   | X | X |   |
|---|---|---|---|
|   | X |   |   |
|---|---|---|---|

Enter Player2's move as (x,y)
>(1,4)
|---|---|---|---|
|   | O | O | O |
|---|---|---|---|
|   | X | X |   |
|---|---|---|---|
|   | X |   |   |
|---|---|---|---|

Player2 wins!
```
#### Example 3:
```
Enter the first player's name (Player1 by default)
>Donald
First player's name: Donald
Enter the second player's name (Player2 by default)
>
Second player's name: Player2

Enter the field size (3x3 by default)
>2x2
Field size: 3x3

|---|---|---|-y
|   |   |   |
|---|---|---|
|   |   |   |
|---|---|---|
|   |   |   |
|---|---|---|
|
x

Enter Donald's move as (x,y)
>(3,3)
|---|---|---|
|   |   |   |
|---|---|---|
|   |   |   |
|---|---|---|
|   |   | X |
|---|---|---|

Enter Player2's move as (x,y)
>(2,2)
|---|---|---|
|   |   |   |
|---|---|---|
|   | O |   |
|---|---|---|
|   |   | X |
|---|---|---|

Enter Donald's move as (x,y)
>(1,1)
|---|---|---|
| X |   |   |
|---|---|---|
|   | O |   |
|---|---|---|
|   |   | X |
|---|---|---|

Enter Player2's move as (x,y)
>(1,2)
|---|---|---|
| X | O |   |
|---|---|---|
|   | O |   |
|---|---|---|
|   |   | X |
|---|---|---|

Enter Donald's move as (x,y)
>(3,2)
|---|---|---|
| X | O |   |
|---|---|---|
|   | O |   |
|---|---|---|
|   | X | X |
|---|---|---|

Enter Player2's move as (x,y)
>(3,1)
|---|---|---|
| X | O |   |
|---|---|---|
|   | O |   |
|---|---|---|
| O | X | X |
|---|---|---|

Enter Donald's move as (x,y)
>(1,3)
|---|---|---|
| X | O | X |
|---|---|---|
|   | O |   |
|---|---|---|
| O | X | X |
|---|---|---|

Enter Player2's move as (x,y)
>(2,3)
|---|---|---|
| X | O | X |
|---|---|---|
|   | O | O |
|---|---|---|
| O | X | X |
|---|---|---|

Enter Donald's move as (x,y)
>(2,1)
|---|---|---|
| X | O | X |
|---|---|---|
| X | O | O |
|---|---|---|
| O | X | X |
|---|---|---|

Draw!
```
