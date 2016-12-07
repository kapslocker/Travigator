package com.frodo.travigator.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageView;

import com.frodo.travigator.R;

/**
 * Created by Kapil on 9/6/2015.
 */
public class Blink extends AsyncTask<String,Void,String > {

    private static ImageView img;

    private Runnable r3,r4;


    public Blink(ImageView view){
        this.img=view;



    }

    @Override
    protected String doInBackground(String... arg0){

        final Handler h = new Handler();

        r3 = new Runnable() {

            @Override
            public void run() {
                img.setImageResource(R.drawable.neutral);

                h.postDelayed(r4,300);
            }
        };

        r4 = new Runnable() {

            @Override
            public void run() {
                img.setImageResource(R.drawable.next);
                h.postDelayed(r3,300);
            }
        };

        r3.run();






        return "random";
    }


    @Override
    protected void onPostExecute(String result){




    }


}
