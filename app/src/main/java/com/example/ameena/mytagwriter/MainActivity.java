package com.example.ameena.mytagwriter;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button nfcSwitch,nfcWriterSwitch;
    private NfcAdapter nfcAdapter;
    public PendingIntent mNfcPendingIntent;
    boolean readerModeIsEnabled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcSwitch = (Button) findViewById(R.id.nfcswitch);
        nfcWriterSwitch = (Button) findViewById(R.id.writerswitch);
        nfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
        if(!nfcAdapter.isEnabled()){
            nfcSwitch.setText(R.string.when_nfc_off);
        }
        else
        {
            nfcSwitch.setText(R.string.when_nfc_on);
            nfcWriterSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   readerModeIsEnabled = enableTagWriteMode();
                }
            });
        }
        nfcSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!nfcAdapter.isEnabled())
                {
                    Toast.makeText(MainActivity.this, "You need to switch on NFC to Use this application", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                }
            }
        });

     }

    private boolean enableTagWriteMode() {
        mNfcPendingIntent = PendingIntent.getActivity(MainActivity.this, 0,
                new Intent(MainActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
                IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
        nfcAdapter.enableForegroundDispatch(MainActivity.this,mNfcPendingIntent, mWriteTagFilters, null );

        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)){
            Log.d("aquib","ameen tag is detected!!!!!");
            Toast.makeText(this,"A new tag is detected!! ",Toast.LENGTH_LONG).show();
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefRecord record = NdefRecord.createMime( ((TextView)findViewById(R.id.mimetype)).getText().toString(),
                    ((TextView)findViewById(R.id.mimevalue)).getText().toString().getBytes());
            NdefMessage ndefmessages = new NdefMessage(new NdefRecord[]{record});
            if (writeTag(ndefmessages, detectedTag)){
                Toast.makeText(this, "Successfully wrote to tag", Toast.LENGTH_SHORT).show();
                Log.d("aquib","successfully wrote to tag");
                nfcWriterSwitch.setText(R.string.when_writer_disbaled);
            }

        }
    }

    private boolean writeTag(NdefMessage ndefmessages, Tag detectedTag) {
        int size = ndefmessages.toByteArray().length;
        nfcWriterSwitch.setText("Writing to tag");
        try{
            Ndef myndef = Ndef.get(detectedTag);
            if(myndef != null)
            {
                myndef.connect();
                if(!myndef.isWritable()){
                    Toast.makeText(this,"Unable to write on this tag as it is unwritable",Toast.LENGTH_LONG).show();
                    return false;
                }
                if(myndef.getMaxSize() < size){
                    Toast.makeText(this,"Unable to write as this tag data is size small",Toast.LENGTH_LONG).show();
                    return false;
                }
                myndef.writeNdefMessage(ndefmessages);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(detectedTag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(ndefmessages);
                        return true;
                    } catch (IOException e) {
                        return false;
                    } catch (FormatException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    return false;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
