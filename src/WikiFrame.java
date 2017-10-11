import javax.swing.*;
import java.net.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import net.sf.json.*;
import org.apache.commons.lang.exception.*;
/**
 * Class that does JSON pulls off geonames, pulls Wikipedia articles regarding cities, and displays summary and map. 
 * It allows the user to see the full article in browser. 
 * @author Mehdi Himmiche
 * @date Apr 17, 2016
 */
public class WikiFrame extends JFrame {
	
	private JTextArea outputArea;
	private JTextField inputField;
	private JLabel mapLabel;
	private JLabel inputLabel;
	private JButton searchButton;
	private JButton browserButton;
	private float lat;
	private float lng;
	private String url;
	
	/**
	 * Constructor, initializes the variables and JPanel components. 
	 */
	public WikiFrame() {
		lat = 0;
		lng = 0;
		inputLabel = new JLabel("Enter name of city");
		mapLabel = new JLabel("Map Picture", JLabel.CENTER);
		mapLabel.setPreferredSize(new Dimension (612, 612));
		browserButton = new JButton("Open in browser");
		browserButton.setEnabled(false);
		ActionListener browserListener = new browserListener();
		browserButton.addActionListener(browserListener);
		outputArea = new JTextArea(10, 30);
		searchButton = new JButton("Search");
		inputField = new JTextField();
		ActionListener searchListener = new searchListener();
		searchButton.addActionListener(searchListener);
		finalPanel();
	}
	
	/**
	 * Method that takes in the input (city name), and does the JSON pull off geonames. 
	 * @param input
	 * @return summary of the wikipedia article about the town input by the user. 
	 * @throws Exception if there is an error pulling JSON data
	 */
	public String readInput(String input) throws Exception {
		url = "https://";
		String summary = "";
		if (!input.equals("")) {
			try {
				summary = "";
				String JSONString = readURL("http://api.geonames.org/wikipediaSearchJSON?q=" + URLEncoder.encode(input, "UTF-8") + 
						"&maxRows=20&formatted=true&username=mhimmiche");
				JSONObject jSonObject = JSONObject.fromObject(JSONString);
				JSONArray jSonArray = (JSONArray)jSonObject.get("geonames");
				int indexOfArticle = correctArticle(jSonArray, input);
				JSONObject output = (JSONObject)(jSonArray.get(indexOfArticle));
				summary = (String) output.get("summary");
				lat = Float.parseFloat("" + output.get("lat"));
				lng = Float.parseFloat("" + output.get("lng"));
				url += output.getString("wikipediaUrl");
			} catch(IOException error) {
				;
			}
		}
		return summary;
	}
	
	/**
	 * Method that checks every article pulled (Wikipedia pulls multiple related article) and checks if the article title matches the input.
	 * If no article title matches, it returns the first article pulled by Wikipedia. 
	 * @param jSonArray, an array of JSON objects containing all information regarding each article. 
	 * @param input, the desired city whose information a user would like to retrieve. 
	 * @return the index of the JSONObject containing a matching title, or 0 if no match was found. 
	 */
	private int correctArticle(JSONArray jSonArray, String input) {
		int correct = 0;
		String title = "";
		for (int i = 0; i < jSonArray.size(); i++) {
			JSONObject output = (JSONObject)(jSonArray.get(i));
			input = input.toLowerCase();
			title = (String) output.get("title");
			title = title.toLowerCase();
			if (input.equals(title)) {
				correct = i;
			}
		}
		return correct;
	}
	
	/**
	 * Creates the final panel that the user sees. Adds 10 pixels around each side of the panel to make it look "prettier"
	 */
	private void finalPanel() {
		JPanel finalPanel = new JPanel();
		add(finalPanel);
		finalPanel.setLayout(new BorderLayout());
		JPanel fullPanel = createPanel();
		finalPanel.add(fullPanel, BorderLayout.CENTER);
		finalPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.EAST);
		finalPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.WEST);
		finalPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.NORTH);
		finalPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.SOUTH);
	}
	
	/**
	 * Creates the panel with all the JPanel components, placed appropriately. 
	 * @return the JPanel of the final panel to be displayed. Used in the finalPanel() method to add the 10 pixels around. 
	 */
	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JPanel searchPanel = searchPanel();
		panel.add(searchPanel, BorderLayout.NORTH);
		JPanel outputPane = outputPanel();
		panel.add(outputPane, BorderLayout.SOUTH);
		JPanel map = mapPanel();
		panel.add(map, BorderLayout.CENTER);
		
		return panel;
	}
	
	/**
	 * Panel for the search bar and button. Creates the search textfield and search button and places them appropriately. 
	 * @return the JPanel containing the search bar and search button. 
	 */
	private JPanel searchPanel() {
		JPanel search = new JPanel();
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new GridLayout(2, 0));
		searchPanel.add(inputLabel);
		searchPanel.add(inputField);
		search.setLayout(new BorderLayout());
		search.add(searchButton, BorderLayout.EAST);
		search.add(searchPanel);
		
		return search;
	}
	
	/**
	 * Creates a JPanel in which to display the map picture. Only adds the initial label at first.
	 * I figured having its own method allows me to expand on it if needed. 
	 * @return JPamel containing the google map image
	 */
	private JPanel mapPanel() {
		JPanel map = new JPanel();
		map.add(mapLabel);
		return map;
	}
	
	/**
	 * Method that pulls a Google Maps image centered around the coordinates of the desired city. 
	 * I used the following tutorial and followed the code there: 
	 * http://www.luv2code.com/2015/05/15/how-to-add-google-maps-to-java-swing-gui/
	 * I found a different API to use, that would display dynamic google maps frames. 
	 * However I did not get the trial code for it in time and had to use static pictures. 
	 */
	private void updateMapLabel() {
		try {
            String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?"
            		+ "center=" + lat + "," + lng +"&zoom=11&size=612x612&scale=2&maptype=hybrid";
            String destFile = "image.jpg";
            URL url = new URL(imageUrl);
            InputStream inputReader = url.openStream();
            OutputStream outputSaver = new FileOutputStream(destFile);
            byte[] b = new byte[2048];
            int length;
            while ((length = inputReader.read(b)) != -1) {
                outputSaver.write(b, 0, length);
            }
            inputReader.close();
            outputSaver.close();
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(null,
        		    "There was an error while trying to load map.",
        		    "Inane error",
        		    JOptionPane.ERROR_MESSAGE);
        }
		ImageIcon imageIcon = new ImageIcon((new ImageIcon("image.jpg"))
				.getImage().getScaledInstance(630, 600,
				java.awt.Image.SCALE_SMOOTH));
		mapLabel.setText("");
		mapLabel.setIcon(imageIcon);
	}
	
	/**
	 * Creates a scrollable text area in which to display the summary of the wikipedia article. 
	 * @return JScrollPane containing the text area for the summary.
	 */
	private JScrollPane outputAreaPane() {
		JScrollPane outputPane = new JScrollPane(outputArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);
		outputArea.setWrapStyleWord(true);
		
		return outputPane;
	}
	
	/**
	 * Creates a JPanel in which to store the JScrollPane containing the output area. 
	 * @return JPanel for the output area
	 */
	private JPanel outputPanel() {
		JPanel output = new JPanel();
		output.setLayout(new BorderLayout());
		JScrollPane outputPane = outputAreaPane();
		output.add(outputPane, BorderLayout.CENTER);
		output.add(browserButton, BorderLayout.EAST);
		
		return output;
	}
	
	/**
	 * Class for the action listener for the search button
	 * @author Mehdi Himmiche
	 * @date Apr 17, 2016
	 */
	class searchListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				String input = inputField.getText();
				String summary = readInput(input);
				if (!summary.equals("")) {
					outputArea.setText(summary);
					updateMapLabel();
					browserButton.setEnabled(true);
				}				
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null,
	        		    "Could not load data from Wikipedia.",
	        		    "JSON error",
	        		    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Class for the action listener for the "View in browser" button. 
	 * I used the documentation to get started:
	 * https://docs.oracle.com/javase/7/docs/api/java/awt/Desktop.html
	 * @author Mehdi Himmiche
	 * @date Apr 17, 2016
	 */
	class browserListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Desktop desk = Desktop.getDesktop();
			try {
				desk.browse(new URL(url).toURI());
			} catch (IOException | URISyntaxException e1) {
				JOptionPane.showMessageDialog(null,
	        		    "There was an error attempting to load the webpage.",
	        		    "Browser Error",
	        		    JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Method that reads the URL for the JSON pull 
	 * I used the tutorial provided for lab 5 for this method. 
	 * @param webservice
	 * @return
	 * @throws Exception
	 */
	public static String readURL(String webservice) throws Exception 
	{	
		URL oracle = new URL(webservice);
		BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

		String inputLine;
		String result = "";

		while ((inputLine = in.readLine()) != null)
			result = result + inputLine;

		in.close();
		return result;
    }
	
}

	
	
