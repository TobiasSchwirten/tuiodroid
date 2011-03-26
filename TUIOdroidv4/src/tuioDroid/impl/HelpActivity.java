package tuioDroid.impl;

import tuioDroid.impl.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


/**
 * Activity that provides a help screen for the user
 * @author T.Schwirten
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
	        String helperText = "This app sends TUIO data." +
	        					"\nRight now there are acceleration and movement always set to zero." +
	        					"\nYou can change IP and port under Settings." +
	        					"\nver: .06 " +
	        					"\nBy: Tobi S.";
	        textView_Help.setText(helperText);
	    }


}
