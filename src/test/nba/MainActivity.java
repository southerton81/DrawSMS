package test.nba;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    final String[] mSymbols = {
        ".", ",", "\\", "?", "!", "-", "(", ")", "@", "/", ":", ";", "+", "%", "*", "=", "<", ">",
        ".", ",", "\\", "?", "!", "-", "(", ")", "@", "/", ":", ";"
    };
    static final int mNumColumns = 10;
    static final int mNumColumnsTools = 2;
    CheckableLinearLayout mSymbolButton;
    ToggleButton mToggleButton;
    ArrayAdapter<String> mAdapterSymbols;
    GridView mSymbolsGrid;
    int mSymbolIndex = 0;
    CNet mMainNet;
    CircularBuffer<CNet> mHistory = new CircularBuffer(10);
    CNetView mNetView;

    public enum Tool {
        MOVE, ERASE, NONE
    }
    
    Tool mSelectedTool = Tool.NONE;

    void AddHistory() {
        mHistory.Add(new CNet(mMainNet));
    }

    void UpdateButtons() {
        Button ButtonUndo = (Button) findViewById(R.id.ButtonUndo);
        ButtonUndo.setEnabled(mHistory.IsPrevExists());
        Button ButtonRedo = (Button) findViewById(R.id.ButtonRedo);
        ButtonRedo.setEnabled(mHistory.IsNextExists());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        mNetView = (CNetView) findViewById(R.id.CNetView);
        PrepareSymbolsGrid();
    }

    public void onResume() {
        super.onResume();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mSymbolIndex = sp.getInt("SymbolIndex", 0);

        UpdateButtons();
    }

    public void onPause() {
        super.onPause();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor PrefsEditor = sp.edit();
        PrefsEditor.putInt("SymbolIndex", mSymbolIndex);
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
            case R.id.send_sms:
                OnSendSMS();
                return true;
            case R.id.open:
                return true;
            case R.id.save:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    CNet GetMainNet() {
        if (mMainNet == null) {
            mMainNet = new CNet(mNumColumns, 10);
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

    private void PrepareSymbolsGrid() {
        mAdapterSymbols = new ArrayAdapter<String>(this, R.layout.item, R.id.ItemTextView, mSymbols);

        mSymbolsGrid = (GridView) findViewById(R.id.SymbolsGrid);
        mSymbolsGrid.setAdapter(mAdapterSymbols);

        mSymbolsGrid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (mSymbolButton != null) {
                    mSymbolButton.setChecked(false);
                 
                }
                
                if (mToggleButton != null){
                       mSelectedTool = Tool.NONE;
                       mToggleButton.toggle();
                       mToggleButton = null;
                } 

                mSymbolButton = (CheckableLinearLayout) v;
                mSymbolIndex = position;
                mSymbolButton.setChecked(true);
            }
        });

        mSymbolsGrid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                RestoreSelectedSymbol();
                mSymbolsGrid.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private void RestoreSelectedSymbol() {
        GridView Grid = mSymbolsGrid;
        int Index = mSymbolIndex;
        int NumColumns = mNumColumns;
        int ChildCount = Grid.getChildCount();
        if (ChildCount > Index) {
            View Child = Grid.getChildAt(Index);
            if (Child != null) {
                Grid.performItemClick(Child, Index, Index / NumColumns);
            }
        }
    }

    private void OnSendSMS() {
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
            mSymbolButton.setChecked(!on);
        }
    }

     public void OnMove(View v) {
       boolean on = ((ToggleButton) v).isChecked();
       
      if (mToggleButton != null)
            if (mToggleButton.isChecked())
                mToggleButton.toggle();
        
        if (on) {
            mSelectedTool = Tool.MOVE;
            mToggleButton = (ToggleButton) v;
        } else {
            mSelectedTool = Tool.NONE;
            mToggleButton = null;
        }
        if (mSymbolButton != null) {
            mSymbolButton.setChecked(!on);
        }
    }
}
