package server;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.util.*;

public class MapSrv {	
	private ServerSocket ss;
	//this is used to don't have to create a DOS every time you are writing to a stream
	private Hashtable<Socket, DataOutputStream> outputStreams = new Hashtable<Socket, DataOutputStream>();
	protected static LinkedList<String> positions = new LinkedList<String>();
	
	// Constructor and while-accept loop
	public MapSrv( int port ) {
		try {
			listen( port );
		} catch (IOException e) {
			System.out.println( "ERR "+getTime()+": Another instance of the server is already running on this port.");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	// Usage: java Server <port>
	static public void main( String args[] ){
		new MapSrv( 49061 );	//create server
	}
	
	private void listen( int port ) throws IOException {
		ss = new ServerSocket( port );
		System.out.println( "INF "+getTime()+": Started the Zincgull Mapserver on port "+port+"\n              listening on "+ss );
		
		while (true) {	//accepting connections forever
			Socket s = ss.accept();		//grab a connection
			System.out.println( "USR "+getTime()+": New connection from "+s );	//msg about the new connection
			DataOutputStream dos = new DataOutputStream( s.getOutputStream() );	//DOS used to write to client
			getOutputStreams().put( s, dos );		//saving the stream
			new MapSrvThread( this, s );		//create a new thread for the stream
		}
	}
	// Enumerate all OutputStreams
	Enumeration<DataOutputStream> enumOutputStreams() {
		return getOutputStreams().elements();
	}
	
	void sendToAll( String message ) {
		synchronized( getOutputStreams() ) {		//sync so that no other thread screws this one over
			for (Enumeration<?> e = enumOutputStreams(); e.hasMoreElements(); ) {
				DataOutputStream dos = (DataOutputStream)e.nextElement();		//get all outputstreams
				try {
					dos.writeUTF( message );		//and send message
				} catch( IOException ie ) { 
					System.out.println( getTime()+": "+ie ); 		//failmsg
				}
			}
		}
	}
	
	void removeConnection( Socket s, double d ) {		//run when connection is discovered dead
		String[] t;
		t = positions.get(getId(d)).split(":");
		synchronized( getOutputStreams() ) {		//dont mess up sendToAll
			positions.remove(getId(d));
			System.out.println( "USR "+getTime()+": Lost connection from "+s );
			getOutputStreams().remove( s );
			sendToAll("/SUB "+d);
			if(positions.isEmpty()) System.out.println( "USR "+getTime()+": No users online" );
			try {
				s.close();
			} catch( IOException ie ) {
				System.out.println( "ERR "+getTime()+": Error closing "+s );
				ie.printStackTrace();
			}
		}
		//begin checking if the chaser left, and if thats the case, select a new chaser.
		if( t[5].equals("2") && positions.size() > 0 ){
			t = positions.getLast().split(":");
			t[5] = "2";
			String coords = t[0]+":"+t[1]+":"+t[2]+":"+t[3]+":"+t[4]+":"+t[5]+":"+t[6]+":"+t[7];
			positions.set(getId(Double.valueOf(t[7])), coords);
			sendToAll("/SUB "+t[7]);
			sendToAll("/ADD "+coords);
		}	//end checking if chaser
	}
	
	public static int getId(Double d){
		String[] tmp;
		for (int i = 0; i < positions.size(); i++) {
			tmp = positions.get(i).split(":");
			if( tmp[7].equals( Double.toString(d) ) ){	//needs to be unique
				return i;
			}
		}
		return 0;
	}
	
	public static String getTime(){
		DateFormat time = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		Date date = new GregorianCalendar().getTime();
		return time.format(date);
	}

	public void setOutputStreams(Hashtable<Socket, DataOutputStream> outputStreams) {
		this.outputStreams = outputStreams;
	}

	public Hashtable<Socket, DataOutputStream> getOutputStreams() {
		return outputStreams;
	}
}

