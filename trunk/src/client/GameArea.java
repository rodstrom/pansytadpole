package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.*;

public class GameArea extends JPanel implements ActionListener, KeyListener, Runnable{
	private static final long serialVersionUID = -5572295459928673608L;
	
	private Socket socket;		//socket connecting to server
	private DataOutputStream dos;
	private DataInputStream dis;
	private int port = 49061;	//mapserver-port
	private Timer tim = new Timer(10,this);
	boolean[] arrowDown = new boolean[4];
	
	protected static LinkedList<Player> player = new LinkedList<Player>();
	
	public GameArea() {		
      	this.addKeyListener(this);
      	this.setBackground(Color.WHITE);
      	this.setDoubleBuffered(true);
      	tim.addActionListener(this);
		tim.start();
		

		Random rnd = new Random();			//random position, minimum 100px from border
		int rx = rnd.nextInt(1030)+100;
		int ry = rnd.nextInt(400)+100;
		connect(true, rx+":"+ry+":1:1");	//try to connect, "true" because its the first time
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (int i = 0; i < player.size(); i++) {
			if(player.get(i).id != 0.0){
				Player p = player.get(i);
				int w = 100;	//sprite-width
				int h = 50;		//sprite-height
				g.drawImage(p.sprite.getImage(), p.xpos-p.turned*(w/2), p.ypos-(h/2), p.turned*w, h, null);
			}
		}
		if(PansyTadpole.isMouseActive()){
			this.requestFocus();
		}
	}

	//keep receiving messages from the server
	public void run() {
		try {
			while (true) {
				String coords = dis.readUTF();
				if (!specialCommand(coords)) {
					String[] temp;
					temp = coords.split(":");
					Player ps = player.get(getId(Double.valueOf(temp[4])));
					if( !temp[4].equals( Double.toString(PansyTadpole.random) ) ){		//only paint new coordinates if they didnt come from this client
						ps.xpos = Integer.parseInt(temp[0]);
						ps.ypos = Integer.parseInt(temp[1]);
						ps.turned = Integer.parseInt(temp[2]);
						ps.speed = Integer.parseInt(temp[3]);
						repaint();
					}
				}
			}
		} catch( IOException ie ) {
			Player p = player.get(getId(PansyTadpole.random));
			Chat.chatOutput.append(PansyTadpole.getTime()+": MAP: Connection reset, reconnecting\n");
			int x = p.xpos;
			int y = p.ypos;
			int t = p.turned;
			int s = p.speed;
			PansyTadpole.connected = false;
			player.clear();
			repaint();
			connect(false, x+":"+y+":"+t+":"+s);
			return;
		}
	}

	private void sendData() {
		Player p = player.get(getId(PansyTadpole.random));
		try {
			dos.writeUTF( p.xpos +":"+ p.ypos +":"+ p.turned +":"+ p.speed +":"+ PansyTadpole.random);
		} catch( IOException ie ) { 
			//Chat.chatOutput.append( Zincgull.getTime()+": MAP: Can't send coordinates\n" );
		}
	}
	
	public void connect(boolean first, String position){
		while (true) {
			try {
				socket = new Socket(PansyTadpole.host, port);
				//create streams for communication
				dis = new DataInputStream( socket.getInputStream() );
				dos = new DataOutputStream( socket.getOutputStream() );
				dos.writeUTF("/HELLO "+position+":"+PansyTadpole.random);
				// Start a background thread for receiving coordinates
				new Thread( this ).start();		//starts run()-method
				if(!first) Chat.chatOutput.append(PansyTadpole.getTime()+": MAP: Connected to mapserver\n");
				return;
			} catch( IOException e ) { 
				//System.out.println(e);
				if(first){
					Chat.chatOutput.append(PansyTadpole.getTime()+": MAP: Can't connect to server, trying again\n");
					first = false;
				}
			}
		}
	}
	
	public static int getId(Double d){
		for (int i = 0; i < player.size(); i++) {
			if( player.get(i).id == d ){	//needs to be unique
				return i;
			}
		}
		return 0;
	}
	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()>=37 && e.getKeyCode()<=40){
			arrowDown[40-e.getKeyCode()]=true;
		}
	}
	
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()>=37 && e.getKeyCode()<=40)
			arrowDown[40-e.getKeyCode()]=false;
	}

	public void keyTyped(KeyEvent e) {}

	public void actionPerformed(ActionEvent e) {
		if ( PansyTadpole.connected ) {
			if (PansyTadpole.isMouseActive()&&(arrowDown[0]||arrowDown[1]||arrowDown[2]||arrowDown[3])) {
				if( calculateMove() ){
					sendData();
					repaint();
				}
			}else{
				for (int i = 0; i < arrowDown.length; i++) {
					arrowDown[i] = false;
				}
			}
		}
	}

	private boolean calculateMove() {
		boolean change = false;
		Player p = player.get(getId(PansyTadpole.random));
		if(arrowDown[0] && (p.ypos+p.speed)<=700){		//height(800) - chat(200) + hide-area(100)
			p.ypos=p.ypos+p.speed;	//move down
			change = true;
		}
		if(arrowDown[1] && (p.xpos+p.speed)<=1330){		//width(1280) - sidebar(50) + hide-area(100)
			p.xpos=p.xpos+p.speed;	//move right
			p.turned = 1;
			change = true;
		}
		if(arrowDown[2] && (p.ypos-p.speed)>=-100){		//0 - hide-area(100)
			p.ypos=p.ypos-p.speed;	//move up
			change = true;
		}
		if(arrowDown[3] && (p.xpos-p.speed)>=-100){		//0 - hide-area(100)
			p.xpos=p.xpos-p.speed;	//move left
			p.turned = -1;
			change = true;
		}
		return change;
	}
	
	//possible commands the server can send
	public boolean specialCommand( String msg ){
		String[] temp;
		temp = msg.substring(5).split(":");
		if( msg.substring(0, 4).equals("/ADD") ){
			int x = Integer.parseInt(temp[0]);
			int y = Integer.parseInt(temp[1]);
			int s = Integer.parseInt(temp[2]);
			int t = Integer.parseInt(temp[3]);
			double i = Double.parseDouble(temp[4]);		
			player.add(new Player(x,y,t,s,i));
			PansyTadpole.connected = true;
			repaint();
			return true;
		}else if( msg.substring(0, 4).equals("/SUB") ){
			//player.remove(getId( Double.parseDouble(temp[4]) ));
			player.set(getId(Double.parseDouble(msg.substring(5))), new Player(0,0,0,0,0.0));
			repaint();
			return true;
		}else if( msg.substring(0, 6).equals("/HELLO") ){
			Chat.chatOutput.append(PansyTadpole.getTime()+": "+msg.substring(7)+"\n");
			return true;
		}
		return false;
	}
}
