package org.swisseph.appui.cli;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.widget.Toast.LENGTH_LONG;
import static org.swisseph.MainActivity.ALLOW_IMPORT_DATA;
import static org.swisseph.MainActivity.SWE_CLI;
import static swisseph.AppConfig.EPHE_PATH;
import static swisseph.SwissephTest.swe_test_main;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.system.Os;
import android.system.OsConstants;
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
import org.apache.commons.lang3.reflect.MethodUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import swisseph.AppConfig;
import swisseph.R;
import swisseph.databinding.FragmentCliBinding;

public class CliFragment extends Fragment {
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
        cliInput.setText("swetest -info");

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
        StringBuilder sout = new StringBuilder();
        File epheFolder = config.appEpheFolder();

        if (!command.contains("-edir")) {
            args.append(" -edir");
            args.append(epheFolder.getAbsolutePath());
        }

        if (command.contains("-info")) {
            File[] files = epheFolder.listFiles();
            if (null == files) return sout;

            for (File file : files) {
                sout.append('\n');
                sout.append(new Date(file.lastModified()).toInstant());
                sout.append("\t\t").append(file.getName());
                sout.append("\n\t\t").append(file.length());
            }

            return sout;
        }

        swe_test_main(args.toString(), sout);

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
            new CopyEpheFileTask(resultData.getData(), config).execute();
        }
    }

    /**
     * CopyEpheFileTask
     */
    static final class CopyEpheFileTask extends AsyncTask<Void, Void, Void> {
        private final AppConfig config;
        private String toastText;
        private final Uri uri;

        CopyEpheFileTask(Uri uri, AppConfig config) {
            toastText = "FAILED to init...";
            this.config = config;
            this.uri = uri;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = config.getContext();
            File assetsDest = config.appEpheFolder();

            toastText = "FAILED to start...";
            ContentResolver contentResolver = context.getContentResolver();
            String epheFileName = resolveFileName(uri, contentResolver);
            toastText = "FAILED to import: " + epheFileName;

            try (InputStream epheIn = contentResolver.openInputStream(uri)) {
                File assetFileDest = new File(assetsDest, epheFileName);

                if (assetFileDest.isFile()) {
                    toastText = "ALREADY imported: " + epheFileName;
                } else {
                    OutputStream out = new FileOutputStream(assetFileDest);
                    IOUtils.copyLarge(epheIn, out);
                    IOUtils.closeQuietly(epheIn);
                    IOUtils.closeQuietly(out);
                    toastText = "IMPORTED: " + epheFileName;
                }
            } catch (Exception ex) {
                Log.e(SWE_CLI, "FAILED to import: " + epheFileName, ex);
                toastText = "FAILED to import: " + ex.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            Toast.makeText(config.getContext(), toastText, LENGTH_LONG).show();
        }

        private static String resolveFileName(Uri uri, ContentResolver contentResolver) {
            try {
                ParcelFileDescriptor fileDescriptor = contentResolver.openFileDescriptor(uri, "r");
                Object fd = MethodUtils.invokeMethod(fileDescriptor.getFileDescriptor(), "getInt$");
                String path = Os.readlink("/proc/self/fd/" + fd);

                if (OsConstants.S_ISREG(Os.stat(path).st_mode) ||
                        OsConstants.S_ISCHR(Os.stat(path).st_mode)) {
                    return FilenameUtils.getName(path);
                }
            } catch (Exception ex) {
                Log.e(SWE_CLI, "FAILED to import: " + uri, ex);
            }

            return FilenameUtils.getName(uri.getLastPathSegment());
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}