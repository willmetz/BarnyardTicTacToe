package slapshotapp.game.support;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class QuitMessage extends BluetoothMessages {

  /*
   * Constructor for creation of the object not from a parcel.
   */
  public QuitMessage() {
    _MessageID = BluetoothMessages.QUIT_MESSAGE_ID;
  }

  public QuitMessage(byte[] msgContents) {
    ByteBuffer myBuffer = ByteBuffer.wrap(msgContents);
    myBuffer.order(ByteOrder.BIG_ENDIAN);
    populateObjectFromBytes(myBuffer);
  }

  /*
   * (non-Javadoc)
   * @see slapshotapp.game.support.BluetoothMessages#populateObjectFromBytes(java.nio.ByteBuffer)
   * Populates the contents of the version message from a byte buffer.
   */
  public void populateObjectFromBytes(ByteBuffer msgBuffer) {
    super.populateObjectFromBytes(msgBuffer);
  }

  /*
   * (non-Javadoc)
   * @see slapshotapp.game.support.BluetoothMessages#convertObjectToBytes()
   *
   * Converts the objects contents to a byte buffer
   *
   * @return ByteBuffer containing objects contents
   */
  public ByteBuffer convertObjectToBytes() {
    ByteBuffer baseMsgBuffer = super.convertObjectToBytes();

    //make a new buffer that can hold the contents of the base message and this message
    byte[] message = new byte[baseMsgBuffer.capacity()];

    ByteBuffer buffer = ByteBuffer.wrap(message);

    buffer.order(ByteOrder.BIG_ENDIAN);

    buffer.put(baseMsgBuffer);

    buffer.rewind();

    return buffer;
  }
}