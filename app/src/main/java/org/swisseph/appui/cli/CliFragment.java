package org.swisseph.appui.cli;

import static swisseph.AppConfig.EPHE_PATH;
import static swisseph.AppConfig.JPL_PATH;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import swisseph.AppConfig;
import swisseph.R;
import swisseph.SwissephTest;
import swisseph.databinding.FragmentCliBinding;

public class CliFragment extends Fragment {
    private FragmentCliBinding binding;
    private AppConfig config;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCliBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView cliOutput = binding.cliOutput;
        cliOutput.setMovementMethod(new ScrollingMovementMethod());
        cliOutput.setHorizontallyScrolling(true);

        config = new AppConfig(getContext());
        config.extractAssets(EPHE_PATH, config.appEpheFolder());
        config.extractAssets(JPL_PATH, config.appJplFolder());

        Button exeCliInput = binding.exeCliInput;
        Button clsCliInput = binding.clsCliInput;

        EditText cliInput = binding.cliInput;
        cliInput.setText("swetest -?");

        clsCliInput.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            String[] commands = getResources().getStringArray(R.array.sweTestCommands);
            builder.setSingleChoiceItems(commands, 0, (dialog, item) -> {
                cliInput.setText(commands[item]);
                dialog.dismiss();
            });

            builder.setTitle("swetest examples");
            builder.create().show();
        });

        exeCliInput.setOnClickListener(v -> {
            cliOutput.setText("");
            StringBuilder sout = sweTestMain(cliInput.getText().toString());
            cliOutput.setText(sout.toString());
        });

        return root;
    }

    private StringBuilder sweTestMain(String command) {
        StringBuilder args = new StringBuilder(command);

        if (!command.contains("-edir")) {
            args.append(" -edir");
            args.append(config.appEpheFolder().getAbsolutePath());
        }

        StringBuilder sout = new StringBuilder();
        SwissephTest.swe_test_main(args.toString(), sout);

        return sout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}