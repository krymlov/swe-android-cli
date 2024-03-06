package org.swisseph.appui.cli;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.widget.Toast.LENGTH_LONG;
import static swisseph.AppConfig.EPHE_PATH;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import swisseph.AppConfig;
import swisseph.R;
import swisseph.SwissephTest;
import swisseph.databinding.FragmentCliBinding;

public class CliFragment extends Fragment {
    private static final int ALLOW_IMPORT_DATA = 101;
    private FragmentCliBinding binding;
    private AppConfig config;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCliBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView cliOutput = binding.cliOutput;
        cliOutput.setMovementMethod(new ScrollingMovementMethod());
        cliOutput.setHorizontallyScrolling(true);

        config = new AppConfig(getContext());
        config.extractAssets(EPHE_PATH, config.appEpheFolder());

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();

        if (itemId == R.id.menu_action_import) {
            openDirToImportEphe();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openDirToImportEphe() {
        Intent intent = new Intent(ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, ALLOW_IMPORT_DATA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultData == null) return;

        if (requestCode == ALLOW_IMPORT_DATA && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that the user selected.
            Uri uri = resultData.getData();
            File assetsDest = config.appEpheFolder();
            try (InputStream epheIn = getContext().getContentResolver().openInputStream(uri)) {
                String epheFileName = FilenameUtils.getName(uri.getLastPathSegment());
                File assetFileDest = new File(assetsDest, epheFileName);

                if (assetFileDest.isFile()) {
                    Toast.makeText(getContext(), "Already imported!", LENGTH_LONG).show();
                } else {
                    OutputStream out = new FileOutputStream(assetFileDest);
                    IOUtils.copyLarge(epheIn, out);
                    IOUtils.closeQuietly(epheIn);
                    IOUtils.closeQuietly(out);

                    Toast.makeText(getContext(), "Imported!", LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                Log.e("swisseph.cli", "Failed to import EPH file: " + uri, ex);
                Toast.makeText(getContext(), "Failed to import!", LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}