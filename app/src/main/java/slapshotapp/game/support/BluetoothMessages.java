package slapshotapp.game.support;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BluetoothMessages {
  public static final int MESSAGE_ID_START_BYTE = 0;
  public static final int MESSAGE_ID_SIZE = 2;
  public static final int MESSAGE_CONTENT_START_BYTE = 2;

  public static final short VERSION_MESSAGE_ID = 1;
  public static final short BOARD_SIZE_MESSAGE_ID = 2;
  public static final short PLAYER_NAME_MESSAGE_ID = 3;
  public static final short PLAYER_IMAGE_MESSAGE_ID = 4;
  public static final short START_GAME_MESSAGE_ID = 5;
  public static final short MAKE_MOVE_MESSAGE_ID = 6;
  public static final short NEW_GAME_MESSAGE_ID = 7;
  public static final short QUIT_MESSAGE_ID = 8;
  public static final short INVALID_MOVE_MESSAGE_ID = 9;

  protected short _MessageID;

  /*
   * Constructor used to create an instance of object.
   */
  public BluetoothMessages() {

  }

  public BluetoothMessages(byte[] msgContents) {
    ByteBuffer myBuffer = ByteBuffer.wrap(msgContents);
    populateObjectFromBytes(myBuffer);
  }

  /*
   * Function to get the message id value
   *
   * @return the message ID
   */
  public short getMessageID() {
    return _MessageID;
  }

  /*
   * This method will populate the base bluetooth message data
   * from a byte buffer.
   *
   * @param msgBuffer ByteBuffer from which to assemble message data
   */
  public void populateObjectFromBytes(ByteBuffer msgBuffer) {
    msgBuffer.rewind();
    _MessageID = msgBuffer.getShort();
  }

  /*
   * Converts the message object data to bytes.
   *
   * @return a bytebuffer of the message object data
   */
  public ByteBuffer convertObjectToBytes() {
    ByteBuffer myBuffer = ByteBuffer.wrap(new byte[2]);

    myBuffer.order(ByteOrder.BIG_ENDIAN);

    myBuffer.putShort(_MessageID);

    myBuffer.rewind();

    return myBuffer;
  }
}

