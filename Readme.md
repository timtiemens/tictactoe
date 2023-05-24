Inspiration https://www.kaggle.com/datasets/aungpyaeap/tictactoe-endgame-dataset-uci
  Introduction page says "About 65.3% are positive (wins for X)


Definitions:
  Turn - the act of placing an 'X' or 'O' in an empty cell
  Game - a series of turns that end in a win, loss or draw
  Board State - the state of the board after 0 to 9 turns
  Game State  - the state of the board after a Game


Paper https://informatika.stei.itb.ac.id/~rinaldi.munir/Matdis/2021-2022/Makalah2021/Makalah-Matdis-2021%20(148).pdf
  Total Games X wins:  131184   51%
  Total Games O wins:   77904   31%
  Total Games Draws :   46080   18%
    Total Games        255168  100%


Number of Games-wrong    9!       362880   naive, overcounts because of "stops"
Number of Games                   255168

Final board states       :  2^9      512      naive
maximum board states     :  3^9      19683    naive X,O,blank states for 9 cells

Final board states                   958      "reachable", but symmetry dupes
Final board states                   104      "reachable" and no symmetry dupes



Disable wayland on 22.04
https://linuxconfig.org/how-to-enable-disable-wayland-on-ubuntu-22-04-desktop
