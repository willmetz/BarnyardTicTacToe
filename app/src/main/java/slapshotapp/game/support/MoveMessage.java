package slapshotapp.game.support;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MoveMessage extends BluetoothMessages {
  protected int _Column, _Row;

  public static final int SIZE_OF_MOVE_MSG_CONTENTS = 8;

  /*
   * Constructor for creation of the object not from a parcel.
   */
  public MoveMessage() {
    _MessageID = BluetoothMessages.MAKE_MOVE_MESSAGE_ID;
    _Column = _Row = 0;
  }

  public MoveMessage(byte[] msgContents) {
    ByteBuffer myBuffer = ByteBuffer.wrap(msgContents);
    myBuffer.order(ByteOrder.BIG_ENDIAN);
    populateObjectFromBytes(myBuffer);
  }

  /*
   * Method to set the column and row move values of the message
   *
   * @param row the row number of the move
   * @param column the column number of the move
   */
  public void SetMove(int row, int column) {
    _Row = row;
    _Column = column;
  }

  /*
   * Method to retrieve the move row value from the message.
   *
   * @return The row number.
   */
  public int GetMoveRow() {
    return _Row;
  }

  /*
   * Method to retrieve the move column value from the message.
   *
   * @return The column number.
   */
  public int GetMoveColumn() {
    return _Column;
  }

  /*
   * (non-Javadoc)
   * @see slapshotapp.game.support.BluetoothMessages#populateObjectFromBytes(java.nio.ByteBuffer)
   * Populates the contents of the message from a byte buffer.
   *
   * @param msgBuffer A ByteBuffer from which to populate the object's data from
   */
  public void populateObjectFromBytes(ByteBuffer msgBuffer) {
    super.populateObjectFromBytes(msgBuffer);
    _Row = msgBuffer.getInt();
    _Column = msgBuffer.getInt();
  }

  /*
   * (non-Javadoc)
   * @see slapshotapp.game.support.BluetoothMessages#convertObjectToBytes()
   * Converts the data of this object to a byte buffer
   *
   * @return a ByteBuffer containing the data of this object
   */
  public ByteBuffer convertObjectToBytes() {
    ByteBuffer baseMsgBuffer = super.convertObjectToBytes();

    //make a new buffer that can hold the contents of the base message and this message
    byte[] message = new byte[baseMsgBuffer.capacity() + SIZE_OF_MOVE_MSG_CONTENTS];

    ByteBuffer msgBuffer = ByteBuffer.wrap(message);

    msgBuffer.order(ByteOrder.BIG_ENDIAN);

    msgBuffer.put(baseMsgBuffer);
    msgBuffer.putInt(_Row);
    msgBuffer.putInt(_Column);

    msgBuffer.rewind();

    return msgBuffer;
  }
}