package org.gasmyr.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.gasmyr.travelmantics.core.TravelDeal;
import org.gasmyr.travelmantics.util.Constants;
import org.gasmyr.travelmantics.util.FirebaseUtil;
import org.gasmyr.travelmantics.util.Toaster;

public class DealActivity extends AppCompatActivity {
    private static final int CODE = 32;
    private EditText titleView, priceView, descriptionView;
    private ImageView imageView;
    private Button uploadButton;
    private TravelDeal travelDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        titleView = findViewById(R.id.titleTextViewId);
        priceView = findViewById(R.id.priveTextViewId);
        descriptionView = findViewById(R.id.descriptionTextViewId);
        imageView = findViewById(R.id.imageViewId);
        uploadButton = findViewById(R.id.uploadImageId);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, getApplicationContext().getString(R.string.upload_picture)), CODE);
            }
        });
        TravelDeal receivedDeal = (TravelDeal) getIntent().getSerializableExtra(Constants.DEAL_EXTRA);
        if (receivedDeal == null) {
            receivedDeal = new TravelDeal();
        }
        this.travelDeal = receivedDeal;
        fillDeal();
    }

    private void fillDeal() {
        titleView.setText(travelDeal.getTitle());
        descriptionView.setText(travelDeal.getDescription());
        priceView.setText(travelDeal.getPrice());
        showImage(travelDeal.getImageUrl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.saveMenu).setVisible(true);
            menu.findItem(R.id.deleteMenu).setVisible(true);
            enableEditTexts(true);
        } else {
            menu.findItem(R.id.saveMenu).setVisible(false);
            menu.findItem(R.id.deleteMenu).setVisible(false);
            enableEditTexts(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveMenu:
                saveCurrentDeal();
                return true;
            case R.id.deleteMenu:
                deleteCurrentDeal();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.storageReference.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            travelDeal.setImageUrl(uri.toString());
                            travelDeal.setImageName(taskSnapshot.getStorage().getPath());
                            showImage(travelDeal.getImageUrl());

                        }
                    });
                }
            });
        }
    }

    private void saveCurrentDeal() {
        travelDeal.setDescription(descriptionView.getText().toString());
        travelDeal.setTitle(titleView.getText().toString());
        travelDeal.setPrice(priceView.getText().toString());
        if (travelDeal.getId() != null) {
            FirebaseUtil.databaseReference.child(travelDeal.getId()).setValue(travelDeal);
        } else {
            FirebaseUtil.databaseReference.push().setValue(travelDeal);
        }
        clean();
        Toaster.info(DealActivity.this, getApplicationContext().getString(R.string.deal_saved));
        backToList();
    }

    private void deleteCurrentDeal() {
        if (travelDeal == null) {
            Toaster.error(DealActivity.this, getApplicationContext().getString(R.string.deal_is_empty));
            return;
        }
        FirebaseUtil.databaseReference.child(travelDeal.getId()).removeValue();
        if (travelDeal.getImageName() != null && !travelDeal.getImageUrl().isEmpty()) {
            StorageReference ref = FirebaseUtil.firebaseStorage.getReference().child(travelDeal.getImageName());
            ref.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toaster.error(DealActivity.this, e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toaster.info(DealActivity.this, getApplicationContext().getString(R.string.deletedDealMessage));
                }
            });
        }
        Toaster.info(DealActivity.this, getApplicationContext().getString(R.string.DealRemoved));
    }

    private void backToList() {
        Intent intent = new Intent(DealActivity.this, ListActivity.class);
        startActivity(intent);
    }

    private void clean() {
        titleView.setText("");
        priceView.setText("");
        descriptionView.setText("");
        titleView.requestFocus();
    }

    public void enableEditTexts(boolean value) {
        titleView.setEnabled(value);
        priceView.setEnabled(value);
        descriptionView.setEnabled(value);
        uploadButton.setEnabled(value);
    }


    private void showImage(String url) {
        if (url != null && !url.isEmpty()) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get().load(url).resize(width, width * 2 / 3).centerCrop().into(imageView);
        }
    }
}
