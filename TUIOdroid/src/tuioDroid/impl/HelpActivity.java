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

package tuioDroid.impl;

import tuioDroid.impl.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


/**
 * Activity that provides a help screen for the user
 * @author Tobias Schwirten
 * @author Martin Kaltenbrunner
 */
public class HelpActivity extends Activity{
	
	/**
	 *  Called when the activity is first created. 
	 */
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.helplayout);
	        
	        TextView textView_Help = (TextView)findViewById(R.id.textHelp);
	        String helperText = "TUIOdroid Version 0.7 " +
			"\n(c) by Tobias Schwirten & Martin Kaltenbrunner" +
			"\n" +
			"\nThis Application sends multitouch events via TUIO/UDP, " +
	        "you can change the target IP address and port number in the settings dialog" +
	        "\n" +
	        "\nYou can find further information about the TUIO protocol and framework at TUIO.org";
	        					
	        textView_Help.setText(helperText);
	    }
}
