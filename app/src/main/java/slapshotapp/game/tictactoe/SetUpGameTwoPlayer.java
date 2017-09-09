package slapshotapp.game.tictactoe;

import android.content.Intent;
import android.os.Bundle;
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
    }

    @Override public void startGame(Bundle bundle) {
        myIntent = new Intent();

        myIntent.setClassName("slapshotapp.game.tictactoe",
            "slapshotapp.game.tictactoe.TwoPlayerGame");
        myIntent.putExtras(bundle);
        //launch the activity
        startActivityForResult(myIntent, PLAY_GAME_ID);
    }
}
