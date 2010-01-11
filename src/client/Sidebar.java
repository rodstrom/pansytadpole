package client;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author rodstrom
 * The sidebar with the scoreboard.
 */
public class Sidebar extends JPanel {
	private static final long serialVersionUID = 1435646083804109826L;
	static JTextArea lblScoreboard = new JTextArea();
	
	/**
	 * Create a new sidebar.
	 */
	public Sidebar(){
		this.setPreferredSize(new Dimension(140, 500));
		lblScoreboard.setBackground( this.getBackground() );
		lblScoreboard.setPreferredSize(new Dimension(140, 500));
		lblScoreboard.setEditable(false);
		this.add(lblScoreboard);
	}
	
	/**
	 * Update the scoreboard with all connected players nicknames and scores.
	 * Also show who is hunting and which one you are playing.
	 * Also show which server you are connected to.
	 */
	public static void scoreboardUpdate(){
		lblScoreboard.setText("  "+PansyTadpole.host+"\n");
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
				lblScoreboard.append( "  "+scores );
			}
		}
	}
}
