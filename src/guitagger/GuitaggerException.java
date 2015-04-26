package guitagger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GuitaggerException extends Exception{
	private String message;
	
	public GuitaggerException(String message){
		super(message);
		System.out.println(message);
		this.message = message;
	}
	
	// open a new dialog window
	public void showDialog(){
		new ErrorDialog(this.message);
	}
}

//class to handle the error dialogs 
class ErrorDialog {
	public ErrorDialog(String message){
		JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
	}
}