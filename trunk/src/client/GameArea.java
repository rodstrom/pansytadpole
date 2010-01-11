package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.*;

/**
 * @author rodstrom
 * Handles the area with the players and where things happen in the game.
 */
public class GameArea extends JPanel implements ActionListener, KeyListener, Runnable{
	private static final long serialVersionUID = -5572295459928673608L;
	
	private Socket socket;		//socket connecting to server
	private DataOutputStream dos;
	private DataInputStream dis;
	private int port = 49061;	//mapserver-port
	private Timer tim = new Timer(10,this);
	boolean[] arrowDown = new boolean[4];
	
	int w_sprite = 100;	//sprite-width
	int h_sprite = 50;	//sprite-height
	int w_side = 140;
	int h_chat = 200;
	int max_x = 1280 - w_side; 
	int max_y = 800 - h_chat;
	
	//pixels from center of sprite to edges of picture
	int north_sprite = 13;
	int south_sprite = 13;
	int west_sprite = 43;
	int east_sprite = 26;
	
	protected static LinkedList<Player> player = new LinkedList<Player>();
	
	/**
	 * Starts the GameArea, giving the player a random position and starts a connection to the server.
	 */
	public GameArea() {		
      	this.addKeyListener(this);
      	this.setBackground(Color.WHITE);
      	this.setDoubleBuffered(true);
      	tim.addActionListener(this);
		//tim.start();	
    	
		Random rnd = new Random();			//random position, minimum 100px from border
		int rx = rnd.nextInt(1030)+100;
		int ry = rnd.nextInt(400)+100;
		connect(true, rx+":"+ry+":1:1:0:0:");	//try to connect, "true" because its the first time
		//connect(true, 1100+":"+100+":1:1");	//try to connect, "true" because its the first time
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if( hidden() >= 2 ){		//create a crosshair to make it easier to move the player
			g.drawLine(p().xpos, p().ypos-150, p().xpos, p().ypos+150);		//y-line
			g.drawLine(p().xpos-150, p().ypos, p().xpos+150, p().ypos);		//x-line
		}else if( hidden() == 1 ){	//create a diagonal crosshair
			g.drawLine(p().xpos-150, p().ypos-150, p().xpos+150, p().ypos+150);
			g.drawLine(p().xpos+150, p().ypos-150, p().xpos-150, p().ypos+150);
		}
		for (int i = 0; i < player.size(); i++) {
			if(player.get(i).id != 0.0){	//dont paint disconnected players
				Player p_i = player.get(i);
				g.drawImage(p_i.sprite.getImage(), p_i.xpos-p_i.turned*(w_sprite/2), p_i.ypos-(h_sprite/2), p_i.turned*w_sprite, h_sprite, null);
			}
		}
		if(PansyTadpole.isMouseActive()){
			this.requestFocus();
		}
	}
	
	/**
	 * Checks if the character is hiding, and how much.<br>
	 * Sets up a grid to make it possible for the player to locate his/her character.<br>
	 * Makes sure the player doesn't earns any points when hiding.<br>
	 * Also makes sure that the server knows when player hides.
	 * 
	 * @return	<b>0</b>	Player isn't hiding<br>
	 * <b>1</b>	Player is completely hidden in a corner, needs diagonal crosshair<br>
	 * <b>2</b>	Player needs a crosshair for locating<br>
	 * <b>3</b>	Player is completely hidden, but not in a corner
	 */
	private int hidden() {
		boolean hiding = false;
		//first check if partly hidden
		if( player.size() > 0 ){
			if( ( 0 >= p().xpos ) || ( p().xpos >= max_x ) || ( 0 >= p().ypos ) || ( p().ypos >= max_y ) ){
				//START of checking complete hiding
				if( 		//if hidden in X
					(((p().turned==-1) && 
						((-west_sprite >= p().xpos) || 			//left, <-				//___________________PROBLEM_______________________!!!
						(max_x + east_sprite <= p().xpos)))) ||	//right, <-
					(-east_sprite >= p().xpos) ||				//left, ->
					(max_x + west_sprite <= p().xpos)			//right, ->
				){
					//stoppa poängräknare för spelare
					//meddela att man gömt sig
					hiding = true;			//it was hiding
				}
				if( 	//if hidden in Y
						(-south_sprite >= p().ypos) ||			//up
						(max_y + north_sprite <= p().ypos)		//down
				){
					if(hiding) return 1;	//if it was hiding in X, and got here, it's hiding in a corner
					hiding = true;			//it was hiding
				}
				//END of checking complete hiding
				if(hiding && p().status != 2) p().status = 1;
				if(hiding) return 3;	//it was hiding in x or y, or both... 3 completely hidden, regular crosshair
				if(p().status != 2) p().status = 0;
				return 2;											//2 partly hidden, regular crosshair
			}
			//ge poäng till spelare som vågar vistas ute på planen
			if(p().status != 2) p().status = 0;
		}
		return 0;												//0 not hidden, no crosshair
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			while (true) {
				String coords = dis.readUTF();
				System.out.println(coords);
				if (!specialCommand(coords)) {
					String[] temp;
					temp = coords.split(":");
					Player p_s = player.get(getId(Double.valueOf(temp[7])));	//this is the current player
					if( !temp[7].equals( Double.toString(PansyTadpole.random) ) ){		//only paint new coordinates if they didnt come from this client
						p_s.xpos = Integer.parseInt(temp[0]);		//setting new data for the player
						p_s.ypos = Integer.parseInt(temp[1]);
						p_s.turned = Integer.parseInt(temp[2]);
						p_s.speed = Integer.parseInt(temp[3]);
						p_s.points = Integer.parseInt(temp[4]);
						p_s.status = Integer.parseInt(temp[5]);
						p_s.nick = temp[6];
						repaint();
					}
				}
			}
		} catch( IOException ie ) {
			Chat.chatOutput.append(PansyTadpole.getTime()+": Lost connection to the maps-server.\n");
			PansyTadpole.connected(false);	//lost connection
			tim.stop();
			int x = p().xpos;		//saving playerdata
			int y = p().ypos;
			int t = p().turned;
			int s = p().speed;
			int p = p().points;
			int h = p().status;
			player.clear();			//clears all players
			repaint();
			connect(false, x+":"+y+":"+t+":"+s+":"+p+":"+h+":");	//reconnect with previous data
			return;
		}
	}

	/**
	 * Send position, status etc. to the server.
	 */
	private void sendData() {
		try {
			dos.writeUTF( p().xpos +":"+ p().ypos +":"+ p().turned +":"+ p().speed +":"+ p().points +":"+ p().status +":"+ PansyTadpole.nick +":"+ PansyTadpole.random);
		} catch( IOException ie ) { 
			Chat.chatOutput.append( PansyTadpole.getTime()+": Error while sending coordinates.\n" );
		}
	}
	
	/**
	 * Connect to the mapserver.
	 * @param first 		Boolean 	true if this is the first connection or false if it's a reconnect-attempt
	 * @param player_data	String		the players position, id etc.
	 */
	public void connect(boolean first, String player_data){
		while ( true ) {
			try {
				socket = new Socket(PansyTadpole.host, port);
				//create streams for communication
				dis = new DataInputStream( socket.getInputStream() );
				dos = new DataOutputStream( socket.getOutputStream() );
				dos.writeUTF("/HELLO "+player_data+PansyTadpole.nick+":"+PansyTadpole.random);
				// Start a background thread for receiving coordinates
				new Thread( this ).start();		//starts run()-method
				if(!first) Chat.chatOutput.append(PansyTadpole.getTime()+": Reconnected to the maps-server.\n");
				Sidebar.scoreboardUpdate();
				return;
			} catch( IOException e ) { 
				//System.out.println(e);
				/*if(first){
					Chat.chatOutput.append(PansyTadpole.getTime()+": MAPS: Can't connect to server, trying again\n");
					first = false;
				}*/
			}
		}
	}
	
	/**
	 * Use a player in the Linked List of players, we need the players ID to do this.
	 * @param d	Double	the id of the player that we want to use
	 * @return Double	where in the linked list player we can find this player
	 */
	public static int getId(Double d){
		for (int i = 0; i < player.size(); i++) {
			if( player.get(i).id == d ){	//needs to be unique
				return i;
			}
		}
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()>=37 && e.getKeyCode()<=40){
			arrowDown[40-e.getKeyCode()]=true;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()>=37 && e.getKeyCode()<=40)
			arrowDown[40-e.getKeyCode()]=false;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {	//happens every 5ms
		increase_points();
		if (PansyTadpole.isMouseActive() && (arrowDown[0]||arrowDown[1]||arrowDown[2]||arrowDown[3])) {
			if( calculateMove() ){
				collisionCheck();
				sendData();
				repaint();
			}
		}else{
			for (int i = 0; i < arrowDown.length; i++) {
				arrowDown[i] = false;
			}
		}
	}
	
	/**
	 * NOT YET FULLY FUNCTIONAL!
	 * Checking if this client is used by the player chasing everyone.
	 * If this is the case we need to check if it collides with another player.
	 * Then we tell this to the server which then handles the switching of player-statuses.
	 */
	private void collisionCheck() {
		if( p().status != 2 ) return;	//only do this if the client is the chaser
		int xdiff, ydiff;
		for (int i = 0; i < player.size(); i++) {
			if( player.get(i).id != 0.0 && player.get(i).status == 0 ){		//a connected player which isnt hidden or chasing
				xdiff = p().xpos - player.get(i).xpos;
				ydiff = p().ypos - player.get(i).ypos;
				if( (ydiff <= 26) && (ydiff >= -26) ){
					if( ( (p().turned == 1) && (player.get(i).turned == -1) ) || ( (p().turned == -1) && (player.get(i).turned == 1) ) ){
						if( (xdiff <= 26) && (xdiff >= -26) ){
							
						}
					}else if( ( (p().turned == -1) && (player.get(i).turned == -1) ) || ( (p().turned == 1) && (player.get(i).turned == 1) ) ){
						if( (xdiff <= 69) && (xdiff >= -69) ){
							try {
								dos.writeUTF("/GOT "+player.get(i).id+":"+p().id);
								Chat.chatOutput.append( PansyTadpole.getTime()+": Caught "+player.get(i).nick+".\n" );
							} catch (IOException e) {
								Chat.chatOutput.append( PansyTadpole.getTime()+": Error while sending collision-info.\n" );
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Calculate the players new position if they try to move.
	 * @return Boolean if the player have moved anything at all or not
	 */
	private boolean calculateMove() {
		boolean change = false;
		if(arrowDown[0] && !arrowDown[2] && (p().ypos+p().speed) <= max_y + 100){	//height(800) - chat(200) + hide-area(100)
			p().ypos += p().speed;	//move down
			change = true;
		}
		if(arrowDown[1] && !arrowDown[3] && (p().xpos+p().speed) <= max_x + 100){	//width(1280) - sidebar(140) + hide-area(100)
			p().xpos += p().speed;	//move right
			p().turned = 1;
			change = true;
		}
		if(arrowDown[2] && !arrowDown[0] && (p().ypos-p().speed) >= -100){			//0 - hide-area(100)
			p().ypos -= p().speed;	//move up
			change = true;
		}
		if(arrowDown[3] && !arrowDown[1] && (p().xpos-p().speed) >= -100){			//0 - hide-area(100)
			p().xpos -= p().speed;	//move left
			p().turned = -1;
			change = true;
		}
		return change;
	}

	/**
	 * Checks if a command sent by the server is a special-command or just coordinates.
	 * @param msg	String	the message sent by the server
	 * @return	Boolean		if it was a special-command or not
	 */
	public boolean specialCommand( String msg ){
		String[] temp;
		temp = msg.substring(5).split(":");
		if( msg.substring(0, 4).equals("/ADD") ){
			int x = Integer.parseInt(temp[0]);
			int y = Integer.parseInt(temp[1]);
			int s = Integer.parseInt(temp[2]);
			int t = Integer.parseInt(temp[3]);
			int p = Integer.parseInt(temp[4]);
			int h = Integer.parseInt(temp[5]);
			String n = temp[6];
			double i = Double.parseDouble(temp[7]);		
			player.add(new Player(x,y,t,s,p,h,n,i));
			sendData();
			repaint();
			return true;
		}else if( msg.substring(0, 4).equals("/SUB") ){
			//player.remove(getId( Double.parseDouble(temp[4]) ));
			player.remove( getId(Double.parseDouble( msg.substring(5) )) );
			//player.set(getId(Double.parseDouble(msg.substring(5))), new Player(0,0,0,0,0,0,null,0.0));
			repaint();
			return true;
		}else if( msg.substring(0, 6).equals("/NICK ") ){
			String n = temp[0];
			double i = Double.parseDouble(temp[1]);	
			player.get(getId(i)).nick = n;
			return true;
		}else if( msg.substring(0, 6).equals("/HELLO") ){
			//Chat.chatOutput.append(PansyTadpole.getTime()+": "+msg.substring(7)+"\n");
			PansyTadpole.connected(true);
			tim.start();
			return true;
		}
		return false;
	}
	
	/**
	 * NOT YET FULLY FUNCTIONAL!
	 * Increase the players points every second if the player isn't hiding nor is chasing the other players around.
	 * FOR NOW IT ONLY UPDATES THE SCOREBOARD...
	 */
	public void increase_points(){
		if ( PansyTadpole.connections == 2 ){
			//if ( hidden() == 0 || hidden() == 2) p().points += 5;	//gives the player 100 points/second if not hiding
			Sidebar.scoreboardUpdate();
		}
	}
	
	/**
	 * Get and set the player that this client is handling.
	 * Easier to read code if there are lots of "p()" instead of "player.get(getId(PansyTadpole.random))" everywhere.
	 * @return Player	the player that this client handles
	 */
	public Player p(){
		if( player.size() > 0 ){
			return player.get(getId(PansyTadpole.random));
		}
		return null;
	}

	/**
	 * To update a players nickname we need to change it in the client, and then tell the server about the change.
	 */
	public void updateNick() {
		if( player.size() > 0 ){
			player.get(getId(PansyTadpole.random)).nick = PansyTadpole.nick;
			try {
				dos.writeUTF("/NICK "+PansyTadpole.nick+":"+PansyTadpole.random);
			} catch (IOException e) { e.printStackTrace(); }
			sendData();
		}
	}
}
