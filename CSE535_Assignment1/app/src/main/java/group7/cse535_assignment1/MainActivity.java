package group7.cse535_assignment1;


import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;
//import android.support.design.widget.AppBarLayout;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.widget.RelativeLayout;


public class MainActivity extends AppCompatActivity {

    /* Declare variables */
    private EditText patientid, age, patientname;
    private Button runbutton, stopbutton;

    private GraphView Graph;
    private LinearLayout layout;

    private String[] axisy = new String[]{"2000", "1500", "1000", "500", "0"};
    private String[] axisx = new String[]{"0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
    private String[] runAixsX; // store the axis x dynamically

    private String name, id;
    private List<float[]> data = new ArrayList<float[]>(); // store every patients' data
    private float[] runData; // to store the patient's data according to the id of user input and shift left each second

    private Map<String, Integer> idmap = new HashMap<String, Integer>(); // the map of patient's id and its data list index
    private int index = 0, threadnum = 1;
    private float leftshift = 0.0f; // add one each second to record the number of shifting
    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* find the view according to its id and assign it to the variable*/
        patientid = (EditText) findViewById(R.id.editText);
        age = (EditText) findViewById(R.id.editText2);
        patientname = (EditText) findViewById(R.id.editText3);
        runbutton = (Button)findViewById(R.id.button);
        stopbutton = (Button)findViewById(R.id.button2);
        name = patientname.getText().toString();
        layout = (LinearLayout) findViewById(R.id.lay);

        /* listen to the runbutton to be clicked */
        runbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                /* turn the input id to string */
                String tempid = patientid.getText().toString();

                if(!tempid.equals(id) || exit)
                {
                    id = tempid;

                /* if the id isn't in the id map, create this id map, and a random float array data.
                 Concatenate it to the data list which store each patient's data */
                    if (!idmap.containsKey(id)) {
                        idmap.put(id, index);
                        index++;
                        float minX = 0.0f, maxX = 2000.0f, d;
                        float[] addData = new float[100];
                        Random r = new Random();
                        int i = 0;
                        for (i = 0; i < 100; i++) {
                            d = r.nextFloat() * (maxX - minX) + minX;
                            addData[i] = d;
                        }
                        data.add(addData);
                    }

                    runData = data.get(idmap.get(id));
                    leftshift = 0.0f;
                    runAixsX = axisx;

                /* create a thread to add a new view each second, and the maximum thread number is 2 */
                    if(threadnum == 1)
                    {
                        Thread thread = new delayedViewAdditionThread();
                        thread.start();
                        threadnum = 2;
                    }
                }
            }
        });

        /* listen to the runbutton to be clicked */
        stopbutton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                /* if the stop button was clicked, the thread would jump out of the while loop and be killed,
                *  and then remove the graph view */
                exit = true;
                layout.removeView(Graph);

            }
        });
    }

    /* thread class that add new view each second */
    public class delayedViewAdditionThread extends Thread
    {
        private android.os.Handler handler = new android.os.Handler()
        {
            @Override
            public void handleMessage(android.os.Message msg)
            {
                switch(msg.what)
                {
                    case 0 :
                        addView();
                        break;
                    case 1 :
                        removeview();
                    default :
                        break;
                }
            }
        };
        @Override
        public void run()
        {
            exit = false;
            while(!exit)
            {
                try
                {
                    /* send handler message to remove view, and let the thread sleep 1 ms */
                    handler.sendEmptyMessage(1);
                    Thread.sleep(1);
                    /* send handler message to add view, and let the thread sleep 1 s */
                    handler.sendEmptyMessage(0);
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            threadnum = 1;
            handler.sendEmptyMessage(2);
        }

        /* function that shift the data left and create a new view */
        private void addView()
        {
            /* each 9 second, create a new axis x */
            if(leftshift == 9.0f)
            {
                runAixsX = Arrays.copyOfRange(runAixsX, 1, runAixsX.length+1);
                runAixsX[runAixsX.length-1] = Integer.toString(Integer.parseInt(runAixsX[runAixsX.length-2])+10);
                leftshift = 0.0f;
            }

            /* create a new GraphView with new data each round/second */
            Graph = new GraphView(MainActivity.this, runData, name, runAixsX, axisy, GraphView.LINE, leftshift*16.0f);

            /* add one(second) each round */
            leftshift += 1.0f;

            /* add view to layout */
            layout.addView(Graph);

            /* shift patient's data left */
            float[] temp = new float[100];
            temp = Arrays.copyOfRange(runData, 1, runData.length+1);
            temp[temp.length-1] = runData[0];
            runData = Arrays.copyOf(temp, temp.length);

        }

        /* function that remove the view */
        private void removeview() {
            layout.removeView(Graph);
        }
    }

}
