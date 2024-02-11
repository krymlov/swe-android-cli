package swisseph;

import static android.os.Environment.DIRECTORY_MOVIES;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public MainActivity() {
        System.loadLibrary("swe-2.10.03");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText putCmd = findViewById(R.id.put_command);
        TextView outCmd = findViewById(R.id.out_command);

        Button clsCmd = findViewById(R.id.cls_command);
        Button exeCmd = findViewById(R.id.exe_command);

        clsCmd.setOnClickListener(v -> {
            String epheFolders = Environment.getExternalStoragePublicDirectory
                    (DIRECTORY_MOVIES).getAbsolutePath() + "/ephe";
            putCmd.setText("swetest -testaa97 -edir" + epheFolders);
            outCmd.setText("");
        });

        exeCmd.setOnClickListener(v -> {
            outCmd.setText("");
            StringBuilder sout = new StringBuilder();
            SwephExp.swe_test_main(putCmd.getText().toString(), sout);
            outCmd.setText(sout.toString());
        });
    }
}