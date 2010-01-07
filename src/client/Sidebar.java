package client;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class Sidebar extends JPanel {
	private static final long serialVersionUID = 1435646083804109826L;
	static JTextArea lblScoreboard = new JTextArea();
	static JLabel lblHost = new JLabel(PansyTadpole.host);
	static JLabel lblRand = new JLabel(Double.toString(PansyTadpole.random));
	
	public Sidebar(){
		this.setPreferredSize(new Dimension(140, 500));
		lblScoreboard.setBackground( this.getBackground() );
		lblScoreboard.setPreferredSize(new Dimension(130, 500));
		lblScoreboard.setEditable(false);
		this.add(lblHost);
		this.add(lblScoreboard);
		this.add(lblRand);		//DEBUG
	}
	
	public static void scoreboardUpdate(){
		lblScoreboard.setText("");
		String status = "_";
		for (int i = 0; i < GameArea.player.size(); i++) {
			if ( GameArea.player.get(i).id != 0.0 ){
				if( GameArea.player.get(i).status == 1 ){
					status = "H";
				}else if( GameArea.player.get(i).status == 2 ){
					status = "C";
				}else{
					status = "_";
				}
				String scores;
				
				if( GameArea.player.get(i).id == PansyTadpole.random ){
					scores = 

						status + ">" + 
						PansyTadpole.nick +" | "+ 
						GameArea.player.get(i).points +"\n";
				}else{
					scores = 
						status + " " + 
						GameArea.player.get(i).nick +" | "+ 
						GameArea.player.get(i).points +"\n";
				}
				lblScoreboard.append( scores );
			}
		}
	}
}
