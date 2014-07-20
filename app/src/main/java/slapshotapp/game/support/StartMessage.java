package slapshotapp.game.support;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StartMessage extends BluetoothMessages
{
	protected byte _Abort;
	
	public static final int SIZE_OF_START_MSG_CONTENTS = 1; 
	public static final byte ABORT_SET = 1;

	/*
	 * Constructor for creation of the object not from a parcel.
	 */
	public StartMessage()
	{
		_MessageID = BluetoothMessages.START_GAME_MESSAGE_ID;
		_Abort = 0;
	}
	
	public StartMessage(byte[] msgContents)
	{
		ByteBuffer myBuffer = ByteBuffer.wrap(msgContents);
		myBuffer.order(ByteOrder.BIG_ENDIAN);
		populateObjectFromBytes(myBuffer);
	}
	
	/*
	 * Method to cause the abort value to be set for this message
	 * 
	 */
	public void Abort()
	{
		_Abort = ABORT_SET;
	}
	
	/*
	 * Method to retrieve the abort value from the message.
	 * 
	 * @return The value of abort for this message.
	 */
	public byte isAbortSet()
	{
		return _Abort;
	}
	

	/*
	 * (non-Javadoc)
	 * @see slapshotapp.game.support.BluetoothMessages#populateObjectFromBytes(java.nio.ByteBuffer)
	 * Populates the contents of the version message from a byte buffer.
	 */
	public void populateObjectFromBytes(ByteBuffer msgBuffer)
	{
		super.populateObjectFromBytes(msgBuffer);
		_Abort = msgBuffer.get();
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
		byte[] message = new byte[baseMsgBuffer.capacity() + SIZE_OF_START_MSG_CONTENTS];
		
		ByteBuffer buffer = ByteBuffer.wrap(message);
		
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		buffer.put(baseMsgBuffer);
		buffer.put(_Abort);
		
		buffer.rewind();
		
		return buffer;
	}
	
}