package com.ycl.todo;

import android.content.Context;
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
    private ArrayList<String> tasks;
    private ArrayAdapter<String> tasksAdapter;
    private ListView lvTasks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readItems();
        lvTasks = (ListView) findViewById(R.id.lvTasks);
        tasks = new ArrayList<String>();
        tasksAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, tasks);
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
                        tasks.remove(pos);
                        // Refresh the adapter
                        tasksAdapter.notifyDataSetChanged();
                        // Return true consumes the long click event (marks it handled)
                        writeItems();
                        return true;
                    }

                });
    }

    public void onAddItem(View v) {
        EditText typeTask = (EditText) findViewById(R.id.typeTask);
        String itemText = typeTask.getText().toString();
        int[] checkNoneReturn = checkNone(itemText);
        if (checkNoneReturn[0] == 0){
            String trimmed = itemText.substring(checkNoneReturn[1]);
            tasks.add(trimmed);
        }else{
            spawnToast("Please enter something!", true);
        }
        typeTask.setText("");
        writeItems();
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

    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            tasks = new ArrayList<String>(FileUtils.readLines(todoFile));
        } catch (IOException e) {
            tasks = new ArrayList<String>();
        }
    }

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            FileUtils.writeLines(todoFile, tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
