package slapshotapp.game.support.bluetooth_protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import slapshotapp.game.support.bluetooth_protocol.BluetoothMessages;

public class PlayerImageMessage extends BluetoothMessages {
    protected String _ImageName;
    private final int BYTES_PER_CHAR = 2;

    /*
     * Constructor for creation of the object.
     */
    public PlayerImageMessage() {
        _MessageID = BluetoothMessages.PLAYER_IMAGE_MESSAGE_ID;
    }

    /*
     * Constructor for the creation of the player name
     * message from a byte buffer.
     *
     * @param msgContents byte buffer from which to populate the player
     * name field
     */
    public PlayerImageMessage(byte[] msgContents) {
        ByteBuffer myBuffer = ByteBuffer.wrap(msgContents);
        myBuffer.order(ByteOrder.BIG_ENDIAN);
        populateObjectFromBytes(myBuffer);
    }

    /*
     * Method to set the image name value of the message
     *
     * @param name the name to set for the image
     */
    public void SetImageName(String name) {
        _ImageName = name;
    }

    /*
     * Method to retrieve the image name from the message.
     *
     * @return String representing the image name
     */
    public String GetImageName() {
        return _ImageName;
    }

    /*
     * (non-Javadoc)
     * @see slapshotapp.game.support.bluetooth_protocol.BluetoothMessages#populateObjectFromBytes(java.nio.ByteBuffer)
     * Populates the contents of the version message from a byte buffer.
     */
    public void populateObjectFromBytes(ByteBuffer msgBuffer) {
        super.populateObjectFromBytes(msgBuffer);

        //determine the size of the player name
        int numChars = msgBuffer.getInt();

        if (numChars > 0) {
            char[] nameBuffer = new char[numChars];

            for (int i = 0; i < numChars; i++) {
                nameBuffer[i] = msgBuffer.getChar();
            }

            _ImageName = String.copyValueOf(nameBuffer);
        }
    }

    /*
     * (non-Javadoc)
     * @see slapshotapp.game.support.bluetooth_protocol.BluetoothMessages#convertObjectToBytes()
     * Converts the contents of this object into a byte buffer.
     */
    public ByteBuffer convertObjectToBytes() {
        ByteBuffer baseMsgBuffer = super.convertObjectToBytes();

        //make a new buffer that can hold the contents of the base message and this message
        byte[] message =
            new byte[baseMsgBuffer.capacity() + 4 + (_ImageName.length() * BYTES_PER_CHAR)];

        ByteBuffer msgBuffer = ByteBuffer.wrap(message);

        msgBuffer.order(ByteOrder.BIG_ENDIAN);

        msgBuffer.put(baseMsgBuffer);
        msgBuffer.putInt(_ImageName.length());
        for (int i = 0; i < _ImageName.length(); i++) {
            msgBuffer.putChar(_ImageName.charAt(i));
        }

        msgBuffer.rewind();

        return msgBuffer;
    }
}