package slapshotapp.game.support;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PlayerNameMessage extends BluetoothMessages {
    protected String _PlayerName;
    private final int BYTES_PER_CHAR = 2;

    /*
     * Constructor for creation of the object.
     */
    public PlayerNameMessage() {
        _MessageID = BluetoothMessages.PLAYER_NAME_MESSAGE_ID;
    }

    /*
     * Constructor for the creation of the player name
     * message from a byte buffer.
     *
     * @param msgContents byte buffer from which to populate the player
     * name field
     */
    public PlayerNameMessage(byte[] msgContents) {
        ByteBuffer myBuffer = ByteBuffer.wrap(msgContents);
        myBuffer.order(ByteOrder.BIG_ENDIAN);
        populateObjectFromBytes(myBuffer);
    }

    /*
     * Method to set the player name value of the message
     *
     * @param name the name to set for the player
     */
    public void SetPlayerName(String name) {
        _PlayerName = name;
    }

    /*
     * Method to retrieve the player name from the message.
     *
     * @return String representing the players name
     */
    public String GetPlayerName() {
        return _PlayerName;
    }

    /*
     * (non-Javadoc)
     * @see slapshotapp.game.support.BluetoothMessages#populateObjectFromBytes(java.nio.ByteBuffer)
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

            _PlayerName = String.copyValueOf(nameBuffer);
        }
    }

    /*
     * (non-Javadoc)
     * @see slapshotapp.game.support.BluetoothMessages#convertObjectToBytes()
     * Converts the contents of this object into a byte buffer.
     */
    public ByteBuffer convertObjectToBytes() {
        ByteBuffer baseMsgBuffer = super.convertObjectToBytes();

        //make a new buffer that can hold the contents of the base message and this message
        byte[] message =
            new byte[baseMsgBuffer.capacity() + 4 + (_PlayerName.length() * BYTES_PER_CHAR)];

        ByteBuffer msgBuffer = ByteBuffer.wrap(message);

        msgBuffer.order(ByteOrder.BIG_ENDIAN);

        msgBuffer.put(baseMsgBuffer);
        msgBuffer.putInt(_PlayerName.length());
        for (int i = 0; i < _PlayerName.length(); i++) {
            msgBuffer.putChar(_PlayerName.charAt(i));
        }

        msgBuffer.rewind();

        return msgBuffer;
    }
}