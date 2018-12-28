package com.example.androidchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private String mUserId;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String name;

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
        setContentView(R.layout.activity_main);

        // Firebase AuthとDatabaseを初期化
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        // データベースのルートノードの参照を作成する
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // ログイン済みのユーザーかチェック
        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        } else {
            mUserId = mFirebaseUser.getUid();

            // ListViewをセットアップする
            final ListView listView = findViewById(R.id.listView);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
            listView.setAdapter(adapter);

            // Add items via the Button and EditText at the bottom of the view.
            final EditText text = findViewById(R.id.editText);
            final FloatingActionButton button = findViewById(R.id.addButton);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    /*
                    * .childは指定されたノードが存在する場合そのノードを参照し、存在しない場合ノードを作成する
                    * 下のコードは/users/<user id>/items/<item id>/messageパスに入力されたテキストを保存する
                    * .pushメソッドで一意のキーを使用する新しい子locationを生成する
                    * それを使ってitemが追加されるたびにその一意のキーを生成する
                    * .setValueメソッドでは定義されたパスへのデータの書き込みや置換を実行する
                    * */
                    if (!text.getText().toString().equals("")) {
                        mDatabase.child(mUserId).child("messages").push().setValue(name + ": " + text.getText().toString());
                        text.setText("");
                    }
                }
            });


            mDatabase.child(mUserId).child("profile").child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         name = (String)dataSnapshot.getValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            // Use Firebase to populate the list
            mDatabase.child(mUserId).child("messages").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //adapter.add((String)dataSnapshot.child("message").getValue());
                    adapter.add((String)dataSnapshot.getValue());
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    //adapter.remove((String) dataSnapshot.child("message").getValue());
                    adapter.remove((String) dataSnapshot.getValue());
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // メニューバーのアイテムが選択されたときの処理
        switch (item.getItemId()) {
            case R.id.menu_chat:
                break;
            /*
            case R.id.menu_friends:
                // まだ実装してない
                loadFriendsView();
                break;*/
            case R.id.menu_profile:
                loadProfileView();
                break;
            case R.id.menu_logout:
                loadLogoutView();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onStop() {
        super.onStop();
        mFirebaseAuth.signOut();
    }

    private void loadFriendsView() {
        Intent intent = new Intent(this, FriendsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadProfileView() {
        Intent intent = new Intent(this, ProfileActivity.class);
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

}
