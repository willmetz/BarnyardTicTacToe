package slapshotapp.game.support;

import android.provider.BaseColumns;

public final class TicTacToeDBContract 
{
	/*
	 * Empty constructor to prevent class from being instantiated
	 */
	private TicTacToeDBContract(){}
	
	public static final String DATA_BASE_NAME = "barnyard_tictactoe.db";
	public static final int DATA_BASE_VERSION = 1;
	
	
	public static abstract class BluetoothConnectionsEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "bluetooth_connections";
		public static final String COLUMN_NAME_MAC_ADDRESS = "mac_address";
		public static final String COLUMN_NAME_PLAYER_NAME = "player_name";
		public static final String COLUMN_NAME_DEV_NAME = "device_name";
		
		public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (" +
			_ID + " INTEGER PRIMARY KEY," + 
			COLUMN_NAME_MAC_ADDRESS + " TEXT," +
			COLUMN_NAME_PLAYER_NAME + " TEXT," +
			COLUMN_NAME_DEV_NAME + " TEXT" + " )";
		
		public static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + TABLE_NAME;
		
		public static final String[] SQL_ALL_DATA_COLUMNS =  {_ID,
														COLUMN_NAME_MAC_ADDRESS, 
														COLUMN_NAME_PLAYER_NAME,
														COLUMN_NAME_DEV_NAME};
		
	}
}
