package com.example.readers_pub_ebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FantasyActivity extends AppCompatActivity {

    public static final int galleryPick = 1;
    private Uri imageUri;
    private ImageButton addBookCoverImage;
    private EditText addContent, addTitle, addescription;
    private Button updateBookButton;
    private String description, title, content;
    private StorageReference fantasyBookCoverReference;
    private String  downloadUrl;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference fantasyBookRef;
    private ProgressDialog progressDialog;
    private long fantasyBookCount = 0;
    private AtomicInteger count = new AtomicInteger(0);
    private  int firstCount = 0;
    private String bookCount ,saveCurrentDate, saveCurrentTime, postRandomName, currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fantasy);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("fantasy");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        fantasyBookCoverReference = FirebaseStorage.getInstance().getReference();
        fantasyBookRef = FirebaseDatabase.getInstance().getReference().child("fantasy");
        progressDialog = new ProgressDialog(this);

        addBookCoverImage = findViewById(R.id.FantasybookCover);
        addTitle = findViewById(R.id.FantasybookTitle);
        addContent = findViewById(R.id.FantasybookContent);
        addescription = findViewById(R.id.FantasybookDescription);
        updateBookButton = findViewById(R.id.updateFantasyBook);

        addBookCoverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        updateBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateFantasyBookInfo();
            }
        });
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
        }
    }


    private void validateFantasyBookInfo() {
        description = addescription.getText().toString();
        title = addTitle.getText().toString();
        content = addContent.getText().toString();
        if (imageUri == null){
            Toast.makeText(this, "please select an image.. ", Toast.LENGTH_LONG).show();
        }else if (description.isEmpty()|| title.isEmpty()|| content.isEmpty()){
            Toast.makeText(this, "please input book contents.. ", Toast.LENGTH_LONG).show();
        }else {


            progressDialog.setTitle("adding new post");
            progressDialog.setMessage("please wait, while we update your post");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            storeImageToFirebaseStorage();
        }
    }


    private void storeImageToFirebaseStorage() {

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat(" dd-MMMM-YYYY");
        saveCurrentDate =currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime =currentTime.format(callForTime.getTime());

        postRandomName = saveCurrentDate +saveCurrentTime;

        firstCount = count.incrementAndGet();
        bookCount = String.valueOf(firstCount);


        final StorageReference filePath = fantasyBookCoverReference.child("bookCoverImages").child(imageUri.getLastPathSegment()  + postRandomName + ".jpg");

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
        });
    }

    private void saveImagetoDatabase() {

        fantasyBookRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                fantasyBookCount = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        HashMap postMap = new HashMap();
        postMap.put("content", content);
        postMap.put("description", description);
        postMap.put("postImage", downloadUrl);
        postMap.put("counter",fantasyBookCount);
        postMap.put("title", title);

        fantasyBookRef.child( bookCount + postRandomName).updateChildren(postMap)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()){
                            progressDialog.dismiss();

                            Toast.makeText(FantasyActivity.this, "new book is updated Successfully ", Toast.LENGTH_SHORT).show();
                        }else {
                            String message = task.getException().getMessage();
                            progressDialog.dismiss();
                            Toast.makeText(FantasyActivity.this, "Error occurred while updating ur post:  " + message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}