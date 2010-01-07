package server;
import java.io.*;
import java.net.*;

public class MapSrvThread extends Thread {
	private MapSrv server;
	private Socket socket;
	private Double user;

	public MapSrvThread( MapSrv server, Socket socket ) {
		this.server = server;
		this.socket = socket;
		start();
	}

	public void run() {	
		try {
			DataInputStream dis = new DataInputStream( socket.getInputStream() );	//gets messages from client
			while (true) {
				String coords = dis.readUTF();
				if (!specialCommand(coords)) {
					String[] temp;
					temp = coords.split(":");
					user = Double.parseDouble(temp[7]);
					MapSrv.positions.set(MapSrv.getId(user), coords);
					server.sendToAll( coords );
					System.out.println( "MAP "+MapSrv.getTime()+": COORDS: "+coords );
				}
			}
		} catch( EOFException ie ) {		//no failmsg
		} catch( IOException ie ) {
		} finally {
			server.removeConnection( socket, user );	//closing socket when connection is lost
		}
	}
	
	void sendTo( String message ) {
		try {
			DataOutputStream dos = new DataOutputStream( socket.getOutputStream() );	//get outputstreams
			dos.writeUTF( message );		//and send message
		} catch( IOException ie ) { 
			System.out.println( ChatSrv.getTime()+": "+ie ); 		//failmsg
		}
	}
	
	public boolean specialCommand( String msg ){
		if( msg.substring(0, 6).equals("/NICK ") ){	//expecting a hello-message at first connection
			for (int i = 0; i < MapSrv.positions.size(); i++) {
				sendTo(msg);
			}
			return true;
		}else if( msg.substring(0, 6).equals("/HELLO") ){
			msg = msg.substring(7);
			System.out.println( "USR "+MapSrv.getTime()+": "+ msg );
			String[] t;
			t = msg.split(":");
			user = Double.parseDouble(t[7]);
			sendTo("/HELLO MAPS: Welcome to the Pansy Tadpole mapserver!");		//welcome-message
			
			int online = 0;
			for (int i = 0; i < MapSrv.positions.size(); i++) {
				String[] tmp_id = MapSrv.positions.get(i).split(":");
				if ( tmp_id[7] != "0.0" ){
					sendTo("/ADD "+MapSrv.positions.get(i));
					online ++;
				}
			}
			if(online == 0){
				t[5] = "2";
				msg = t[0]+":"+t[1]+":"+t[2]+":"+t[3]+":"+t[4]+":"+t[5]+":"+t[6]+":"+t[7];
			}
			MapSrv.positions.add(msg);
			server.sendToAll("/ADD "+msg);
			return true;
		} 
		return false;
	}
}
