package org.gasmyr.travelmantics.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.gasmyr.travelmantics.ListActivity;
import org.gasmyr.travelmantics.R;
import org.gasmyr.travelmantics.core.TravelDeal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {

    private static final int RC_SIGN_IN = 123;
    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;
    public static FirebaseUtil firebaseUtil;
    public static FirebaseAuth firebaseAuth;
    public static FirebaseAuth.AuthStateListener authStateListener;
    public static ArrayList<TravelDeal> travelDeals;
    public static boolean isAdmin = false;
    public static FirebaseStorage firebaseStorage;
    public static StorageReference storageReference;
    private static ListActivity caller;
    private static List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());

    private FirebaseUtil() {
    }


    public static void openReference(String reference, ListActivity activity) {
        if (firebaseUtil == null) {
            firebaseUtil = new FirebaseUtil();
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
            caller = activity;
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null) {
                        signIn();
                    } else {
                        checkAdmin(firebaseAuth.getUid());
                        Toaster.info(caller.getApplicationContext(), "Welcome back!");
                    }
                }
            };
            connectToStorage();
        }
        travelDeals = new ArrayList<>();
        databaseReference = firebaseDatabase.getReference().child(reference);
    }

    private static void connectToStorage() {
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child(Constants.TRAVEL_STORAGE_PIC);
    }

    private static void checkAdmin(String uid) {
        DatabaseReference reference = firebaseDatabase.getReference().child(Constants.TRAVEL_ADMIN_PATH).child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                caller.showMenu();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        reference.addChildEventListener(listener);

    }

    private static void signIn() {
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.travel)
                        .setTosAndPrivacyPolicyUrls("https://github.com/syntrydy",
                                "https://www.linkedin.com/in/gasmyrmougang/")
                        .build(),
                RC_SIGN_IN);
    }

    public static void attachListerner() {
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    public static void detachListerner() {
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

}
