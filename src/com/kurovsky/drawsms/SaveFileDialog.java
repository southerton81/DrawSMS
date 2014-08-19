
package com.kurovsky.drawsms;

import com.kurovsky.drawsms.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.File;

public class SaveFileDialog {
    private final Activity mActivity;
    File mPath;
    private FileNameListener mFileListener;
    EditText mEditText;
    
    public interface FileNameListener {
        void fileNameEntered(File file, Activity activity);
    }
    
    public SaveFileDialog(Activity activity, File path) {
        this.mActivity = activity;
        mPath = path;
    }

    public Dialog CreateFileDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(R.string.savefile_label);

        mEditText = new EditText(mActivity);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
      
        LinearLayout layout = new LinearLayout(mActivity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(10, 5, 10, 5);
        layout.addView(mEditText);
        builder.setView(layout);
        
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
               if (mFileListener != null) {
                   String FileName = mEditText.getText().toString();
                   if (FileName.length() == 0)
                       return;
                   File SaveFile = new File(mPath + "/" + FileName + ".txt");
                   
                   Integer Number = new Integer(0);
                   for (int i = 2; i <= 100; i++) {
                       Number = i;
                       if (SaveFile.exists()) {
                           SaveFile = new File(mPath + "/" + FileName + Number.toString() + ".txt");
                       } else {
                           break;
                       }
                       
                       if (i == 100) {
                           Toast.makeText(mActivity, FileName + ".txt" + " " + mActivity.getString(R.string.failedtosavefile_label), Toast.LENGTH_SHORT).show();
                           return;
                       }
                    }

                    if (mFileListener != null) {
                        mFileListener.fileNameEntered(SaveFile, mActivity);
                    }
                }
            }
        });
        builder.setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
               
            }
        });

        return builder.show();
    }
    
     public void ShowDialog() {
        CreateFileDialog().show();
    }
     
     public void AddFileNameListener(SaveFileDialog.FileNameListener listener) {
        mFileListener = listener;
    }
}
