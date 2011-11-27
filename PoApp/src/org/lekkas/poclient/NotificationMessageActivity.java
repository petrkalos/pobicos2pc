/*package org.lekkas.poclient;

public class NotificationMessageActivity extends Activity {
	private static final String TAG = "NotificationMessageActivity";
	private String msg;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);
        Intent intent = getIntent();
        if(intent.hasExtra("message_data"))
        	msg = intent.getStringExtra("message_data");
	}
	@Override
    protected void onStart() {
    	super.onStart();
    	System.out.println(TAG+ "Message Activity Created");
    	showMessage();
    }
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    @Override 
    public void onStop(){
    	System.out.println(TAG+ "OnStop()");
    	super.onStop();
    }

    public void showMessage() {
    	final AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	final TextView text = new TextView(this);
    	text.setText(msg);
    	alert.setView(text);

		alert.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		});
		alert.show();
    }
}
*/