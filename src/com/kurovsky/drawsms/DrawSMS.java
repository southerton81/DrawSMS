package com.kurovsky.drawsms;

import com.kurovsky.drawsms.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DrawSMS extends Activity {
    ArrayList<GridObject> mGridObjects;

    static class ViewHolder {
        TextView text;
    }

    public class GridObject {

        private String name;
        private boolean checked;

        public GridObject(String name, boolean state) {
            super();
            this.name = name;
            this.checked = state;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean getState() {
            return checked;
        }

        public void setState(boolean state) {
            this.checked = state;
        }
    }
    
    private class CustomArrayAdapter<T> extends ArrayAdapter  {
        private LayoutInflater mInflater;

        CustomArrayAdapter(Context context, int resource, int textViewResourceId, T[] objects){
            super(context, resource, textViewResourceId, objects);
            mInflater = LayoutInflater.from(context);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            GridObject object = mGridObjects.get(position);
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item, null);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.ItemTextView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(object.getName());

            if (object.getState() == true) {
                holder.text.setBackgroundColor(Color.YELLOW);
            } else {
                holder.text.setBackgroundColor(Color.WHITE);
            }
            return convertView;
        }

        public int getCount() {
            return mGridObjects.size();
        }
    }
    
    final String[] mSymbolsMisc = {
        "★", "☆", "\u2661", "☏", "♪", "✓", "☞", "♧", "♀", "♂",
        "|", "-", "_", "/", "\\", "\"", "=", "*", "[", "]",  
        "~", "Δ", "Λ", "<", ">", "^", "{", "}",  "#", "¤", 
        "!", ":", ";", "+", "(", ")", "'", "%", "&", "Ξ",
        "£", "$", "Φ", "¥", "Γ", "Ω", "Π", "Ψ", "Σ", "Θ",
        "Ø", "ø", "Æ", "æ", "ß", "¿", "¡", "@", "✧", "▉",
        "↖", "↗", "↘", "↙", "←", "↑", "→", "↓", "↰", "↱",
   
        "RU", "EN"
    };
    
    final String[] mSymbols = {
        "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
        "⇧", "a", "s", "d", "f", "g", "h", "j", "k", "l",
        "z", "x", "c", "v", "b", "n", "m", ".", ",", "?",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
         
        "RU", "SY"
    };
    final String[] mSymbolsCaps = {
        "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
        "⇧", "A", "S", "D", "F", "G", "H", "J", "K", "L",
        "Z", "X", "C", "V", "B", "N", "M", ".", ",", "?",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",

        "RU", "SY"
    };
    
    final String[] mSymbolsRu = { 
        "й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з",
        "⇧", "ф", "ы", "в", "а", "п", "р", "о", "л", "д",
        "я", "ч", "с", "м", "и", "т", "ь", "б", "ю", "?",
        "х", "ъ", "ж", "э", "ё", ".", ",", "?", ":", ";",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
  
        "EN", "SY"
    };
    final String[] mSymbolsCapsRu = {
        "Й", "Ц", "У", "К", "Е", "Н", "Г", "Ш", "Щ", "З",
        "⇧", "Ф", "Ы", "В", "А", "П", "Р", "О", "Л", "Д",
        "Я", "Ч", "С", "М", "И", "Т", "Ь", "Б", "Ю", "?",
        "Х", "Ъ", "Ж", "Э", "Ё", ".", ",", "?", ":", ";",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
 
        "EN", "SY"
    };
   
    static final int mNumColumnsTools = 2;
    static int mFileNumber = 0;
    GridObject mSymbolButton;
    ToggleButton mToggleButton;
    ArrayAdapter<String> mAdapterSymbols;
    GridView mSymbolsGrid;
    int mSymbolIndex = 0;
    CNet mMainNet;
    CNet mDemoNet;
    CircularBuffer<CNet> mHistory = new CircularBuffer(10);
    CNetView mNetView;
    OpenFileDialog mOpenDialog;
    SaveFileDialog mSaveDialog;
    OpenSmsDialog mOpenSMSDialog;
    private boolean mIsCaps = false;
    private boolean mIsCyr = false;
    private boolean mIsMisc = false;
    
    public enum Tool {
        MOVE, ERASE, NONE
    }
    Tool mSelectedTool = Tool.NONE;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        mNetView = (CNetView) findViewById(R.id.CNetView);
        ToggleButton ButtonErase = (ToggleButton) findViewById(R.id.ButtonErase);

        ButtonErase.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                if (!mMainNet.IsEmpty()) {
                    mMainNet.Clear();
                    AddHistory();
                }
                UpdateButtons();
                mNetView.postInvalidate();

                return false;
            }
        });

        //PrepareSymbolsGrid(mIsCaps, -1);    
    }

    void AddHistory() {
        mHistory.Add(new CNet(mMainNet));
    }

    boolean IsHistoryEmpty() {
        return mHistory.IsEmpty();
    }

    void UpdateButtons() {
        Button ButtonUndo = (Button) findViewById(R.id.ButtonUndo);
        ButtonUndo.setEnabled(mHistory.IsPrevExists());
        Button ButtonRedo = (Button) findViewById(R.id.ButtonRedo);
        ButtonRedo.setEnabled(mHistory.IsNextExists());
    }    

    public void onResume() {
        super.onResume();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mSymbolIndex = sp.getInt("SymbolIndex", 0);
        mIsCaps = sp.getBoolean("IsCaps", false);
        mIsCyr = sp.getBoolean("IsCyr", false);
        mIsMisc = sp.getBoolean("IsMisc", true);
        GetMainNet().Restore(sp, "MainNet");
        mHistory.Restore(sp);
        
        UpdateButtons();
        PrepareSymbolsGrid(mIsCaps, mSymbolIndex);    
    }

    public void onPause() {
        super.onPause();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor PrefsEditor = sp.edit();
        GetMainNet().Store(PrefsEditor, "MainNet");
        PrefsEditor.putInt("SymbolIndex", mSymbolIndex);
        PrefsEditor.putBoolean("IsCaps", mIsCaps);
        PrefsEditor.putBoolean("IsCyr", mIsCyr);
        PrefsEditor.putBoolean("IsMisc", mIsMisc);
                
        mHistory.Store(PrefsEditor);
        PrefsEditor.commit();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.smsmenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.open:
                return OnOpenText();
            case R.id.save:
                return OnSaveText();
            default:
                return super.onContextItemSelected(item);
        }
    }

    CNet GetMainNet() {
        if (mMainNet == null) {
            mMainNet = new CNet();
        }
        return mMainNet;
    }

    char GetSelectedSymbol() {
        String Symbol = (String) mSymbolsGrid.getAdapter().getItem(mSymbolIndex);
        return Symbol.charAt(0);
    }

    Tool GetSelectedTool() {
        return mSelectedTool;
    }

    private void CreateGridObjects(String[] keys) {
        mGridObjects = new ArrayList<GridObject>();     
        for (String s : keys) {
            mGridObjects.add(new GridObject(s, false));
        }
    }
        
    private void PrepareSymbolsGrid(boolean caps, int scrollPos) {
        if (caps) {
            mIsCaps = true;

            if (mIsMisc){
                CreateGridObjects(mSymbolsMisc);
                mAdapterSymbols = new CustomArrayAdapter<String>(this, R.layout.item, R.id.ItemTextView, mSymbolsMisc);
            }
            else if (mIsCyr) {
                CreateGridObjects(mSymbolsCapsRu);
                mAdapterSymbols = new CustomArrayAdapter<String>(this, R.layout.item, R.id.ItemTextView, mSymbolsCapsRu);
            } else {
                CreateGridObjects(mSymbolsCaps);
                mAdapterSymbols = new CustomArrayAdapter<String>(this, R.layout.item, R.id.ItemTextView, mSymbolsCaps);
            }
        } else {
            mIsCaps = false;

            if (mIsMisc){
                CreateGridObjects(mSymbolsMisc);
                mAdapterSymbols = new CustomArrayAdapter<String>(this, R.layout.item, R.id.ItemTextView, mSymbolsMisc);
            }
            else if (mIsCyr) {
                CreateGridObjects(mSymbolsRu);
                mAdapterSymbols = new CustomArrayAdapter<String>(this, R.layout.item, R.id.ItemTextView, mSymbolsRu);
            } else {
                CreateGridObjects(mSymbols);
                mAdapterSymbols = new CustomArrayAdapter<String>(this, R.layout.item, R.id.ItemTextView, mSymbols);
            }
        }

        mSymbolsGrid = (GridView) findViewById(R.id.SymbolsGrid);
        mSymbolsGrid.setAdapter(mAdapterSymbols);

        mSymbolsGrid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (mSymbolButton != null) {
                    mSymbolButton.setState(false);
                }

                if (mToggleButton != null) {
                    mSelectedTool = Tool.NONE;
                    mToggleButton.toggle();
                    mToggleButton = null;
                }

                String Symbol = (String) mSymbolsGrid.getAdapter().getItem(position);
                if (Symbol.contains("⇧")) {
                    PrepareSymbolsGrid(!mIsCaps, -1);
                } 
                else if (Symbol.contains("SY")) {
                    mIsMisc = true;
                    PrepareSymbolsGrid(mIsCaps, -1);
                    mSymbolIndex = 0;
                } 
                else if (Symbol.contains("RU")) {
                    mIsMisc = false;
                    mIsCyr = true;
                    PrepareSymbolsGrid(mIsCaps, -1);
                    mSymbolIndex = 0;
                } 
                else if (Symbol.contains("EN")) {
                    mIsMisc = false;
                    mIsCyr = false;
                    PrepareSymbolsGrid(mIsCaps, -1);
                    mSymbolIndex = 0;
                }
                else {
                    mSymbolButton = mGridObjects.get(position);
                    mSymbolButton.setState(true);
                    mAdapterSymbols.notifyDataSetChanged();
                    mSymbolIndex = position;
                }
            }
        });

        mSymbolsGrid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                RestoreSelectedSymbol();
                mSymbolsGrid.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        
        if (scrollPos != -1) {
            mSymbolsGrid.smoothScrollToPosition(scrollPos);
        }
    }

    private void RestoreSelectedSymbol() {
        try {
            mSymbolButton = mGridObjects.get(mSymbolIndex);
        } catch (IndexOutOfBoundsException e) {
        }
        if (mSymbolButton != null) {
            mSymbolButton.setState(true);
            mAdapterSymbols.notifyDataSetChanged();
        }
    }

    public void OnUndo(View v) {
        if (mHistory.IsPrevExists()) {
            mMainNet.CopyFrom(mHistory.Prev());
            UpdateButtons();
            mNetView.postInvalidate();
        }
    }

    public void OnRedo(View v) {
        if (mHistory.IsNextExists()) {
            mMainNet.CopyFrom(mHistory.Next());
            UpdateButtons();
            mNetView.postInvalidate();
        }
    }

    public void OnErase(View v) {
        boolean on = ((ToggleButton) v).isChecked();

        if (mToggleButton != null) {
            if (mToggleButton.isChecked()) {
                mToggleButton.toggle();
            }
        }

        if (on) {
            mSelectedTool = Tool.ERASE;
            mToggleButton = (ToggleButton) v;
        } else {
            mSelectedTool = Tool.NONE;
            mToggleButton = null;
        }
        if (mSymbolButton != null) {
            mSymbolButton.setState(!on);
        }
    }

    public void OnMove(View v) {
        boolean on = ((ToggleButton) v).isChecked();

        if (mToggleButton != null) {
            if (mToggleButton.isChecked()) {
                mToggleButton.toggle();
            }
        }

        if (on) {
            mSelectedTool = Tool.MOVE;
            mToggleButton = (ToggleButton) v;
        } else {
            mSelectedTool = Tool.NONE;
            mToggleButton = null;
        }
        if (mSymbolButton != null) {
            mSymbolButton.setState(!on);
        }
    }

    public void OnSendSMS(View v) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        StringBuilder SB = new StringBuilder(256);

        int Length = mMainNet.mNetCharacters.length;
        for (int h = 0; h < Length; h++) {
            int Length2 = mMainNet.mNetCharacters[h].length;
            for (int w = 0; w < Length2; w++) {
                SB.append(mMainNet.mNetCharacters[w][h]);
            }

            SB.append("\n");
        }

        String Str = SB.toString();
        sendIntent.putExtra("sms_body", Str);
        sendIntent.setType("vnd.android-dir/mms-sms");
        startActivity(sendIntent);
    }

    public void OnOpenSMS(View v) {
        mOpenSMSDialog = new OpenSmsDialog(this);

        mOpenSMSDialog.AddSMSContentsReadListener(new OpenSmsDialog.SMSContentsListener() {
            public void SMSContentsRead(String contents, Activity activity) {
                CNet Net = GetMainNet();

                if (IsHistoryEmpty()) {
                    AddHistory();
                }

                String Delim = "[\n]";
                String[] Lines = contents.split(Delim);
                String Line = null;
                String EmptyString = new String();

                for (int i = 0; i < Net.GetSize().y; i++) {
                    if (i >= Lines.length) {
                        Line = EmptyString;
                    } else {
                        Line = Lines[i];
                    }

                    Net.SetLine(Line, i);
                }

                AddHistory();
                UpdateButtons();
                mNetView.postInvalidate();
            }
        });
        mOpenSMSDialog.ShowDialog();
    }

    private boolean OnOpenText() {
        File Path = getFilesDir();
        if (!Path.exists()) {
            Path = Environment.getExternalStorageDirectory();
        }

        CopySamples(Path);

        mOpenDialog = new OpenFileDialog(this, Path, null/*".txt"*/);
        mOpenDialog.addFileListener(new OpenFileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                CNet Net = GetMainNet();
                if (IsHistoryEmpty()) {
                    AddHistory();
                }

                FileToNet(file, Net);
                AddHistory();
                UpdateButtons();
                mNetView.postInvalidate();
            }
        });
        mOpenDialog.showDialog();
        return true;
    }

    private boolean OnSaveText() {
        File Path = getFilesDir();
        if (!Path.exists()) {
            Path = Environment.getExternalStorageDirectory();
        }

        mSaveDialog = new SaveFileDialog(this, Path);
        mSaveDialog.AddFileNameListener(new SaveFileDialog.FileNameListener() {
            public void fileNameEntered(File file, Activity activity) {
                try {
                    BufferedWriter Writer = new BufferedWriter(new FileWriter(file));

                    CNet Net = GetMainNet();
                    for (int i = 0; i < Net.GetSize().y; i++) {
                        String Line = Net.GetLine(i);
                        Writer.write(Line, 0, Line.length());
                        Writer.newLine();
                    }

                    Writer.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(DrawSMS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(DrawSMS.class.getName()).log(Level.SEVERE, null, ex);
                }

                Toast.makeText(activity, activity.getString(R.string.savedas_label) + " " + file.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        mSaveDialog.ShowDialog();
        return true;
    }

    private void CopySamples(File path) {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("samples");
        } catch (IOException e) {
            Log.e("tag", "Failed to get assets file list", e);
        }
        if (files == null) {
            return;
        }

        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                File outFile = new File(path, filename);
                if (!outFile.exists()) {
                    in = assetManager.open("samples/" + filename);
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                    in.close();
                    in = null;
                    out.flush();
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    boolean FileToNet(File file, CNet net) {
        try {
            FileInputStream fin = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));

            String Line = null;
            String EmptyString = new String();
            for (int i = 0; i < net.GetSize().y; i++) {
                Line = reader.readLine();
                if (Line == null) {
                    Line = EmptyString;
                }
                net.SetLine(Line, i);
            }
            reader.close();
            fin.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
