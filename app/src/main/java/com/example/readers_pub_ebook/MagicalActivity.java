package com.example.readers_pub_ebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class MagicalActivity extends AppCompatActivity {

    public static final int galleryPick = 1;
    private Uri imageUri,pdfUri;
    private ImageButton addBookCoverImage;
    private EditText addContent, addTitle, addDescription;
    public String description, title, postRandomName, currentUserID, downloadUrl, pdfUrl;
    private StorageReference storageReference;
    private DatabaseReference magicalBookRef;
    private ProgressDialog progressDialog;
    private long magicalBookCount = 0;
    //    private AtomicInteger count = new AtomicInteger(0);
//    private int firstCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magical);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Magical");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        magicalBookRef = FirebaseDatabase.getInstance().getReference().child("magical");
        progressDialog = new ProgressDialog(this);

        addBookCoverImage = findViewById(R.id.magicalbookCover);
        addTitle = findViewById(R.id.magicalbookTitle);
        addContent = findViewById(R.id.magicalbookContent);
        addDescription = findViewById(R.id.magicalbookDescription);
        Button updateBookButton = findViewById(R.id.updateMagicalBook);
        Button selectPdfFile = findViewById(R.id.addMagicalPdfFile);

        addBookCoverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        updateBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateMagicalBookInfo();
            }
        });


        selectPdfFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MagicalActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    selectPdf();
                }else{
                    ActivityCompat.requestPermissions(MagicalActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 9  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectPdf();
        }else{
            Toast.makeText(this, "please grant permission ", Toast.LENGTH_LONG).show();
        }
    }

    private void selectPdf() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 56);
    }

    private void openGallery() {
        Intent GalleryIntent = new Intent();
        GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        GalleryIntent.setType("image/*");
        startActivityForResult(GalleryIntent,galleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null){

            imageUri = data.getData();
            addBookCoverImage.setImageURI(imageUri);
        } else if (requestCode == 56 && resultCode == RESULT_OK && data != null){
            pdfUri = data.getData();
            addContent.setText(String.format("%s%s", pdfUri, getString(R.string.pdf)));
        }else {
            Toast.makeText(this, "please select a file", Toast.LENGTH_LONG).show();
        }
    }


    private void validateMagicalBookInfo() {
        description = addDescription.getText().toString();
        title = addTitle.getText().toString();
//        content = addContent.getText().toString();
        if (imageUri == null){
            Toast.makeText(this, "please select an image.. ", Toast.LENGTH_LONG).show();
        }else if (pdfUri == null){
            Toast.makeText(this, "please select a pdf file.. ", Toast.LENGTH_LONG).show();
        }else if (description.isEmpty()|| title.isEmpty()){
            Toast.makeText(this, "please input book contents.. ", Toast.LENGTH_LONG).show();
        }else {


            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("adding new book");
            progressDialog.setMessage("please wait, while we update your book");
            progressDialog.show();
            progressDialog.setProgress(0);
            progressDialog.setCanceledOnTouchOutside(true);
            storeImageToFirebaseStorage();
        }
    }


    private void storeImageToFirebaseStorage() {

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat(" dd-MMM-YYYY", Locale.ENGLISH);
        String saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        String saveCurrentTime = currentTime.format(callForTime.getTime());
        postRandomName = saveCurrentDate + saveCurrentTime;

        final StorageReference pdfPath = storageReference.child("pdfFilePath").child(postRandomName + ".pdf");
        final StorageReference filePath = storageReference.child("bookCoverImages").child(imageUri.getLastPathSegment()  + postRandomName + ".jpg");

        pdfPath.putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getTask().isSuccessful()){
                    pdfPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            pdfUrl = uri.toString();
                            saveImagetoDatabase();
                        }
                    });
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);

            }
        });

        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getTask().isSuccessful()){
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            saveImagetoDatabase();

                        }
                    });
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);

            }
        });
    }

    private void saveImagetoDatabase() {

        magicalBookRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
//              firstCount = count.incrementAndGet();
                    magicalBookCount = dataSnapshot.getChildrenCount();
                }else {
                    magicalBookCount = 0;
//                    firstCount = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("content", pdfUrl);
        postMap.put("description", description);
        postMap.put("bookCover", downloadUrl);
//        postMap.put("counter",magicalBookCount);
        postMap.put("title", title);

        magicalBookRef.child(currentUserID + postRandomName).updateChildren(postMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                            Toast.makeText(MagicalActivity.this, "new book is updated Successfully ", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }else {
                            String message = Objects.requireNonNull(task.getException()).getMessage();
                            progressDialog.dismiss();
                            Toast.makeText(MagicalActivity.this, "Error occurred while updating ur book:  " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void sendUserToMainActivity(){
        Intent MainIntent = new Intent(MagicalActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

}