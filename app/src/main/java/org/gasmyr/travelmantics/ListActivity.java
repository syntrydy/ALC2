package org.gasmyr.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.gasmyr.travelmantics.adapter.DealAdapter;
import org.gasmyr.travelmantics.util.FirebaseUtil;
import org.gasmyr.travelmantics.util.Toaster;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DealAdapter adapter;
    public static final int RC_SIGN_IN = 3445;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
    }

    private void fillRecyclerView() {
        recyclerView=findViewById(R.id.travelsListRv);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new DealAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu,menu);
        MenuItem item=menu.findItem(R.id.insert_menu);
        if(!FirebaseUtil.isAdmin){
            item.setVisible(false);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListerner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillRecyclerView();
        FirebaseUtil.attachListerner();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.insert_menu) {
            Intent intent=new Intent(ListActivity.this, DealActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.logout_menu) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUtil.attachListerner();
                       Toaster.info(getApplicationContext(),"Successfully sign out!");
                    }
                });
        FirebaseUtil.detachListerner();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                  fillRecyclerView();
            } else {
                Toaster.error(getApplicationContext(),"Invalid credentials!");
            }
        }
    }

    public void showMenu() {
        invalidateOptionsMenu();
    }
}
