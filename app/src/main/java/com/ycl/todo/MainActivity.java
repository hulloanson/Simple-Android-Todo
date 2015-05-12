package com.ycl.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity{
    private ArrayList<Long> tasksId;
    private ArrayList<String> tasksString;
    private ArrayAdapter<String> tasksAdapter;
    private ListView lvTasks;
    private dbHelper myDb;
    public SQLiteDatabase openedDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvTasks = (ListView) findViewById(R.id.lvTasks);
        tasksId  = new ArrayList<Long>();
        tasksString = new ArrayList<String>();
        myDb = new dbHelper(this);
        SQLiteDatabase openedDb = myDb.getWritableDatabase();
        loadAll(openedDb);
        tasksAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, tasksString);
        lvTasks.setAdapter(tasksAdapter);
        setupListViewListener();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds tasks to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupListViewListener() {
        lvTasks.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter,
                                                   View item, int pos, long id) {
                        // Remove the item within array at position
                        tasksString.remove(pos);
                        // Refresh the adapter
                        tasksAdapter.notifyDataSetChanged();
                        // Return true consumes the long click event (marks it handled)
                        removeItem(openedDb, tasksId.get(pos));
                        return true;
                    }

                });
    }

    public void onAddItem(View v) {
        String trimmed = "";
        EditText typeTask = (EditText) findViewById(R.id.typeTask);
        String itemText = typeTask.getText().toString();
        int[] checkNoneReturn = checkNone(itemText);
        if (checkNoneReturn[0] == 0){
            trimmed = itemText.substring(checkNoneReturn[1]);
            tasksString.add(trimmed);
        }else{
            spawnToast("Please enter something!", true);
        }
        typeTask.setText("");
        writeItem(openedDb, trimmed);
    }

    public int[] checkNone(String s){
        int None;
        int i = 0;
        if (s.equals("") || s.equals("\n")){
            None = 1;
        }else{
            None = 1;
            for (i = 0; i < s.length(); i++) {
                char temp = s.charAt(i);
                if (temp != ' ' && temp != '\n'){
                    None = 0;
                    break;
                }else{}
            }
        }
        int[] result;
        result = new int[]{None, i};
        return result;
    }

    public void spawnToast(CharSequence text, boolean lengthShort){
        Context context = getApplicationContext();
        int duration;
        if (lengthShort) {
            duration = Toast.LENGTH_SHORT;
        }else{
            duration = Toast.LENGTH_LONG;
        }
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

//    private void checkTodoTxt(){
//        File todoDir = new File(Environment.getExternalStorageDirectory().getPath()+"/com.ycl.todo");
//        File todoF = new File(Environment.getExternalStorageDirectory().getPath()+"/com.ycl.todo/todo.txt");
//        if(todoF.exists() && !todoF.isDirectory()){
//            readItems();
//        }else{
//            todoDir.mkdir();
//            try {
//                todoF.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public static abstract class DbEntry implements BaseColumns {
        public static final String tableName = "tasks";
        public static final String taskId = "id";
        public static final String taskName = "title";
//            public static final String taskDue = "due";
//            public static final String taskDescription = "description";
    }

    public class dbHelper extends SQLiteOpenHelper{
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "tasksList.db";
        private final String SQL_CREATE_ENTRIES = "CREATE TABLE " + DbEntry.tableName + "(" + DbEntry._ID + " INTEGER PRIMARY KEY," + DbEntry.taskName + " TEXT" + " )";
        private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DbEntry.tableName;

        public dbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private void loadAll(SQLiteDatabase db){
        final String[] columns = {DbEntry._ID, DbEntry.taskName};
        Cursor result = db.query(DbEntry.tableName, columns, null, null, null, null, null);
        result.moveToFirst();
        tasksId.add(result.getLong(result.getColumnIndexOrThrow(DbEntry._ID)));
        tasksString.add(result.getString(result.getColumnIndexOrThrow(DbEntry.taskName)));
        while(!(result.isAfterLast())){
            result.moveToNext();
            tasksId.add(result.getLong(result.getColumnIndexOrThrow(DbEntry._ID)));
            tasksString.add(result.getString(result.getColumnIndexOrThrow(DbEntry.taskName)));
        }
    }

    private void writeItem(SQLiteDatabase db, String taskString){
        ContentValues insertValues = new ContentValues();
        insertValues.put(DbEntry.taskName, taskString);
        long newId = db.insert(DbEntry.tableName, null, insertValues);
        tasksId.add(newId);
    }

    private void removeItem(SQLiteDatabase db, long taskId){
        String columnSelection = DbEntry.taskId;
        String[] valueSelection = {String.valueOf(taskId)};
        db.delete(DbEntry.tableName, columnSelection, valueSelection);
    }
}
