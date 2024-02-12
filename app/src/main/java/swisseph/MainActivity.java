package swisseph;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    AppConfig config;

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

        config = new AppConfig(getBaseContext());
        config.extractAssets(AppConfig.EPHE_PATH, config.appEpheFolder());
        config.extractAssets(AppConfig.JPL_PATH, config.appJplFolder());

        putCmd.setText("swetest -?");

        clsCmd.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String[] commands = getResources().getStringArray(R.array.sweTestCommands);
            builder.setSingleChoiceItems(commands, 0, (dialog, item) -> {
                putCmd.setText(commands[item]);
                dialog.dismiss();
            });

            builder.setTitle("swetest examples");
            builder.create().show();
            outCmd.setText("");
        });

        exeCmd.setOnClickListener(v -> {
            outCmd.setText("");
            StringBuilder sout = sweTestMain(putCmd.getText().toString());
            outCmd.setText(sout.toString());
        });
    }

    private StringBuilder sweTestMain(String command) {
        StringBuilder args = new StringBuilder(command);

        if (!command.contains("-edir")) {
            args.append(" -edir");
            args.append(config.appEpheFolder().getAbsolutePath());
        }

        StringBuilder sout = new StringBuilder();
        SwephExp.swe_test_main(args.toString(), sout);
        return sout;
    }
}