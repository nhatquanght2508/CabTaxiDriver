package com.example.myrog.cabtaxidriver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.myrog.cabtaxidriver.Common.Common;
import com.example.myrog.cabtaxidriver.Model.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn,btnRegister;
    RelativeLayout rootLayout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                        .setDefaultFontPath("fonts/Book_Antiqua.ttf")
                                        .setFontAttrId(R.attr.fontPath)
                                        .build());
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users= db.getReference(Common.user_driver_tbl);



        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        rootLayout = (RelativeLayout) findViewById(R.id.RootLayout);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInDialog();
            }
        });
    }

    private void showSignInDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("ĐĂNG NHẬP");
        dialog.setMessage("Hãy sử dụng email để đăng nhập");


        LayoutInflater inflater = LayoutInflater.from(this);
        View signin_layout = inflater.inflate(R.layout.layout_signin,null);

        final MaterialEditText edtEmail = (MaterialEditText) signin_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = (MaterialEditText) signin_layout.findViewById(R.id.edtPassword);


        dialog.setView(signin_layout);
        dialog.setPositiveButton("ĐĂNG NHẬP", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                //set disable button
                btnSignIn.setEnabled(false);


                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootLayout,"Nhập vào Email của bạn",Snackbar.LENGTH_SHORT).show();
                    return;
                }


                if (TextUtils.isEmpty(edtPassword.getText().toString())){
                    Snackbar.make(rootLayout,"Nhập vào mật khảu của bạn",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtPassword.getText().toString().length()<6){
                    Snackbar.make(rootLayout,"Mật khẩu quá ngắn !!!",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                final SpotsDialog waitingdialog = new SpotsDialog(MainActivity.this);
                waitingdialog.show();
                //login

                auth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingdialog.dismiss();
                                startActivity(new Intent(MainActivity.this,Welcome.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingdialog.dismiss();
                        Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();

                        //Active button
                        btnSignIn.setEnabled(true);
                    }
                });


            }
        });
        dialog.setNegativeButton("HỦY BỎ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("ĐĂNG KÝ");
        dialog.setMessage("Điền đầy đủ thông tin đăng ký");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText edtEmail = (MaterialEditText) register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = (MaterialEditText) register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = (MaterialEditText) register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = (MaterialEditText) register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);

        dialog.setPositiveButton("ĐĂNG KÝ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootLayout,"Nhập vào Email của bạn",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPhone.getText().toString())){
                    Snackbar.make(rootLayout,"Nhập vào số điện thoại của bạn",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())){
                    Snackbar.make(rootLayout,"Điền vào mật khẩu cần đăng ký",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtPassword.getText().toString().length()<6){
                    Snackbar.make(rootLayout,"Mật khẩu quá ngắn !!!",Snackbar.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Users user = new Users();
                        user.setEmail(edtEmail.getText().toString());
                        user.setPassword(edtPassword.getText().toString());
                        user.setName(edtName.getText().toString());
                        user.setPhone(edtPhone.getText().toString());

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(rootLayout,"Đăng ký tài khoản thành công",Snackbar.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootLayout,"Lỗi" + e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout,"Failed" + e.getMessage(),Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.setNegativeButton("HỦY BỎ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }
}
