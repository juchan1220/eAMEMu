package tk.nulldori.eamemu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.NfcFCardEmulation;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {
    NfcAdapter nfcAdapter = null;
    NfcFCardEmulation nfcFCardEmulation = null;
    ComponentName componentName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PackageManager pm = this.getPackageManager();

        boolean isSupport = pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF);

        if(isSupport == false){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("이 기기는 지원하지 않습니다").setMessage("이 앱을 사용하기 위해서 필요한 기능을 기기에서 지원하지 않습니다.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        else {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            nfcFCardEmulation = NfcFCardEmulation.getInstance(nfcAdapter);
            componentName = new ComponentName("tk.nulldori.eamemu", "tk.nulldori.eamemu.eAMEMuService");

            boolean sys = nfcFCardEmulation.registerSystemCodeForService(componentName, "4000");
            boolean res = nfcFCardEmulation.setNfcid2ForService(componentName, "02FE123412341234");

            if(sys == true){
                Log.d("nfcFCardEmulation", "register system code is successful!");
            }
            else{
                Log.d("nfcFCardEmulation", "register system code is failed...");
            }

            if(res == true){
                Log.d("nfcFCardEmulation", "setNfcid2 is successful!");
            }
            else {
                Log.d("nfcFCardEmulation", "setNfcid2 is failed...");
            }
        }

        TextInputLayout inputLayout = findViewById(R.id.sid_input_layout);
        inputLayout.setCounterEnabled(true);
        inputLayout.setCounterMaxLength(16);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(nfcFCardEmulation != null && componentName != null){
            Log.d("MainActivity onResume()", "enabled!");
            nfcFCardEmulation.enableService(this, componentName);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(nfcFCardEmulation != null && componentName != null){
            Log.d("MainActivity onPause()", "disabled...");
            nfcFCardEmulation.disableService(this);
        }
    }

    public void applyClick(View view){
        TextInputLayout inputLayout = findViewById(R.id.sid_input_layout);
        EditText editText = inputLayout.getEditText();

        if(editText == null){
            return;
        }

        String s = editText.getText().toString().toUpperCase();

        if(s == null || s.length() < 16){
            inputLayout.setError("sid는 반드시 16자리 16진수여야 합니다.");
            return ;
        }

        s = s.toUpperCase();

        if(s.matches("[0-9a-fA-F]+") == false){
            inputLayout.setError("sid는 반드시 16자리 16진수여야 합니다.");
            return ;
        }
        if(s.substring(0,4).contentEquals("02FE") == false){
            inputLayout.setError("sid는 반드시 02FE로 시작해야합니다.");
            return ;
        }

        inputLayout.setError(null);

        if(nfcFCardEmulation != null && componentName != null){
            nfcFCardEmulation.disableService(this);
            boolean res = nfcFCardEmulation.setNfcid2ForService(componentName, s);
            if(res == true){
                Log.d("Apply","setNfcid2 is successful!");
            }
            else{
                Log.d("Apply","setNfcid2 is failed...");
            }
            nfcFCardEmulation.enableService(this, componentName);
        }
    }
}
