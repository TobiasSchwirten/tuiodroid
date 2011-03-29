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
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * Activity that provides Settings for the user
 * @author Tobias Schwirten
 * @author Martin Kaltenbrunner
 */
public class SettingsActivity extends Activity{


	/**
	 *  Called when the activity is first created. 
	 */
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.settingslayout);
	        
	        Button btn_OK = (Button)findViewById(R.id.button);
	        btn_OK.setOnClickListener(listener_OkBtn);

	        EditText editText_IP = (EditText)findViewById(R.id.et_IP);
	        String ip = (getIntent().getExtras().getString("IP_in"));
	        editText_IP.setText(ip);
	        
	        EditText editText_port = (EditText)findViewById(R.id.et_Port);
	        int port = getIntent().getExtras().getInt("Port_in");
	        editText_port.setText(Integer.toString(port));
	        
	        CheckBox checker_Info = (CheckBox)findViewById(R.id.checkB_ExtraInfo);
	        boolean drawAdditionalInfo = getIntent().getExtras().getBoolean("ExtraInfo");
	        checker_Info.setChecked(drawAdditionalInfo);

	        Spinner spinner = (Spinner) findViewById(R.id.spinner);
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.orientation_array, android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        
	        spinner.setAdapter(adapter);
	        spinner.setSelection(getIntent().getExtras().getInt("ScreenOrientation"));
	       
	     
	    }

	 
	 
	 
	 
	 /**
	  * Listener for the OK button
	  */
	 private OnClickListener listener_OkBtn = new OnClickListener(){
	        
		  public void onClick(View v){                        
	           Intent responseIntent = new Intent();
	           
	           responseIntent.putExtra("IP",((TextView) findViewById(R.id.et_IP)).getText().toString());
	           responseIntent.putExtra("Port", ((TextView) findViewById(R.id.et_Port)).getText().toString());
	           responseIntent.putExtra("ExtraInfo", ((CheckBox)findViewById(R.id.checkB_ExtraInfo)).isChecked());
	           responseIntent.putExtra("ScreenOrientation",  ((Spinner) (findViewById(R.id.spinner))).getSelectedItemPosition());

	          
	           /*Setting result for this activity */
	           setResult(RESULT_OK, responseIntent);
	           
	           finish();
	        }
	    };    


	 
}
