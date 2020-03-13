package com.analysys.visualdemo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.analysys.visualdemo.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class TopVisualMainActivity extends Activity {


    private static final String TAG = "TopVisualMainActivity";
    private static final int PHOTO_WAS_PICKED = 2;
    private static final String LOGTAG = "sanbo";
    private String s = "ws://192.168.200.110:9091?appkey=7752552892442721d&version=1.0&os=Android";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_visual);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_visual, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (PHOTO_WAS_PICKED == requestCode && null != data) {
            final Uri imageUri = data.getData();
            if (null != imageUri) {
                // AsyncTask, please...
                final ContentResolver contentResolver = getContentResolver();
                try {
                    final InputStream imageStream = contentResolver.openInputStream(imageUri);
                    System.out.println("DRAWING IMAGE FROM URI " + imageUri);
                    final Bitmap background = BitmapFactory.decodeStream(imageStream);
                    getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(),
                            background));
                } catch (final FileNotFoundException e) {
                    Log.e(LOGTAG, "Image apparently has gone away", e);
                }
            }
        }
    }

    public void dialog(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确认退出吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                TopVisualMainActivity.this.finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void pupupWindow(final View view) {
        startActivity(new Intent(this, PopupActivity.class));
    }

    public void btnListView(final View view) {
        startActivity(new Intent(this, ListViewActivity.class));
    }

    public void textView(final View view) {
        TextView tv = (TextView) this.findViewById(R.id.textView);
        tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "你点击了textview");
            }
        });
    }

    public void addButton(View view) {
        startActivity(new Intent(this, DynamicAddActivity.class));
    }

    public void buttonVis(final View view) {
        startActivity(new Intent(this, VisualMainActivity.class));
    }

    public void customPupupWindow(final View view) {
        startActivity(new Intent(this, GravityActivity.class));
    }

    public void goB(final View view) {
        startActivity(new Intent(this, VisualActivityB.class));
    }

    public void btnTestAct(final View view) {
        startActivity(new Intent(this, TestActivity.class));
    }

    public void setBackgroundImage(final View view) {
        final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PHOTO_WAS_PICKED);
    }

    private String generateDistinctId() {
        final Random random = new Random();
        final byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        return Base64.encodeToString(randomBytes, Base64.NO_WRAP | Base64.NO_PADDING);
    }

    private int hourOfTheDay() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    private long hoursSinceEpoch() {
        final Date now = new Date();
        final long nowMillis = now.getTime();
        return nowMillis / 1000 * 60 * 60;
    }

    private String domainFromEmailAddress(String email) {
        String ret = "";
        final int atSymbolIndex = email.indexOf('@');
        if ((atSymbolIndex > -1) && (email.length() > atSymbolIndex)) {
            ret = email.substring(atSymbolIndex + 1);
        }

        return ret;
    }

    public void nestingBtn(final View view) {
        Intent intent = new Intent(this, NestingActivity.class);
        startActivity(intent);
    }

    public void sendToServer(View view) {
    }

//    public void sameLayoutOne(View view) {
//        Intent intent = new Intent(this, LoginActivityA.class);
//        startActivity(intent);
//    }
//
//    public void sameLayoutTwo(View view) {
//        Intent intent = new Intent(this, LoginActivityB.class);
//        startActivity(intent);
//    }
}
