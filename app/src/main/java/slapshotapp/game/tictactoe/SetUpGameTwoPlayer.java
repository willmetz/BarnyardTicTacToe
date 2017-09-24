package slapshotapp.game.tictactoe;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import slapshotapp.game.support.Player;

public class SetUpGameTwoPlayer extends SetUpGame {
    /** Called when the activity is first created. */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _GameType = StartGame.TWO_PLAYER_GAME;

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
            _PlayerOne.setDefaultName("Player 1");
            _PlayerOne.SetSymbol(_SymbolNameList[0],
                _SymbolNameToDrawableHash.get(_SymbolNameList[0]));
            _PlayerTwo.setDefaultName("Player 2");
            _PlayerTwo.SetSymbol(_SymbolNameList[1],
                _SymbolNameToDrawableHash.get(_SymbolNameList[1]));
        }

        _PlayerOneName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        _PlayerOneName.setNextFocusDownId(_PlayerOneName.getId());
        _PlayerTwoName.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    @Override public void startGame(Bundle bundle) {
        myIntent = new Intent(this, TwoPlayerGame.class);
        myIntent.putExtras(bundle);

        if(_PlayerOne != null){
            _PlayerOne.SetName(_PlayerOneName.getText().toString());
        }

        if(_PlayerTwo != null){
            _PlayerTwo.SetName(_PlayerTwoName.getText().toString());
        }

        //launch the activity
        startActivityForResult(myIntent, PLAY_GAME_ID);
    }
}
