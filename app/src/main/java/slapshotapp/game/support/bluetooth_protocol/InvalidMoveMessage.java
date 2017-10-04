package slapshotapp.game.support.bluetooth_protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import slapshotapp.game.support.bluetooth_protocol.BluetoothMessages;

public class InvalidMoveMessage extends BluetoothMessages {
    /*
     * Constructor for creation of the object not from a parcel.
     */
    public InvalidMoveMessage() {
        _MessageID = BluetoothMessages.INVALID_MOVE_MESSAGE_ID;
    }

    public InvalidMoveMessage(byte[] msgContents) {
        ByteBuffer myBuffer = ByteBuffer.wrap(msgContents);
        myBuffer.order(ByteOrder.BIG_ENDIAN);
        populateObjectFromBytes(myBuffer);
    }

    /*
     * (non-Javadoc)
     * @see slapshotapp.game.support.bluetooth_protocol.BluetoothMessages#populateObjectFromBytes(java.nio.ByteBuffer)
     * Populates the contents of the version message from a byte buffer.
     */
    public void populateObjectFromBytes(ByteBuffer msgBuffer) {
        super.populateObjectFromBytes(msgBuffer);
    }

    /*
     * (non-Javadoc)
     * @see slapshotapp.game.support.bluetooth_protocol.BluetoothMessages#convertObjectToBytes()
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