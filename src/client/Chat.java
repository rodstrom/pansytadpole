package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 * @author rodstrom
 * The chat and commandline of the game.
 */
public class Chat extends JPanel implements Runnable {
	private static final long serialVersionUID = -6395460343649750082L;
	private JTextField chatInput = new JTextField();
	static TextArea chatOutput = new TextArea();	//not a JTextArea due to bad scroll-support

	private Socket socket;		//socket connecting to server
	private DataOutputStream dos;
	private DataInputStream dis;
	private int port = 49060;	//chatserver-port
	
	private ActionListener al = new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
			if( !e.getActionCommand().isEmpty() ){	//make sure it's not null
				processMessage( e.getActionCommand() );
			}
		}
	};
	
	/**
	 * Create a new Chat. Then connect to the chatserver.
	 */
	public Chat() {		
		chatOutput.setEditable(false);
		chatOutput.setBackground(Color.BLACK);
		chatOutput.setForeground(Color.GREEN);
		chatOutput.setPreferredSize(new Dimension(1280, 180));
		
		chatInput.setPreferredSize(new Dimension(1280, 20));

		this.setLayout( new BorderLayout() );
		this.add( "North", chatInput );
		this.add( "Center", chatOutput );
		this.setVisible(true);
		//_ISSUE: client wont start until server is started...
		connectServer(true);	//try to connect, "true" because its the first time
	}

	/**
	 * Handles everything that gets typed by the user.
	 * Then sends it to the server.
	 * @param message
	 */
	private void processMessage( String message ) {
		try {
			if ( message.substring(0, 1).equals("/") ) {	//only special commands start with a /
				if( message.length() == 5 && message.equals("/info") ){
					chatOutput.append("\t  Server is "+PansyTadpole.host+"\n");
					chatOutput.append("\t  Nickname is "+PansyTadpole.nick+"\n");
					chatOutput.append("\t  Unique ID is "+PansyTadpole.random+"\n");
				}else{
					dos.writeUTF( message );				//send command in full
				}
			}else{
				dos.writeUTF( "/msg "+message );		//send as regular message
			}
			chatInput.setText( "" );		//clear inputfield
		} catch( IOException ie ) { 
			System.out.println( ie ); 
			chatOutput.append( PansyTadpole.getTime()+": Error while sending message.\n" );
		}
	}
	
	/**
	 * Connect to the server.
	 * @param first Boolean	is this the first time we're connecting?
	 */
	public void connectServer(boolean first){
		while (true) {
			try {
				socket = new Socket(PansyTadpole.host, port);
				//create streams for communication
				dis = new DataInputStream( socket.getInputStream() );
				dos = new DataOutputStream( socket.getOutputStream() );
				dos.writeUTF( "/HELLO "+PansyTadpole.nick +":"+ PansyTadpole.random );	//say hello to server, nickname and unique random
				// Start a background thread for receiving messages
				new Thread( this ).start();		//starts run()-method
				chatInput.addActionListener( al );
				if(!first) chatOutput.append(PansyTadpole.getTime()+": Reconnected to the chat-server.\n");
				return;
			} catch( IOException e ) {
				
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			while (true) {
				String message = dis.readUTF();		//read
				if( !specialCommand(message) ){
					chatOutput.append( PansyTadpole.getTime()+": "+message+"\n" );	//print
				}
			}
		} catch( IOException ie ) { 
			//System.out.println( ie );	//debug, not necessary with below line
			chatOutput.append(PansyTadpole.getTime()+": Lost connection to the chat-server.\n");
			PansyTadpole.connected(false);	//lost connection
			chatInput.removeActionListener( al );
			connectServer(false);
		}
	}
	
	/**
	 * Check if the server sent a special-command.
	 * @param msg String the command received from the server
	 * @return
	 */
	public boolean specialCommand( String msg ){
		if( msg.substring(0, 1).equals("/") ){
			if( msg.length() >= 6){
				if( msg.substring(0, 5).equals("/msg ") ){
					return false;
				}else if( msg.substring(0, 6).equals("/nick ") ){	//expecting a hello-message at first connection
					PansyTadpole.nick = msg.substring(6);
					PansyTadpole.ga.updateNick();
					return true;
				}else if( msg.substring(0, 6).equals("/HELLO") ){	//expecting a hello-message at first connection
					//chatOutput.append( PansyTadpole.getTime()+": "+msg.substring(7)+"\n" );
					PansyTadpole.connected(true);
					return true;
				}
			}
		}
		return false;
	}	
}
