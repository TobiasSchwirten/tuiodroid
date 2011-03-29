/*
 TUIOdroid http://www.tuio.org/
 An Open Source TUIO Tracker for Android
 (c) 2011 by Tobias Schwirten and Martin Kaltenbrunner
 
 TUIOdroid is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 TUIOdroid is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with TUIOdroid.  If not, see <http://www.gnu.org/licenses/>.
*/

package tuioDroid.osc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;


/**
 * Represents OSC connection
 * @author Tobias Schwirten
 * @author Martin Kaltenbrunner
 */
public class OSCInterface {
	
	/**
	 * IP address of the OSC receiver
	 */
	private InetAddress inetAdress;
	
	/**
	 * Port on which the OSC receiver listens to 
	 */
	private int port;
	
	/**
	 * The OSC out port for this OSC Interface
	 */
	private OSCPortOut sender;

	
	
	/**
	 * Default
	 */
	public OSCInterface(){
		
	}
	
	/**
	 * Constructor
	 */
	public OSCInterface(String inetAddress, int port){
		
		try {
			this.inetAdress = InetAddress.getByName(inetAddress);
			this.port = port;
			sender = new OSCPortOut(inetAdress, port);
			
			System.out.println("*********OSC Interface connected to: " + this.port + "  " + this.inetAdress);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Sends list of OSC messages as OSC bundles
	 * @param oscMessagesList List of OSC messages
	 */
	public void sendMessage(List <OSCMessage> oscMessagesList){

         OSCBundle bundle = new OSCBundle(listToArray(oscMessagesList));

         try {
			sender.send(bundle);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	
	/**
	 * Sends a single OSC message 
	 * @param oscMessage
	 */
	public void sendSingleMessage(OSCMessage oscMessage){

		try {
			sender.send(oscMessage); 
			printOSCData(oscMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
  
	}
	
	
	/**
	 * Sends OSC bundle
	 * @param bundle
	 */
	public void sendOSCBundle (OSCBundle bundle){
	
		try {
			sender.send(bundle);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	/**
	 * Prints out OSC data 
	 * @param oscMessage
	 */
	public void printOSCData(OSCMessage oscMessage){
	
		
		Object [] args = oscMessage.getArguments();

		
		for (int i=0; i<args.length; i++){
			System.out.print(args[i] + " / ");
		}

		System.out.println();

	}
	
	
	

	/**
	 * Converts List of OSC Message into an array of OSC Messages
	 * @param oscMessagesList
	 * @return 
	 */
	public OSCMessage[] listToArray (List <OSCMessage> oscMessagesList){
		
		OSCMessage [] oscMessagesArray = new OSCMessage [oscMessagesList.size()];
		
		for (int i=0; i<oscMessagesList.size(); i++){
			oscMessagesArray[i]=oscMessagesList.get(i);
		}
		
		return oscMessagesArray;

	}


	
	public String getInetAdress() {
		return inetAdress.getHostAddress();
	}


	public int getPort() {
		return port;
	}
	
	
	public void closeInteface (){
		this.sender.close();
	}

	
	
}
