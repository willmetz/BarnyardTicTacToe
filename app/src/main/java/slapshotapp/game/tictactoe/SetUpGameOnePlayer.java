package slapshotapp.game.tictactoe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;
import java.util.Random;
import slapshotapp.game.support.Player;

public class SetUpGameOnePlayer extends SetUpGame {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _GameType = StartGame.ONE_PLAYER_GAME;

        //create the players (give them default names and symbols)
        _PlayerOne = Player.getInstance(Player.PLAYER_ONE);
        _PlayerTwo = Player.getInstance(Player.PLAYER_TWO);

        if (savedInstanceState != null) {
            _PlayerOne.SetName(savedInstanceState.getString(PLAYER_ONE_NAME_KEY));
            _PlayerOne.SetSymbol(savedInstanceState.getString(PLAYER_ONE_ICON_KEY),
                _SymbolNameToDrawableHash.get(savedInstanceState.getString(PLAYER_ONE_ICON_KEY)));
            _PlayerTwo.SetName(savedInstanceState.getString(PLAYER_TWO_NAME_KEY));
            randomizeComputerIcon();

            _GameBoardModeSpinner.setSelection(
                savedInstanceState.getShort(GAME_BOARD_DIMENSION_KEY));
            _GameBoardDimensions = savedInstanceState.getShort(GAME_BOARD_DIMENSION_KEY);
        } else {
            _PlayerOne.setDefaultName("Player 1");
            _PlayerOne.SetSymbol(_SymbolNameList[0],
                _SymbolNameToDrawableHash.get(_SymbolNameList[0]));
            _PlayerTwo.setDefaultName("Computer");
            randomizeComputerIcon();
        }

        //As this is a one player game we only need to show player 1 setup info
        playerTwoGroup.setVisibility(View.GONE);

        _PlayerOneName.setImeOptions(EditorInfo.IME_ACTION_DONE);

        _PlayerOneName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);

                if(actionID == EditorInfo.IME_ACTION_DONE && keyEvent.getAction() == KeyEvent.ACTION_DOWN && inputMethodManager != null){
                    inputMethodManager.hideSoftInputFromWindow(_PlayerOneName.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View viewClicked, int position,
        long rowId) {
        super.onItemSelected(adapterView, viewClicked, position, rowId);

        randomizeComputerIcon();
    }

    private void randomizeComputerIcon() {
        //Make sure that the computer player is not the same icon as player one
        Random num = new Random();
        String computerPlayerIcon = "";

        while (true) {
            int index = num.nextInt(_SymbolNameList.length);

            if (!_SymbolNameList[index].contentEquals(_PlayerOne.GetSymbolValue())) {
                computerPlayerIcon = _SymbolNameList[index];
                break;
            }
        }

        _PlayerTwo.SetSymbol(computerPlayerIcon, _SymbolNameToDrawableHash.get(computerPlayerIcon));

        //set the sound effect for player 2
        this.setPlayerSoundEffect(computerPlayerIcon, _PlayerTwo);
    }

    @Override
    public void startGame(Bundle bundle) {
        myIntent = new Intent(this, OnePlayerGame.class);
        myIntent.putExtras(bundle);

        if(_PlayerOne != null){
            String name = _PlayerOneName.getText().toString();
            _PlayerOne.SetName(name);
        }

        //launch the activity
        startActivityForResult(myIntent, PLAY_GAME_ID);
    }
}
