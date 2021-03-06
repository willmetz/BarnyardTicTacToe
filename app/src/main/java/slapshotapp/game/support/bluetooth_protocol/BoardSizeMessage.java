package slapshotapp.game.support.bluetooth_protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import slapshotapp.game.support.bluetooth_protocol.BluetoothMessages;

public class BoardSizeMessage extends BluetoothMessages {
    protected short _BoardSize;

    public static final int SIZE_OF_BOARD_SIZE_MSG_CONTENTS = 2;

    /*
     * Constructor for creation of the object not from a parcel.
     */
    public BoardSizeMessage() {
        _MessageID = BluetoothMessages.BOARD_SIZE_MESSAGE_ID;
    }

    public BoardSizeMessage(byte[] msgContents) {
        ByteBuffer myBuffer = ByteBuffer.wrap(msgContents);
        myBuffer.order(ByteOrder.BIG_ENDIAN);
        populateObjectFromBytes(myBuffer);
    }

    /*
     * Method to set the board size value of the message
     *
     * @param size The board size number to set
     */
    public void SetBoardSize(short size) {
        _BoardSize = size;
    }

    /*
     * Method to retrieve the board size from the message.
     *
     * @return short representing the board size.
     */
    public short GetBoardSize() {
        return _BoardSize;
    }

    /*
     * (non-Javadoc)
     * @see slapshotapp.game.support.bluetooth_protocol.BluetoothMessages#populateObjectFromBytes(java.nio.ByteBuffer)
     * Populates the contents of the version message from a byte buffer.
     */
    public void populateObjectFromBytes(ByteBuffer msgBuffer) {
        super.populateObjectFromBytes(msgBuffer);
        _BoardSize = msgBuffer.getShort();
    }

    public ByteBuffer convertObjectToBytes() {
        ByteBuffer baseMsgBuffer = super.convertObjectToBytes();

        //make a new buffer that can hold the contents of the base message and this message
        byte[] message = new byte[baseMsgBuffer.capacity() + SIZE_OF_BOARD_SIZE_MSG_CONTENTS];

        ByteBuffer msgBuffer = ByteBuffer.wrap(message);

        msgBuffer.order(ByteOrder.BIG_ENDIAN);

        msgBuffer.put(baseMsgBuffer);
        msgBuffer.putShort(_BoardSize);

        msgBuffer.rewind();

        return msgBuffer;
    }
}