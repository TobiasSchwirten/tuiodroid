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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.text.util.Linkify;
import java.util.regex.Pattern;

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
	        
	        try {
	        	String appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; 
	        	TextView textView_Version = (TextView)findViewById(R.id.textVersion);
	        	textView_Version.setText("v"+appVersion);
	        } catch (Exception e) {}
	        
	        TextView textView_Link = (TextView)findViewById(R.id.textHelp);
	        Pattern pattern = Pattern.compile("TUIO.org");
	        Linkify.addLinks(textView_Link, pattern, "http://");
	        
	        Button btn_OK = (Button)findViewById(R.id.okButton);
	        btn_OK.setOnClickListener(listener_OkBtn);
	    }
	 
	 /**
	  * Listener for the OK button
	  */
	 private OnClickListener listener_OkBtn = new OnClickListener(){
	        
		  public void onClick(View v){                        	           
	           finish();
	        }
	    };
}
