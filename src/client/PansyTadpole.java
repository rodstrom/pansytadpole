package client;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.swing.JApplet;

/**
 * @author rodstrom
 * The game!
 * One player is chasing the others around, the chased players get points for every second they are on the field.
 * If they hide they wont get any points, and the player cahsing them needs to be fast, or else she/he won't get any points.
 * @param host String the servers address
 */
public class PansyTadpole extends JApplet implements MouseListener{
	private static final long serialVersionUID = 7197415241156375302L;
	private static boolean mouseActive = false;

	private static Random randomize = new Random();
	static double random = randomize.nextDouble();
	
	static String host, nick;
	static int connections = 0;
	private static boolean first = true;
	static GameArea ga = new GameArea();

	/* (non-Javadoc)
	 * @see java.applet.Applet#init()
	 */
	public void init() {	
		host = getParameter("host");		//defines the address to the mapserver
		nick = ""+(int)(random*1000000);	//a random nickname is chosen, the player can change it later
		this.add(new Sidebar(), BorderLayout.EAST);		//start the sidebar with scoreboard
		this.add(new Chat(), BorderLayout.SOUTH);		//start the chat/commandline
		this.add(ga, BorderLayout.CENTER);				//start the gamearea, where things happen
		this.addMouseListener(this);					//checks so that the mouse is within the applet, so that it doesnt keep moving when you change window or anything
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Container#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		super.paint(g);
	}
	
	/**
	 * Get the current time, for the chat-output
	 * @return String current time
	 */
	public static String getTime(){
		DateFormat time = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		Date date = new GregorianCalendar().getTime();
		return time.format(date);
	}
	
	/**
	 * Keep track of connections, if there are 2 connections you are connected to both servers.
	 * @param b	Boolean	true if connected, false if disconnected
	 */
	public static void connected(boolean b){
		if(b){
			connections++;
			if ( connections == 2 && first ) {		//if 2 then both map and chatserver are connected
				Chat.chatOutput.append(getTime()+": Welcome to Pansy Tadpole!\n\t  To look at the list of possible commands type \"/help\".\n\t  Change your nickname by typing \"/nick \" followed by the desired nickname.\n");
				first = false;
			} else if ( connections == 2 ) {
				Chat.chatOutput.append(getTime()+": Completely reconnected to Pansy Tadpole!\n");
			}
		}else{
			connections--;
			if ( connections == 0 ) {
				Chat.chatOutput.append(getTime()+": You have no connection to the server, please wait or contact admin.\n");
			} else {
				//Chat.chatOutput.append(getTime()+": Lost connection to a server, please wait or contact admin.\n");
			}
		}
	}
	
	//SETTERS&GETTERS BELOW
	
	public static void setMouseActive(boolean mouseActive) {
		PansyTadpole.mouseActive = mouseActive;
	}

	public static boolean isMouseActive() {
		return mouseActive;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		setMouseActive(true);
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		setMouseActive(false);
		repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub	
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}
}
