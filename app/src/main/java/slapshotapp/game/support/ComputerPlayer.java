package slapshotapp.game.support;

import java.util.Random;

import slapshotapp.game.tictactoe.GameBoard;

public class ComputerPlayer 
{

	private GameBoard _TheGame;	
	private int _MyID, _OpponentID;
	private Random _RandomMove;
	private int _RowColCells;
	
	
	public ComputerPlayer(GameBoard game, int myID, int opponentID, int rowsCols)
	{
		//now there is a reference to the gameboard
		_TheGame = game;
		
		_MyID = myID;
		_OpponentID = opponentID;
		_RowColCells = rowsCols;
		
		//create the random number generator
		_RandomMove = new Random(); 
	}
	
	public Cell MakeComputerMove()
	{
		Cell myCell;
		
		//check to see if one away from a win
		myCell = _TheGame.CheckForPendingGameOver(_MyID);		
		
		//if the cell is empty then check if there is a loss that needs to be blocked
		if(myCell == null)
		{
			//check and see if the opponents move needs to be blocked
			myCell = _TheGame.CheckForPendingGameOver(_OpponentID);
		}
		
		//if the cell is empty check to see if there is a good move to set up for a win next time
		if(myCell == null)
		{
			//check and see if there is a good move that should be made
			myCell = _TheGame.SuggestMove(_MyID, _OpponentID);
		}
	
		//if a move was not made then make a random move
		if(myCell == null)
		{
			myCell = makeRandomMove();
		}
		
		return myCell;
	}
	
	/*
	 * Makes a random move to an un-occupied cell.
	 * 
	 * @return the cell of the random move.
	 */
	private Cell makeRandomMove()
	{
		Cell myCell = null;
		int row, col;
		
		while(myCell == null)
		{
			row = _RandomMove.nextInt(_RowColCells);
			col = _RandomMove.nextInt(_RowColCells);
			
			if(_TheGame.IsCellEmpty(row, col))
			{
				//create a new cell to return to caller (don't care about image as this is just
				//for coordinates
				myCell = new Cell (row, col, null);
			}
		}
		
		return myCell;
	}
	
	
	
}
