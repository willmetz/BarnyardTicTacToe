package slapshotapp.game.tictactoe;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartGame extends AppCompatActivity {
  public static final short ONE_PLAYER_GAME = 1, TWO_PLAYER_GAME = 2, BLUETOOTH_GAME = 3;

  private final int REQUEST_ENABLE_BT = 1237;

  @BindView(R.id.VersionInfo) TextView versionString;

  @BindView(R.id.bluetoothButton) Button blueToothGameButton;

  /** Called when the activity is first created. */
  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start_screen);

    ButterKnife.bind(this);

    versionString.setText(getVersionString());

    if (BluetoothAdapter.getDefaultAdapter() == null) {// Device does not support Bluetooth

      //remove the bluetooth game option
      blueToothGameButton.setVisibility(View.INVISIBLE);
      blueToothGameButton.setClickable(false);
    } else {//device does support bluetooth

      //make sure the bluetooth game option is available
      blueToothGameButton.setVisibility(View.VISIBLE);
      blueToothGameButton.setClickable(true);
    }
  }

  @OnClick(R.id.onePlayerButton) protected void onePlayerGameSelected() {
    startActivity(new Intent(this, SetUpGameOnePlayer.class));
  }

  @OnClick(R.id.twoPlayerButton) protected void twoPlayerGameSelected() {
    startActivity(new Intent(this, SetUpGameTwoPlayer.class));
  }

  @OnClick(R.id.bluetoothButton) protected void blueToothGameSelected() {
    //Make sure that bluetooth is enabled
    BluetoothAdapter myAdapter = BluetoothAdapter.getDefaultAdapter();
    if (!myAdapter.isEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    } else {
      //this game will be played over bluetooth
      startActivity(new Intent(this, StartBluetoothGame.class));
    }
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_ENABLE_BT) {
      if (resultCode == RESULT_OK) {
        startActivity(new Intent(this, StartBluetoothGame.class));
      } else {
        showBluetoothDisabledAlert();
      }
    }
  }

  private String getVersionString() {
    String version;

    //attempt to read the version string from the manifest xml file
    try {
      PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
      version = "Version: " + pi.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      version = "Version: Unable to retrieve";
    }

    return version;
  }

  private void showBluetoothDisabledAlert() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Bluetooth not enabled");

    //set this class to be the listener
    builder.setPositiveButton("Select another game mode", new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        //TODO think about disabling the bluetooth option here
      }
    });
    builder.setMessage("Bluetooth needs to be enabled for this game mode");

    //create the dialog
    builder.create().show();
  }
}

