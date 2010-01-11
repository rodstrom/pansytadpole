package client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * @author rodstrom
 * Here we have the players.
 */
public class Player {
	URL url;
	ImageIcon sprite;
	int xpos = 80;
	int ypos = 50;
	int speed = 1;
	int turned = 1;
	int points = 0;
	int status = 0;	//0 chased, 1 chased hidden, 2 chaser
	String nick = "";
	double id;
	
	/**
	 * Create a new player with default values.
	 * @param i
	 */
	public Player( double i ){
		this.id = i;
	}
	
	/**
	 * Set every datafield manually.
	 * @param x
	 * @param y
	 * @param s
	 * @param t
	 * @param p
	 * @param h
	 * @param n
	 * @param i
	 */
	public Player( int x, int y, int s, int t, int p, int h, String n, double i ){
		try {
			url = new URL( "http://pansytadpole.rodstrom.se/tadpole.png" );
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		sprite = new ImageIcon(url);
		this.xpos = x;
		this.ypos = y;
		this.speed = s;
		this.turned = t;
		this.points = p;
		this.status = h;
		this.nick = n;
		this.id = i;
	}
}
