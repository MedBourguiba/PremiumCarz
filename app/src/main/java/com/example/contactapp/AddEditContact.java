package com.example.contactapp;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.canhub.cropper.CropImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageActivity;

import java.security.Permission;

public class AddEditContact extends AppCompatActivity {

    private ImageView profileIv;
    private EditText nameEt,phoneEt,emailEt,noteEt;
    private FloatingActionButton fab;

    //String variable;
    private String id,image,name,phone,email,note,addedTime,updatedTime;
    private Boolean isEditMode;

    //action bar
    private ActionBar actionBar;

    //permission constant
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int REQUEST_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 200;
    private static final int IMAGE_FROM_GALLERY_CODE = 300;
    private static final int IMAGE_FROM_CAMERA_CODE = 400;

    private static final int READ_STORAGE_PERMISSION_CODE = 103;

    // string array of permission
    private String[] cameraPermission;
    private String[] storagePermission;

    //Image uri var
    private Uri imageUri;

    //database helper
    private DbHelper dbHelper;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);

        // Initialize db and permissions
        dbHelper = new DbHelper(this);
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_IMAGES};

        // Initialize actionBar
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Initialize view elements
        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        emailEt = findViewById(R.id.emailEt);
        noteEt = findViewById(R.id.noteEt);
        fab = findViewById(R.id.fab);

        // Get intent data
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);

        if (isEditMode) {
            // Set toolbar title for editing
            actionBar.setTitle("Update Reclaim");

            // Get other values from intent
            id = intent.getStringExtra("ID");
            name = intent.getStringExtra("NAME");
            phone = intent.getStringExtra("PHONE");
            email = intent.getStringExtra("EMAIL");
            note = intent.getStringExtra("NOTE");
            addedTime = intent.getStringExtra("ADDEDTIME");
            updatedTime = intent.getStringExtra("UPDATEDTIME");

            // Get image URI string from intent
            image = intent.getStringExtra("IMAGE_URI");


                // Default image if no image is available
                profileIv.setImageResource(R.drawable.ic_baseline_person_24);


            // Set values in editText fields
            nameEt.setText(name);
            phoneEt.setText(phone);
            emailEt.setText(email);
            noteEt.setText(note);

            imageUri = Uri.parse(image);

            // If image is empty, set default icon, otherwise load the image
            if (image.equals("")) {
                profileIv.setImageResource(R.drawable.ic_baseline_person_24);
            } else {
                // Grant persistable URI permission before setting the image URI
                grantUriPermission(imageUri);

                profileIv.setImageURI(imageUri);
            }

        } else {
            // Add mode
            actionBar.setTitle("Reclaim Now");
        }

        // Set up event handlers
        fab.setOnClickListener(v -> {
            // Optionally show a confirmation dialog before saving and going back
            new AlertDialog.Builder(this)
                    .setTitle("Save Changes?")
                    .setMessage("Do you want to save the changes before going back?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        saveData(); // Save the data
                        finish(); // Go back after saving
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        finish(); // Go back without saving
                    })
                    .show();
        });

        profileIv.setOnClickListener(v -> showImagePickerDialog());
    }

    // Method to handle URI permissions
    private void grantUriPermission(Uri imageUri) {
        try {
            // Check and grant permission for the URI
            getContentResolver().takePersistableUriPermission(imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (SecurityException e) {
            Log.e("AddEditContact", "Permission denied for URI: " + imageUri.toString());
            e.printStackTrace();
        }
    }



    private void showImagePickerDialog() {

        //option for dialog
        String options[] = {"Camera","Gallery"};

        // Alert dialog builder
        AlertDialog.Builder builder  = new AlertDialog.Builder(this);

        //setTitle
        builder.setTitle("Choose An Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle item click
                if (which == 0){ //start from 0 index
                    //camera selected
                    if (!checkCameraPermission()){
                        //request camera permission
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }
                        
                }else if (which == 1){
                    //Gallery selected
                    if (!checkStoragePermission()){
                        //request storage permission
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }
                    
                }
            }
        }).create().show();
    }

    private void pickFromGallery() {
        //intent for taking image from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*"); // only Image

        startActivityForResult(galleryIntent,IMAGE_FROM_GALLERY_CODE);
    }

    private void pickFromCamera() {

//       ContentValues for image info
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"IMAGE_TITLE");
        values.put(MediaStore.Images.Media.DESCRIPTION,"IMAGE_DETAIL");

        //save imageUri
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to open camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

        startActivityForResult(cameraIntent,IMAGE_FROM_CAMERA_CODE);
    }

    private void saveData() {
        // Take user-given data in variables
        name = nameEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        note = noteEt.getText().toString().trim();

        // Get current time to save as added time
        String timeStamp = "" + System.currentTimeMillis();

        // Validate fields to ensure they are not empty
        if (name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Name is required", Toast.LENGTH_SHORT).show();
            return; // Return early if validation fails
        }

        if (phone.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Phone number is required", Toast.LENGTH_SHORT).show();
            return; // Return early if validation fails
        }

        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(getApplicationContext(), "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return; // Return early if phone number is invalid
        }

        if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Email is required", Toast.LENGTH_SHORT).show();
            return; // Return early if validation fails
        }

        if (!isValidEmail(email)) {
            Toast.makeText(getApplicationContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
            return; // Return early if email is invalid
        }

        if (note.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Note is required", Toast.LENGTH_SHORT).show();
            return; // Return early if validation fails
        }

        // If all fields are filled and valid, proceed with saving
        if (!name.isEmpty() && !phone.isEmpty() && !email.isEmpty() && !note.isEmpty()) {
            // Check if in edit or add mode to save data in SQL
            if (isEditMode) {
                // Edit mode
                dbHelper.updateContact(
                        "" + id,
                        "" + image, // Ensure 'image' is correctly assigned elsewhere
                        "" + name,
                        "" + phone,
                        "" + email,
                        "" + note,
                        "" + addedTime,
                        "" + timeStamp // updated time will be the new time
                );
                Toast.makeText(getApplicationContext(), "Updated Successfully....", Toast.LENGTH_SHORT).show();

            } else {
                // Add mode
                if (imageUri != null) {
                    Log.d("SaveData", "Image URI before insert: " + imageUri.toString());
                    long id = dbHelper.insertContact(
                            "" + imageUri.toString(), // Only call toString() if imageUri is not null
                            "" + name,
                            "" + phone,
                            "" + email,
                            "" + note,
                            "" + timeStamp,
                            "" + timeStamp
                    );
                    Toast.makeText(getApplicationContext(), "Inserted Successfully.... " + id, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
                }
            }

            // After successfully saving data, send an SMS
            String message = "Your reclaim is successfully sent !"; // Customize your message here
            TwilioService.sendSms(phone, message);
            // Go back to the previous screen or handle post-save behavior
            finish(); // Or any other navigation you'd prefer after saving
        }
    }


    // Method to validate phone number (simple regex for example)
    private boolean isValidPhoneNumber(String phone) {
        String phoneRegex = "^[+]?[0-9]{10,13}$"; // Example: allows for international format and numbers
        return phone.matches(phoneRegex);
    }

    // Method to validate email address (simple regex)
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"; // Basic email format validation
        return email.matches(emailRegex);
    }

    //ctr + O

    //back button click
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    //check camera permission
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result & result1;
    }

    //request for camera permission
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_PERMISSION_CODE); // handle request permission on override method
    }

    //check storage permission
    private boolean checkStoragePermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result1;
    }

    //request for camera permission
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_PERMISSION_CODE);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            switch (requestCode) {
                case CAMERA_PERMISSION_CODE:
                    // Handle CAMERA and STORAGE permissions
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
                        // Both permissions granted, proceed to pick from camera
                        pickFromCamera();
                    } else {
                        // Permissions denied, inform user and guide them to settings
                        Toast.makeText(getApplicationContext(), "Camera & Storage Permission needed.", Toast.LENGTH_SHORT).show();
                        showPermissionExplanation("Camera & Storage", requestCode);
                    }
                    break;

                case STORAGE_PERMISSION_CODE:
                    // Handle STORAGE permission
                    boolean storagePermissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storagePermissionAccepted) {
                        // Permission granted, proceed to pick from gallery
                        pickFromGallery();
                    } else {
                        // Storage permission denied, show a message and guide to settings
                        Toast.makeText(getApplicationContext(), "Storage Permission needed.", Toast.LENGTH_SHORT).show();
                        showPermissionExplanation("Storage", requestCode);
                    }
                    break;

                case READ_STORAGE_PERMISSION_CODE:
                    // Handle read external storage permission
                    boolean readStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (readStorageAccepted) {
                        // Permission granted, load the image
                        loadImage();
                    } else {
                        // Permission denied, show a message
                        Toast.makeText(this, "Read Storage Permission needed to load image.", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    private void loadImage() {
        // Assuming imageUri is the URI you are loading from the Intent
        if (imageUri != null) {
            // Grant Persistable URI Permission
            try {
                getContentResolver().takePersistableUriPermission(imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (SecurityException e) {
                Log.e("AddEditContact", "Permission denied for URI: " + imageUri.toString());
                e.printStackTrace();
            }

            // Load the image after granting the permission
            profileIv.setImageURI(imageUri);
        } else {
            // Handle case where the image URI is null or empty
            profileIv.setImageResource(R.drawable.ic_baseline_person_24);
        }
    }
    /**
     * Show an explanation to the user on why the permission is needed and guide them to the settings page.
     * @param permissionName The name of the permission being requested.
     * @param requestCode The request code for the permission.
     */
    private void showPermissionExplanation(String permissionName, int requestCode) {
        // Here, you can show a dialog to explain why the permission is necessary.
        // If the user has denied the permission multiple times, give an option to go to app settings.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(permissionName + " permission is required to proceed. Please enable it in the settings.")
                .setCancelable(false)
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Redirect user to app settings
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, requestCode);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle cancellation if needed
                        dialog.dismiss();
                    }
                });
        builder.show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_FROM_GALLERY_CODE) {
                // Get the URI of the selected image
                imageUri = data.getData();
                Log.d("ImageSelected", "Image URI from Gallery: " + imageUri.toString());
            }
            /*else if (requestCode == IMAGE_FROM_CAMERA_CODE) {
                // Get the URI of the captured image from camera
                imageUri = ;
                Log.d("ImageSelected", "Image URI from Camera: " + imageUri.toString());
            }

             */
            // Optionally, update the UI (e.g., show the image in an ImageView)
            if (imageUri != null) {
                profileIv.setImageURI(imageUri);  // Update image view with the selected image
            }
        }
    }


    // create view object in java file
    // Profile image taking with user permission and crop functionality
    // first permission from manifest,check,request permission
    // by clicking profileIv open dialog to choose image
    // pickImage and save in ImageUri variable
    // create activity for crop image in manifest file
    // next tutorial we create SQLite database and Add data.
    // create a class called "Constants" for database and table filed title
    // now insert data in database from AddEditContact Class
    // now run application , we done for our insert function





}