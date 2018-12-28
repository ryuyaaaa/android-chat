package com.example.androidchat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    Intent intent;
    Uri filePath;

    private DatabaseReference mDatabase;
    private String mUserId;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 234;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // menu.xmlの内容を読み込む
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Intentの設定
        // UserIdを受け取る
        intent = getIntent();

        final ImageView profileImage = findViewById(R.id.profile_image);
        final TextView editProfileImage = findViewById(R.id.edit_profile_image);
        final EditText profileName = findViewById(R.id.profile_name);
        final EditText profileComment = findViewById(R.id.profile_comment);
        final Button saveButton = findViewById(R.id.profile_save);

        // Firebase AuthとDatabaseを初期化
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        // データベースのルートノードの参照を作成する
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Storageの初期化
        storageReference = FirebaseStorage.getInstance().getReference();


        // ログイン済みかチェック
        if (mFirebaseAuth == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            // ログイン済みの場合
            mUserId = mFirebaseUser.getUid();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                final long ONE_MEGABYTE = 1024 * 1024;
                storageReference.child("images/" + mUserId + "/profile.jpg").getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Glide.with(ProfileActivity.this).load(bytes).into(profileImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

                mDatabase.child(mUserId).child("profile").child("name").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        profileName.setText((String)dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mDatabase.child(mUserId).child("profile").child("comment").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        profileComment.setText((String)dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                editProfileImage.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent1 = new Intent();
                        intent1.setType("image/*");
                        intent1.setAction(Intent.ACTION_GET_CONTENT);
                        System.out.println("intent開始");
                        startActivityForResult(Intent.createChooser(intent1, "Select an Image"), PICK_IMAGE_REQUEST);
                    }
                });

                saveButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        /*
                         *
                         * アイコン変更があればそれをクラウドに保存。
                         * 名前、コメント変更があればそれをデータベースに保存。
                         *
                         * */

                        /*データベースにnameを反映*/
                        if (!profileName.getText().toString().equals("")) {
                            mDatabase.child(mUserId).child("profile").child("name").setValue(profileName.getText().toString());
                            System.out.println("データベースにnameを反映");
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                            builder.setMessage("名前を入力してください")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                            builder.show();
                        }

                        /*データベースにcommentを反映*/
                        mDatabase.child(mUserId).child("profile").child("comment").setValue(profileComment.getText().toString());
                        System.out.println("データベースにcommentを反映");

                        Toast.makeText(getApplicationContext(), "保存しました", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }


    private void uploadFile() {
        System.out.println("uploadFileに入りました。");

        if (filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference riversRef = storageReference.child("images/" + mUserId + "/profile.jpg");

            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "File Uploaded", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage((int) progress + "% Uploaded...");
                        }
                    });
        } else {
            // display a error toast
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("onActivityResultに入りました。");
        ImageView profileImage = findViewById(R.id.profile_image);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                uploadFile();
                // 画像をView上に反映
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // メニューバーのアイテムが選択されたときの処理
        switch (item.getItemId()) {
            case R.id.menu_chat:
                loadChatView();
            /*
            case R.id.menu_friends:
                // まだ実装してない
                loadFriendsView();
                break;*/
            case R.id.menu_profile:
                break;
            case R.id.menu_logout:
                loadLogoutView();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadChatView() {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    private void loadFriendsView() {
        Intent intent = new Intent(this, FriendsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadLogInView() {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadLogoutView() {
        mFirebaseAuth.signOut();
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public void onStop() {
        super.onStop();
        mFirebaseAuth.signOut();
    }

}
