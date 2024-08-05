# my-tic-tac-toe-strategy | [main README.md](../README.md)

The version of Tic-Tac-Toe outlined here is interesting for a number of reasons:
1. The axes of the grid are reversed. The authors of the project want the x-axis to be the vertical axis, and the y-axis to be the horizontal axis.
2. The dimensions of the Tic-Tac-Toe board may be larger than the standard 3x3 grid.

## What is standard?
* A winner has 3 cells in sequence. 
* The 3-cell sequence may be:
  * horizontal
  * vertical
  * diagonal (SW to NE)
  * anti-diagonal (NW to SE)

# Strategy
* to help solve the game we need a way to track and evaluate the status of each player
* to do this we will make use of a Virtual Board of dimension 3x3
* The number of Virtual Boards is:

    __(A - 2) * (B - 2)__

* where 

##### A=number of rows
##### B=number of columns
##### So, if we have an 8x9 (8 rows, 9 columns) Board, there will be 42 ((8-2)*(9-2)=6*7=42) Virtual Boards



## Terminology
### Board
* the playing area for the game
### Cell
* any entry on the Board
### Diagonal
* diagonal : goes from lower left to upper right
* anti-diagonal : goes from upper left to lower right
### Virtual Board
* made up of:
  * top-left coordinate which indicates its projection onto the main Board
  * 3 Pairs representing the count of X and O for vertical cells
  * 3 Pairs representing the count of X and O for horizontal cells
  * 3 Pairs representing the count of X and O for diagonal cells
  * 3 Pairs representing the count of X and O for anti-diagonal cells


|---|---|---|-y
|0,0|0,1|0,2|
|---|---|---|
|1,0|1,1|1,2|
|---|---|---|
|2,0|2,1|2,2|
|---|---|---|
|
x

