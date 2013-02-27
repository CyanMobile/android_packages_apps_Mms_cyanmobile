package com.android.mms.ui;

import com.android.mms.R;
import com.android.mms.data.Conversation;
import com.android.internal.util.EmojiParser;
import com.android.internal.util.SmileyParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.ContentUris;
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
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
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
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.CursorAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContactsEntity;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.text.InputFilter.LengthFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.Normalizer;

public class QuickReplyBox extends Activity implements OnClickListener {

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
    private AlertDialog mSmileyDialog;
    private AlertDialog mEmojiDialog;
    private View mEmojiView;
    private boolean mWaitingForSubActivity;
    private boolean enableEmojis;
    private boolean mNothing = true;
    private Handler mHandler = new Handler();
    private static final int REQUEST_CODE_INSERT_CONTACT_INFO = 1;
    private static final int MENU_INSERT_EMOJI          = 2;
    private static final int MENU_INSERT_CONTACT_INFO   = 3;
    private static final int MENU_INSERT_SMILEY         = 4;

    // InputFilter which attempts to substitute characters that cannot be
    // encoded in the limited GSM 03.38 character set. In many cases this will
    // prevent the keyboards auto-correction feature from inserting characters
    // that would switch the message from 7-bit GSM encoding (160 char limit)
    // to 16-bit Unicode encoding (70 char limit).
	
    private class StripUnicode implements InputFilter {

        private CharsetEncoder gsm =
            Charset.forName("gsm-03.38-2000").newEncoder();

        private Pattern diacritics =
            Pattern.compile("\\p{InCombiningDiacriticalMarks}");

        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            Boolean unfiltered = true;
            StringBuilder output = new StringBuilder(end - start);

            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
	
                // Character is encodable by GSM, skip filtering
                if (gsm.canEncode(c)) {
                    output.append(c);
                }
                // Character requires Unicode, try to replace it
                else {
                    unfiltered = false;
                    String s = String.valueOf(c);
	
                    // Try normalizing the character into Unicode NFKD form and
                    // stripping out diacritic mark characters.
                    s = Normalizer.normalize(s, Normalizer.Form.NFKD);
                    s = diacritics.matcher(s).replaceAll("");
	
                    // Special case characters that don't get stripped by the
                    // above technique.
                    s = s.replace("\u0152", "OE");
                    s = s.replace("\u0153", "oe");
                    s = s.replace("\u0141", "L");
                    s = s.replace("\u0142", "l");
                    s = s.replace("\u0110", "DJ");
                    s = s.replace("\u0111", "dj");
                    s = s.replace("\u0391", "A");
                    s = s.replace("\u0392", "B");
                    s = s.replace("\u0395", "E");
                    s = s.replace("\u0396", "Z");
                    s = s.replace("\u0397", "H");
                    s = s.replace("\u0399", "I");
                    s = s.replace("\u039a", "K");
                    s = s.replace("\u039c", "M");
                    s = s.replace("\u039d", "N");
                    s = s.replace("\u039f", "O");
                    s = s.replace("\u03a1", "P");
                    s = s.replace("\u03a4", "T");
                    s = s.replace("\u03a5", "Y");
                    s = s.replace("\u03a7", "X");
                    s = s.replace("\u03b1", "A");
                    s = s.replace("\u03b2", "B");
                    s = s.replace("\u03b3", "\u0393");
                    s = s.replace("\u03b4", "\u0394");
                    s = s.replace("\u03b5", "E");
                    s = s.replace("\u03b6", "Z");
                    s = s.replace("\u03b7", "H");
                    s = s.replace("\u03b8", "\u0398");
                    s = s.replace("\u03b9", "I");
                    s = s.replace("\u03ba", "K");
                    s = s.replace("\u03bb", "\u039b");
                    s = s.replace("\u03bc", "M");
                    s = s.replace("\u03bd", "N");
                    s = s.replace("\u03be", "\u039e");
                    s = s.replace("\u03bf", "O");
                    s = s.replace("\u03c0", "\u03a0");
                    s = s.replace("\u03c1", "P");
                    s = s.replace("\u03c3", "\u03a3");
                    s = s.replace("\u03c4", "T");
                    s = s.replace("\u03c5", "Y");
                    s = s.replace("\u03c6", "\u03a6");
                    s = s.replace("\u03c7", "X");
                    s = s.replace("\u03c8", "\u03a8");
                    s = s.replace("\u03c9", "\u03a9");
                    s = s.replace("\u03c2", "\u03a3");

                    output.append(s);
                }
            }
	
            // No changes were attempted, so don't return anything
            if (unfiltered) {
                return null;
            }
            // Source is a spanned string, so copy the spans from it
            else if (source instanceof Spanned) {
                SpannableString spannedoutput = new SpannableString(output);
                TextUtils.copySpansFrom(
                    (Spanned) source, start, end, null, spannedoutput, 0);

                return spannedoutput;
            }
            // Source is a vanilla charsequence, so return output as-is
            else {
                return output;
            }
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.quick_reply_box);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        boolean stripUnicode = prefs.getBoolean(MessagingPreferenceActivity.STRIP_UNICODE, false);
        enableEmojis = prefs.getBoolean(MessagingPreferenceActivity.ENABLE_EMOJIS, false);
        SmileyParser.init(this);
        EmojiParser.init(this);

        Bundle extras = getIntent().getExtras();
        avatar = (Bitmap) extras.get("avatars");
        mPhoneNumber = extras.getString("numbers");
        mContactName = extras.getString("names");
        mSmsMessage = extras.getString("inmessage");
        mSmsDateIn = extras.getString("indate");
        messageId = extras.getLong("id");
        contactIcon = (ImageView) findViewById(R.id.name_labelpicture);
        if (avatar != null) {
            contactIcon.setImageBitmap(avatar);
        }
        mNameLabel = (TextView) findViewById(R.id.name_label);
        mNameLabel.setText(mContactName);
        mSmsDate = (TextView) findViewById(R.id.smstimein);
        mSmsDate.setText(mSmsDateIn);
        mSmsBody = (TextView) findViewById(R.id.smsmessagein);
        mSmsBody.setText(replaceWithEmotes(mSmsMessage));
        mSendSmsButton = (Button) findViewById(R.id.send_sms_button);
        mSendSmsButton.setOnClickListener(this);
        mEditBox = (EditText) findViewById(R.id.edit_box);
        mEditBox.setOnClickListener(this);

        int maxTextLength = Integer.parseInt(prefs.getString("max_text_length", "2000"));
        LengthFilter lengthFilter = new LengthFilter(maxTextLength);

        if (stripUnicode) {
            mEditBox.setFilters(new InputFilter[] { new StripUnicode(), lengthFilter });
        } else {
            mEditBox.setFilters(new InputFilter[] { lengthFilter });
        }

        registerForContextMenu(mEditBox);
        setFinishOnTouchOutside(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        menu.add(0, MENU_INSERT_SMILEY, 0, R.string.menu_insert_smiley).setIcon(
                R.drawable.ic_menu_emoticons);

	if (enableEmojis) {
		menu.add(0, MENU_INSERT_EMOJI, 0, R.string.menu_insert_emoji).setIcon(
                R.drawable.ic_menu_sbm_emoji);
	}

        menu.add(0, MENU_INSERT_CONTACT_INFO, 0, R.string.menu_insert_contact)
            .setIcon(R.drawable.ic_menu_contact);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_INSERT_SMILEY:
                showSmileyDialog();
                break;
            case MENU_INSERT_EMOJI:
		showEmojiDialog();
		break;
            case MENU_INSERT_CONTACT_INFO:
                Intent intentInsertContactInfo = new Intent(Intent.ACTION_PICK,
                        Contacts.CONTENT_URI);
                startActivityForResult(intentInsertContactInfo, REQUEST_CODE_INSERT_CONTACT_INFO);
                break;
        }

        return true;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode)
    {
        // requestCode >= 0 means the activity in question is a sub-activity.
        if (requestCode >= 0) {
            mWaitingForSubActivity = true;
        }

        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mWaitingForSubActivity = false;          // We're back!

        if (resultCode != RESULT_OK){
            return;
        }

        switch(requestCode) {
            case REQUEST_CODE_INSERT_CONTACT_INFO:
                showInsertContactInfoDialog(data.getData());
                break;
            default:
                // TODO
                break;
        }
    }

    private CharSequence[] getContactInfoData(long contactId) {
        final String[] projection = new String[] {
            Data.DATA1, Data.DATA2, Data.DATA3, Data.MIMETYPE
        };
        final String where = Data.CONTACT_ID + "=? AND ("
                        + Data.MIMETYPE + "=? OR "
                        + Data.MIMETYPE + "=? OR "
                        + Data.MIMETYPE + "=? OR "
                        + Data.MIMETYPE + "=?)";
        final String[] whereArgs = new String[] {
            String.valueOf(contactId),
            CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
            CommonDataKinds.Website.CONTENT_ITEM_TYPE
        };

        final Cursor cursor = getContentResolver().query(Data.CONTENT_URI,
                projection, where, whereArgs, Data.MIMETYPE);

        if (cursor == null) {
            return null;
        }

        final int count = cursor.getCount();
        final int dataIndex = cursor.getColumnIndex(Data.DATA1);
        final int typeIndex = cursor.getColumnIndex(Data.DATA2);
        final int labelIndex = cursor.getColumnIndex(Data.DATA3);
        final int mimeTypeIndex = cursor.getColumnIndex(Data.MIMETYPE);

        if (count == 0) {
            cursor.close();
            return null;
        }

        final CharSequence[] entries = new CharSequence[count];

        for (int i = 0; i < count; i++) {
            cursor.moveToPosition(i);

            String data = cursor.getString(dataIndex);
            int type = cursor.getInt(typeIndex);
            String label = cursor.getString(labelIndex);
            String mimeType = cursor.getString(mimeTypeIndex);

            if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                entries[i] = Phone.getTypeLabel(getResources(), type, label) + ": " + data;
            } else if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                entries[i] = Email.getTypeLabel(getResources(), type, label) + ": " + data;
            } else if (mimeType.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
                entries[i] = StructuredPostal.getTypeLabel(getResources(), type, label)
                        + ": " + data;
            } else {
                entries[i] = data;
            }
        }

        cursor.close();

        return entries;
    }

    private void showInsertContactInfoDialog(Uri contactUri) {
        long contactId = -1;
        String displayName = null;

        final String[] projection = new String[] {
            Contacts._ID, Contacts.DISPLAY_NAME
        };
        final Cursor cursor = getContentResolver().query(contactUri,
                projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactId = cursor.getLong(0);
                displayName = cursor.getString(1);
            }
            cursor.close();
        }

        final CharSequence[] entries = (contactId >= 0) ? getContactInfoData(contactId) : null;

        if (contactId < 0 || entries == null) {
            Toast.makeText(this, R.string.cannot_find_contacts, Toast.LENGTH_SHORT).show();
            return;
        }

        final boolean[] itemsChecked = new boolean[entries.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_contact_picture);
        builder.setTitle(displayName);

        builder.setMultiChoiceItems(entries, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                itemsChecked[which] = isChecked;
            }
        });

        builder.setPositiveButton(R.string.insert_contact_info_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < entries.length; i++) {
                    if (itemsChecked[i]) {
                        int start = mEditBox.getSelectionStart();
                        int end = mEditBox.getSelectionEnd();
                        mEditBox.getText().replace(
                                Math.min(start, end), Math.max(start, end), entries[i] + "\n");
                    }
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        builder.show();
    }

    private CharSequence replaceWithEmotes(String body) {
        SpannableStringBuilder buf = new SpannableStringBuilder();

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

    private void showSmileyDialog() {
        if (mSmileyDialog == null) {
            int[] icons = SmileyParser.DEFAULT_SMILEY_RES_IDS;
            String[] names = getResources().getStringArray(
                    SmileyParser.DEFAULT_SMILEY_NAMES);
            final String[] texts = getResources().getStringArray(
                    SmileyParser.DEFAULT_SMILEY_TEXTS);

            final int N = names.length;

            List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
            for (int i = 0; i < N; i++) {
                // We might have different ASCII for the same icon, skip it if
                // the icon is already added.
                boolean added = false;
                for (int j = 0; j < i; j++) {
                    if (icons[i] == icons[j]) {
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    HashMap<String, Object> entry = new HashMap<String, Object>();

                    entry. put("icon", icons[i]);
                    entry. put("name", names[i]);
                    entry.put("text", texts[i]);

                    entries.add(entry);
                }
            }

            final SimpleAdapter a = new SimpleAdapter(
                    this,
                    entries,
                    R.layout.smiley_menu_item,
                    new String[] {"icon", "name", "text"},
                    new int[] {R.id.smiley_icon, R.id.smiley_name, R.id.smiley_text});
            SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView) {
                        Drawable img = getResources().getDrawable((Integer)data);
                        ((ImageView)view).setImageDrawable(img);
                        return true;
                    }
                    return false;
                }
            };
            a.setViewBinder(viewBinder);

            AlertDialog.Builder b = new AlertDialog.Builder(this);

            b.setTitle(getString(R.string.menu_insert_smiley));

            b.setCancelable(true);
            b.setAdapter(a, new DialogInterface.OnClickListener() {
                @Override
                @SuppressWarnings("unchecked")
                public final void onClick(DialogInterface dialog, int which) {
                    HashMap<String, Object> item = (HashMap<String, Object>) a.getItem(which);
                    mEditBox.append((String)item.get("text"));
                    dialog.dismiss();
                }
            });

            mSmileyDialog = b.create();
        }

        mSmileyDialog.show();
    }

    private void showEmojiDialog() {
        if (mEmojiDialog == null) {
            int[] icons = EmojiParser.DEFAULT_EMOJI_RES_IDS;

            int layout = R.layout.emoji_insert_view;
            mEmojiView = getLayoutInflater().inflate(layout, null);

            final GridView gridView = (GridView) mEmojiView.findViewById(R.id.emoji_grid_view);
            gridView.setAdapter(new ImageAdapter(this, icons));
            final EditText editText = (EditText) mEmojiView.findViewById(R.id.emoji_edit_text);
            final Button button = (Button) mEmojiView.findViewById(R.id.emoji_button);

            gridView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    // We use the new unified Unicode 6.1 emoji code points
                    CharSequence emoji = EmojiParser.getInstance().addEmojiSpans(EmojiParser.mEmojiTexts[position]);
                    editText.append(emoji);
                }
            });

            gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                        long id) {
                    // We use the new unified Unicode 6.1 emoji code points
                    CharSequence emoji = EmojiParser.getInstance().addEmojiSpans(EmojiParser.mEmojiTexts[position]);
                    mEditBox.append(emoji);
                    mEmojiDialog.dismiss();
                    return true;
                }
            });

            button.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditBox.append(editText.getText());
                    mEmojiDialog.dismiss();
                }
            });

            AlertDialog.Builder b = new AlertDialog.Builder(this);

            b.setTitle(getString(R.string.menu_insert_emoji));

            b.setCancelable(true);
            b.setView(mEmojiView);

            mEmojiDialog = b.create();
        }

        final EditText editText = (EditText) mEmojiView.findViewById(R.id.emoji_edit_text);
        editText.setText("");

        mEmojiDialog.show();
    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (v == mSendSmsButton) {
            if (keysAreShowing) {
                imm.hideSoftInputFromWindow(mEditBox.getWindowToken(), 0);
                keysAreShowing = false;
            }
            sendSms();
        } else if (v == mEditBox) {
            imm.showSoftInput(mEditBox, 0);
            keysAreShowing = true;
            mEditBox.requestFocus();
        }
    }

    private void sendSms() {
        mNothing = false;
        String mMessage = mEditBox.getText().toString();
        if (mMessage == null || TextUtils.isEmpty(mMessage)) {
            Toast.makeText(this, R.string.quick_reply_not_sending, Toast.LENGTH_SHORT).show();
            return;
        }
        SmsManager sms = SmsManager.getDefault();
        try {
            sms.sendTextMessage(mPhoneNumber, null, mMessage, null, null);
        } catch (IllegalArgumentException e) {
        }
        setRead();
        addMessageToSent(mMessage);
        Toast.makeText(this, R.string.quick_reply_sending, Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               setFinish();
            }
        }, 300);
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
    public void onBackPressed() {
        setFinish();
    }

    private void setFinish() {
        if (mNothing) {
            setOnDismiss();
        } else {
            finish();
        }
    }

    private void setOnDismiss() {
        mNothing = false;
        //re-did the dismiss to allow you to stay, close, or mark as read.
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	alert.setTitle("Nothing Has Been Sent");
    	alert.setMessage("Did you want to cancel this message?");
    	
    	alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	setFinish();
            }
        });
    	
    	alert.setNegativeButton("Mark Read", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			setRead();
    			setFinish();
            }
    	});
    	
    	alert.show();
    }
}
