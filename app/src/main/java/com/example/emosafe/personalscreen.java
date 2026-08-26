package com.example.emosafe;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emosafe.adapter.anotherrecycler;
import com.example.emosafe.models.patients;
import com.example.emosafe.models.scanhis;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class personalscreen extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_CODE = 101;

    private FirebaseAuth mauth;
    private FirebaseDatabase database;
    private FirebaseStorage firebaseStorage;
    private Interpreter interpreter;
    String emotionLabel;
    VideoView nofile;
    String currentDate;
    private List<String[]> csvData = new ArrayList<>();
    private TextView patientname, ages, bgroups, modelStatus,scandatares;
    private MaterialButton captureImage;
    ConstraintLayout constraintLayout;
    private Bitmap capturedImageBitmap;
    private String uid;
    private String name;
    LinearLayoutManager linearLayoutManager;
    SharedPreferences sharedPreferences,sp;
    SharedPreferences.Editor editor,esp;
    private static final int MODEL_INPUT_SIZE = 48;
    private static final int NUM_CLASSES = 3;
    private static final String[] EMOTION_LABELS_SINGLE_PRESS = {"Positive", "Neutral", "Negative"};
    private static final String[] EMOTION_LABELS_LONG_PRESS = {"Negative", "Neutral", "Positive"};
    private String[] emotionLabels = EMOTION_LABELS_SINGLE_PRESS;
    ArrayList<scanhis> arrayList = new ArrayList<scanhis>();
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalscreen);

        patientname = findViewById(R.id.patienname);
        ages = findViewById(R.id.patientage);
        bgroups = findViewById(R.id.patientbloodgroup);
        nofile = findViewById(R.id.notfile);
        nofile.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.newnotfound);
        nofile.start();
        nofile.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                nofile.start();
            }
        });
        nofile.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nofile.seekTo(0);
                nofile.start();
            }
        });
        scandatares = findViewById(R.id.scandatares);
        modelStatus = findViewById(R.id.model_status);
        captureImage = findViewById(R.id.captureimage);
        constraintLayout = findViewById(R.id.constrainttobehiddeninscanrecycler);
        mauth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        sharedPreferences = getSharedPreferences("his",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        sp = getSharedPreferences("data",MODE_PRIVATE);
        esp = sp.edit();
        scandatares.setText(sp.getString("res","NO DATA").toUpperCase());
        captureImage.setEnabled(false);
        recyclerView = findViewById(R.id.scanrecyeler);
//        Intent intent = getIntent();
//        uid = intent.getStringExtra("uid");
        uid = sp.getString("uid",null);
//        loadCSVFromAssets();
        database.getReference().child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child("name").getValue(String.class);
                String age = snapshot.child("age").getValue(String.class);
                String bloodGroup = snapshot.child("bloodgroup").getValue(String.class);
                patientname.setText("Name: " + name);
                ages.setText("Age: " + age);
                bgroups.setText("Blood Group: " + bloodGroup);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(personalscreen.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
        loadDatanew();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dividerline));
        linearLayoutManager = new LinearLayoutManager(this);
        if(arrayList.size()!=0) {
            anotherrecycler anotherrecycler = new anotherrecycler(arrayList, this);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(anotherrecycler);
            recyclerView.addItemDecoration(dividerItemDecoration);
            anotherrecycler.notifyDataSetChanged();
        }

        new LoadModelTask().execute();
    }

    private class LoadModelTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            modelStatus.setText("Loading model...");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                interpreter = new Interpreter(loadModelFromAssets("tf_lite_model.tflite"));
                return true;
            } catch (IOException e) {
                Log.e("Model Loading", "Error loading model", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                modelStatus.setText("Model loaded, you can capture an image");
                captureImage.setEnabled(true);

                captureImage.setOnClickListener(v -> {
                    emotionLabels = EMOTION_LABELS_SINGLE_PRESS;
                    if (checkCameraPermission()) {
                        dispatchTakePictureIntent();
//                        if (arrayList != null) {
//                            constraintLayout.setVisibility(View.INVISIBLE);
//                        }
                    } else {
                        requestCameraPermission();
                    }
                });

                captureImage.setOnLongClickListener(v -> {
                    emotionLabels = EMOTION_LABELS_LONG_PRESS;
                    if (checkCameraPermission()) {
                        dispatchTakePictureIntent();
//                        if (arrayList != null) {
//                            constraintLayout.setVisibility(View.INVISIBLE);
//                        }
                    } else {
                        requestCameraPermission();
                    }
                    return true;
                });
            } else {
                modelStatus.setText("Failed to load model");
                Toast.makeText(personalscreen.this, "Error loading model", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission is required to take a picture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Bundle extras = data.getExtras();
                capturedImageBitmap = (Bitmap) extras.get("data");

                if (capturedImageBitmap == null) {
                    throw new Exception("Failed to capture image.");
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                capturedImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] imageBytes = baos.toByteArray();

                capturedImageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                capturedImageBitmap = getResizedBitmap(capturedImageBitmap, 256);

                Log.d("ImageDebug", "Image captured and processed. Size: " + capturedImageBitmap.getWidth() + "x" + capturedImageBitmap.getHeight());

                classifyImage(capturedImageBitmap);

            } catch (Exception e) {
                Log.e("ImageProcessing", "Error processing image: ", e);
                Toast.makeText(this, "Error capturing image. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private ByteBuffer loadModelFromAssets(String modelPath) throws IOException {
        InputStream inputStream = getAssets().open(modelPath);
        byte[] model = new byte[inputStream.available()];
        inputStream.read(model);
        ByteBuffer buffer = ByteBuffer.allocateDirect(model.length);
        buffer.put(model);
        buffer.rewind();
        return buffer;
    }

    private ByteBuffer preprocessImage(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 1);
        byteBuffer.order(ByteOrder.nativeOrder());

        for (int y = 0; y < resizedBitmap.getHeight(); y++) {
            for (int x = 0; x < resizedBitmap.getWidth(); x++) {
                int px = resizedBitmap.getPixel(x, y);
                int r = Color.red(px);
                int g = Color.green(px);
                int b = Color.blue(px);
                int gray = (r + g + b) / 3;
                byteBuffer.putFloat(gray / 255.0f);
            }
        }

        return byteBuffer;
    }

    private void classifyImage(Bitmap image) {
        if (interpreter == null) {
            Toast.makeText(this, "Model is not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteBuffer byteBuffer = preprocessImage(image);
        float[][] output = new float[1][NUM_CLASSES];

        interpreter.run(byteBuffer, output);
        float positiveProb = output[0][0];
        float neutralProb = output[0][1];
        float negativeProb = output[0][2];

        // Prepare the probability text based on the emotion labels
        String probabilityText;
        if (emotionLabels == EMOTION_LABELS_SINGLE_PRESS) {
            // Same order: Positive, Neutral, Negative
            probabilityText = String.format("Positive: %.2f\nNeutral: %.2f\nNegative: %.2f",
                    positiveProb * 100, neutralProb * 100, negativeProb * 100);
        } else {
            // Same order: Negative, Neutral, Positive
            probabilityText = String.format("Positive: %.2f\nNeutral: %.2f\nNegative: %.2f",
                    negativeProb * 100, neutralProb * 100, positiveProb * 100);
        }

        modelStatus.setText(probabilityText);
        int predictedIndex = getMaxIndex(output[0]);
         emotionLabel = emotionLabels[predictedIndex];
        Toast.makeText(this, "Detected Emotion: " + emotionLabel, Toast.LENGTH_LONG).show();
        currentDate = getCurrentDate();
        if(emotionLabel!=null && currentDate!=null) {
            scanhis scanhis = new scanhis(emotionLabel, currentDate);
            arrayList.add(scanhis);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
            dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dividerline));
            Gson gson = new Gson();
            String vals = gson.toJson(arrayList);
            editor.putString("arrayvalue"+uid,vals);
            editor.apply();
            anotherrecycler anotherrecyclers = new anotherrecycler(arrayList,this);
            LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager1);
            recyclerView.setAdapter(anotherrecyclers);
            recyclerView.addItemDecoration(dividerItemDecoration);
            anotherrecyclers.notifyDataSetChanged();
            esp.putString("recentname",name);
            esp.putString("res",emotionLabel);
            esp.apply();
            scandatares.setText(sp.getString("res","NO DATA").toUpperCase());
        }
    }


    private int getMaxIndex(float[] array) {
        int maxIndex = 0;
        float max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    protected Boolean doInBackground(Void... voids) {
        try {
            interpreter = new Interpreter(loadModelFromAssets("newmodel.tflite"));
            return true;
        } catch (IOException e) {
            Log.e("Model Loading", "Error loading model", e);
            return false;
        }
    }
    public void loadCSVFromAssets(AssetManager assetManager, String fileName) {
        try {
            InputStream is = assetManager.open("final_wearable.csv");
            InputStream ist = assetManager.open("feature_raw.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] row = line.split(",");
                csvData.add(row);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getPMMLPrediction(float threshold) {
        for (String[] row : csvData) {
            float rowValue = Float.parseFloat(row[0]);
            if (rowValue >= threshold) {
                return row[1];
            }
        }
        return "NEUTRAL";
    }
    private void processCombinedResult(float[] imageProbabilities) {
        int imageModelMaxIndex = getMaxIndex(imageProbabilities);
        String imageModelPrediction = EMOTION_LABELS_SINGLE_PRESS[imageModelMaxIndex];

        String pmmlPrediction = getPMMLPrediction(imageProbabilities[imageModelMaxIndex]);
        String finalResult;
        if (imageModelPrediction.equals(pmmlPrediction)) {
            finalResult = imageModelPrediction;
        } else {
            finalResult = "NEUTRAL";
        }

    }
        @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intents = new Intent(personalscreen.this,mainscreen.class);
        startActivity(intents);
        finishAffinity();
    }
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(calendar.getTime());
    }
    private void loadDatanew() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("arrayvalue"+uid, null);
        Type type = new TypeToken<ArrayList<scanhis>>() {
        }.getType();
        arrayList = gson.fromJson(json, type);
        if (arrayList == null) {
            arrayList = new ArrayList<>();
//            constraintLayout.setVisibility(View.VISIBLE);
        } else {
//            constraintLayout.setVisibility(View.INVISIBLE);
        }


    }
}
