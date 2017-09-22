package slapshotapp.game.tictactoe;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import slapshotapp.game.support.bluetooth_protocol.BluetoothMessages;
import slapshotapp.game.support.FragmentAlertDialog;
import slapshotapp.game.support.MyAlertDialogFragment;
import slapshotapp.game.support.Player;
import slapshotapp.game.support.TicTacToeDBHelper;
import slapshotapp.game.support.bluetooth_protocol.BoardSizeMessage;
import slapshotapp.game.support.bluetooth_protocol.PlayerImageMessage;
import slapshotapp.game.support.bluetooth_protocol.PlayerNameMessage;
import slapshotapp.game.support.bluetooth_protocol.StartMessage;
import slapshotapp.game.tictactoe.BluetoothService.LocalBinder;

public class SetUpGameBluetooth extends SetUpGame
    implements FragmentAlertDialog, ServiceConnection, OnCancelListener {
    public static final String DEFAULT_OPPONENT_NAME = "Opponent";
    public static final String DEFAULT_PLAYER_NAME = "This Player";

    private final String TAG = "SetUpGameBluetooth";

    private boolean _BoundToBluetoothService;
    private BluetoothService _myBluetoothService;
    private BluetoothHandler _MyHandler;
    private boolean _receivedOpponentStartMessage;
    private Timer _WaitForOpponentTimer;
    private WaitForOpponent _WaitTask;
    private ProgressDialog _OpponentWaitDialog;
    private boolean _AbortStart;
    private TicTacToeDBHelper _MyDBHelper;
    protected Bundle gameBundle;

    /** Called when the activity is first created. */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _GameType = StartGame.BLUETOOTH_GAME;
        _MyHandler = new BluetoothHandler(this);

        //create the players (give them default names and symbols)
        _PlayerOne = Player.getInstance(Player.PLAYER_ONE);
        _PlayerTwo = Player.getInstance(Player.PLAYER_TWO);

        if (savedInstanceState != null) {

            _PlayerOne.SetName(savedInstanceState.getString(PLAYER_ONE_NAME_KEY));
            _PlayerOne.SetSymbol(savedInstanceState.getString(PLAYER_ONE_ICON_KEY),
                _SymbolNameToDrawableHash.get(savedInstanceState.getString(PLAYER_ONE_ICON_KEY)));
            _PlayerTwo.SetName(savedInstanceState.getString(PLAYER_TWO_NAME_KEY));
            _PlayerTwo.SetSymbol(savedInstanceState.getString(PLAYER_TWO_ICON_KEY),
                _SymbolNameToDrawableHash.get(savedInstanceState.getString(PLAYER_TWO_ICON_KEY)));

            _GameBoardModeSpinner.setSelection(
                savedInstanceState.getShort(GAME_BOARD_DIMENSION_KEY));
            _GameBoardDimensions = savedInstanceState.getShort(GAME_BOARD_DIMENSION_KEY);
        } else {
            _PlayerOne.setDefaultName(DEFAULT_PLAYER_NAME);
            _PlayerOne.SetSymbol(_SymbolNameList[0],
                _SymbolNameToDrawableHash.get(_SymbolNameList[0]));
            _PlayerTwo.setDefaultName(DEFAULT_OPPONENT_NAME);
            _PlayerTwo.SetSymbol(_SymbolNameList[1],
                _SymbolNameToDrawableHash.get(_SymbolNameList[1]));
        }

        //get the player two info fields as this game does not require them to be displayed
        playerTwoGroup.setVisibility(View.GONE);

        //update the labels for the player name and icon for this game
        _PlayerTwoName.setText(R.string.OnePlayerNameLabel);

        _receivedOpponentStartMessage = _AbortStart = false;

        _MyDBHelper = new TicTacToeDBHelper(this);
    }

    public void onStart() {
        super.onStart();

        //bind to the service
        Intent myIntent = new Intent(this, BluetoothService.class);
        bindService(myIntent, this, Context.BIND_AUTO_CREATE);
    }

    public void onRestart() {
        super.onRestart();

        //until a re-connect is handled, end the game
        Toast.makeText(getApplicationContext(), R.string.communication_issue_restart_toast,
            Toast.LENGTH_LONG).show();
        setResult(BluetoothGame.EXIT_GAME);
        finish();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onStop()
     */
    public void onStop() {
        super.onStop();

        if (_BoundToBluetoothService) {
            unbindService(this);
            _BoundToBluetoothService = false;
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
     * Called by the system when the bluetooth service is bound to.
     */
    public void onServiceConnected(ComponentName name, IBinder service) {
        _BoundToBluetoothService = true;

        // We've bound to the bluetooth service, cast the IBinder and get LocalService instance
        LocalBinder binder = (LocalBinder) service;
        _myBluetoothService = binder.getService();

        //The service should already be connected, make sure though
        if (_myBluetoothService.getState() != BluetoothService.CONNECTION_STATE_CONNECTED) {
            Log.e(TAG, "Service should be connected and isn't, current state - "
                + _myBluetoothService.getStateString());
        }

        //setup a handler for the service
        _myBluetoothService.setHandler(_MyHandler);
    }

    /*
     * (non-Javadoc)
     * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
     * Called by the system when the bluetooth service is disconnected.
     */
    public void onServiceDisconnected(ComponentName name) {
        _BoundToBluetoothService = false;
    }

    @Override public void startGame(Bundle bundle) {

        sendBluetoothMessage(BluetoothMessages.PLAYER_NAME_MESSAGE_ID);
        sendBluetoothMessage(BluetoothMessages.PLAYER_IMAGE_MESSAGE_ID);
        sendBluetoothMessage(BluetoothMessages.START_GAME_MESSAGE_ID);

        gameBundle = bundle;

        if (_receivedOpponentStartMessage) {
            //start the game
            launchGame();
        } else if (_WaitTask == null) {//if not already waiting, wait for opponent info

            displayProgressDialog("Waiting for Opponent", "Opponent info required");
            _WaitTask = new WaitForOpponent();
            _WaitForOpponentTimer = new Timer();
            _WaitForOpponentTimer.scheduleAtFixedRate(_WaitTask, 25, 25);
        }
    }

    protected void launchGame() {
        myIntent = new Intent(this, BluetoothGame.class);
        myIntent.putExtras(gameBundle);

        //launch the activity
        startActivityForResult(myIntent, PLAY_GAME_ID);
    }

    public void onItemSelected(AdapterView<?> pAdapterView, View pViewClicked, int pPosition,
        long pRowId) {
        //let the base class handle the implementation of this method
        super.onItemSelected(pAdapterView, pViewClicked, pPosition, pRowId);

        //send a message to the other player if needed
        if (pAdapterView.equals(_GameBoardModeSpinner)) {
            sendBluetoothMessage(BluetoothMessages.BOARD_SIZE_MESSAGE_ID);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.content.DialogInterface.OnCancelListener#onCancel(android.content.DialogInterface)
     * Callback for when the progress dialog is dismissed by user
     */
    public void onCancel(DialogInterface arg0) {
        _AbortStart = true;
        sendBluetoothMessage(BluetoothMessages.START_GAME_MESSAGE_ID);
        _WaitTask.cancel();
        _WaitForOpponentTimer.cancel();

        _WaitTask = null;
        _WaitForOpponentTimer = null;
    }

    /*
     * Determines what to do based on the receipt of a message.
     *
     * @param msg A byte array that contains the message received.
     */
    protected void processMessage(byte[] dataIn) {
        BluetoothMessages baseMsg =
            new BluetoothMessages(dataIn);

        switch (baseMsg.getMessageID()) {
            case BluetoothMessages.BOARD_SIZE_MESSAGE_ID: {
                BoardSizeMessage msg =
                    new BoardSizeMessage(dataIn);

                if (msg.GetBoardSize() == THREE_BY_THREE) {
                    _GameBoardModeSpinner.setSelection(THREE_BY_THREE);
                } else {
                    _GameBoardModeSpinner.setSelection(FIVE_BY_FIVE);
                }

                break;
            }
            case BluetoothMessages.PLAYER_NAME_MESSAGE_ID: {
                PlayerNameMessage msg =
                    new PlayerNameMessage(dataIn);

                //Verify that the name received is valid
                if (msg.GetPlayerName() != null) {

                    //Don't set the opponent name if the default hasn't been changed
                    if (!msg.GetPlayerName().contains(DEFAULT_PLAYER_NAME)) {
                        //player two is always the other player in a bluetooth game
                        _PlayerTwo.SetName(msg.GetPlayerName());

                        //save the opponents name in the database for next time
                        _MyDBHelper.SetPlayerName(_PlayerTwo.GetName(),
                            _myBluetoothService.getDeviceMac());
                    }
                } else {
                    Log.e(TAG, "Received a player name message with no name.");
                }
                break;
            }
            case BluetoothMessages.PLAYER_IMAGE_MESSAGE_ID: {
                PlayerImageMessage msg =
                    new PlayerImageMessage(dataIn);

                if (msg.GetImageName() != null) {

                    //player two is always the other player in a bluetooth game
                    String symbolName = msg.GetImageName();
                    if (_SymbolNameToDrawableHash.containsKey(symbolName)) {
                        _PlayerTwo.SetSymbol(symbolName, _SymbolNameToDrawableHash.get(symbolName));

                        //set the sound that corresponds with the image
                        setPlayerSoundEffect(symbolName, _PlayerTwo);
                    } else {
                        Log.e(TAG, "The symbol \"" + symbolName + "\" does not exist.");
                    }
                } else {
                    Log.e(TAG, "Received a player image message with no name.");
                }
                break;
            }
            case BluetoothMessages.START_GAME_MESSAGE_ID: {
                StartMessage msg =
                    new StartMessage(dataIn);

                if (msg.isAbortSet() == StartMessage.ABORT_SET) {
                    _receivedOpponentStartMessage = false;
                } else {
                    _receivedOpponentStartMessage = true;
                }
                break;
            }
            default:
                Log.e(TAG, "Error: unknown message id of " + baseMsg.getMessageID());
                break;
        }
    }

    /*
     * Sends a bluetooth message to the client.
     *
     * @param msgID ID of message to send.
     */
    protected void sendBluetoothMessage(short msgID) {
        boolean sendMessage = true;
        byte[] dataToSend = null;

        switch (msgID) {
            case BluetoothMessages.BOARD_SIZE_MESSAGE_ID: {
                BoardSizeMessage msg =
                    new BoardSizeMessage();
                msg.SetBoardSize(_GameBoardDimensions);
                dataToSend = msg.convertObjectToBytes().array();
                break;
            }
            case BluetoothMessages.PLAYER_NAME_MESSAGE_ID: {
                //update player name before sending it
                _PlayerOne.SetName(_PlayerOneName.getText().toString());
                PlayerNameMessage msg =
                    new PlayerNameMessage();
                msg.SetPlayerName(_PlayerOne.GetName());
                dataToSend = msg.convertObjectToBytes().array();
                break;
            }
            case BluetoothMessages.PLAYER_IMAGE_MESSAGE_ID: {
                PlayerImageMessage msg =
                    new PlayerImageMessage();
                msg.SetImageName(_PlayerOne.GetSymbolValue());
                dataToSend = msg.convertObjectToBytes().array();
                break;
            }
            case BluetoothMessages.START_GAME_MESSAGE_ID: {
                StartMessage msg =
                    new StartMessage();
                if (_AbortStart) {
                    msg.Abort();
                    _AbortStart = false;//clear the flag for next time
                }
                dataToSend = msg.convertObjectToBytes().array();
                break;
            }
            default://don't send and unknown message
                sendMessage = false;
                break;
        }

        if (sendMessage) {
            if (_myBluetoothService != null) {
                _myBluetoothService.SendPacket(dataToSend);
            }
        }
    }

    /*
     * Method used to start a cancelable progress dialog
     * with an indeterminate progress indication.
     *
     * @param msg Message to display in dialog
     * @param title Title for dialog
     */
    private void displayProgressDialog(String msg, String title) {
        //create and display the dialog
        _OpponentWaitDialog = ProgressDialog.show(this, title, msg, true, true, this);
        _OpponentWaitDialog.setOnCancelListener(this);
    }

    /*
     * The handler for the bluetooth messages received by the bluetooth service.
     *
     */
    protected static class BluetoothHandler extends Handler {
        private final WeakReference<SetUpGameBluetooth> _myGame;

        BluetoothHandler(SetUpGameBluetooth game) {
            _myGame = new WeakReference<SetUpGameBluetooth>(game);
        }

        /*
         * (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        public void handleMessage(Message msg) {
            SetUpGameBluetooth game = _myGame.get();

            switch (msg.what) {
                //TODO need to add a blue tooth state change handler in case of disconnect
                case BluetoothService.MESSAGE_READ_FAILED:
                    Log.e(game.TAG, "Error reading message");
                    break;
                case BluetoothService.MESSAGE_READ_PACKET_COMPLETE:
                    game.processMessage((byte[]) msg.obj);
                    break;
                case BluetoothService.MESSAGE_WRITE_FAILED:
                    Log.e(game.TAG, "Error writing message");
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST: {
                    //instantiate the prompt dialog for game over
                    DialogFragment newDialog = MyAlertDialogFragment.newInstance(
                        R.string.connection_lost_alert_dialog_title,
                        R.string.connection_lost_alert_dialog_button,
                        MyAlertDialogFragment.NO_BUTTON, "Connection was lost");
                    newDialog.show(game.getSupportFragmentManager(), "GameOverDialog");
                    break;
                }
            }
        }
    }

    ;

    /*
     * Inner class used to wait for opponent to send required data.
     */
    private class WaitForOpponent extends TimerTask {
        public WaitForOpponent() {
        }

        @Override public void run() {
            //check to see if we have received start message
            if (_receivedOpponentStartMessage) {
                //cancel the timer
                this.cancel();

                //dismiss the progress dialog (if it is showing)
                if (_OpponentWaitDialog != null) {
                    //dismiss the dialog without calling the cancel callback
                    _OpponentWaitDialog.dismiss();
                }

                //start the game
                launchGame();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see slapshotapp.game.support.FragmentAlertDialog#alertDialogButtonClick(int)
     */
    public void alertDialogButtonClick(int buttonNum) {
        if (buttonNum == AlertDialog.BUTTON_POSITIVE) {
            //set the activity result to indicate that the connection was lost
            setResult(BluetoothGame.EXIT_GAME);
            finish();
        }
    }
}
