package slapshotapp.game.tictactoe;

import android.graphics.drawable.Drawable;
import slapshotapp.game.support.Cell;

public class GameBoard {
  public static enum GameState {
    NotFinished, Draw, Winner;
  }

  private Cell[] _Game;
  private GameState CurrentGameResult;
  private int _MoveCount, _CellsInRowCol;

  /*
   * Constructs a new gameboard
   *
   * @param emptyImage The image to display on a cell when it is not occupied
   * @param gameBoardDimensions The dimensions of the gameboard
   */
  public GameBoard(Drawable emptyImage, int gameboardDimensions) {
    _CellsInRowCol = gameboardDimensions;

    _Game = new Cell[(_CellsInRowCol * _CellsInRowCol)];

    //create the cells for the game board
    for (int i = 0, index = 0; i < _CellsInRowCol; i++) {
      for (int j = 0; j < _CellsInRowCol; j++, index++) {
        _Game[index] = new Cell(i, j, emptyImage);
      }
    }

    //set the result of the game to not done
    CurrentGameResult = GameState.NotFinished;

    //zero out the move count
    _MoveCount = 0;
  }

  /*
   * Used to determine if a given row column sequence
   * is on the board.
   *
   * @param row The number of the row
   * @param col The number of the column
   *
   * @return true if on board, false otherwise
   */
  public boolean IsMoveOnBoard(int row, int col) {
    if (row >= _CellsInRowCol || col >= _CellsInRowCol) {
      return false;
    } else {
      return true;
    }
  }

  /*
   * Makes a move, if the game is not completed.
   *
   * @param row The row of the move
   * @param col The column of the move
   * @param image The image to display on the cell
   * @param ownerID The player ID to set the cell too
   *
   * @return true if the move was made, false if it was not
   */
  public boolean MakeMove(int row, int col, Drawable image, int ownerID) {
    boolean result = false;

    //only allow a move if the game is not completed
    if (CurrentGameResult == GameState.NotFinished) {
      if (_Game[convertToIndex(row, col)].SetCellState(image, ownerID)) {
        //increment the number of overall moves
        _MoveCount++;
        result = true;
      }

      //check for a win (Only if enough a move has been done)
      if (result) {
        if (playerWon(row, col, ownerID)) {
          CurrentGameResult = GameState.Winner;
        }
      }
    }

    return result;
  }

  /*
   * This will suggest a move for the player to make attempting
   * to win, not preventing a loss.
   *
   * @param currentPlayerID The id if the player to suggest the move for
   * @param opponentPlayerID The id of the opponent
   *
   * @return cell of suggested move, can be null if no move found.
   *
   */
  public Cell SuggestMove(int currentPlayerID, int opponentPlayerID) {
    Cell myCell = null;

    for (int i = 0; i < _CellsInRowCol; i++) {
      //check to make sure the row contains at least one instance of the current player marker
      if (matchingOwnerIDInRow(i, currentPlayerID) > 0) {
        //make sure there are no opponent symbols in the row
        if (matchingOwnerIDInRow(i, opponentPlayerID) == 0) {
          //return one of the empty cells in the row
          myCell = getEmptyCellInRow(i);
        }
      }
    }

    //if nothing was found in the rows then check the columns
    if (myCell == null) {
      for (int i = 0; i < _CellsInRowCol; i++) {
        //check to make sure the column contains at least one instance of the current player marker
        if (matchingOwnerIDInColumn(i, currentPlayerID) > 0) {
          //make sure there are no opponent symbols in the column
          if (matchingOwnerIDInColumn(i, opponentPlayerID) == 0) {
            //return one of the empty cells in the row
            myCell = getEmptyCellInColumn(i);
          }
        }
      }
    }

    //if nothing was found, check the diagonals
    if (myCell == null) {
      for (int i = 0; i < PlayGame.NUMBER_OF_DIAGONALS; i++) {
        //check to see if the diagonal contains at least one marker from current player
        if (matchingOwnerIDInDiagonal(i, currentPlayerID) > 0) {
          //make sure the diagonal does not contain markers from the opponent
          if (matchingOwnerIDInDiagonal(i, opponentPlayerID) == 0) {
            //return one of the empty cells in the row
            myCell = getEmptyCellInDiagonal(i);
          }
        }
      }
    }

    return myCell;
  }

  /*
   * This suggests a move that will end the game
   *
   * @param currentPlayerID The id of the player to check if the win is imminent
   *
   * @return The cell recommended for the move, can be null.
   */
  public Cell CheckForPendingGameOver(int currentPlayerID) {
    Cell myCell = null;

    //check the rows
    for (int i = 0; i < _CellsInRowCol; i++) {
      if (matchingOwnerIDInRow(i, currentPlayerID) == (_CellsInRowCol - 1)) {
        myCell = getEmptyCellInRow(i);
        if (myCell != null) {
          break;
        }
      }
    }

    //check the columns, only if nothing found in rows
    if (myCell == null) {
      for (int i = 0; i < _CellsInRowCol; i++) {
        if (matchingOwnerIDInColumn(i, currentPlayerID) == (_CellsInRowCol - 1)) {
          myCell = getEmptyCellInColumn(i);
          if (myCell != null) {
            break;
          }
        }
      }
    }

    //check the diagonals only if nothing found in rows or columns
    if (myCell == null) {
      for (int i = 0; i < PlayGame.NUMBER_OF_DIAGONALS; i++) {
        if (matchingOwnerIDInDiagonal(i, currentPlayerID) == (_CellsInRowCol - 1)) {
          myCell = getEmptyCellInDiagonal(i);
          if (myCell != null) {
            break;
          }
        }
      }
    }

    return myCell;
  }

  /*
   * Checks and returns the result of the game.
   *
   * @param player1ID The ID of player one
   * @param player2ID The ID of player two
   *
   * @return The state of the game
   */
  public GameState CheckGameResult(int player1ID, int player2ID) {
    //if the game is not completed, check for a draw
    if (CurrentGameResult == GameState.NotFinished) {
      CheckForDraw(player1ID, player2ID);
    }

    return CurrentGameResult;
  }

  public GameState GetGameState() {
    return CurrentGameResult;
  }

  public void ClearGame() {
    for (int i = 0; i < (_CellsInRowCol * _CellsInRowCol); i++) {
      _Game[i].ClearCell();
    }

    _MoveCount = 0;
    CurrentGameResult = GameState.NotFinished;
  }

  public boolean IsCellEmpty(int pRow, int pCol) {
    return _Game[convertToIndex(pRow, pCol)].IsCellEmpty();
  }

  public Drawable GetGameCellImage(int pRow, int pCol) {
    return _Game[convertToIndex(pRow, pCol)].GetImage();
  }

  //********************Start of Private Methods***************************

  /*
   * Checks to make sure that there is still a possibility for
   * a player to win.
   *
   * @param player1ID The ID for player one
   * @param player2ID The ID for player two
   */
  private void CheckForDraw(int player1ID, int player2ID) {
    int diagWinNotPossibleCount = 0, colWinNotPossibleCount = 0, rowWinNotPossibleCount = 0;

    //check all rows and colums to make sure there are not two different markers
    for (int i = 0; i < _CellsInRowCol; i++) {
      //checking rows
      if (matchingOwnerIDInRow(i, player1ID) > 0) {
        if (matchingOwnerIDInRow(i, player2ID) > 0) {
          rowWinNotPossibleCount++;
        }
      }

      //checking columns
      if (matchingOwnerIDInColumn(i, player1ID) > 0) {
        if (matchingOwnerIDInColumn(i, player2ID) > 0) {
          colWinNotPossibleCount++;
        }
      }
    }

    //check diagonals to see if there are two different markers
    for (int i = 0; i < PlayGame.NUMBER_OF_DIAGONALS; i++) {
      if (matchingOwnerIDInDiagonal(i, player1ID) > 0) {
        if (matchingOwnerIDInDiagonal(i, player2ID) > 0) {
          diagWinNotPossibleCount++;
        }
      }
    }

    if (colWinNotPossibleCount == _CellsInRowCol
        && rowWinNotPossibleCount == _CellsInRowCol
        && diagWinNotPossibleCount == PlayGame.NUMBER_OF_DIAGONALS) {
      CurrentGameResult = GameState.Draw;
    }
  }

  private int convertToIndex(int pRow, int pCol) {
    return ((pRow * _CellsInRowCol) + pCol);
  }

  private Cell getEmptyCellInDiagonal(int pDiagNum) {
    Cell myCell = null;

    if (pDiagNum == PlayGame.LEFT_TOP_TO_RIGHT_BOTTOM) {
      for (int i = 0, j = 0; i < _CellsInRowCol; j++, i++) {
        if (IsCellEmpty(i, j)) {
          myCell = _Game[convertToIndex(i, j)];
          break;
        }
      }
    } else//PlayGame.LEFT_BOTTOM_TO_RIGHT_TOP
    {
      for (int i = _CellsInRowCol - 1, j = 0; j < _CellsInRowCol; j++, i--) {
        if (IsCellEmpty(i, j)) {
          myCell = _Game[convertToIndex(i, j)];
          break;
        }
      }
    }

    return myCell;
  }

  private Cell getEmptyCellInColumn(int pCol) {
    Cell myCell = null;

    for (int i = 0; i < _CellsInRowCol; i++) {
      if (IsCellEmpty(i, pCol)) {
        myCell = _Game[convertToIndex(i, pCol)];
        break;
      }
    }

    return myCell;
  }

  private Cell getEmptyCellInRow(int pRow) {
    Cell myCell = null;

    for (int i = 0; i < _CellsInRowCol; i++) {
      if (IsCellEmpty(pRow, i)) {
        myCell = _Game[convertToIndex(pRow, i)];
        break;
      }
    }

    return myCell;
  }

  /*
   * Determines the number of cells in a row that match the
   * given owner.
   *
   * @param row The number of the row to check
   * @param ownerID The ID of the owner to check the row for
   *
   * @return the number of cells that contain the ownerID in the row
   */
  private int matchingOwnerIDInRow(int row, int ownerID) {
    int count = 0;

    for (int i = 0; i < _CellsInRowCol; i++) {
      if (_Game[convertToIndex(row, i)].CellContains(ownerID)) {
        count++;
      }
    }

    return count;
  }

  /*
   * Determines the number of cells in a column that match the
   * given owner.
   *
   * @param col The number of the column to check
   * @param ownerID The ID of the owner to check the row for
   *
   * @return the number of cells that contain the ownerID in the column
   */
  private int matchingOwnerIDInColumn(int col, int ownerID) {
    int count = 0;

    for (int i = 0; i < _CellsInRowCol; i++) {
      if (_Game[convertToIndex(i, col)].CellContains(ownerID)) {
        count++;
      }
    }

    return count;
  }

  /*
   * Counts the number of cells matching the given owner in both
   * diagonals.
   *
   * @param diagNum LEFT_TOP_TO_RIGHT_BOTTOM or LEFT_BOTTOM_TO_RIGHT_TOP
   * @param ownerID the ID of the owner to check the diagonal for
   *
   * @return the number of cells matching the given ownerID in the specified diagonal
   */
  private int matchingOwnerIDInDiagonal(int diagNum, int ownerID) {
    int count = 0;

    if (diagNum == PlayGame.LEFT_TOP_TO_RIGHT_BOTTOM) {
      for (int i = 0, j = 0; i < _CellsInRowCol; j++, i++) {
        if (_Game[convertToIndex(i, j)].CellContains(ownerID)) {
          count++;
        }
      }
    } else//PlayGame.LEFT_BOTTOM_TO_RIGHT_TOP
    {
      for (int i = _CellsInRowCol - 1, j = 0; j < _CellsInRowCol; j++, i--) {
        if (_Game[convertToIndex(i, j)].CellContains(ownerID)) {
          count++;
        }
      }
    }

    return count;
  }

  /*
   * Checks to see if the move made by the given player ID
   * resulted in a win.
   *
   *  @param row The row number of the move
   *  @param col The column number of the move
   *  @param playerID The ID of the player that made the move
   *
   *  @return true if the player won, false otherwise
   */
  private boolean playerWon(int row, int col, int playerID) {
    boolean winner = false;

    //check the row the move was made in
    if (matchingOwnerIDInRow(row, playerID) == _CellsInRowCol) {
      winner = true;
    } else if (matchingOwnerIDInColumn(col, playerID) == _CellsInRowCol) {
      winner = true;
    } else if (matchingOwnerIDInDiagonal(PlayGame.LEFT_TOP_TO_RIGHT_BOTTOM, playerID)
        == _CellsInRowCol
        || matchingOwnerIDInDiagonal(PlayGame.LEFT_BOTTOM_TO_RIGHT_TOP, playerID)
        == _CellsInRowCol) {
      winner = true;
    }

    return winner;
  }
}
