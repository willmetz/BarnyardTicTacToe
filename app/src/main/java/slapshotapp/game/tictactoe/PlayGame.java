package slapshotapp.game.tictactoe;

import slapshotapp.game.support.Cell;
import slapshotapp.game.support.FragmentAlertDialog;
import slapshotapp.game.support.MyAlertDialogFragment;
import slapshotapp.game.support.Player;
import slapshotapp.game.tictactoe.GameBoard.GameState;
import slapshotapp.game.tictactoe.R;

import android.app.AlertDialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public abstract class PlayGame extends ActionBarActivity implements FragmentAlertDialog
{
	public static final int EXIT_GAME = 1;
	public static final int BACK_BUTTON = 2;
	protected DialogFragment _gameOverDialog;
	
	/*
	 * Called when the game board is clicked
	 * 
	 * @param target Item clicked
	 */
	public abstract void GameBoardClickListener(View target);
	
	/*
	 * Called when the game is first started.  Only called once.
	 */
	public abstract void InitGame();
	
	/*
	 * Called when the game loses focus but is still visible.
	 * Also called on startup.
	 */
	public abstract void FocusLost();
	
	/*
	 * Called when the game is no longer visible.
	 */
	public abstract void GameNotVisible();
	
	/*
	 * Called when the game regains focus after losing it.
	 * Also called on startup.
	 */
	public abstract void FocusGained();
	
	/*
	 * Called when the game is brought back into view after
	 * being removed from view.
	 */
	public abstract void StartGame();
	
	public static final int NUMBER_OF_DIAGONALS = 2;
	public static final int MIN_MOVES_FOR_LOSS = 2;
	public static final int LEFT_TOP_TO_RIGHT_BOTTOM = 0;
	public static final int LEFT_BOTTOM_TO_RIGHT_TOP = 1;
	
	
	protected GameBoard _MyGame;
	
	protected ImageButton _MyButton;
	protected boolean _SoundOn;
	protected int _Mode;
	protected Player _PlayerOne, _PlayerTwo, _CurrentPlayer, _LastPlayerToGoFirst;
	protected int _CellsInRow;
	protected GameState _CurrentGameState;
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) 
    {	
		int tempDimensions; 
		
        super.onCreate(savedInstanceState);
        
        //get the player instances
        _PlayerOne = Player.getInstance(Player.PLAYER_ONE);
        _PlayerTwo = Player.getInstance(Player.PLAYER_TWO);
        
        //get the game info parameters
        Bundle b = getIntent().getExtras();
        tempDimensions = b.getShort(SetUpGame.GAME_BOARD_DIMENSION_KEY);
            	
        //before a layout item can be set the layout needs to be set
        if(tempDimensions == SetUpGame.THREE_BY_THREE)
        {
        	_Mode = SetUpGame.THREE_BY_THREE;
        	setContentView(R.layout.game_board);
        	_CellsInRow = 3;          	
        }
        else
        {
        	_Mode = SetUpGame.FIVE_BY_FIVE;
        	setContentView(R.layout.game_board_5_5);
        	_CellsInRow = 5;
        }
        
        
       //create a new instance of the game board:
        _MyGame = new GameBoard(getResources().getDrawable(R.drawable.empty), _CellsInRow);
        
        _CurrentGameState = _MyGame.GetGameState(); 
        
        //set the current player to the first player
        _CurrentPlayer = _LastPlayerToGoFirst = _PlayerOne;
        
        //set the sound attribute to on
        _SoundOn = true;       
        
        updateScreenData();
        
        //so that the media play volume is controlled by default and NOT the ringer volume
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);  
        
        InitGame();
    }
    
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
    public void onResume()
    {
    	super.onResume();
    	
    	FocusGained();
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onStart()
     */
    public void onStart()
    {
    	super.onStart();
    	
    	StartGame();
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onPause()
     */
    public void onPause()
    {
    	super.onPause();
    	
    	FocusLost();    	
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onStop()
     */
    public void onStop()
    {
    	super.onStop();

    	GameNotVisible();
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onDestroy()
     */
    public void onDestroy()
    {
    	super.onDestroy();
    }
    
    @Override
    public void onBackPressed()
    {
    	setResult(BACK_BUTTON);
    	finish();
    }
    
    /*
     * This is called by android when the menu key is pressed. 
     */
    public boolean onCreateOptionsMenu(Menu pMenu)
    {
    	//call the parent to attach any system level menus(recommended by android)
    	super.onCreateOptionsMenu(pMenu);
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.game_menu, pMenu);
    	
    	//true needs to be returned in order to display the menu
    	return true;   	
    }
    
    
       
    
	/*
	 * (non-Javadoc)
	 * @see slapshotapp.game.tictactoe.FragmentAlertDialog#alertDialogButtonClick(int)
	 */
	public void alertDialogButtonClick(int buttonNum) 
	{
		switch(buttonNum)
		{
			case AlertDialog.BUTTON_POSITIVE:
				ResetGame();
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				setResult(EXIT_GAME);
				finish();
				break;
		}
	}
	
	//callback for the menu selections
    public boolean onOptionsItemSelected(MenuItem pItem)
    {
    	switch(pItem.getItemId())
    	{
    	case R.id.menu_MainMenu:
    		finish();
    		break;
    	case R.id.menu_enableSound:
    		_SoundOn = true;
    		break;
    	case R.id.menu_disableSound:
    		_SoundOn = false;
    		break;
    	default:
    		return super.onOptionsItemSelected(pItem);
    	}
    	
    	return true;
    }
    
    //this is called by android right before the menu is displayed
    public boolean  onPrepareOptionsMenu(Menu pMenu)
    {
		MenuItem item;
		
		    	
    	//check to see which radio button should be checked for sound
    	if(_SoundOn)
    	{
    		item = pMenu.findItem(R.id.menu_disableSound);
    		item.setChecked(false);
    		
    		item = pMenu.findItem(R.id.menu_enableSound);
    		item.setChecked(true);
    	}
    	else
    	{
    		
    		item = pMenu.findItem(R.id.menu_enableSound);
    		item.setChecked(false);
    		
    		item = pMenu.findItem(R.id.menu_disableSound);
    		item.setChecked(true);   		
    	}
    	
    	return true;
    }
    
    
	
    /*
     * Called by the implementor of the game
     * when a button on the screen is pressed.
     * 
     * @param targetID The pressed button ID
     * 
     * @return cell the cell of the performed move (null if no move)
     */
    public Cell BoardMove(int targetID)
    {
    	int playerMoveRow, playerMoveCol;
    	boolean moveAllowed = false;    	
    	

   		//find out which button was pressed
    	switch(targetID)
    	{
	    	// 3 X 3 game
	    	case R.id.Cell_0_0:
	    		playerMoveRow = 0;
	    		playerMoveCol = 0;
	    		break;	    		
	    	case R.id.Cell_0_1:
	    		playerMoveRow = 0;
	    		playerMoveCol = 1;
	    		break;
	    	case R.id.Cell_0_2:
	    		playerMoveRow = 0;
	    		playerMoveCol = 2;
	    		break;
	    	case R.id.Cell_1_0:
	    		playerMoveRow = 1;
	    		playerMoveCol = 0;    		
	    		break;
	    	case R.id.Cell_1_1:
	    		playerMoveRow = 1;
	    		playerMoveCol = 1;
	    		break;
	    	case R.id.Cell_1_2:
	    		playerMoveRow = 1;
	    		playerMoveCol = 2;
	    		break;
	    	case R.id.Cell_2_0:
	    		playerMoveRow = 2;
	    		playerMoveCol = 0;
	    		break;
	    	case R.id.Cell_2_1:
	    		playerMoveRow = 2;
	    		playerMoveCol = 1;	
	    		break;
	    	case R.id.Cell_2_2:
	    		playerMoveRow = 2;
	    		playerMoveCol = 2;
	    		break;
    		
	    	//5 x 5 game
	    	case R.id.Five_Five_Cell_0_0:
	    		playerMoveRow = 0;
	    		playerMoveCol = 0;
	    		break;	    		
	    	case R.id.Five_Five_Cell_0_1:
	    		playerMoveRow = 0;
	    		playerMoveCol = 1;
	    		break;
	    	case R.id.Five_Five_Cell_0_2:
	    		playerMoveRow = 0;
	    		playerMoveCol = 2;
	    		break;
	    	case R.id.Five_Five_Cell_0_3:
	    		playerMoveRow = 0;
	    		playerMoveCol = 3;
	    		break;
	    	case R.id.Five_Five_Cell_0_4:
	    		playerMoveRow = 0;
	    		playerMoveCol = 4;
	    		break;
	    	case R.id.Five_Five_Cell_1_0:
	    		playerMoveRow = 1;
	    		playerMoveCol = 0;    		
	    		break;
	    	case R.id.Five_Five_Cell_1_1:
	    		playerMoveRow = 1;
	    		playerMoveCol = 1;
	    		break;
	    	case R.id.Five_Five_Cell_1_2:
	    		playerMoveRow = 1;
	    		playerMoveCol = 2;
	    		break;
	    	case R.id.Five_Five_Cell_1_3:
	    		playerMoveRow = 1;
	    		playerMoveCol = 3;
	    		break;
	    	case R.id.Five_Five_Cell_1_4:
	    		playerMoveRow = 1;
	    		playerMoveCol = 4;
	    		break;
	    	case R.id.Five_Five_Cell_2_0:
	    		playerMoveRow = 2;
	    		playerMoveCol = 0;
	    		break;
	    	case R.id.Five_Five_Cell_2_1:
	    		playerMoveRow = 2;
	    		playerMoveCol = 1;	
	    		break;
	    	case R.id.Five_Five_Cell_2_2:
	    		playerMoveRow = 2;
	    		playerMoveCol = 2;
	    		break;
	    	case R.id.Five_Five_Cell_2_3:
	    		playerMoveRow = 2;
	    		playerMoveCol = 3;
	    		break;
	    	case R.id.Five_Five_Cell_2_4:
	    		playerMoveRow = 2;
	    		playerMoveCol = 4;
	    		break;
	    	case R.id.Five_Five_Cell_3_0:
	    		playerMoveRow = 3;
	    		playerMoveCol = 0;
	    		break;
	    	case R.id.Five_Five_Cell_3_1:
	    		playerMoveRow = 3;
	    		playerMoveCol = 1;	
	    		break;
	    	case R.id.Five_Five_Cell_3_2:
	    		playerMoveRow = 3;
	    		playerMoveCol = 2;
	    		break;
	    	case R.id.Five_Five_Cell_3_3:
	    		playerMoveRow = 3;
	    		playerMoveCol = 3;
	    		break;
	    	case R.id.Five_Five_Cell_3_4:
	    		playerMoveRow = 3;
	    		playerMoveCol = 4;
	    		break;
	    	case R.id.Five_Five_Cell_4_0:
	    		playerMoveRow = 4;
	    		playerMoveCol = 0;
	    		break;
	    	case R.id.Five_Five_Cell_4_1:
	    		playerMoveRow = 4;
	    		playerMoveCol = 1;	
	    		break;
	    	case R.id.Five_Five_Cell_4_2:
	    		playerMoveRow = 4;
	    		playerMoveCol = 2;
	    		break;
	    	case R.id.Five_Five_Cell_4_3:
	    		playerMoveRow = 4;
	    		playerMoveCol = 3;
	    		break;
	    	case R.id.Five_Five_Cell_4_4:
	    		playerMoveRow = 4;
	    		playerMoveCol = 4;
	    		break;
	
	    	default://should never get here, this is just to remove warnings
				playerMoveRow = 0;
				playerMoveCol = 0;
    	}
    	
    	//attempt to make the move
    	moveAllowed = _MyGame.MakeMove(playerMoveRow, 
    			playerMoveCol, 
    			_CurrentPlayer.GetSymbolImage(), 
    			_CurrentPlayer.GetPlayerID());
    	
    	if(moveAllowed)
    	{    		
    		//play the sound (if sound enabled)
    		if(_SoundOn)
    		{
    			_CurrentPlayer.PlaySoundEffect();
    		}   		
    		
    		//set the symbol in the button to the current player marker
    		this.updateScreen();
    		
    		if(!gameOver())//check to see if the game is over
    		{
    			//switch to the next player
    			this.nextPlayer();
    	    	
    		}
    		
    		
    		return new Cell(playerMoveRow, playerMoveCol, null);
	    	
    	}//end moveAllowed
    	
    	return null;
    }   
    
  
    protected void updateScreen()
    {  	
    	if(_Mode == SetUpGame.THREE_BY_THREE)
    	{
	    	//update the images for all of the buttons
	    	_MyButton = (ImageButton)findViewById(R.id.Cell_0_0); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(0,0));
	    	_MyButton = (ImageButton)findViewById(R.id.Cell_0_1); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(0,1));
	    	_MyButton = (ImageButton)findViewById(R.id.Cell_0_2); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(0,2));
	    	_MyButton = (ImageButton)findViewById(R.id.Cell_1_0); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(1,0));
	    	_MyButton = (ImageButton)findViewById(R.id.Cell_1_1); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(1,1));
	    	_MyButton = (ImageButton)findViewById(R.id.Cell_1_2); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(1,2));
	    	_MyButton = (ImageButton)findViewById(R.id.Cell_2_0); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(2,0));
	    	_MyButton = (ImageButton)findViewById(R.id.Cell_2_1); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(2,1));
	    	_MyButton = (ImageButton)findViewById(R.id.Cell_2_2); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(2,2));
    	}
    	else
    	{
    		//update the images for all of the buttons
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_0_0); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(0,0));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_0_1); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(0,1));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_0_2); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(0,2));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_0_3); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(0,3));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_0_4); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(0,4));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_1_0); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(1,0));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_1_1); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(1,1));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_1_2); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(1,2));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_1_3); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(1,3));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_1_4); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(1,4));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_2_0); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(2,0));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_2_1); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(2,1));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_2_2); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(2,2));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_2_3); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(2,3));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_2_4); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(2,4));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_3_0); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(3,0));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_3_1); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(3,1));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_3_2); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(3,2));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_3_3); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(3,3));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_3_4); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(3,4));	    	
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_4_0);
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(4,0));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_4_1); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(4,1));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_4_2); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(4,2));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_4_3); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(4,3));
	    	_MyButton = (ImageButton)findViewById(R.id.Five_Five_Cell_4_4); 
	    	_MyButton.setImageDrawable(_MyGame.GetGameCellImage(4,4));
    	}
    	
    }
    
    protected boolean gameOver()
    {
    	boolean gameOver = false;
    	_CurrentGameState = _MyGame.CheckGameResult(_PlayerOne.GetPlayerID(), 
    			_PlayerTwo.GetPlayerID());
    	
    	//check to see if the player won the game
		if(_CurrentGameState == GameState.Winner)
		{
			String msg = _CurrentPlayer.GetName() + " WINS!!";
			//instantiate the prompt dialog for game over
			_gameOverDialog = MyAlertDialogFragment.newInstance(
					R.string.game_over_alert_dialog_title, 
					R.string.game_over_button_one_title, 
					R.string.game_over_button_two_title,
					msg);
			_gameOverDialog.show(getSupportFragmentManager(), "GameOverDialog");
			
			//display the winner a message
			this.setPlayerMessage(msg);

			//increment the appropriate win count
			_CurrentPlayer.IncrementWinCount();
	    	
			this.updateWinCountMessages();
			
			gameOver = true;
		}
		else if(_CurrentGameState == GameState.Draw)
		{
			String msg = "The Game is a Draw!!";
			//display a message indicating a draw has occurred
			this.setPlayerMessage(msg);
			
			//instantiate the prompt dialog for game over
			_gameOverDialog = MyAlertDialogFragment.newInstance(
					R.string.game_over_alert_dialog_title, 
					R.string.game_over_button_one_title, 
					R.string.game_over_button_two_title,
					msg);
			_gameOverDialog.show(getSupportFragmentManager(), "GameOverDialog");
			
			gameOver = true;
		}
		
		return gameOver;
    }
        
    protected void nextPlayer()
    {
    	//toggle to the next player
    	if(_CurrentPlayer == _PlayerOne)
    	{
    		_CurrentPlayer = _PlayerTwo;
    	}
    	else
    	{
    		_CurrentPlayer = _PlayerOne;
    	}
    	
    	//change the text prompt
    	String msg = _CurrentPlayer.GetName() + "'s Turn";
    	this.setPlayerMessage(msg);
    }
    
    protected void setPlayerMessage(String msg)
    {
    	TextView myTextBox;
    	
    	if(_Mode == SetUpGame.THREE_BY_THREE)
    		myTextBox = (TextView)findViewById(R.id.GamePromptText);
    	else
    		myTextBox = (TextView)findViewById(R.id.Five_Five_GamePromptText);
    	
    	myTextBox.setText(msg);
    }
    
    protected void ResetGame()
    {
    	if(_gameOverDialog != null)
    	{    		
			_gameOverDialog.dismiss();
    	}
    	
    	//first the game needs to be cleared
    	_MyGame.ClearGame();   	
    	
    	//update the screen with the new cleared data
    	this.updateScreen();
    	
    	//Alternate player 1 and 2 for who goes first
    	if(_LastPlayerToGoFirst == _PlayerOne){
    		_LastPlayerToGoFirst = _CurrentPlayer = _PlayerTwo;
    	}else{
    		_LastPlayerToGoFirst = _CurrentPlayer = _PlayerOne;
    	}
    	
    	
    	//change the prompt on the screen
    	String msg = _CurrentPlayer.GetName() + "'s Turn";
    	this.setPlayerMessage(msg);
    	
    }

    protected void updateWinCountMessages()
    {	
    	String msg;
    	TextView playerMsg;
    	
    	if(_Mode == SetUpGame.THREE_BY_THREE)
    		playerMsg = (TextView)findViewById(R.id.PlayerOneWinCount);
    	else
    		playerMsg = (TextView)findViewById(R.id.Five_Five_PlayerOneWinCount);
    	
		msg = _PlayerOne.GetName() + ": " + _PlayerOne.GetWinCount() +  
			((_PlayerOne.GetWinCount() == 1) ? " win":" wins");
    	
    	playerMsg.setText(msg);
    	
    	if(_Mode == SetUpGame.THREE_BY_THREE)
    		playerMsg = (TextView)findViewById(R.id.PlayerTwoWinCount);
    	else
    		playerMsg = (TextView)findViewById(R.id.Five_Five_PlayerTwoWinCount);
    	
		msg = _PlayerTwo.GetName() + ": " + _PlayerTwo.GetWinCount() 
			+ ((_PlayerTwo.GetWinCount() == 1) ? " win":" wins");
		
    	playerMsg.setText(msg);
    	
    }

    /*
     * Method that will update the text displayed on the screen.
     */
	protected void updateScreenData()
	{
		 //prompt the first player it is their turn      
        setPlayerMessage( _CurrentPlayer.GetName() + "'s Turn");
        
        //display the correct names for win count messages
        updateWinCountMessages();
	}
	
    
}
