package slapshotapp.game.tictactoe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


/* 
 * This class has several threads.  One to 
 * Initiate a connection, one to listen for
 * connections, and one to manage the connection.
 */
public class BluetoothService extends Service
{
	public static final short PROTOCOL_VERSION = 1;
	
	//messages sent from the service to clients
	public static final int MESSAGE_READ_PACKET_COMPLETE = 0;
	public static final int MESSAGE_READ_FAILED = 1;
	public static final int MESSAGE_WRITE_FAILED = 2;
	public static final int MESSAGE_CONNECTION_STATE = 3;
	public static final int MESSAGE_LISTEN_THREAD_FAIL = 4;
	public static final int MESSAGE_CONNECT_THREAD_FAIL = 5;
	public static final int MESSAGE_MANAGER_THREAD_FAIL = 6;
	public static final int MESSAGE_CONNECTION_LOST = 7;
	
	// Constants that indicate the current connection state
    public static final int CONNECTION_STATE_NONE = 0;       // we're doing nothing
    public static final int CONNECTION_STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int CONNECTION_STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int CONNECTION_STATE_CONNECTED = 3;  // now connected to a remote device
    
    private final int PACKET_HEADER_SIZE = 4;
    
    private final UUID MY_UUID = UUID.fromString("7f82db70-994d-11e2-9e96-0800200c9a66");
	private final String NAME_OF_SERVICE = "BluetoothService";
	private final String TAG = "Bluetooth Service";
	
	private int _myState;
    private BluetoothAdapter _myBTAdapter;
    
    private AcceptThread _myAcceptThread;
    private ConnectThread _myConnectThread;
    private ConnectionManager _myConnectionManagerThread;
    
    private Handler _myHandler;
    private boolean _IsServer;
    private String _deviceAddress, _deviceName;
    
    private final IBinder _myBinder = new LocalBinder();

    /*
     * Local Binder class.
     * 
     * Provides clients access to all public methods for the Bluetooth Service.
     */
    public class LocalBinder extends Binder{
    	BluetoothService getService(){
    		return BluetoothService.this;
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    public void onCreate()
    {
    	_myState = CONNECTION_STATE_NONE;
    	_myBTAdapter = BluetoothAdapter.getDefaultAdapter();
    	_IsServer = false;
    	_deviceAddress = _deviceName = "None";
    	
    	IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(_myBroadcastReceiver, filter);
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy()
    {
    	//unregister the broadcast receiver
    	this.unregisterReceiver(_myBroadcastReceiver);
    	stop();
    }
    
    /*
     * Returns an instance of the binder so that an
     * application can interact with the service.
     */
    public IBinder onBind(Intent intent) 
    {    	
        return _myBinder;
    }

    /*
     *     (non-Javadoc)
     * @see android.app.Service#onUnbind(android.content.Intent)
     * 
     * For now returns false indicating that 
     * rebinding is not allowed
     */
    public boolean onUnbind(Intent intent)
    {
    	return false;
    }
    
    private final BroadcastReceiver _myBroadcastReceiver = new BroadcastReceiver()
    {

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			
			if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				//determine if the connection lost was the connection for this service
				if(device.getAddress().equals(_deviceAddress)){
					if(_myHandler != null){
			    		_myHandler.obtainMessage(MESSAGE_CONNECTION_LOST).sendToTarget();
			    		//halt this connection
			    		stop();
			    	}
				}
				
				
			}
		}
    		
    };
    
    
    /*
     * Sets the handler for the service to send messages through.
     * 
     * @param inHandler The handler for the service to use.
     */
    public synchronized void setHandler(Handler inHandler)
    {
    	if(inHandler != null){    		
    		_myHandler = inHandler;
    	}
    }
    
    /*
     * Removes the handler for the service to send messages through.
     * 
     */
    public synchronized void removeHandler()
    {
    	_myHandler.removeCallbacksAndMessages(null);
    	_myHandler = null;
    }
    
    /*
     * A thread safe method to get the current state of the service.
     * 
     * @return The state of this service
     */
    public synchronized int getState()
    {
    	return _myState;
    }
    
    /*
     * Used to determine if this service is a server or client
     * 
     * @return true if it is a server, false otherwise
     */
    public boolean IsServer()
    {
    	return _IsServer;
    }
    
    /*
     * Retrieves the connected devices bluetooth name
     * 
     * @return Name of connected bluetooth device
     */
    public String getDeviceName()
    {
    	return _deviceName;
    }
    
    /*
     * Retrieves the connected devices bluetooth MAC Address
     * 
     * @return MAC Address of connected bluetooth device
     */
    public String getDeviceMac()
    {
    	return _deviceAddress;
    }
    
    
    /*
     * A method to get the state of the service in string form.
     */
    public synchronized String getStateString()
    {
    	String result = "";
    	
    	switch(_myState)
    	{
    		case CONNECTION_STATE_NONE:
	    		result = "No Connection State";
	    		break;
    		case CONNECTION_STATE_LISTEN:
	    		result = "Listening";
	    		break;
    		case CONNECTION_STATE_CONNECTING:
	    		result = "Connecting";
	    		break;
    		case CONNECTION_STATE_CONNECTED:
	    		result = "Connected";
	    		break;
    	}
    	
    	return result;
    }
    
    /*
     * A thread safe method to start listening for a connection.
     */
    public synchronized void start()
    {
    	//make sure that the threads attempting to make a connection and 
    	//that would maintain a connection are canceled
    	if(_myConnectThread != null){
    		_myConnectThread.cancel();
    		_myConnectThread = null;
    	}
    	if(_myConnectionManagerThread != null){
    		_myConnectionManagerThread.cancel();
    		_myConnectionManagerThread = null;
    	}
    	
    	if(_myAcceptThread == null){
    		_myAcceptThread = new AcceptThread();
    		_myAcceptThread.start();
    	}    	
    	
    	setState(CONNECTION_STATE_LISTEN);
    }
    
    /*
     * A thread safe method to stop the connection service, and
     * all threads associated with it.
     */
    public synchronized void stop()
    {
    	if(_myConnectThread != null){
    		_myConnectThread.cancel();
    		_myConnectThread = null;
    	}
    	if(_myConnectionManagerThread != null){
    		_myConnectionManagerThread.cancel();
    		_myConnectionManagerThread = null;
    	}    	
    	if(_myAcceptThread != null){
    		_myAcceptThread.cancel();
    		_myAcceptThread = null;
    	}
    	
    	setState(CONNECTION_STATE_NONE);
    }
    
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) 
    {    	
        // Cancel any thread attempting to make a connection
        if (_myConnectThread != null && _myState == CONNECTION_STATE_CONNECTING) {
        	_myConnectThread.cancel(); 
        	_myConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (_myConnectionManagerThread != null) {
        	_myConnectionManagerThread.cancel(); 
        	_myConnectionManagerThread = null;
        }

        // Start the thread to connect with the given device
        _myConnectThread = new ConnectThread(device);
        _myConnectThread.start();
        setState(CONNECTION_STATE_CONNECTING);
    }
    
    /**
     * Write to the Connection Manager in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public boolean SendPacket(byte[] dataToSend) 
    {
        // Create temporary object
        ConnectionManager r;
        
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (_myState != CONNECTION_STATE_CONNECTED){
            	Log.e(TAG, "Can't send data while not connected!!");
            	return false;
            }else{
            	r = _myConnectionManagerThread;
            }            
        }
        
        //append the packet size to the front of the data to be sent
        int packetSize = dataToSend.length;
        
        byte[] outPacketBuffer = new byte[packetSize + PACKET_HEADER_SIZE];
        
        ByteBuffer myBuffer = ByteBuffer.wrap(outPacketBuffer);
        myBuffer.order(ByteOrder.BIG_ENDIAN);
        //put the header info in the packet to send
        myBuffer.putInt(packetSize);
        myBuffer.put(dataToSend);
        
        // Perform the write unsynchronized
        return r.WritePacket(myBuffer.array());
    }
    
    /*
     * Sets the state of the service.
     * 
     * @param inState State to set the service to.
     */
    private synchronized void setState(int inState)
    {
    	//send a message to the handler indicating a state change
    	if(_myHandler != null){
    		_myHandler.obtainMessage(MESSAGE_CONNECTION_STATE, inState, _myState, null).sendToTarget();
    	}    	
    	
    	_myState = inState;
    }
    
    /*
     * Called to initiate the connection management thread after 
     * a connection has been made.
     * 
     * @param inSocket the socket which has the connection established
     */
    private synchronized void manageConnection(BluetoothSocket inSocket)
    {
    	//stop listening for connections as a connection has been made
    	if(_myAcceptThread != null){
    		_myAcceptThread.cancel();
    		_myAcceptThread = null;
    	}
    	
    	//stop trying to make any connections as a connection has been made
    	if(_myConnectThread != null){
    		_myConnectThread.cancel();
    		_myConnectThread = null;
    	}
    	
    	//remove any previous connections if any exist
    	if(_myConnectionManagerThread != null){
    		_myConnectionManagerThread.cancel();
    		_myConnectionManagerThread = null;
    	}  
    	
    	//start the connection managing thread
    	_myConnectionManagerThread = new ConnectionManager(inSocket);
		_myConnectionManagerThread.start();
		
    	setState(CONNECTION_STATE_CONNECTED);
    }
    
    private class AcceptThread extends Thread
    {
    	private final BluetoothServerSocket _myServerSocket;
    	private final String TAG = "Accept Thread";
    	private boolean _exitThread;
    	
    	/*
    	 * Constructor.
    	 */
    	public AcceptThread()
    	{
    		BluetoothServerSocket tmp = null;

            // Create a new listening server socket, given that there is no security related data
    		// sent in this game use an insecure connection to avoid a pairing request.
            try {
                tmp = _myBTAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_OF_SERVICE, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            _myServerSocket = tmp;
    	}
    	
    	/*
    	 * (non-Javadoc)
    	 * @see java.lang.Thread#run()
    	 */
    	public void run()
    	{
    		if(_myServerSocket == null){
    			Log.e(TAG,"Unable to retrieve a Bluetooth socket, exiting accept thread");
    			_myAcceptThread = null;
    			
    			if(_myHandler != null){
					_myHandler.obtainMessage(MESSAGE_LISTEN_THREAD_FAIL, -1, -1, -1).sendToTarget();
				}
    			return;
    		}
    		//set the name of the thread, mainly for debug purposes
    		setName("AcceptThread");
    		    		
    		_exitThread = false;
    		
    		BluetoothSocket acceptSocket = null;
    		
    		//listen for a connection
    		while(_myState != CONNECTION_STATE_CONNECTED && !_exitThread){
	    		try {
	    			//note this will block until returned
	    			acceptSocket = _myServerSocket.accept();
	    		}
	    		catch(IOException e){
	    			Log.e(TAG, "Bluetooth socket connection failed");
	    		}
	    		
	    		if(acceptSocket != null){
	    			synchronized(BluetoothService.this){
	    				switch(_myState){
	    					
		    				case CONNECTION_STATE_LISTEN:
		    				case CONNECTION_STATE_CONNECTING:
		    					if(!_exitThread){
			    					setState(CONNECTION_STATE_CONNECTING);
			    					//everything looks good, proceed to connect
			    					manageConnection(acceptSocket);
			    					_IsServer = true;
		    					}
		    					break;
		    				case CONNECTION_STATE_NONE:
		    				case CONNECTION_STATE_CONNECTED:
		    					// Either not ready or already connected. Terminate new socket.
	                            try {
	                            	acceptSocket.close();
	                            } catch (IOException e) {
	                                Log.e(TAG, "Could not close unwanted socket", e);
	                            }
	                            break;
	    				}
	    			}
	    		}
    		}
    		
    	}
    	
    	/*
    	 * Cancel thread.
    	 */
    	public void cancel()
    	{
    		try {
    			_exitThread = true;
    			if(_myServerSocket != null){
    				_myServerSocket.close();
    			}
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
    	}
    }
    
    
    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread
    {
    	private final BluetoothSocket _socket;
    	private final String TAG = "Connect Thread";
    	private boolean _exitThread; 
        
    	/*
    	 * Constructor
    	 * @param device A bluetooth device to connect to.
    	 */
        public ConnectThread(BluetoothDevice device) {
            
            //create a temp bluetoothsocket as the member variable is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice, given that there is no sensitive info
            // sent in this game use an insecure connection this way a pairing request
            // is not required.
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            _socket = tmp;
        }
        
        /*
         * (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        public void run()
        {
        	if(_socket == null){
        		Log.e(TAG,"Error, Server socket is null - exiting ConnectThread");
    			if(_myHandler != null){
					_myHandler.obtainMessage(MESSAGE_CONNECT_THREAD_FAIL, -1, -1, -1).sendToTarget();
				}
        		return;
        	}
        	setName("ConnectThread");
        	
        	//make sure the exit thread flag is cleared
        	_exitThread = false;

            // Always cancel discovery because it will slow down a connection
        	_myBTAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
            	_socket.connect();
            } catch (IOException e) {           	
                // Close the socket
                try {
                	_socket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                
                // Start the service over to restart listening mode
                BluetoothService.this.start();
                return;
            }         
                        
            _IsServer = false;

            //the connection has been made clear this instance of the connect
            //thread so as not have the close method on the socket called
            synchronized(BluetoothService.this){
            	_myConnectThread = null;
            }
            
            // Start the connected thread
            if(!_exitThread){
            	manageConnection(_socket);
            }
        }
        
        /*
         * Cancels the thread by closing the socket.
         */
        public void cancel() {
            try {
            	_exitThread = true;
                _socket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    
    
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectionManager extends Thread
    {
    	private final InputStream _MyReader;
    	private final OutputStream _MyWriter;
    	private final BluetoothSocket _Socket;
    	private boolean _Exiting, _packetHeaderReceived;
    	private final String TAG = "Connection Manager";
    	private int _packetByteCount, _expectedPacketSize, _headerByteCount;
    	private byte [] _packetBuffer;
    	
    	private final int BUFFER_SIZE = 1024;
    	
    	/*
    	 * Constructor.
    	 * 
    	 * @param inSocket A bluetooth socket in which a connection
    	 * has been established.
    	 */
    	public ConnectionManager(BluetoothSocket inSocket)
    	{
    		_Socket = inSocket;
    		_deviceAddress = _Socket.getRemoteDevice().getAddress();
    		_deviceName = _Socket.getRemoteDevice().getName();
    		    		
    		_packetByteCount = _expectedPacketSize = _headerByteCount = 0;
    		_packetHeaderReceived = false;
    		
    		//The temp streams are needed since the reader and writer are final
    		OutputStream tempWriter = null;
    		InputStream tempReader = null;
    		
    		try{
    			tempWriter = _Socket.getOutputStream();
    			tempReader = _Socket.getInputStream();
    		}catch(IOException e){
    			Log.e(TAG, "Failed to get reader or writer " + e);
    			tempWriter = null;
    			tempReader = null;
    		}   
    		
    		_MyReader = tempReader;
    		_MyWriter = tempWriter;
    		
    		_Exiting = false;
	    }
    	
    	/*
    	 * Causes the thread to exit.
    	 */
    	public void cancel()
    	{
    		_Exiting = true;
    		try {
				_MyReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				_MyWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	/*
    	 * Thread run method override.
    	 * (non-Javadoc)
    	 * @see java.lang.Thread#run()
    	 */
    	public void run()
    	{
    		if(_MyWriter == null || _MyReader == null){
    			Log.e(TAG, "Writer or Reader == null, exiting thread.");
    			if(_myHandler != null){
					_myHandler.obtainMessage(MESSAGE_MANAGER_THREAD_FAIL, -1, -1, -1).sendToTarget();
				}
    			return;
    		}
    		
    		setName("Connection Manager Thread");
    		byte[] buffer = new byte[BUFFER_SIZE];
    		int bytesRead;
    		
    		while(!_Exiting){
    			
    			//Try and read from the buffer (note this is a blocking call)
    			try{
    				bytesRead = _MyReader.read(buffer, 0, BUFFER_SIZE); 
    			}catch(IOException  e){
    				bytesRead = 0;
    				Log.e(TAG,"Read Failed" ,e);
    				synchronized(BluetoothService.this){
    					if(_myHandler != null){
    						_myHandler.obtainMessage(MESSAGE_READ_FAILED, -1, -1, null).sendToTarget();
    					}
    				}
    			}
    			catch (IndexOutOfBoundsException e){
    				bytesRead = 0;
    				Log.e(TAG,"Read Failed" ,e);
    				synchronized(BluetoothService.this){
    					if(_myHandler != null){
    						_myHandler.obtainMessage(MESSAGE_READ_FAILED, -1, -1, null).sendToTarget();
    					}
    				}
    			}
    			
    			buildPacket(buffer, bytesRead);
    		}    			
    	}
    	
    	/*
    	 * Method to write data to the bluetooth socket.
    	 * 
    	 * @param buffer A byte buffer to send
    	 * @return true on success, false on failure
    	 */
    	public synchronized boolean WritePacket(byte buffer[])
    	{
    		boolean success = false;
    		
    		try{
    			_MyWriter.write(buffer);
    			success = true;
    		}catch(IOException e){
    			Log.e(TAG,"Write Failed" ,e);
				if(_myHandler != null){
					_myHandler.obtainMessage(MESSAGE_WRITE_FAILED, -1, -1, null).sendToTarget();
				}
    		}
    		
    		return success;
    	}
    	
    	/*
    	 * Method to build a packet out of byte buffer data
    	 * 
    	 * @param inBuffer a byte buffer of data to parse
    	 * @param byteCount The size of the data, in bytes
    	 */
    	private void buildPacket(byte [] inBuffer, int byteCount)
    	{
    		for(int i = 0; i < byteCount; i++){
    			
    			//if the packet header hasn't been received then add add byte to packet header
    			if(!_packetHeaderReceived){
    				_expectedPacketSize <<= 8;
    				_expectedPacketSize += inBuffer[i];
    				
    				//determine if the packet header is complete
    				if(++_headerByteCount == PACKET_HEADER_SIZE){
    					_packetHeaderReceived = true;
    					//reset the header byte count for the next packet
    					_headerByteCount = 0;
    					
    					//reset the packet byte counter for this message
    					_packetByteCount = 0;
    					
    					//create a new buffer for the message
    					_packetBuffer = new byte[_expectedPacketSize];
    				}
    			}
    			else{//the header has been received
	    			//add the byte to the buffer
	    			_packetBuffer[_packetByteCount++] = inBuffer[i];
	    			
	    			if(_packetByteCount == _expectedPacketSize){//message is complete
    					synchronized(BluetoothService.this){
        					if(_myHandler != null){
        						_myHandler.obtainMessage(MESSAGE_READ_PACKET_COMPLETE, 
        								_packetByteCount, -1, _packetBuffer).sendToTarget();
        					}
        				}
    					
    					//reset the counts for the next message    					
    					_packetHeaderReceived = false;
    				}
    			}
      		}
    	}//End buildPacket
    }
    
}
