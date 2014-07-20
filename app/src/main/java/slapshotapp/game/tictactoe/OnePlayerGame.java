package slapshotapp.game.tictactoe;

import slapshotapp.game.support.Cell;
import slapshotapp.game.support.ComputerPlayer;
import slapshotapp.game.support.Player;
import slapshotapp.game.tictactoe.GameBoard.GameState;
import android.os.Handler;
import android.view.View;

public class OnePlayerGame extends PlayGame implements Runnable
{
	private ComputerPlayer _TheComputer;
	private Player _ComputerPlayer;
	private Handler _MyHandle;
	
	private final int COMPUTER_DEFAULT_DELAY = 500; //milliseconds for delay
	
	@Override
	public void InitGame()
	{		
		
		_TheComputer = new ComputerPlayer(_MyGame,
    			_PlayerTwo.GetPlayerID(), 
    			_PlayerOne.GetPlayerID(), _CellsInRow);
		
		//The computer player will be player 2
		_ComputerPlayer = _PlayerTwo;
		
		_MyHandle = new Handler();	
	}
	
	@Override
	public void StartGame()
    {    	
    	
    }
	
    
	@Override
	public void FocusLost() 
	{
		//stop running the computer player on pause
    	_MyHandle.removeCallbacks(this);
		
	}

	@Override
	public void GameNotVisible() 
	{
		
	}

	@Override
	public void FocusGained() 
	{
		//make sure that the computer player is running
    	_MyHandle.postDelayed(this, COMPUTER_DEFAULT_DELAY);
	}
	
    public void GameBoardClickListener(View target)
    {
    	//Don't allow a move while the computer is going
    	if(_CurrentPlayer != _ComputerPlayer){
    		BoardMove(target.getId());    		
    	}
    }

    /*
     * The run method must be part of the thread that owns the view
     * as a separate thread cannot modify the view.
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
	public void run() 
	{
		if((_CurrentPlayer == _ComputerPlayer) && 
				(_MyGame.GetGameState() == GameState.NotFinished) &&
				!_PlayerOne.IsSoundPlaying())
		{
			Cell myCell;
	    	
	    	//have the computer make a move
			myCell = _TheComputer.MakeComputerMove();
			
			//make the move on the game board
			_MyGame.MakeMove(myCell.GetCellRow(), myCell.GetCellColumn(), 
					_CurrentPlayer.GetSymbolImage(), 
					_CurrentPlayer.GetPlayerID());
			
			//play the sound (if the sound is enabled)
			if(_SoundOn)
			{
				_CurrentPlayer.PlaySoundEffect();
			}
			
			updateScreen();
			
			//check again to see if the game is over
			if(!gameOver())
			{
				//switch to the next player
				nextPlayer();
			}
		}
		
		_MyHandle.postDelayed(this, COMPUTER_DEFAULT_DELAY);
	}

	

}
