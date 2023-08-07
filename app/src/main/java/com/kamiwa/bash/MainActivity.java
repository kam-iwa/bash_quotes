package com.kamiwa.bash;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public static final String DB_NAME = "data.db";
    public static final String DB_SUB_PATH = "/databases/" + DB_NAME;
    private static String APP_DATA_PATH = "";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String appDataPath = this.getApplicationInfo().dataDir;

        File dbFolder = new File(appDataPath + "/databases");//Make sure the /databases folder exists
        dbFolder.mkdir();//This can be called multiple times.

        File dbFilePath = new File(appDataPath + "/databases/data.db");

        if (!dbFilePath.exists()){
            try {
                InputStream inputStream = this.getAssets().open("data.db");
                OutputStream outputStream = new FileOutputStream(dbFilePath);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer))>0)
                {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e){
                //handle
            }
        }

        APP_DATA_PATH = this.getApplicationInfo().dataDir;
        String mPath = APP_DATA_PATH + DB_SUB_PATH;
        SQLiteDatabase db = SQLiteDatabase.openDatabase(mPath, null, 0);

        TextView txQuoteNumber = findViewById(R.id.quote_number);
        TextView txQuoteLink = findViewById(R.id.quote_link);
        TextView txQuoteText = findViewById(R.id.quote_text);
        txQuoteText.setMovementMethod(new ScrollingMovementMethod());

        Button btnQuoteNext = findViewById(R.id.quote_next);
        btnQuoteNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Cursor cursor = db.rawQuery("SELECT id, link, text FROM quote ORDER BY RANDOM() LIMIT 1;", new String[]{});
                cursor.moveToFirst();

                String quoteNumber = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                txQuoteNumber.setText(quoteNumber);

                String quoteLink = cursor.getString(cursor.getColumnIndexOrThrow("link"));
                quoteLink = quoteLink.substring(0,quoteLink.length() - 1);
                txQuoteLink.setText(quoteLink);

                String quoteText = cursor.getString(cursor.getColumnIndexOrThrow("text"));
                quoteText = quoteText.substring(0,quoteText.length() - 2);
                txQuoteText.setText(quoteText);
                txQuoteText.scrollTo(0, 0);
                //Intent intent = new Intent(getApplicationContext(), RSSFeedActivity.class)

            }
        });

        txQuoteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) txQuoteLink.getText()));
                    startActivity(browserIntent);
                }
                catch (ActivityNotFoundException e) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://bash.org.pl"));
                    startActivity(browserIntent);
                }

            }
        });
    }
}
