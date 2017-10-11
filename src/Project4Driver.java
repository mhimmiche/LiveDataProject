import javax.swing.JFrame;
/**
 * Driver class for project 4
 * @author Mehdi Himmiche
 * @date Apr 17, 2016
 */
public class Project4Driver {
	public static void main(String[] args) {
		JFrame frame = new WikiFrame();
		frame.setTitle("Mehdi Himmiche - Project 4: Wikipedia Viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.pack();
	    frame.setVisible(true);
	}
}
