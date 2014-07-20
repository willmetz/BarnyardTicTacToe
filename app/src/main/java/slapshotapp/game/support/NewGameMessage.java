package slapshotapp.game.support;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NewGameMessage extends BluetoothMessages
{
	protected byte _ResponseField;
	
	public static final int SIZE_OF_NEW_GAME_MSG_CONTENTS = 1; 
	public static final byte NO_RESPONSE_REQUIRED = 0;
	public static final byte AGREE_TO_NEW_GAME = 1;
	public static final byte DENY_NEW_GAME = 2;
	public static final byte REQUEST_RESPONSE = 3;

	/*
	 * Constructor for creation of the object not from a parcel.
	 */
	public NewGameMessage()
	{
		_MessageID = BluetoothMessages.NEW_GAME_MESSAGE_ID;
		_ResponseField = NO_RESPONSE_REQUIRED;
	}
	
	public NewGameMessage(byte[] msgContents)
	{
		ByteBuffer myBuffer = ByteBuffer.wrap(msgContents);
		myBuffer.order(ByteOrder.BIG_ENDIAN);
		populateObjectFromBytes(myBuffer);
	}
	
	/*
	 * Method to set the response field value
	 */
	public void SetResponseField(byte val)
	{
		_ResponseField = val;
	}
	
	/*
	 * Method to retrieve the response value of the message.
	 * 
	 * @return The value of the response field for the message.
	 */
	public byte getResponseField()
	{
		return _ResponseField;
	}
	

	/*
	 * (non-Javadoc)
	 * @see slapshotapp.game.support.BluetoothMessages#populateObjectFromBytes(java.nio.ByteBuffer)
	 * Populates the contents of the version message from a byte buffer.
	 */
	public void populateObjectFromBytes(ByteBuffer msgBuffer)
	{
		super.populateObjectFromBytes(msgBuffer);
		_ResponseField = msgBuffer.get();
	}
	
	/*
	 * (non-Javadoc)
	 * @see slapshotapp.game.support.BluetoothMessages#convertObjectToBytes()
	 * 
	 * Converts the objects contents to a byte buffer
	 * 
	 * @return ByteBuffer containing objects contents
	 */
	public ByteBuffer convertObjectToBytes()
	{
		ByteBuffer baseMsgBuffer = super.convertObjectToBytes();
				
		//make a new buffer that can hold the contents of the base message and this message
		byte[] message = new byte[baseMsgBuffer.capacity() + SIZE_OF_NEW_GAME_MSG_CONTENTS];
		
		ByteBuffer buffer = ByteBuffer.wrap(message);
		
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		buffer.put(baseMsgBuffer);
		buffer.put(_ResponseField);
		
		buffer.rewind();
		
		return buffer;
	}
	
}