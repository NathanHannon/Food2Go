package com.food2go;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.food2go.Common.Common;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import static java.util.Locale.CANADA;

public class Profile extends AppCompatActivity implements View.OnClickListener
{
    //Image Picker
    private String cameraFilePath;
    private static final byte GALLERY_REQUEST_CODE = 100;
    private static final byte CAMERA_REQUEST_CODE = 101;

    //Buttons
    ImageButton btnAvatar;
    Button btnSave;

    //Text Fields
    EditText txtUserName;
    EditText txtPhone;
    EditText txtPassword;

    //Labels
    TextView lblUsername;
    TextView lblPassword;
    TextView lblPhone;

    //Database
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    //User Info
    String currentUser = Common.currentUser.toString();
    String userID = Common.currentUser.getId();
    String userPhone = Common.currentUser.getPhoneNumber();
    protected String userPassword = Common.currentUser.getPassword();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Labels
        lblUsername = findViewById(R.id.user_name_label);
        lblPhone = findViewById(R.id.phone_label);
        lblPassword = findViewById(R.id.password_label);

        //Text Fields
        txtPhone = findViewById(R.id.editPhone);
        txtPassword = findViewById(R.id.editPassword);
        txtUserName = findViewById(R.id.editUserName);

        //Set Text
        txtUserName.setText(userID);
        txtPhone.setText(userPhone);
        txtPassword.setText(userPassword);

        //Buttons
        btnAvatar = findViewById(R.id.avatar);
        btnAvatar.setOnClickListener(this);
        btnSave = findViewById(R.id.save);
        btnSave.setOnClickListener(this);
    }

    public void NullMediaException() throws Exception {
        throw new Exception("Media not found!");
    }

    private void SaveUser()
    {
        db.child("Users").child(currentUser).child(userID).setValue(txtUserName.toString());
        db.child("Users").child(currentUser).child(userPhone).setValue(Integer.parseInt(txtPhone.getText().toString()));
        db.child("Users").child(currentUser).child(userPassword).setValue(txtPassword.getText().toString());
    }
    //Gallery
    private void PickFromGallery()
    {
        //Create an Intent with action as ACTION_PICK
        Intent gallery_intent= new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        gallery_intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        gallery_intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(gallery_intent,GALLERY_REQUEST_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == GALLERY_REQUEST_CODE)
            {
                //data.getData returns the content URI for the selected Image
                Uri selectedImage = data.getData();
                btnAvatar.setImageURI(selectedImage);
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                // Get the cursor
                Cursor cursor = getContentResolver().query(Objects.requireNonNull(selectedImage), filePathColumn, null, null, null);
                // Move to first row
                Objects.requireNonNull(cursor).moveToFirst();
                //Get the column index of MediaStore.Images.Media.DATA
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                //Gets the String value in the column
                String imgDecodableString = cursor.getString(columnIndex);

                cursor.close();
                // Set the Image in ImageView after decoding the String
                btnAvatar.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
            }
            else if (requestCode == CAMERA_REQUEST_CODE)
            {
                btnAvatar.setImageURI(Uri.parse(cameraFilePath));
            }
        }
    }
    //Camera
    private void CaptureFromCamera() {
        try
        {
            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
            startActivityForResult(camera_intent, CAMERA_REQUEST_CODE);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private File createImageFile() throws IOException
    {
        // Create an image file name
        DateFormat timeStamp = DateFormat.getDateInstance(DateFormat.LONG, CANADA);
        String imageFileName = "JPEG_" + timeStamp.toString() + "_";
        //This is the directory in which the file will be created. This is the default location of Camera photos
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for using again
        cameraFilePath = "file://" + image.getAbsolutePath();
        return image;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.avatar:
            {
                PickFromGallery();
            }
            case R.id.save:
            {
                SaveUser();
                Toast.makeText(Profile.this, "Saved!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

