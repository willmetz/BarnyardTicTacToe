package slapshotapp.game.support;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TicTacToeDBHelper extends SQLiteOpenHelper {
  private final String DEFAULT_NAME = "Unknown Player";
  private final String DEFAULT_DEV_NAME = "Unknown Device";
  private final String TAG = "TicTacToeDBHelper";

  public TicTacToeDBHelper(Context context) {
    super(context, TicTacToeDBContract.DATA_BASE_NAME, null, TicTacToeDBContract.DATA_BASE_VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL(TicTacToeDBContract.BluetoothConnectionsEntry.SQL_CREATE_ENTRIES);
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //Current upgrade policy is to drop the table and create a new table
    db.execSQL(TicTacToeDBContract.BluetoothConnectionsEntry.SQL_DELETE_ENTRIES);
    onCreate(db);
  }

  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    onUpgrade(db, oldVersion, newVersion);
  }

  /*
   * Determines if the database contains a given mac address
   *
   * @return true if the database does contain the mac address, false otherwise
   */
  public boolean ContainsMacAddress(String mac) {
    SQLiteDatabase db;
    boolean result;

    try {
      db = this.getReadableDatabase();
    } catch (SQLiteException e) {
      Log.e(TAG, "Unable to open database");
      return false;
    }

    String projection[] = { TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_MAC_ADDRESS };
    String filter = TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_MAC_ADDRESS + " = ?";
    String filterArgs[] = { mac };

    //determine if the connection is already in the database
    Cursor c =
        db.query(TicTacToeDBContract.BluetoothConnectionsEntry.TABLE_NAME,  // The table to query
            projection,               // The columns to return
            filter,                                // The filter for which rows to return
            filterArgs,                               // The values for the row filter
            null,                                // don't group the rows
            null,                                // don't filter by row groups
            null                                 // The sort order
        );

    if (c.getCount() == 1) {
      result = true;
    } else {
      result = false;
    }

    c.close();
    db.close();

    return result;
  }

  /*
   * Adds a connection to the database.
   *
   * @param devName The name of the device for the connection
   * @param mac The mac address for the connection
   *
   * @return true on success, false otherwise
   */
  public boolean AddConnection(String devName, String mac) {

    SQLiteDatabase db;
    boolean result = true;

    //first check and see if the database contains the MAC address as we do not want duplicates
    if (ContainsMacAddress(mac)) {
      return false;
    }

    try {
      db = this.getWritableDatabase();
    } catch (SQLiteException e) {
      Log.e(TAG, "Unable to open database");
      return false;
    }

    ContentValues values = new ContentValues();
    values.put(TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_DEV_NAME, devName);
    values.put(TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_MAC_ADDRESS, mac);
    values.put(TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_PLAYER_NAME, DEFAULT_NAME);

    //try to insert the row into the database, check for an error
    if (db.insert(TicTacToeDBContract.BluetoothConnectionsEntry.TABLE_NAME, null, values) < 0) {
      Log.e(TAG, "Unable to add device to database");
      result = false;
    }

    db.close();

    return result;
  }

  /*
   * Used to determine if the name at the given mac address is a default name
   *
   * @param mac The mac address to check and see if the name is a default name
   *
   * @return true if the name is the default, false otherwise
   */
  public boolean IsNameDefault(String mac) {
    SQLiteDatabase db;
    boolean result = false;

    try {
      db = this.getReadableDatabase();
    } catch (SQLiteException e) {
      Log.e(TAG, "Unable to open database");
      return false;
    }

    String projection[] = { TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_PLAYER_NAME };
    String filter = TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_MAC_ADDRESS + " = ?";
    String filterArgs[] = { mac };

    //determine if the connection is already in the database
    Cursor c =
        db.query(TicTacToeDBContract.BluetoothConnectionsEntry.TABLE_NAME,  // The table to query
            projection,               // The columns to return
            filter,                                // The filter for which rows to return
            filterArgs,                               // The values for the row filter
            null,                                // don't group the rows
            null,                                // don't filter by row groups
            null                                 // The sort order
        );

    if (c.getCount() == 1) {

      c.moveToFirst();

      try {
        String playerName = c.getString(c.getColumnIndexOrThrow(
            TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_PLAYER_NAME));

        if (playerName.contains(DEFAULT_NAME)) {
          result = true;
        }
      } catch (IllegalArgumentException e) {
        //don't need to do anything here as false will be returned, just show an error
        Log.e(TAG, "Unable to retrieve player name");
      }
    }

    c.close();
    db.close();

    return result;
  }

  /*
   * Used to get the player name associated with the given MAC address.
   *
   * @param mac The mac address to get the name for.
   *
   * @return The name of the player corresponding to the mac address.
   */
  public String GetPlayerName(String mac) {
    String name = DEFAULT_NAME;
    SQLiteDatabase db;

    try {
      db = this.getReadableDatabase();
    } catch (SQLiteException e) {
      Log.e(TAG, "Unable to open database");
      return name;
    }

    String projection[] = { TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_PLAYER_NAME };
    String filter = TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_MAC_ADDRESS + " = ?";
    String filterArgs[] = { mac };

    //determine if the connection is already in the database
    Cursor c =
        db.query(TicTacToeDBContract.BluetoothConnectionsEntry.TABLE_NAME,  // The table to query
            projection,               // The columns to return
            filter,                                // The filter for which rows to return
            filterArgs,                               // The values for the row filter
            null,                                // don't group the rows
            null,                                // don't filter by row groups
            null                                 // The sort order
        );

    if (c.getCount() == 1) {

      c.moveToFirst();

      try {
        name = c.getString(c.getColumnIndexOrThrow(
            TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_PLAYER_NAME));
      } catch (IllegalArgumentException e) {
        //log an error, the return value is already set to handle this case
        Log.e(TAG, "Unable to retrieve player name");
      }
    }

    c.close();
    db.close();

    return name;
  }

  /*
   * Used to get the device name associated with the given MAC address.
   *
   * @param mac The mac address to get the name for.
   *
   * @return The name of the device corresponding to the mac address.
   */
  public String GetDeviceName(String mac) {
    String name = DEFAULT_DEV_NAME;
    SQLiteDatabase db;

    try {
      db = this.getReadableDatabase();
    } catch (SQLiteException e) {
      Log.e(TAG, "Unable to open database");
      return name;
    }

    String projection[] = { TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_DEV_NAME };
    String filter = TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_MAC_ADDRESS + " = ?";
    String filterArgs[] = { mac };

    //determine if the connection is already in the database
    Cursor c =
        db.query(TicTacToeDBContract.BluetoothConnectionsEntry.TABLE_NAME,  // The table to query
            projection,               // The columns to return
            filter,                                // The filter for which rows to return
            filterArgs,                               // The values for the row filter
            null,                                // don't group the rows
            null,                                // don't filter by row groups
            null                                 // The sort order
        );

    if (c.getCount() == 1) {

      c.moveToFirst();

      try {
        name = c.getString(c.getColumnIndexOrThrow(
            TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_DEV_NAME));
      } catch (IllegalArgumentException e) {
        //log an error, the return value is already set to handle this case
        Log.e(TAG, "Unable to retrieve player name");
      }
    }

    c.close();
    db.close();

    return name;
  }

  /*
   * Modifies a players name in the database associated with the given MAC.
   *
   * @param name The name of the player
   * @param mac The mac address
   *
   * @return true on success, false otherwise
   */
  public boolean SetPlayerName(String name, String mac) {
    SQLiteDatabase db;
    boolean result = false;

    try {
      db = this.getWritableDatabase();
    } catch (SQLiteException e) {
      Log.e(TAG, "Unable to open database");
      return false;
    }

    ContentValues values = new ContentValues();
    values.put(TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_PLAYER_NAME, name);

    //set a clause to determine what row to update
    String rowUpdateClause =
        TicTacToeDBContract.BluetoothConnectionsEntry.COLUMN_NAME_MAC_ADDRESS + " = ?";

    //set the arguments for the row clause
    String arguments[] = { mac };

    //try to insert the row into the database, check for an error
    int rowsUpdated =
        db.update(TicTacToeDBContract.BluetoothConnectionsEntry.TABLE_NAME, values, rowUpdateClause,
            arguments);

    //only 1 row should be updated if the command succeeded
    if (rowsUpdated == 1) {
      result = true;
    }

    db.close();

    return result;
  }
}
