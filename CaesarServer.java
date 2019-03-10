/** 
	
Description: This server is used with the Network Programming homework
				 assignment. It will communicate with the client.

				The client will first jbSend a command to the server. This
				command is ENCRYPT, DECRYPT, or ERROR.
			
				If a valid command is sent, the text will be Encrypted or 
				Decrypted by the server and the text returned.   
            
							
Date: Nov 26, 2018
Author:  Pranav Jain		

*/


import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class CaesarServer extends Application implements CaesarConstants, EventHandler<ActionEvent> {
 
private VBox root = new VBox(8);
private Stage stage;
private Scene scene;
private Button btnStart = new Button("Start");
private TextField shift = new TextField();
private ServerSocket socket = null;
private static byte shiftValue = DEFAULT_SHIFT;
private byte shiftVal;
	
   /** main  */
public static void main(String [] args){
      launch(args);
 }
   
   /** constructor */
   public void start(Stage _stage) {
      
      stage = _stage;
      stage.setTitle("Caesar Cipher Server");
      stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
         public void handle(WindowEvent evt) { 
         System.exit(0); 
        }
    });
      
      //  shift value and button
      FlowPane fpRow = new FlowPane(8,8);
      fpRow.getChildren().addAll(new Label("Shift: "), shift, btnStart);
      btnStart.setOnAction(this);
      root.getChildren().add(fpRow);

      // setting the scene and show the stage
      scene = new Scene(root);
      stage.setScene(scene);
      stage.show();
      
      // Set the default shift amount
		shiftVal = DEFAULT_SHIFT;
      shift.setText("" + DEFAULT_SHIFT);
      
	} // constructor
   
  public void handle(ActionEvent ae) {
      
    String label = ((Button)ae.getSource()).getText();
    switch(label) {
      case "Start": start();
                    break;
      case "Stop":  stop();
                    break;
      }
   }
	
	/** start ... starting a server thread to wait for connections */
	public void start() {
      try {
         shiftVal = (byte) Integer.parseInt(shift.getText());
         if(shiftVal < 1 || shiftVal > 25)

            System.out.println("Enter number between 1 and 25 inclusive");
         //else {  System.exit(1); }
      }
      catch(NumberFormatException nfe) {
       System.out.println("Enter numbers only " +nfe);
       
       }
      
      shift.setEditable(false);
      btnStart.setText("Stop");

      Thread t = new ServerThread();
      t.start();
   }
   
   /** Class for the thread for the server to listen for new clients */
   class ServerThread extends Thread {
      public void run() {
   		try{
            shiftValue = (byte) Integer.parseInt(shift.getText());
   			socket = new ServerSocket( PORT_NUMBER );
   			
   			try {
               while(true)
   				new CaesarThread( socket.accept() ).start();	// Waiting for the client connection
            }

            catch(Exception e) {
               
            }
            Platform.runLater(new Runnable() {
               public void run() {
                  btnStart.setText("Start");
                  shift.setEditable(true);
               }
            } );
            return;
   		}

   		catch(IOException ioe) {System.exit(1);}
         catch(Exception e) { System.exit(1);}
      }
	} 
   
   /** stop - stoping the server thread by closing the server socket
   */
   
   public void stop() {
      try {
         socket.close();
      }
      catch(Exception e) {

      }
	} 
   
	/** 
		CaesarThread - processes one request from a client
	*/
	public class CaesarThread extends Thread {
	
		private Socket client;
      private byte myShift;
      private String test_client;
		
		// constructor
		public CaesarThread( Socket me ) {
         myShift = shiftValue;
			client = me;
         test_client = me.getInetAddress().getHostName() + ":" + me.getPort();
		}
		
		// run
  public void run() {
			
         boolean encrypt = true;				
		
      	byte diff = 'a'-'A';					// convert lower back to upper case
			String message; 					// encrypt / decrypt
			
         Scanner scn = null;              // read from the client
			PrintWriter pw = null;				// Write to the client
			
         try{
				scn = new Scanner(new InputStreamReader(client.getInputStream()));
							
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream() )));
			}
			catch(Exception e){	}
			
			message = scn.nextLine();		

			if( message != null && (
            (encrypt = message.equalsIgnoreCase("ENCRYPT") ) || message.equalsIgnoreCase("DECRYPT") ) ) {
				
            pw.println("OK");
				pw.flush();
			}
			else
			{
				pw.println("ERROR");
				pw.flush();
				return;					
     }

			while( scn.hasNextLine()) {
            message = scn.nextLine();   					

            byte [] temp = message.toLowerCase().getBytes();

				// Looking at each character in the message
				for( int i=0; i<temp.length; i++ ){
					byte ch = temp[i];

					if( ch>='a' && ch<='z' ){	

						if( encrypt )			
							temp[i]=(byte)(((ch-'a')+myShift)%26 + 'a');
						else // (decrypt)
							temp[i]=(byte)((((ch-'a')-myShift+26)%26)+ 'a');
					}

					if( Character.isUpperCase( message.charAt(i)) )
						temp[i]-=('a'-'A');
				} 
				
				String returnMessage = new String( temp );
				pw.println( returnMessage );
				pw.flush();
			} 
         
		} // end run()
	} // end CaesarThread
}//end CaesarServer