/* Class responsible for the model and the controller */

package guitagger;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.images.Artwork;

public class Tagger extends Observable{
	private AudioFile audioFile;
	private Tag tag;
	private HashMap<String,Field> fields;
	List<Image> artworkImageList;
	//private BufferedImage artwork;
	
	public Tagger(){
		fields = new HashMap<String,Field>();
		artworkImageList = new ArrayList<Image>();
		// initialise the fields
		fields.put("artist",new Field(FieldKey.ARTIST,null));
		fields.put("title",new Field(FieldKey.TITLE,null));
		fields.put("album",new Field(FieldKey.ALBUM,null));
		fields.put("year", new Field(FieldKey.YEAR,null));
		fields.put("genre", new Field(FieldKey.GENRE,null));
		fields.put("lyrics", new Field(FieldKey.LYRICS,null));
	}
		
	// updates the view with current info (Observer pattern)
	public void updateView(){
		setChanged();
		notifyObservers();	
	}
	
	// get audio file tag and refresh the view
	public void openAudioFile(String path) throws GuitaggerException{
		tag = getTag(path);
		storeTagFields(tag);
	}
	
	// get the tag from audio file
	private Tag getTag(String path) throws GuitaggerException{
		try{
			audioFile = AudioFileIO.read(new File(path));
			return audioFile.getTag();
		} catch(Exception e){
			throw new GuitaggerException("Unable to read file, the file is damaged or it's not an audio file");
		}
	}
	
	// Store the tag data into Tagger Field 
	private void storeTagFields(Tag tag) throws GuitaggerException{
		List<Artwork> artworkList =  tag.getArtworkList();
		for(Artwork artwork : artworkList ) {
			try {
				artworkImageList.add((Image) artwork.getImage());
			} catch (IOException e) {
				throw new GuitaggerException("artwork could not be loaded");
			}
		}
		
		for (Field field : fields.values()) {
			String value = tag.getFirst(field.getFieldKey());
		    field.setValue(value);
		}		
	}
	
	// get the key field ex:getField("artist")
	public String getField(String key){
		return fields.get(key).getValue();
	}
	
	// set the key field ex:setField("artist","Green Day")
	public void setField(String key, String newValue){
		Field field = fields.get(key);
		field.setValue(newValue);
	}
		
	public List<Image> getArtworks(){
		return this.artworkImageList;
	}
	
	/*public void setArtwork(BufferedImage artwork){
		
	}*/
	
	// commit the updates on the audio file
	public void applyChanges() throws GuitaggerException{
		Tag tag = audioFile.getTag();
		try{
			for (Field field : fields.values()) {
				tag.setField(field.getFieldKey(),field.getValue());
			}	
			
		} catch(Exception e){
			throw new GuitaggerException("Could not update ARTIST field");
		}
		try {
			audioFile.commit();
		} catch (CannotWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

// Responsible for handling the Tag fields (except artwork)
class Field{
	private FieldKey fieldKey;
	private String value;
	
	public Field(FieldKey fieldKey, String value){
		this.fieldKey = fieldKey;
		this.value = value;
	}
	
	public FieldKey getFieldKey(){
		return fieldKey;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
}
