package com.android.mms.ui;

import com.android.mms.R;
import com.android.mms.data.Conversation;
import com.android.mms.util.EmojiParser;
import com.android.mms.util.SmileyParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ContentValues;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class QuickReplyBox extends Activity implements OnDismissListener, OnClickListener {

    private Bitmap avatar;
    private boolean keysAreShowing;
    private ImageView contactIcon;
    private TextView mNameLabel;
    private TextView mSmsBody;
    private TextView mSmsDate;
    private String mPhoneNumber;
    private String mContactName;
    private String mSmsMessage;
    private String mSmsDateIn;
    private Button mSendSmsButton;
    private EditText mEditBox;
    private Context mContext;
    private long messageId;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        LayoutInflater inflater = LayoutInflater.from(this);
        final View mView = inflater.inflate(R.layout.quick_reply_box, null);
        AlertDialog alert = new AlertDialog.Builder(this).setView(mView).create();

        Bundle extras = getIntent().getExtras();
        avatar = (Bitmap) extras.get("avatars");
        mPhoneNumber = extras.getString("numbers");
        mContactName = extras.getString("names");
        mSmsMessage = extras.getString("inmessage");
        mSmsDateIn = extras.getString("indate");
        messageId = extras.getLong("id");
        contactIcon = (ImageView) mView.findViewById(R.id.name_labelpicture);
        if (avatar != null) {
            contactIcon.setImageBitmap(avatar);
        }
        mNameLabel = (TextView) mView.findViewById(R.id.name_label);
        mNameLabel.setText(mContactName);
        mSmsDate = (TextView) mView.findViewById(R.id.smstimein);
        mSmsDate.setText(mSmsDateIn);
        mSmsBody = (TextView) mView.findViewById(R.id.smsmessagein);
        mSmsBody.setText(replaceWithEmotes(mSmsMessage));
        mSendSmsButton = (Button) mView.findViewById(R.id.send_sms_button);
        mSendSmsButton.setOnClickListener(this);
        mEditBox = (EditText) mView.findViewById(R.id.edit_box);
        mEditBox.setOnClickListener(this);
        alert.setOnDismissListener(this);
        alert.show();
    }

    @Override
    public void onDestroy() {
        finish();
        super.onDestroy();
    }

    private CharSequence replaceWithEmotes(String body) {
        SpannableStringBuilder buf = new SpannableStringBuilder();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        boolean enableEmojis = prefs.getBoolean(MessagingPreferenceActivity.ENABLE_EMOJIS, false);

        if (!TextUtils.isEmpty(body)) {
            SmileyParser parser = SmileyParser.getInstance();
            CharSequence smileyBody = parser.addSmileySpans(body);
            if (enableEmojis) {
                EmojiParser emojiParser = EmojiParser.getInstance();
                smileyBody = emojiParser.addEmojiSpans(smileyBody);
            }
            buf.append(smileyBody);
        }
        return buf;
    }

    @Override
    public void onClick(View v) {
        Handler h = new Handler();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (v == mSendSmsButton) {
            if (keysAreShowing) {
                imm.hideSoftInputFromWindow(mEditBox.getWindowToken(), 0);
                keysAreShowing = false;
            }
            sendSms();
            Toast.makeText(this, R.string.quick_reply_sending, Toast.LENGTH_SHORT).show();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                  finish();
                }
            }, 300);
        } else if (v == mEditBox) {
            imm.showSoftInput(mEditBox, 0);
            keysAreShowing = true;
            mEditBox.requestFocus();
        }
    }

    private void sendSms() {
        String mMessage = null;
        mMessage = mEditBox.getText().toString();
        SmsManager sms = SmsManager.getDefault();
        try {
            sms.sendTextMessage(mPhoneNumber, null, mMessage, null, null);
        } catch (IllegalArgumentException e) {
        }
        setRead();
        addMessageToSent(mMessage);
    }

    private void setRead() {
        Conversation.markAllConversationsAsSeen(getApplicationContext(), true);
        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(123);
    }

    private void addMessageToSent(String messageSent) {
        ContentValues sentSms = new ContentValues();
        sentSms.put("address", mPhoneNumber);
        sentSms.put("body", messageSent);
        getContentResolver().insert(Uri.parse("content://sms/sent"), sentSms);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //re-did the dismiss to allow you to stay, close, or mark as read.
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Nothing Has Been Sent");
    	alert.setMessage("Did you want to cancel this message?");
    	
    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	finish();
            }
        });
        
    	alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	LayoutInflater inflater = LayoutInflater.from(QuickReplyBox.this);
                final View mView = inflater.inflate(R.layout.quick_reply_box, null);
                AlertDialog alert = new AlertDialog.Builder(QuickReplyBox.this).setView(mView).create();

                contactIcon = (ImageView) mView.findViewById(R.id.name_labelpicture);
                if (avatar != null) {
                    contactIcon.setImageBitmap(avatar);
                }
                mNameLabel = (TextView) mView.findViewById(R.id.name_label);
                mNameLabel.setText(mContactName);
                mSmsDate = (TextView) mView.findViewById(R.id.smstimein);
                mSmsDate.setText(mSmsDateIn);
                mSmsBody = (TextView) mView.findViewById(R.id.smsmessagein);
                mSmsBody.setText(replaceWithEmotes(mSmsMessage));
                mSendSmsButton = (Button) mView.findViewById(R.id.send_sms_button);
                mSendSmsButton.setOnClickListener(QuickReplyBox.this);
                mEditBox = (EditText) mView.findViewById(R.id.edit_box);
                mEditBox.setOnClickListener(QuickReplyBox.this);
                alert.setOnDismissListener(QuickReplyBox.this);
                alert.show();
            }
        });
    	
    	alert.setNeutralButton("Mark Read", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			setRead();
    			finish();
            }
    	});
    	
    	alert.show();
    }
}
