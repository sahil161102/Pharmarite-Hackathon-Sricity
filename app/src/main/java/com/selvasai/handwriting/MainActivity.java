/*!
 *  @date 2020/01/02
 *  @file MainActivity.java
 *  @author SELVAS AI
 *
 *  Copyright 2020. SELVAS AI Inc. All Rights Reserved.
 */

package com.selvasai.handwriting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.diotek.dhwr.DHWR;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    private WritingRecognizer mWritingRecognizer;
    private WritingView mWritingView;
    private TextView mVersion;
    private TextView mCandidates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        copyResourceToStorage();
        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWritingRecognizer.destroy();
    }

    @SuppressLint("SetTextI18n")
    private void initialize() {
        mWritingRecognizer = new WritingRecognizer(getApplicationContext());
        mWritingView = findViewById(R.id.canvas);
        mWritingView.setRecognizer(mWritingRecognizer);
        Button clearButton = findViewById(R.id.clear);
        if (clearButton != null) {
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleClear();
                }
            });
        }

        Button recognizeButton = findViewById(R.id.recognize);
        if (recognizeButton != null) {
            recognizeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleRecognize();
                }
            });
        }

        RadioGroup languageGroup = findViewById(R.id.languageGroup);
        if (languageGroup != null) {
            languageGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int id) {
                    handleLanguageChanged(id);
                }
            });
        }

        mVersion = findViewById(R.id.version);
        mVersion.setText("Model : " + Build.MODEL + "\n"
                + "Version : " + mWritingRecognizer.getVersion() + "\n"
                + "Due date : " + mWritingRecognizer.getDueDate());
        mCandidates = findViewById(R.id.candidates);
    }

    private void copyResourceToStorage() {
        String path = "hdb";
        String[] fileList = null;
        try {
            fileList = getAssets().list(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (fileList == null) {
            return;
        }

        for (String file : fileList) {
            try {
                String filePath = (path + "/" + file);
                try {
                    FileInputStream fis = openFileInput(file);
                    fis.close();
                } catch (FileNotFoundException e) {
                    InputStream is = getAssets().open(filePath);
                    final int bufferSize = is.available();
                    byte[] buffer = new byte[bufferSize];
                    int readSize = is.read(buffer);
                    is.close();
                    if (readSize > 0) {
                        FileOutputStream fos = openFileOutput(file, Activity.MODE_PRIVATE);
                        fos.write(buffer);
                        fos.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleClear() {
        mCandidates.setVisibility(View.GONE);
        mCandidates.setText("");
        mWritingView.clear();
        mWritingRecognizer.clearInk();
    }

    private void handleRecognize() {
        mWritingView.clear();
        mCandidates.setText(mWritingRecognizer.recognize());
        mCandidates.setVisibility(View.VISIBLE);
        mWritingRecognizer.clearInk();
    }

    private void handleLanguageChanged(int id) {
        int language = DHWR.DLANG_KOREAN;
        int option = DHWR.DTYPE_KOREAN;
        if (id == R.id.english) {
            language = DHWR.DLANG_ENGLISH;
            option = DHWR.DTYPE_UPPERCASE | DHWR.DTYPE_LOWERCASE | DHWR.DTYPE_SIGN | DHWR.DTYPE_NUMERIC;
        }
        mWritingRecognizer.setLanguage(language, option);
    }
}
