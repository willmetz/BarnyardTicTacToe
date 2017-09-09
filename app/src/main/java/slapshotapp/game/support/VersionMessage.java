package slapshotapp.game.support;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VersionMessage extends BluetoothMessages {
    protected short _Version;

    public static final int SIZE_OF_VERSION_MSG_CONTENTS = 2;

    /*
     * Constructor for creation of the object not from a parcel.
     */
    public VersionMessage() {
        _MessageID = BluetoothMessages.VERSION_MESSAGE_ID;
    }

    public VersionMessage(byte[] msgContents) {
        ByteBuffer myBuffer = ByteBuffer.wrap(msgContents);
        myBuffer.order(ByteOrder.BIG_ENDIAN);
        populateObjectFromBytes(myBuffer);
    }

    /*
     * Method to set the version value of the message
     *
     * @param version The version number to set
     */
    public void SetVersion(short version) {
        _Version = version;
    }

    /*
     * Method to retrieve the version from the message.
     *
     * @return integer representing the version.
     */
    public short GetVersion() {
        return _Version;
    }

    /*
     * (non-Javadoc)
     * @see slapshotapp.game.support.BluetoothMessages#populateObjectFromBytes(java.nio.ByteBuffer)
     * Populates the contents of the version message from a byte buffer.
     */
    public void populateObjectFromBytes(ByteBuffer msgBuffer) {
        super.populateObjectFromBytes(msgBuffer);
        _Version = msgBuffer.getShort();
    }

    public ByteBuffer convertObjectToBytes() {
        ByteBuffer baseMsgBuffer = super.convertObjectToBytes();

        //make a new buffer that can hold the contents of the base message and this message
        byte[] versionMessage = new byte[baseMsgBuffer.capacity() + SIZE_OF_VERSION_MSG_CONTENTS];

        ByteBuffer verMsgBuffer = ByteBuffer.wrap(versionMessage);

        verMsgBuffer.order(ByteOrder.BIG_ENDIAN);

        verMsgBuffer.put(baseMsgBuffer);
        verMsgBuffer.putShort(_Version);

        verMsgBuffer.rewind();

        return verMsgBuffer;
    }
}