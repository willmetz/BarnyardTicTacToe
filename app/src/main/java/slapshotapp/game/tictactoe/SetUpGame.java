package slapshotapp.game.tictactoe;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.io.IOException;
import java.util.HashMap;
import slapshotapp.game.fragments.InputDialogFragment;
import slapshotapp.game.support.Player;

public abstract class SetUpGame extends AppCompatActivity
    implements OnItemSelectedListener, InputDialogFragment.ActionListener {
  //public members
  public static final String GAME_BOARD_DIMENSION_KEY =
      "slapshotapp.game.tictactoe.board_dimensions";
  public static final String PLAYER_ONE_NAME_KEY = "slapshotapp.game.tictactoe.player_one_name";
  public static final String PLAYER_ONE_ICON_KEY = "slapshotapp.game.tictactoe.player_one_icon";
  public static final String PLAYER_TWO_NAME_KEY = "slapshotapp.game.tictactoe.player_two_name";
  public static final String PLAYER_TWO_ICON_KEY = "slapshotapp.game.tictactoe.player_two_icon";
  public static final int EXIT_GAME = 0;
  public static final int THREE_BY_THREE = 0, FIVE_BY_FIVE = 1;

  //private static members
  private static final String TAG = "SetUpGame Class";

  //protected constants
  protected final int NUMBER_OF_SYMBOLS = 8;
  protected final int PLAY_GAME_ID = 99;

  //protected members
  protected HashMap<String, Drawable> _SymbolNameToDrawableHash;
  protected HashMap<String, Integer> _SymbolNameToSoundEffectHash;
  protected String[] _SymbolNameList;
  protected Intent myIntent;
  protected ArrayAdapter<String> _PlayerOneSpinnerList, _PlayerTwoSpinnerList;
  protected short _GameBoardDimensions, _GameType;
  protected Player _PlayerOne, _PlayerTwo;

  @BindView(R.id.PlayerOneSpinner) protected Spinner _PlayerOneSpinner;

  @BindView(R.id.PlayerTwoSpinner) protected Spinner _PlayerTwoSpinner;

  @BindView(R.id.gameBoardDimensionSpinner) protected Spinner _GameBoardModeSpinner;

  @BindView(R.id.PlayerTwoName) protected TextView _PlayerTwoName;

  @BindView(R.id.PlayerOneName) protected TextView _PlayerOneName;

  @BindView(R.id.PlayerTwoIconContainer) LinearLayout playerTwoIconContainer;

  @BindView(R.id.playerTwoNameContainer) LinearLayout playerTwoNameContainer;

  public abstract void startGame(Bundle bundle);

  @OnClick({ R.id.PlayerOneName, R.id.PlayerTwoName }) void playerNameClicked(View view) {
    if (view == _PlayerOneName) {
      showPlayerNameDialog("Player One Name", _PlayerOne);
    } else {
      showPlayerNameDialog("Player Two Name", _PlayerTwo);
    }
  }

  /** Called when the activity is first created. */
  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //set the layout to display
    setContentView(R.layout.setup_screen);

    ButterKnife.bind(this);

    //create the hash
    _SymbolNameToDrawableHash = new HashMap<String, Drawable>(NUMBER_OF_SYMBOLS);
    _SymbolNameToSoundEffectHash = new HashMap<String, Integer>(NUMBER_OF_SYMBOLS);

    //initialize the symbols and the mapping hash
    this.initSymbolsAndSounds();

    //set up the game mode spinner list
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.GameModeSize,
        android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    _GameBoardModeSpinner.setAdapter(adapter);
    _GameBoardModeSpinner.setOnItemSelectedListener(this);

    //set the default gameboard size to 3x3
    _GameBoardDimensions = THREE_BY_THREE;

    //assign the spinner lists
    this.setSpinnerDefaultLists();
  }

  /*
   * (non-Javadoc)
   * @see android.app.Activity#onDestroy()
   */
  public void onDestroy() {
    super.onDestroy();
    if (_PlayerOne != null) {
      _PlayerOne.ReleaseMediaPlayer();
    }
    if (_PlayerTwo != null) {
      _PlayerTwo.ReleaseMediaPlayer();
    }

    _PlayerOne = _PlayerTwo = null;
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    outState.putShort(GAME_BOARD_DIMENSION_KEY, _GameBoardDimensions);
    outState.putCharSequence(PLAYER_ONE_NAME_KEY, _PlayerOne.GetName());
    outState.putCharSequence(PLAYER_ONE_ICON_KEY, _PlayerOne.GetSymbolValue());
    outState.putCharSequence(PLAYER_TWO_NAME_KEY, _PlayerTwo.GetName());
    outState.putCharSequence(PLAYER_TWO_ICON_KEY, _PlayerTwo.GetSymbolValue());
  }

  /*
   * Starts the game activity.
   */
  @OnClick(R.id.startGameButton) public void startGameButtonClicked() {

    //create a bundle for the data to pass the game
    Bundle b = new Bundle();

    //tell the game if the computer needs to play or not
    b.putShort(GAME_BOARD_DIMENSION_KEY, _GameBoardDimensions);

    startGame(b);
  }

  //listener for the spinner items
  public void onItemSelected(AdapterView<?> pAdapterView, View pViewClicked, int pPosition,
      long pRowId) {
    String itemSelected = (String) pAdapterView.getItemAtPosition(pPosition);

    if (pAdapterView.equals(_PlayerOneSpinner)) {
      _PlayerOne.SetSymbol(itemSelected, _SymbolNameToDrawableHash.get(itemSelected));

      //set the sound effect for player 1
      this.setPlayerSoundEffect(itemSelected, _PlayerOne);
    } else if (pAdapterView.equals(_PlayerTwoSpinner)) {
      _PlayerTwo.SetSymbol(itemSelected, _SymbolNameToDrawableHash.get(itemSelected));

      //set the sound effect for player 2
      this.setPlayerSoundEffect(itemSelected, _PlayerTwo);
    } else if (pAdapterView.equals(_GameBoardModeSpinner)) {
      if (itemSelected.equalsIgnoreCase("3 x 3")) {
        _GameBoardDimensions = THREE_BY_THREE;
      } else {
        _GameBoardDimensions = FIVE_BY_FIVE;
      }
    }
  }

  //when nothing is selected in a spinner
  public void onNothingSelected(AdapterView<?> arg0) {
    // just need to implement method, do nothing

  }

  public void showPlayerNameDialog(String title, Player player) {
    String currentName = player.GetName();

    InputDialogFragment dialogFragment;

    if (player.isNameDefault()) {
      dialogFragment = InputDialogFragment.newInstance(title, null, player.GetPlayerID());
    } else {
      dialogFragment = InputDialogFragment.newInstance(title, currentName, player.GetPlayerID());
    }

    dialogFragment.setListener(this);

    dialogFragment.show(getSupportFragmentManager(), "dialogTag");
  }

  /*
   * Method to get the current name of player two, attempts
   * to get the name from the text edit box but if that fails then
   * uses the current name.  For a non two player game this will just
   * return the name of player 2 and not check the text edit.
   *
   * @return playerName a String representing the players name
   */
  protected String getPlayerTwoName() {

    //set the value to return to be the current name of the player in case something is incorrect
    //in the edit text area
    String playerName = "Unknown";
    if (_PlayerTwo != null) {
      playerName = _PlayerTwo.GetName();
    }

    return playerName;
  }

  /*
   * Method to get the current name of player one, attempts
   * to get the name from the text edit box but if that fails then
   * uses the current name.
   *
   * @return playerName a String representing the players name
   */
  protected String getPlayerOneName() {
    //set the value to return to be the current name of the player in case something is incorrect
    //in the edit text area
    String playerName = "Unknown";
    if (_PlayerOne != null) {
      playerName = _PlayerOne.GetName();
    }

    return playerName;
  }

  /*
   * This method will init the spinner drop down list values.
   */
  protected void setSpinnerDefaultLists() {
    //player one
    _PlayerOneSpinnerList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
    _PlayerOneSpinnerList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    _PlayerOneSpinner.setAdapter(_PlayerOneSpinnerList);
    _PlayerOneSpinner.setOnItemSelectedListener(this);

    //player two
    _PlayerTwoSpinnerList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
    _PlayerTwoSpinnerList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    _PlayerTwoSpinner.setAdapter(_PlayerTwoSpinnerList);
    _PlayerTwoSpinner.setOnItemSelectedListener(this);

    //init the drop down options
    for (int i = 0; i < NUMBER_OF_SYMBOLS; i++) {
      _PlayerOneSpinnerList.add(_SymbolNameList[i]);

      _PlayerTwoSpinnerList.add(_SymbolNameList[i]);
    }
  }

  /*
   * This method initializes the names to sound hash.
   */
  protected void initSymbolsAndSounds() {
    Drawable myDrawable;

    //create the symbol name array(increment the count if a symbol is added)
    _SymbolNameList = new String[NUMBER_OF_SYMBOLS];

    //this list is used as a hash key and to build the spinners
    //add an item to here and the hash in order to add a new image
    _SymbolNameList[0] = "Horse";
    _SymbolNameList[1] = "Cow";
    _SymbolNameList[2] = "Rooster";
    _SymbolNameList[3] = "Pig";
    _SymbolNameList[4] = "Dog";
    _SymbolNameList[5] = "Cat";
    _SymbolNameList[6] = "Goat";
    _SymbolNameList[7] = "Duck";

    //set up the hash for the drawable associated with the key
    myDrawable = getResources().getDrawable(R.drawable.horse);
    _SymbolNameToDrawableHash.put(_SymbolNameList[0], myDrawable);

    myDrawable = getResources().getDrawable(R.drawable.cow);
    _SymbolNameToDrawableHash.put(_SymbolNameList[1], myDrawable);

    myDrawable = getResources().getDrawable(R.drawable.chicken);
    _SymbolNameToDrawableHash.put(_SymbolNameList[2], myDrawable);

    myDrawable = getResources().getDrawable(R.drawable.pig);
    _SymbolNameToDrawableHash.put(_SymbolNameList[3], myDrawable);

    myDrawable = getResources().getDrawable(R.drawable.dog);
    _SymbolNameToDrawableHash.put(_SymbolNameList[4], myDrawable);

    myDrawable = getResources().getDrawable(R.drawable.cat_image);
    _SymbolNameToDrawableHash.put(_SymbolNameList[5], myDrawable);

    myDrawable = getResources().getDrawable(R.drawable.goat_image);
    _SymbolNameToDrawableHash.put(_SymbolNameList[6], myDrawable);

    myDrawable = getResources().getDrawable(R.drawable.duck_image);
    _SymbolNameToDrawableHash.put(_SymbolNameList[7], myDrawable);

    //set up the hash for the location of the sound effect to pair with icon
    _SymbolNameToSoundEffectHash.put(_SymbolNameList[0], R.raw.horse_sound);
    _SymbolNameToSoundEffectHash.put(_SymbolNameList[1], R.raw.cow_sound);
    _SymbolNameToSoundEffectHash.put(_SymbolNameList[2], R.raw.rooster_sound);
    _SymbolNameToSoundEffectHash.put(_SymbolNameList[3], R.raw.pig_sound);
    _SymbolNameToSoundEffectHash.put(_SymbolNameList[4], R.raw.dog_sound);
    _SymbolNameToSoundEffectHash.put(_SymbolNameList[5], R.raw.cat_sound);
    _SymbolNameToSoundEffectHash.put(_SymbolNameList[6], R.raw.goat_sound);
    _SymbolNameToSoundEffectHash.put(_SymbolNameList[7], R.raw.duck_sound);
  }

  protected void setPlayerSoundEffect(String effectName, Player player) {
    if (player == null) {
      return;
    }
    //get the raw resource file
    AssetFileDescriptor fd =
        getResources().openRawResourceFd(_SymbolNameToSoundEffectHash.get(effectName));

    //update the players media player to point to this new sound clip
    player.SetSoundEffect(fd);

    try {
      //attempt to close the file descriptor
      fd.close();
    } catch (IOException e) {
      e.printStackTrace();
      Log.e(TAG, "IO Exception when closing file descriptor\n");
    }
  }

  /*
   * (non-Javadoc)
   * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
   */
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PLAY_GAME_ID) {
      if (resultCode == PlayGame.BACK_BUTTON) {
        //TODO perhaps some init here?
      } else if (resultCode == PlayGame.EXIT_GAME) {
        setResult(resultCode);
        finish();
      }
    }
  }

  @Override public void onNameEntered(String name, int playerNumber) {
    if (_PlayerOne.GetPlayerID() == playerNumber) {
      _PlayerOne.SetName(name);
      _PlayerOneName.setText(name);
    } else {
      _PlayerTwo.SetName(name);
      _PlayerTwoName.setText(name);
    }
  }
}
