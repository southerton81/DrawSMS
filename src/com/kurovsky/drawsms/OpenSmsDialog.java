package com.kurovsky.drawsms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OpenSmsDialog {
    Activity mActivity;
    String[] mSMSList;
    boolean mSMSListValid;
    SMSContentsListener mListener;
    
    public interface SMSContentsListener {
        void SMSContentsRead(String contents, Activity activity);
    }
          
    public OpenSmsDialog(Activity activity) {
        mActivity = activity;
        mSMSListValid = LoadSMSList();

        if (!mSMSListValid) {
            Toast.makeText(mActivity, mActivity.getString(R.string.opensmsnotsupported_label), Toast.LENGTH_LONG).show();
        }
    }
    
    public Dialog CreateSmsDialog() {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getString(R.string.view_sms_label));

        builder.setItems(mSMSList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {                
                ContentResolver contentResolver = mActivity.getContentResolver();
                if (contentResolver == null) {
                    return;
                }

                Cursor Cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
                if (Cursor == null) {
                    return;
                }
        
                String Body = null;
                if (!Cursor.moveToPosition(which)){
                    Toast.makeText(mActivity, mActivity.getString(R.string.opensmsfailed_label), Toast.LENGTH_LONG).show();
                    return;
                }
                    
                int BodyIndex = Cursor.getColumnIndex("body");
                if (BodyIndex != -1) {
                    Body = Cursor.getString(BodyIndex);
                }

                if (mListener != null) {
                    mListener.SMSContentsRead(Body, mActivity);
                }
            }
        });

        dialog = builder.show();
        return dialog;
    }

    public void ShowDialog() {
        if (mSMSListValid) {
            CreateSmsDialog().show();
        }
    }

    private boolean LoadSMSList() {
        ContentResolver contentResolver = mActivity.getContentResolver();
        
        if (contentResolver == null) return false;
        
        Cursor Cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
     
        if (Cursor == null) return false;
     
        Cursor.moveToFirst();
        
        Locale CurrentLocale = mActivity.getResources().getConfiguration().locale;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", CurrentLocale);

        List<String> StringList = new ArrayList<String>();
        
        int NumProcessed = 0;
        do {
            int TypeIndex = Cursor.getColumnIndex("type");
            if (TypeIndex == -1) continue;
            try {
            if (!Cursor.getString(TypeIndex).contains("1")) continue; // Not inbox
            } catch (android.database.CursorIndexOutOfBoundsException ex) {
                continue;
            }
          
            String msgData = "";
            
            int AddressIndex = Cursor.getColumnIndex("address");
            int DateIndex = Cursor.getColumnIndex("date");
            int ReadIndex = Cursor.getColumnIndex("read");
           
            if (AddressIndex != -1) msgData += Cursor.getString(AddressIndex);
            msgData += "\n";
            
            if (DateIndex != -1 && CurrentLocale != null) {
                long DateMillis = Cursor.getLong(DateIndex);
                Date date = new Date(DateMillis);

                msgData += " ";
                msgData += sdf.format(date).toString();
            }
   
            if (ReadIndex != -1) {
                if (Cursor.getString(ReadIndex).contains("0")) {
                    msgData += "\n";
                    msgData += "unread";
                }
            }

            StringList.add(msgData);
            NumProcessed++;
            if (NumProcessed > 50) break;
        } while (Cursor.moveToNext());
        
        mSMSList = StringList.toArray(new String[]{});
        return true;
    }

    public void AddSMSContentsReadListener(OpenSmsDialog.SMSContentsListener listener) {
        mListener = listener;
    }
}
