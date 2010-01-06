package client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

public class Player {
	URL url;
	ImageIcon sprite;
	int xpos = 80;
	int ypos = 50;
	int speed = 1;
	int turned = 1;
	int points = 0;
	String nick = "";
	double id;
	
	public Player( double i ){
		this.id = i;
	}
	
	public Player( int x, int y, int s, int t, int p, String n, double i ){
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
		this.nick = n;
		this.id = i;
	}
}
