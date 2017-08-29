package slapshotapp.game.support;

import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.SparseArray;
import java.io.IOException;

public class Player {
  public static final int PLAYER_ONE = 0, PLAYER_TWO = 1;
  public static final String DEFAULT_PLAYER_NAME = "No Name";
  private static SparseArray<Player> _PlayerInstances = new SparseArray<Player>();
  private static int _PlayerIDCount = 0;

  private final int NO_WINS = 0;
  private String _Name;
  private String _SymbolValue;
  private Drawable _SymbolImage;
  private int _WinCount;
  private MediaPlayer _MyPlayer;
  private boolean _SoundEffectLoaded, nameIsDefault;
  private final String TAG = "Player Class";
  private int _UniquePlayerID;

  /*
   * Protected constructor to prevent this class from being instantiated
   * outside of this class.
   */
  protected Player() {
    _Name = DEFAULT_PLAYER_NAME;
    _SymbolValue = "NoSymbol";
    _SymbolImage = null;
    _WinCount = NO_WINS;
    _MyPlayer = null;
    _SoundEffectLoaded = false;
    _UniquePlayerID = _PlayerIDCount++;
  }

  public static synchronized Player getInstance(int whichPlayer) {
    if (_PlayerInstances.get(whichPlayer) == null) {
      //create and add the player to the hash
      _PlayerInstances.put(whichPlayer, new Player());
    } else {
      Log.d("PLAYER", "Instance already exists");
    }

    return _PlayerInstances.get(whichPlayer);
  }

  public void ClearWinCount() {
    _WinCount = NO_WINS;
  }

  public int GetWinCount() {
    return _WinCount;
  }

  public void IncrementWinCount() {
    _WinCount++;
  }

  public boolean PlaySoundEffect() {
    boolean isPlaying = false;

    if (_SoundEffectLoaded && _MyPlayer != null) {
      try {
        _MyPlayer.start();
        isPlaying = true;
      } catch (IllegalStateException e) {
        isPlaying = false;
      }
    }

    return isPlaying;
  }

  public void ReleaseMediaPlayer() {
    if (_MyPlayer != null) {
      _MyPlayer.release();
      _MyPlayer = null;
    }
  }

  /*
   * Returns the players sound effect length in milliseconds
   *
   * @return  Length of players sound in ms
   */
  public int GetSoundEffectLength() {
    int timeMS = 0;

    if (_SoundEffectLoaded && _MyPlayer != null) {
      timeMS = _MyPlayer.getDuration();
    }

    return timeMS;
  }

  public boolean IsSoundPlaying() {
    boolean isPlaying;

    if (_MyPlayer == null) {
      return false;
    }

    try {
      isPlaying = _MyPlayer.isPlaying();
    } catch (IllegalStateException e) {
      isPlaying = false;
    }

    return isPlaying;
  }

  public void SetSoundEffect(AssetFileDescriptor pSoundEffect) {
    _SoundEffectLoaded = false;

    if (_MyPlayer == null) {
      _MyPlayer = new MediaPlayer();
    }

    try {
      _MyPlayer.reset();

      _MyPlayer.setDataSource(pSoundEffect.getFileDescriptor(), pSoundEffect.getStartOffset(),
          pSoundEffect.getLength());

      //attempt to prepare the media player to play
      _MyPlayer.prepare();

      _SoundEffectLoaded = true;
    } catch (IllegalArgumentException e) {

      e.printStackTrace();
      Log.e(TAG, "Illegal Arguments Exception when preparing the media player\n");
    } catch (IllegalStateException e) {
      e.printStackTrace();
      Log.e(TAG, "IllegalStateException Exception when preparing the media player\n");
    } catch (IOException e) {
      Log.e(TAG, "IOException Exception when preparing the media player\n");
      e.printStackTrace();
    }
  }

  public void SetSymbol(String pValue, Drawable pImage) {
    if (pImage != null && pValue != null) {
      _SymbolValue = pValue;
      _SymbolImage = pImage;
    }
  }

  public Drawable GetSymbolImage() {
    return _SymbolImage;
  }

  public void SetSymbolImage(Drawable pImage) {
    _SymbolImage = pImage;
  }

  public String GetName() {
    return _Name;
  }

  public void SetName(String pName) {
    if (pName != null) {
      _Name = pName;
      nameIsDefault = false;
    }
  }

  public void setDefaultName(String name) {
    if (name != null) {
      _Name = name;
      nameIsDefault = true;
    }
  }

  public boolean isNameDefault() {
    return nameIsDefault;
  }

  public String GetSymbolValue() {
    return _SymbolValue;
  }

  public void SetSymbolValue(String pSymbol) {
    _SymbolValue = pSymbol;
  }

  public int GetPlayerID() {
    return _UniquePlayerID;
  }
}
