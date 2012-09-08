package com.android.mms.ui;

import com.android.mms.R;
import com.android.mms.data.Conversation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.widget.Toast;
import android.app.NotificationManager;

public class QuickReader extends Activity implements OnDismissListener {

    private Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Attention!");
    	alert.setMessage("Did you want to mark as read this message?");
    	
    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                        setRead();
    			finish();
            }
        });
        
    	alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                      finish();
            }
        });
    	alert.show();
    }

    @Override
    public void onDestroy() {
        finish();
        super.onDestroy();
    }

    private void setRead() {
        Conversation.markAllConversationsAsSeen(getApplicationContext(),true);
        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(123);
        Toast.makeText(this, R.string.quick_reader, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }
}
