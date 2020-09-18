package com.example.readers_pub_ebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Transaction;

import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Home");


        navigationView = findViewById(R.id.navigation);
        drawerLayout = findViewById(R.id.drawerLayout);

        View navView = navigationView.inflateHeaderView(R.layout.headers_view);
        TextView navHeading = (TextView)navView.findViewById(R.id.genres);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                PickItem(menuItem);
                return false;
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();



    }

    public void PickItem(MenuItem menuItem){
        int id = menuItem.getItemId();

        if (id == R.id.fantasy){
            Intent intent = new Intent(this, FantasyActivity.class);
            startActivity(intent);

        }else if (id == R.id.medicals){
            Intent intent = new Intent(this, MedicalsActivity.class);
            startActivity(intent);

        }else if (id ==R.id.romance){
            Intent intent = new Intent(this, RomanceActivity.class);
            startActivity(intent);

        }else if(id == R.id.science){
            Intent intent = new Intent(this, ScienceActivity.class);
            startActivity(intent);

        }else if(id == R.id.fiction){
            Intent intent = new Intent(this, FictionActivity.class);
            startActivity(intent);

        }else if (id == R.id.classic) {
            Intent intent = new Intent(this, ClassicActivity.class);
            startActivity(intent);
        }else if (id == R.id.comic){
            Intent intent = new Intent(this, ComicActivity.class);
            startActivity(intent);
        }else if (id == R.id.magical){
            Intent intent = new Intent(this, MagicalActivity.class);
            startActivity(intent);
        }else if (id == R.id.history){
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




}