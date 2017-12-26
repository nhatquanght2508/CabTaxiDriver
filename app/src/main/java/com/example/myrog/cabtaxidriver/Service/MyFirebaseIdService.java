package com.example.myrog.cabtaxidriver.Service;

import android.util.Log;

import com.example.myrog.cabtaxidriver.Common.Common;
import com.example.myrog.cabtaxidriver.Model.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by My Rog on 12/2/2017.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("CABI",refreshedToken);
        updateTokenServer(refreshedToken);

    }

    private void updateTokenServer(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);
        Token token = new Token(refreshedToken);
        if (FirebaseAuth.getInstance().getCurrentUser()!=null) // nếu đã đăng nhập thì refresh token
        {
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);

        }
    }
}
