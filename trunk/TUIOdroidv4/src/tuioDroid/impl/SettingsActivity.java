package tuioDroid.impl;

import tuioDroid.impl.R;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;


/**
 * Activity that provides Settings for the user
 * @author T.Schwirten
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
	           
	           /*Setting result for this activity */
	           setResult(RESULT_OK, responseIntent);
	           
	           finish();
	        }
	    };    


	 
}
