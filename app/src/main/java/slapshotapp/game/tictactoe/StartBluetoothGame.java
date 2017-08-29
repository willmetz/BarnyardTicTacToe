package slapshotapp.game.tictactoe;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import slapshotapp.game.support.BluetoothMessages;
import slapshotapp.game.support.FragmentAlertDialog;
import slapshotapp.game.support.MyAlertDialogFragment;
import slapshotapp.game.support.TicTacToeDBHelper;
import slapshotapp.game.support.VersionMessage;
import slapshotapp.game.tictactoe.BluetoothService.LocalBinder;

public class StartBluetoothGame extends FragmentActivity
    implements FragmentAlertDialog, ServiceConnection, OnItemClickListener {
  public static final int BLUETOOTH_GAME_EXIT = 2;

  private final int DISCOVERY_DURATION = 180; //The discovery duration in seconds
  private final String TAG = "StartBluetoothGame";
  private final int ENABLE_DISCOVERY_QUERY = 111;

  private ArrayAdapter<String> _newDevices, _previouslyPlayedAgainstDevices;
  private BluetoothAdapter _myBluetoothAdapter;
  private Boolean _ServiceBound, _devicesFoundInDiscovery, _AttemptingConnection;
  private BluetoothService _myBluetoothService;
  private BluetoothHandler _MyHandler;
  private TicTacToeDBHelper _MyDBHelper;
  private ProgressDialog _progressDialog;
  private SparseArray<String> _newDevicesMapping, _previouslyPlayedDevicesMapping;
  private int _newDeviceCount, _previouslyPlayedDeviceCount;
  private boolean _gameConnectingMessageShowing;

  /** Called when the activity is first created. */
  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //show a progress bar on activity loading
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setContentView(R.layout.bluetooth_device_list);

    //get the database of previous connections
    _MyDBHelper = new TicTacToeDBHelper(this);

    //point the array adapters to the correct lists
    _newDevices = new ArrayAdapter<String>(this, R.layout.device_name);
    _previouslyPlayedAgainstDevices = new ArrayAdapter<String>(this, R.layout.device_name);

    ListView deviceList = (ListView) findViewById(R.id.paired_devices);
    deviceList.setAdapter(_previouslyPlayedAgainstDevices);
    deviceList.setOnItemClickListener(this);

    deviceList = (ListView) findViewById(R.id.new_devices);
    deviceList.setAdapter(_newDevices);
    deviceList.setOnItemClickListener(this);

    _newDevicesMapping = new SparseArray<String>();
    _previouslyPlayedDevicesMapping = new SparseArray<String>();
    _newDeviceCount = _previouslyPlayedDeviceCount = 0;
  }

  public void onStart() {
    super.onStart();

    _myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    // Register for broadcasts when a device is discovered, discovery has finished
    // and discovery has started
    IntentFilter filter = new IntentFilter();
    filter.addAction(BluetoothDevice.ACTION_FOUND);
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
    this.registerReceiver(myBroadcastReceiver, filter);

    _ServiceBound = _AttemptingConnection = _gameConnectingMessageShowing = false;

    //start the bluetooth  service
    Intent myIntent = new Intent(this, BluetoothService.class);
    bindService(myIntent, this, Context.BIND_AUTO_CREATE);
  }

  public void onResume() {
    super.onResume();
  }

  public void onDestroy() {
    super.onDestroy();
  }

  @Override public void onPause() {
    super.onPause();
    //close any resources here if need be
  }

  @Override public void onStop() {
    super.onStop();

    // Unbind from the service
    if (_ServiceBound) {
      unbindService(this);
      _ServiceBound = false;
    }

    // Make sure we're not doing discovery anymore
    if (_myBluetoothAdapter != null) {
      _myBluetoothAdapter.cancelDiscovery();
    }

    // Unregister broadcast listeners
    this.unregisterReceiver(myBroadcastReceiver);
  }

  public void deviceClickListener(View target) {
    switch (target.getId()) {
      case R.id.bluetoothDiscoveryButton:

        enableBluetoothDiscovery();

        break;
    }
  }

  private void enableBluetoothDiscovery() {
    // If we're already discovering, stop it
    if (_myBluetoothAdapter.isDiscovering()) {
      _myBluetoothAdapter.cancelDiscovery();
    }

    //enable other discoverability so that other devices can see us
    if (_myBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
      Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
      discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERY_DURATION);
      startActivityForResult(discoverableIntent, ENABLE_DISCOVERY_QUERY);
    } else {
      Toast.makeText(getApplicationContext(), R.string.discovery_enable_toast_msg,
          Toast.LENGTH_SHORT).show();

      // Request discover from BluetoothAdapter
      _myBluetoothAdapter.startDiscovery();
    }
  }

  /*
   * (non-Javadoc)
   * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
   */
  public void onServiceConnected(ComponentName name, IBinder service) {
    // We've bound to the bluetooth service, cast the IBinder and get LocalService instance
    LocalBinder binder = (LocalBinder) service;
    _myBluetoothService = binder.getService();
    _ServiceBound = true;

    //set the message handler to allow communication with the service
    _MyHandler = new BluetoothHandler(this);
    _myBluetoothService.setHandler(_MyHandler);

    //start the bluetooth service
    _myBluetoothService.start();

    //Search for other bluetooth devices and make this device discoverable
    enableBluetoothDiscovery();
  }

  /*
   * (non-Javadoc)
   * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
   */
  public void onServiceDisconnected(ComponentName name) {
    _ServiceBound = false;
  }

  private void processMessage(byte[] msg) {
    BluetoothMessages base = new BluetoothMessages(msg);

    switch (base.getMessageID()) {
      case BluetoothMessages.VERSION_MESSAGE_ID:
        VersionMessage verMsg = new VersionMessage(msg);
        if (verMsg.GetVersion() == BluetoothService.PROTOCOL_VERSION) {

          //remove the progress dialog
          if (_gameConnectingMessageShowing) {
            _progressDialog.dismiss();
          }

          _MyDBHelper.AddConnection(_myBluetoothService.getDeviceName(),
              _myBluetoothService.getDeviceMac());

          //remove the handler reference to this class
          _myBluetoothService.removeHandler();

          Intent myIntent = new Intent();
          myIntent.setClassName("slapshotapp.game.tictactoe",
              "slapshotapp.game.tictactoe.SetUpGameBluetooth");
          startActivityForResult(myIntent, BLUETOOTH_GAME_EXIT);
        } else {
          Log.e(TAG, "Incompatable versions of game");
          Toast.makeText(getApplicationContext(), R.string.incompatible_versions_msg,
              Toast.LENGTH_LONG).show();
        }
        break;
      default:
        Log.e(TAG, "Error: unknown message id of " + base.getMessageID());
    }
  }

  /*
   * Called when the connection being attempted
   * has been canceled.
   */
  private void cancelConnection() {
    _gameConnectingMessageShowing = false;

    if (_ServiceBound) {
      //stop the connections
      _myBluetoothService.stop();

      //restart the connection listener
      _myBluetoothService.start();
    }

    Toast.makeText(getApplicationContext(), R.string.cancel_connection_toast, Toast.LENGTH_SHORT)
        .show();
  }

  private void receivingNewConnection() {
    _gameConnectingMessageShowing = true;
    _progressDialog = new ProgressDialog(this);
    _progressDialog.setTitle("Bluetooth Connection Pending");
    _progressDialog.setMessage("Connecting to device...");
    _progressDialog.setIndeterminate(true);
    _progressDialog.setCancelable(true);
    _progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

      public void onCancel(DialogInterface dialog) {
        cancelConnection();
      }
    });
    _progressDialog.show();
  }

  private void sendBluetoothMessage(short msgID) {
    boolean sendMessage = true;
    byte[] dataToSend = null;

    switch (msgID) {
      case BluetoothMessages.VERSION_MESSAGE_ID:
        VersionMessage msg = new VersionMessage();
        msg.SetVersion(BluetoothService.PROTOCOL_VERSION);
        dataToSend = msg.convertObjectToBytes().array();
        break;
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

  private final BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      //if a device is found, add it to the appropriate list
      if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        _devicesFoundInDiscovery = true;

        // Get the BluetoothDevice object from the Intent
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        //see if the mac address is in the database
        if (_MyDBHelper.ContainsMacAddress(device.getAddress())) {

          boolean deviceAlreadyInList = false;

          //check to see if the device has been added to the list already
          for (int i = 0; i < _previouslyPlayedDevicesMapping.size(); i++) {
            if (_previouslyPlayedDevicesMapping.get(i).contains(device.getAddress())) {
              deviceAlreadyInList = true;
              break;
            }
          }

          if (!deviceAlreadyInList) {//add the device to the list
            String playerName = _MyDBHelper.GetPlayerName(device.getAddress());
            String devName = _MyDBHelper.GetDeviceName(device.getAddress());
            _previouslyPlayedAgainstDevices.insert(playerName + "'s " + devName,
                _previouslyPlayedDeviceCount);

            //add the mac address for the device to the mapping list
            _previouslyPlayedDevicesMapping.put(_previouslyPlayedDeviceCount, device.getAddress());

            _previouslyPlayedDeviceCount++;
          }
        } else {//if the device is not in the database it must be a new device, see if it needs to be added

          //make sure that the list view and title for new devices are visible
          TextView title = (TextView) findViewById(R.id.new_devices_title);
          ListView list = (ListView) findViewById(R.id.new_devices);

          title.setText(R.string.new_device_list_title);
          if (title.getVisibility() != View.VISIBLE) {
            title.setVisibility(View.VISIBLE);
            list.setVisibility(View.VISIBLE);
          }

          boolean deviceAlreadyInList = false;

          //check to see if the device has been added to the list already
          for (int i = 0; i < _newDevicesMapping.size(); i++) {
            if (_newDevicesMapping.get(i).contains(device.getAddress())) {
              deviceAlreadyInList = true;
              break;
            }
          }

          if (!deviceAlreadyInList) {
            _newDevices.insert("Unknown Player's " + device.getName(), _newDeviceCount);

            //TODO add to sparse array for new devices
            //add the mac address for the device to the mapping list
            _newDevicesMapping.put(_newDeviceCount, device.getAddress());

            _newDeviceCount++;
          }
        }
      } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        setProgressBarIndeterminateVisibility(false);
        Button myButton = (Button) findViewById(R.id.bluetoothDiscoveryButton);
        myButton.setVisibility(View.VISIBLE);

        if (!_AttemptingConnection) {
          if (!_devicesFoundInDiscovery) {
            Toast.makeText(getApplicationContext(), R.string.no_device_toast_message,
                Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(getApplicationContext(), R.string.discovery_complete_toast,
                Toast.LENGTH_SHORT).show();
          }
        }
        _AttemptingConnection = false;
      } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

        _devicesFoundInDiscovery = false;

        setProgressBarIndeterminateVisibility(true);
        Button myButton = (Button) findViewById(R.id.bluetoothDiscoveryButton);
        myButton.setVisibility(View.INVISIBLE);
      }
    }
  };

  // The Handler that gets information back from the BluetoothService
  private static class BluetoothHandler extends Handler {
    private final WeakReference<StartBluetoothGame> _myGame;

    BluetoothHandler(StartBluetoothGame game) {
      _myGame = new WeakReference<StartBluetoothGame>(game);
    }

    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      StartBluetoothGame game = _myGame.get();
      switch (msg.what) {
        case BluetoothService.MESSAGE_CONNECTION_STATE:
          //The connection state has changed, get the new state
          switch (msg.arg1) {
            case BluetoothService.CONNECTION_STATE_NONE:
              break;
            case BluetoothService.CONNECTION_STATE_LISTEN:
              break;
            case BluetoothService.CONNECTION_STATE_CONNECTED:
              game.sendBluetoothMessage(BluetoothMessages.VERSION_MESSAGE_ID);

              break;
            case BluetoothService.CONNECTION_STATE_CONNECTING:
              if (!game._gameConnectingMessageShowing) {
                game.receivingNewConnection();
              }
              break;
          }
          break;
        case BluetoothService.MESSAGE_READ_PACKET_COMPLETE:
          game.processMessage((byte[]) msg.obj);
          break;
        case BluetoothService.MESSAGE_CONNECTION_LOST: {
          //instantiate the prompt dialog for game over
          DialogFragment newDialog =
              MyAlertDialogFragment.newInstance(R.string.connection_lost_alert_dialog_title,
                  R.string.connection_lost_alert_dialog_button, MyAlertDialogFragment.NO_BUTTON,
                  "Connection was lost");
          newDialog.show(game.getSupportFragmentManager(), "GameOverDialog");
          Log.d("StartBluetoothGame", "Message Connection Lost");
          break;
        }
      }
    }
  }

  ;

  /*
   * (non-Javadoc)
   * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
   */
  public void onItemClick(AdapterView<?> parent, View inView, int position, long rowID) {
    ListView pairedDevicesList = (ListView) findViewById(R.id.paired_devices);
    ListView newDevicesList = (ListView) findViewById(R.id.new_devices);
    String macAddress = null;

    // To get the MAC address first determine which list was selected from
    if (parent == pairedDevicesList) {
      macAddress = _previouslyPlayedDevicesMapping.get((int) rowID);
    } else if (parent == newDevicesList) {
      macAddress = _newDevicesMapping.get((int) rowID);
    } else {
      Log.e(TAG, "Unable to determine which list click was from.");
      return;
    }

    if (_myBluetoothAdapter.isDiscovering()) {
      _AttemptingConnection = true;
      _myBluetoothAdapter.cancelDiscovery();
    }

    if (_ServiceBound) {
      _progressDialog = new ProgressDialog(this);
      _progressDialog.setTitle("Bluetooth Connection Pending");
      _progressDialog.setMessage("Connecting to device...");
      _progressDialog.setIndeterminate(true);
      _progressDialog.setCancelable(true);
      _progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

        public void onCancel(DialogInterface dialog) {
          cancelConnection();
        }
      });
      _progressDialog.show();
      _gameConnectingMessageShowing = true;

      if (BluetoothAdapter.checkBluetoothAddress(macAddress)) {
        _myBluetoothService.connect(_myBluetoothAdapter.getRemoteDevice(macAddress));
      }
    }
  }

  /*
   * Result from the activity started.
   *
   * (non-Javadoc)
   * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
   */
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == ENABLE_DISCOVERY_QUERY) {
      if (resultCode == DISCOVERY_DURATION) {
        Toast.makeText(getApplicationContext(), R.string.discovery_enable_toast_msg,
            Toast.LENGTH_SHORT).show();

        // Request discover from BluetoothAdapter
        _myBluetoothAdapter.startDiscovery();
      }

      if (resultCode == RESULT_CANCELED) {
        Toast.makeText(getApplicationContext(), R.string.discovery_not_enabled_toast_msg,
            Toast.LENGTH_LONG).show();
      }
    } else if (requestCode == BLUETOOTH_GAME_EXIT) {
      finish();
    }
  }

  /*
   * (non-Javadoc)
   * @see slapshotapp.game.support.FragmentAlertDialog#alertDialogButtonClick(int)
   */
  public void alertDialogButtonClick(int buttonNum) {
    if (buttonNum == AlertDialog.BUTTON_POSITIVE) {
      finish();
    }
  }
}
