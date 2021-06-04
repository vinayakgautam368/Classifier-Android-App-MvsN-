package com.example.memesvsnotes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.memesvsnotes.ml.Model1;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    
    private ImageView imgView;
    private Button select,predict;
    private TextView tv;
    private Bitmap img;

    private static final int REQUEST_IMAGE_CAPTURE=101;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        imgView=(ImageView) findViewById(R.id.imageView);
        tv=(TextView) findViewById(R.id.textView2);
        select=(Button) findViewById(R.id.button);
        predict=(Button) findViewById(R.id.button2);
        
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                Intent intent= new Intent(Intent.ACTION_GET_CONTENT);  // here when we click on the button then new intent means new screen will open
                intent.setType("image/*");                              // in which we have all the images of the device.
                int requestCode;
                startActivityForResult(intent,requestCode=100);
                
                
                
            }
        });



        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                img=Bitmap.createScaledBitmap(img,224,224, true);    // here we scale the image

                try {
                    Model1 model = Model1.newInstance(getApplicationContext());

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
                    TensorImage tensorImage=new TensorImage(DataType.FLOAT32);
                    tensorImage.load(img);
                    ByteBuffer byteBuffer=tensorImage.getBuffer();
                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    Model1.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    // Releases model resources if no longer used.
                    model.close();

                    if(outputFeature0.getFloatArray()[0]<0.5){
                        tv.setText("MEME");

                    }
                    else{
                        tv.setText("NOTE");
                    }


                } catch (IOException e) {
                    // TODO Handle the exception
                }



            }

        });     
        

    }

    public void takePicture(View view){

        Intent imageTakeIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(imageTakeIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(imageTakeIntent,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==100){
            imgView.setImageURI(data.getData());

            Uri uri=data.getData();
            try {
                img= MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK)
        {     /* Here we first convert Bitmap into Uri and we setthe image of bitmap but give the uri data to the model .                        */

                Bundle extras=data.getExtras();
                Bitmap imageBitmap=(Bitmap) extras.get("data");
                ByteArrayOutputStream bytes =new ByteArrayOutputStream();
                String path=MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),imageBitmap,"val",null);
                Uri uri=Uri.parse(path);
                imgView.setImageBitmap(imageBitmap);
                try {
                img= MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
                } catch (IOException e) {
                e.printStackTrace();
                 }



        }

    }




}