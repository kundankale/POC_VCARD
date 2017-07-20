package com.poc_vcf;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.vcard.VCardComposer;
import com.android.vcard.VCardInterpreter;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V21;
import com.android.vcard.exception.VCardException;
import com.intentfilter.wificonnect.WifiConnectionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //http://www.intentfilter.com/2016/08/programatically-connecting-to-wifi.html
    //https://github.com/nickrussler/Android-Wifi-Hotspot-Manager-Class

    //https://github.com/Kailash23/WiFi-Hotspot/blob/master/app/src/main/java/com/juggernaut/hotspot/MainActivity.java

    ArrayList<String>vCard;
    String vFie="contacttest.vcf";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button button  = (Button)findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


               // new GetContact().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                
                connect();
                
                
            }
        });


    }

    private void connect() {

        String str="BTG_Galaxy On7 Pro";

        WifiConnectionManager wifiConnectionManager = new WifiConnectionManager(MainActivity.this);
        wifiConnectionManager.connectToAvailableSSID(str, new WifiConnectionManager.ConnectionStateChangedListener() {
            @Override
            public void onConnectionEstablished() {

                Log.d("POC", "onConnectionEstablished: ");
            }

            @Override
            public void onConnectionError(String reason) {
                Log.d("POC", "onConnectionError: ");
            }
        });


        wifiConnectionManager.setBindingEnabled(true);
        wifiConnectionManager.checkBoundNetworkConnectivity();

    }

    public class GetContact extends AsyncTask<Void,Void,Void>{


        @Override
        protected Void doInBackground(Void... voids) {

            try {


                getContactsAsVcards();
            }
            catch(Exception e){

                Log.d("POC", "doInBackground: " + "exception " + e.getMessage());
            }
            return null;

        }
    }

    public ArrayList<String> getContactsAsVcards()
    {
        ArrayList<String> vcards = new ArrayList<String>();
        try {

        int icount=0;
        VCardComposer vCardComposer = new VCardComposer(MainActivity.this);

        vCardComposer.init();

        String storage_path = Environment.getExternalStorageDirectory().toString() + File.separator + vFie;

        File file = new File(storage_path);
        if (!file.exists()) {
            file.createNewFile();
        }
            FileOutputStream mFileOutputStream = new FileOutputStream(storage_path, false);
        do {

                String vCard = vCardComposer.createOneEntry();


                icount++;
                Log.d("poc", icount + " -- " + "getContactsAsVcards: " + vCard);
                vcards.add(vCard);









                mFileOutputStream.write(vCard.getBytes());




        } while (!vCardComposer.isAfterLast());

            mFileOutputStream.close();

        }
        catch (Exception e){

        }
        return vcards;

    }


    private void getVcardString() throws Exception {
        // TODO Auto-generated method stub
       vCard = new ArrayList<String>();  // Its global....
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if(cursor!=null&&cursor.getCount()>0)
        {
            int i;
            String storage_path = Environment.getExternalStorageDirectory().toString() + File.separator + vFie;

            File file = new File(storage_path);
            if(!file.exists()){
                file.createNewFile();
            }

            FileOutputStream mFileOutputStream = new FileOutputStream(storage_path, false);
            cursor.moveToFirst();

            Log.d("POC", "getVcardString: total " + cursor.getCount());

            for(i = 0;i<cursor.getCount();i++)
            {
                boolean bflag = get(cursor);
                Log.d("TAG", "Contact "+(i+1));
                cursor.moveToNext();

                if(bflag){
                    mFileOutputStream.write(vCard.get(i).toString().getBytes());
                }

            }
            mFileOutputStream.close();
            cursor.close();
        }
        else
        {
            Log.d("TAG", "No Contacts in Your Phone");
        }
    }

    private boolean get(Cursor cursor) {
        boolean bflag = false;
        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
        AssetFileDescriptor fd;
        try {
            fd = this.getContentResolver().openAssetFileDescriptor(uri, "r");

            if(fd!=null) {
                FileInputStream fis = fd.createInputStream();
                byte[] buf = readBytes(fis);
                fis.read(buf);
                String vcardstring = new String(buf);
                vCard.add(vcardstring);
                bflag = true;
            }
        } catch (Exception e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();

            return false;
        }

        return bflag;

    }

    public byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }


}
