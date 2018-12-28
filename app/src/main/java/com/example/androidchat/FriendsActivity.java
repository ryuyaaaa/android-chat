package com.example.androidchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_friends);

        ListView listView = findViewById(R.id.friend_list_view);
        List<FriendListItem> list = new ArrayList<>();


        // 例えばfor文でFriendsListItemのインスタンスを作っていく

        for (int i = 0; i < 100; i++) {
            FriendListItem item = new FriendListItem();
            item.setText("アイテム" + i);
            item.setImageId(R.drawable.ic_launcher_foreground);
            list.add(item);
        }

        ImageArrayAdapter adapter = new ImageArrayAdapter(this, R.layout.list_view_image_item, list);
        listView.setAdapter(adapter);

        // Firebase AuthとDatabaseを初期化
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        // データベースのルートノードの参照を作成する
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // ログイン済みのユーザーかチェック
        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        }
        // ログイン済みの場合
        mUserId = mFirebaseUser.getUid();






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

    private void loadFriendsView() {
        Intent intent = new Intent(this, FriendsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
