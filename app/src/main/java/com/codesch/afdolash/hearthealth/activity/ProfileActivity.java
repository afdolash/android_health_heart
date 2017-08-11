package com.codesch.afdolash.hearthealth.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.codesch.afdolash.hearthealth.R;
import com.codesch.afdolash.hearthealth.adapter.DataHelper;

public class ProfileActivity extends AppCompatActivity {

    private DataHelper mDbHelper;
    private Cursor cursor;

    private TextInputEditText et_name;
    private TextInputEditText et_age;
    private TextInputEditText et_gender;

    private Button btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        changeStatusBarColor();

        et_name = (TextInputEditText) findViewById(R.id.et_name);
        et_age = (TextInputEditText) findViewById(R.id.et_age);
        et_gender = (TextInputEditText) findViewById(R.id.et_gender);
        btn_save = (Button) findViewById(R.id.btn_save);

        // Database helper
        mDbHelper = new DataHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM profile WHERE no = 1", null);
        cursor.moveToFirst();

        et_name.setText(cursor.getString(1).toString());
        et_age.setText(cursor.getString(2).toString());
        et_gender.setText(cursor.getString(3).toString());

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                db.execSQL("update profile " +
                        "set name='"+ et_name.getText().toString() +"', " +
                        "age='" + et_age.getText().toString()+"', " +
                        "gender='"+ et_gender.getText().toString() +"'");
                Toast.makeText(ProfileActivity.this, "Your profile updated.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
