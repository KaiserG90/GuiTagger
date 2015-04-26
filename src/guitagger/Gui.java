/* Class responsible for the view */

package guitagger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Image;

import javax.swing.ImageIcon;

import org.jaudiotagger.tag.reference.GenreTypes;
public class Gui{
	private mainFrame mainFrame;
	
	public Gui(){
		this.mainFrame = new mainFrame();
		mainFrame.build();
	}
	
	// file has been selected in filechooser
	public void openAudioFile(File file) {
		new FileEditor(file.getAbsolutePath());
	}
	
	// MainContainer is the frame containing the file chooser panel and the menu
	// it is notified when a file has been selected in the filechooser
	private class mainFrame extends JFrame implements Observer{
		private JFrame mainFrame;
		private final String TITLE = "GuiTagger";
		private final Dimension PREFERRED_SIZE;
		
		public mainFrame(){
			PREFERRED_SIZE = new Dimension(500,500);
		}
		
		// add the menu bar and filechooser to the frame
		private JFrame build(){
			mainFrame = new JFrame(TITLE);
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			mainFrame.setLayout(new BorderLayout());
			mainFrame.setSize(PREFERRED_SIZE);
			mainFrame.setMinimumSize(PREFERRED_SIZE);
			mainFrame.add(buildMenuBar(),BorderLayout.NORTH);
			mainFrame.add(buildFileChooserPanel());
			mainFrame.setVisible( true );
			mainFrame.validate();
			
			return mainFrame;
		}
		
		// create the menu bar
		private JMenuBar buildMenuBar(){
			JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
			menuBar.add(menu);
			return menuBar;
		}
		
		// build the file chooser
		private JPanel buildFileChooserPanel(){
			FileChooser fc = new FileChooser();
			fc.addObserver(this); // used to be notified when file has been selected
			return fc.buildPanel();
		}
	
		// a file has been selected in filechooser
		public void update(Observable o, Object arg) {
			openAudioFile((File)arg);
		}
	}
	
	// FileEditor is used to show and modify the audio file metadata in a frame 
	private class FileEditor implements Observer{
		private Tagger tagger;
		private GeneralInfo generalInfoTab;
		private Artwork artworkTab;
		private Lyrics lyricsTab;
		private JFrame fileEditorFrame;

		private final Dimension PREFERRED_SIZE = new Dimension(400,400);
		static final int TEXT_FIELD_LENGTH = 20;
		
		// create a new tagger and let it open the file at filepath
		public FileEditor(String filePath){
			tagger = new Tagger();
			tagger.addObserver(this);
			try {
				tagger.openAudioFile(filePath);
				buildFileEditorFrame();
				refresh(); // populate the frame just created
			} catch (GuitaggerException e) {
				e.showDialog();
			}
		}
		
		public void buildFileEditorFrame(){
			// remove the ugly bottom border of JTabbedPane
			UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(1,0,0,0));
			UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
			fileEditorFrame = new JFrame();
			fileEditorFrame.setSize(PREFERRED_SIZE);
			fileEditorFrame.setMinimumSize(PREFERRED_SIZE);
			JTabbedPane fileEditor = new JTabbedPane();
			
			// Add general info tab to the File editor
			generalInfoTab = new GeneralInfo();
			fileEditor.add("Info",generalInfoTab.buildTab());
			// Add artwork tab to the File editor
			artworkTab = new Artwork();
			fileEditor.add("Artwork",artworkTab.buildTab());
			// Add lyrics tab to the File editor
			lyricsTab = new Lyrics();
			fileEditor.add("Lyrics",lyricsTab.buildTab());
			// Add button to save changes
			JButton saveBtn = new JButton("Save");
			saveBtn.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					save();
				}
			});
			
			fileEditorFrame.add(fileEditor, BorderLayout.NORTH);
			// southpanel contains save button
			JPanel southPanel = new JPanel();
			southPanel.setLayout(new BorderLayout());
			southPanel.setBorder(new EmptyBorder( 20, 20, 20, 20 ));
			southPanel.add(saveBtn,BorderLayout.EAST);
			fileEditorFrame.add(southPanel, BorderLayout.SOUTH);
			
			fileEditorFrame.validate();
			fileEditorFrame.setVisible(true);
		}
		
		// refresh file editor tabs
		public void refresh(){
			fileEditorFrame.setTitle(tagger.getField("title")+" - "+tagger.getField("artist"));
			generalInfoTab.refresh();
			artworkTab.refresh();
			lyricsTab.refresh();
		}
		
		// Save changes made to file metadata
		public void save(){
			generalInfoTab.save();
			artworkTab.save();
			lyricsTab.save();
			try {
				tagger.applyChanges();
			} catch (GuitaggerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// general info panel
		private class GeneralInfo{
			private final Pattern GENRE_PATTERN = Pattern.compile("\\((\\d+)\\).*"); 
			//general info fields
			private JLabel titleLabel;
			private JTextField titleTextField;
			private JLabel artistLabel;
			private JTextField artistTextField;
			private JLabel albumLabel;
			private JTextField albumTextField;
			private JLabel yearLabel;
			private JTextField yearTextField;
			private JLabel genreLabel;
			private JComboBox genreComboBox;
			
			public GeneralInfo(){
				titleLabel = new JLabel("Title");
				titleTextField = new JTextField(TEXT_FIELD_LENGTH);
				artistLabel = new JLabel("Artist");
				artistTextField = new JTextField(TEXT_FIELD_LENGTH);
				albumLabel = new JLabel("Album");
				albumTextField = new JTextField(TEXT_FIELD_LENGTH);
				yearLabel = new JLabel("Year");
				yearTextField = new JTextField(TEXT_FIELD_LENGTH);
				genreLabel = new JLabel("Genre");
				Set<String> genresSet = GenreTypes.getInstanceOf().getValueToIdMap().keySet(); // get all the genres in a set
				String[] genres = genresSet.toArray(new String[genresSet.size()]);
				genreComboBox = new JComboBox<String>(genres);
			}
			
			private String mapGenre(String genre){
				  if (genre == null) {
				    return null;
				  }
				  Matcher matcher=GENRE_PATTERN.matcher(genre);
				  if (matcher.matches()) {
				    int genreId=Integer.parseInt(matcher.group(1));
				    if (genreId >= 0 && genreId < GenreTypes.getInstanceOf().getSize()) {
				      return GenreTypes.getInstanceOf().getValueForId(genreId);
				    }
				  }
				  return genre;
				}
			
			// tab that shows general track info (title,artist...)
			private JPanel buildTab(){		
				JPanel generalInfo = new JPanel();
				generalInfo.setLayout(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;
				//c.anchor = GridBagConstraints.PAGE_START;
				c.weightx = 0.0;
				c.weighty = 0.0;
				c.insets = new Insets(5,10,5,10);
				
				c.gridx = 0;
				c.gridy = 0;
				generalInfo.add(titleLabel,c);
				
				c.gridx=1;
				c.weightx = 0.5;
				generalInfo.add(titleTextField,c);
				
				c.weightx = 0.0;
				c.gridy++;
				c.gridx=0;
				generalInfo.add(artistLabel,c);
				
				c.weightx = 0.5;
				c.gridx=1;
				generalInfo.add(artistTextField,c);
				c.gridy++;
				
				c.weightx = 0.0;
				c.gridx=0;
				generalInfo.add(albumLabel,c);
				
				c.weightx = 0.5;
				c.gridx=1;
				generalInfo.add(albumTextField,c);
				
				c.weightx = 0.0;
				c.gridy++;
				c.gridx=0;
				generalInfo.add(genreLabel,c);
				
				c.weightx = 0.5;
				c.gridx=1;
				generalInfo.add(genreComboBox,c);
				
				c.weightx = 0.0;
				c.gridy++;
				c.gridx=0;
				generalInfo.add(yearLabel,c);
				
				c.weightx = 0.5;
				c.gridx=1;
				generalInfo.add(yearTextField,c);
				
				c.fill = GridBagConstraints.NONE;
				c.gridx=1;
				c.weightx = 0.0;

				c.gridy++;
				
				c.anchor = GridBagConstraints.PAGE_END;
				// button to update the general info (artist, title...)
				return generalInfo;
			}
			
			// update the taggers general info
			private void save(){
				tagger.setField("title", titleTextField.getText());
				tagger.setField("artist", artistTextField.getText());
				tagger.setField("album", albumTextField.getText());
				tagger.setField("year", yearTextField.getText());
				tagger.setField("genre", (String) genreComboBox.getSelectedItem());
			}
			
			// update the gui fields with tagger ones
			private void refresh(){
				artistTextField.setText(tagger.getField("artist"));
				titleTextField.setText(tagger.getField("title"));
				albumTextField.setText(tagger.getField("album"));
				yearTextField.setText(tagger.getField("year"));
				System.out.println(mapGenre(tagger.getField("genre")));
				genreComboBox.setSelectedItem(tagger.getField("genre"));
			}	
		}
		
		// artwork panel
		private class Artwork{
			private JPanel artworkPanel = new JPanel();
			private List<Image> images;
			
			public JPanel buildTab(){
				JPanel artwork = new JPanel();
				artwork.add(artworkPanel);
				return artwork;
			}
			
			private void refresh(){
				images = tagger.getArtworks();
				// display first artwork if any
				if(images.size()>0){
					Image firstImage = images.get(0);
					ImageIcon imageIcon = new ImageIcon(firstImage); // load the image to a imageIcon
					Image image = imageIcon.getImage(); // transform it 
					Image newimg = image.getScaledInstance(200, 200,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
					imageIcon = new ImageIcon(newimg);  // transform it back
					artworkPanel.setLayout(new BorderLayout());
					artworkPanel.add(new JLabel(imageIcon),BorderLayout.SOUTH);
				}
			}
			
			private void save(){
				
			}
		}
		
		// lyrics panel
		private class Lyrics{
			private JPanel lyricsPanel;
			private JTextArea lyricsTextArea = new JTextArea();
			
			public JPanel buildTab(){
				lyricsPanel = new JPanel();
				lyricsPanel.setLayout(new BorderLayout());
				//lyricsTextArea.setBackground(mainContainer.getBackground());
				lyricsTextArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
				lyricsPanel.add(lyricsTextArea);
				return lyricsPanel;
			}
			
			private void refresh(){
				lyricsTextArea.setText(tagger.getField("lyrics"));
			}
			
			private void save(){
				tagger.setField("lyrics",lyricsTextArea.getText());
			}
		}
	
		// update is called by the Tagger to update the GUI
		public void update(Observable obs, Object obj){
			// update what's been displaying in the file editor
			refresh();
		}
	}
	
}

// Responsible for building the jfilechooser and handle the file selection
class FileChooser extends Observable{
	private JPanel fileChooserPanel;
	private File directory; // path of where file chooser will be open
	
	public FileChooser(){
		this.fileChooserPanel = new JPanel();
	}
	
	// build the file chooser panel
	public JPanel buildPanel(){
		JFileChooser fc = new JFileChooser();
		if(directory!=null){
			fc.setCurrentDirectory(directory);
		}
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "Audio", "mp3", "mp4","m4a","m4p","flac","ogg","wav","wave","wma");
		fc.setFileFilter(filter);
		fileChooserPanel.add(fc);
		handleFileChooser(fc); // handle the approve and cancel selection
		return fileChooserPanel;
	}
	
	// handle the file selection
	private void handleFileChooser(JFileChooser fc) {
		fc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
					setChanged();
					notifyObservers(fc.getSelectedFile());	
				}
				else if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
					System.exit(0);
				}
			} 
		});
	  }
}
