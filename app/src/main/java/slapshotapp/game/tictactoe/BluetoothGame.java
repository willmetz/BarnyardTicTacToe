package slapshotapp.game.tictactoe;

import java.lang.ref.WeakReference;
import slapshotapp.game.support.BluetoothMessages;
import slapshotapp.game.support.Cell;
import slapshotapp.game.support.MyAlertDialogFragment;
import slapshotapp.game.tictactoe.BluetoothService.LocalBinder;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class BluetoothGame extends PlayGame implements ServiceConnection
{	
	private BluetoothService _myBluetoothService;
	private boolean _ServiceBound;
	private BluetoothMessageHandler _msgHandler;
	private Cell _lastMoveMade;
	private boolean _connectionLost, _InvalidMoveReceived;
	
	@Override
	public void InitGame()
	{		
		_msgHandler = new BluetoothMessageHandler(this);
	}

	@Override
	public void onRestart()
	{
		super.onRestart();
		
		//until a re-connection is handled, end the game
		Toast.makeText(getApplicationContext(), R.string.communication_issue_restart_toast, 
				Toast.LENGTH_LONG).show();
		setResult(PlayGame.EXIT_GAME);
    	finish();
	}
	
	    
	@Override
    public void onResume()
    {
        super.onResume();

    	//bind to the service, clear bound flag
    	_ServiceBound = false;
    	Intent bindIntent = new Intent(this, BluetoothService.class);
    	bindService(bindIntent, this, Context.BIND_AUTO_CREATE);
    }

    
	@Override
    public void onStop()
    {
        super.onStop();

		//disconnect from the service
    	if(_ServiceBound){
    		
    		unbindService(this);
    		_ServiceBound = false;
    	}	
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        _connectionLost = _InvalidMoveReceived = false;
    }

    @Override
    public void GameBoardClickListener(View target)
    {
    	//let the base class handle the move
    	if(_CurrentPlayer == _PlayerOne){
    		_lastMoveMade = BoardMove(target.getId());
    		
    		if(_lastMoveMade != null){
    			sendMessage(BluetoothMessages.MAKE_MOVE_MESSAGE_ID);
    		}
    	}else{//tell the player that we are waiting for the other player
    		String msg = "Waiting for " + _PlayerTwo.GetName();
    		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    	}
    }

    @Override
	public void onServiceConnected(ComponentName name, IBinder service) 
	{
		Log.d("BluetoothGame", "Service connected");
		
		LocalBinder binder = (LocalBinder)service;
		_myBluetoothService = binder.getService();
		_ServiceBound = true;
		
		//determine who is the first to go in the first game
		if(_myBluetoothService.IsServer()){
			_LastPlayerToGoFirst = _CurrentPlayer = _PlayerOne; 
		}else{
			_LastPlayerToGoFirst = _CurrentPlayer = _PlayerTwo;
		}
		
		//setup a handler for the service
        _myBluetoothService.setHandler(_msgHandler);
		
		updateScreenData();
	}

    @Override
	public void onServiceDisconnected(ComponentName name) 
	{
		_ServiceBound = false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem pItem)
    {
    	switch(pItem.getItemId())
    	{
	    	case R.id.menu_MainMenu://only override the quit option
	    		sendMessage(BluetoothMessages.QUIT_MESSAGE_ID);//alert opponent of quitting
	    		setResult(PlayGame.EXIT_GAME);
	    		finish();
	    		break;
	    	default:
	    		super.onOptionsItemSelected(pItem);
    	}
    	
    	
    	
    	return true;
    }
		
	private void handleOtherPlayerMove(int col, int row)
	{
		boolean failedToHandleMessage = false;
		
		//ensure that the column and rows are in range and id a cell not already selected
		//the game board is 
		if(_MyGame.IsMoveOnBoard(row, col)){
			
			if(_MyGame.IsCellEmpty(row, col)){
				
				_InvalidMoveReceived = false;
				
				//the move is valid, go ahead and make it
				_MyGame.MakeMove(row, col, 
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
			else{
				Log.e("BluetoothGame", "Received Invalid move message(Cell occupied) row = " + row + " col = " + col);
				
				failedToHandleMessage = true;
			}
		}else{
			Log.e("BluetoothGame", "Received Invalid move message row = " + row + " col = " + col);
			
			failedToHandleMessage = true;
		}
		
		//check to ensure that the move was handled
		if(failedToHandleMessage){

			if(!_InvalidMoveReceived){
				//the move is invalid and has not been received as invalid already, request move again
				sendMessage(BluetoothMessages.INVALID_MOVE_MESSAGE_ID);
				_InvalidMoveReceived = true;
			}else{
				//the move has already been received as invalid once, request a new game
				sendMessage(BluetoothMessages.NEW_GAME_MESSAGE_ID);
				ResetGame();
				_InvalidMoveReceived = false;
				Toast.makeText(getApplicationContext(), R.string.communication_issue_restart_toast, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void sendMessage(short msgID)
	{
		boolean sendMsg = true;
		byte dataToSend[] = null;
		
		switch(msgID)
		{
			case BluetoothMessages.MAKE_MOVE_MESSAGE_ID:
				slapshotapp.game.support.MoveMessage moveMsg = new slapshotapp.game.support.MoveMessage();
				moveMsg.SetMove(_lastMoveMade.GetCellRow(), _lastMoveMade.GetCellColumn());
				dataToSend = moveMsg.convertObjectToBytes().array();
				break;
			case BluetoothMessages.QUIT_MESSAGE_ID:
				slapshotapp.game.support.QuitMessage quitMsg = new slapshotapp.game.support.QuitMessage();
				dataToSend = quitMsg.convertObjectToBytes().array();
				break;
			case BluetoothMessages.NEW_GAME_MESSAGE_ID:
				slapshotapp.game.support.NewGameMessage newGameMsg = new slapshotapp.game.support.NewGameMessage();
				newGameMsg.SetResponseField(slapshotapp.game.support.NewGameMessage.NO_RESPONSE_REQUIRED);
				dataToSend = newGameMsg.convertObjectToBytes().array();
				break;
			case BluetoothMessages.INVALID_MOVE_MESSAGE_ID:
				slapshotapp.game.support.InvalidMoveMessage invalidMoveMsg = new slapshotapp.game.support.InvalidMoveMessage();
				dataToSend = invalidMoveMsg.convertObjectToBytes().array();
			default:
				sendMsg = false;
				break;
		}
		
		if(sendMsg){
    		if(_myBluetoothService != null){
	    		_myBluetoothService.SendPacket(dataToSend);
    		}
    	}		
	}
	
	private void processMessage(byte[] buffer)
	{
		BluetoothMessages baseMsg = new BluetoothMessages(buffer);
				
		switch(baseMsg.getMessageID())
		{
			case BluetoothMessages.MAKE_MOVE_MESSAGE_ID:
			{
				slapshotapp.game.support.MoveMessage inMsg = new slapshotapp.game.support.MoveMessage(buffer);
				
				//handle the other players move
				handleOtherPlayerMove(inMsg.GetMoveColumn(), inMsg.GetMoveRow());
				break;
			}
			case BluetoothMessages.QUIT_MESSAGE_ID:
			{
				
				//indicate that the game is exiting due to other player quitting
				String msg = _PlayerTwo.GetName() + " has exited, game is over";
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				
				setResult(PlayGame.EXIT_GAME);
		    	finish();
		    	break;
			}
			case BluetoothMessages.NEW_GAME_MESSAGE_ID:
			{			
				ResetGame();
				break;
			}
			default:
				Log.e("BluetoothGame", "Error unknown message id received: " + baseMsg.getMessageID());
				break;
		}
	}
	
	protected static class BluetoothMessageHandler extends Handler
	{
		private WeakReference<BluetoothGame> _game;
		
		public BluetoothMessageHandler(BluetoothGame inGame)
		{
			_game = new WeakReference<BluetoothGame>(inGame);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			BluetoothGame actualGame = _game.get();
			switch(msg.what)
			{
				case BluetoothService.MESSAGE_READ_FAILED:
					break;
				case BluetoothService.MESSAGE_READ_PACKET_COMPLETE:
					
					actualGame.processMessage((byte[])msg.obj);
					break;
				case BluetoothService.MESSAGE_WRITE_FAILED:
					break;
				case BluetoothService.MESSAGE_CONNECTION_LOST:
            	{
            		actualGame._connectionLost = true;
            		
            		//instantiate the prompt dialog for game over
        			DialogFragment newDialog = MyAlertDialogFragment.newInstance(
        					R.string.connection_lost_alert_dialog_title, 
        					R.string.connection_lost_alert_dialog_button, 
        					MyAlertDialogFragment.NO_BUTTON,
        					"Connection was lost");
        			newDialog.show(actualGame.getSupportFragmentManager(), "GameOverDialog");
            		break;
            	}
			}
		}
	}
	
	@Override
	public void alertDialogButtonClick(int buttonNum) 
	{
		if(_connectionLost){//Lost connection dialog is showing
			if(buttonNum == AlertDialog.BUTTON_POSITIVE )
            {
				setResult(PlayGame.EXIT_GAME);
				finish();
			}
		}
		else{//game over dialog is showing
			switch(buttonNum)
			{
				case AlertDialog.BUTTON_POSITIVE:
					//Send message to other player to reset the game
					sendMessage(BluetoothMessages.NEW_GAME_MESSAGE_ID);
					ResetGame();
					break;
				case AlertDialog.BUTTON_NEGATIVE:
					sendMessage(BluetoothMessages.QUIT_MESSAGE_ID);
					setResult(EXIT_GAME);
					finish();
					break;
			}
		}		
	}
	
	@Override
    public void onBackPressed()
    {
    	setResult(PlayGame.EXIT_GAME);
    	finish();
    }

}
