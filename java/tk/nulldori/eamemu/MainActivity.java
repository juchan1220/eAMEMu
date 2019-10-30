package tk.nulldori.eamemu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.NfcFCardEmulation;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    NfcAdapter nfcAdapter = null;
    NfcFCardEmulation nfcFCardEmulation = null;
    ComponentName componentName = null;
    SharedPreferences sf = null;

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
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        else {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            nfcFCardEmulation = NfcFCardEmulation.getInstance(nfcAdapter);
            componentName = new ComponentName("tk.nulldori.eamemu", "tk.nulldori.eamemu.eAMEMuService");

            nfcFCardEmulation.registerSystemCodeForService(componentName, "4000");

            sf = getSharedPreferences("sf", MODE_PRIVATE);
            String cardId = sf.getString("cardID","");

            TextInputLayout inputLayout = findViewById(R.id.sid_input_layout);
            EditText editText = inputLayout.getEditText();

            inputLayout.setCounterEnabled(true);
            inputLayout.setCounterMaxLength(16);


            if(cardId == null){
                cardId = randomCardID();
                nfcFCardEmulation.setNfcid2ForService(componentName, cardId);
                editText.setText(cardId);
                SharedPreferences.Editor editor = sf.edit();
                editor.putString("cardID", cardId);
                editor.commit();
            }
            else{
                nfcFCardEmulation.setNfcid2ForService(componentName, cardId);
                editText.setText(cardId);
            }
        }
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
                Toast.makeText(this.getApplicationContext(), R.string.apply_success_toast, Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sf.edit();
                editor.putString("cardID", s);
                editor.commit();
                Log.d("Apply","setNfcid2 is successful!");
            }
            else{
                Toast.makeText(this.getApplicationContext(), R.string.apply_failed_toast, Toast.LENGTH_SHORT).show();
                Log.d("Apply","setNfcid2 is failed...");
            }
            nfcFCardEmulation.enableService(this, componentName);
        }
    }

    public void randomClick(View view) {
        TextInputLayout inputLayout = findViewById(R.id.sid_input_layout);
        EditText editText = inputLayout.getEditText();

        if(editText == null){
            return;
        }

        editText.setText(randomCardID());
        Toast.makeText(this.getApplicationContext(), R.string.random_generate_toast, Toast.LENGTH_SHORT).show();
    }

    public String randomCardID(){
        Random random = new Random();

        return "02FE" + String.format("%04x", random.nextInt(65536)).toUpperCase()
                + String.format("%04x", random.nextInt(65536)).toUpperCase()
                + String.format("%04x", random.nextInt(65536)).toUpperCase();
    }
}
