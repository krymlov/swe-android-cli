package org.swisseph.appui.cli;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static android.widget.Toast.LENGTH_LONG;
import static org.swisseph.MainActivity.ALLOW_IMPORT_DATA;
import static org.swisseph.MainActivity.SWE_CLI;
import static org.swisseph.appui.cli.Ephemeris.jpl;
import static org.swisseph.appui.cli.Ephemeris.moshier;
import static org.swisseph.appui.cli.Ephemeris.swiss;
import static swisseph.AppConfig.EPHE_PATH;
import static swisseph.SwissephTest.swe_test_main;

import android.app.Activity;
import android.content.ContentResolver;
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
    Ephemeris ephemerisOption = moshier;
    FragmentCliBinding binding;
    String listEpheFilesCmd;
    AppConfig config;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCliBinding.inflate(inflater, container, false);
        listEpheFilesCmd = getString(R.string.list_ephemeris_files);
        View root = binding.getRoot();

        TextView cliOutput = binding.cliOutput;
        cliOutput.setMovementMethod(new ScrollingMovementMethod());
        cliOutput.setHorizontallyScrolling(true);

        config = new AppConfig(getActivity());
        config.extractAssets(EPHE_PATH, config.appEpheFolder());

        Button exeCliInput = binding.exeCliInput;
        Button clsCliInput = binding.clsCliInput;

        EditText cliInput = binding.cliInput;
        cliInput.setText(listEpheFilesCmd);

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
            args.append(" ").append(ephemerisOption.option);
            args.append(" -edir");
            args.append(epheFolder.getAbsolutePath());
        }

        if (command.contains(listEpheFilesCmd)) {
            File[] files = epheFolder.listFiles();
            if (null == files) return sout;

            sout.append('\n').append(epheFolder.getAbsolutePath());
            for (File file : files) {
                sout.append("\n\n").append(new Date(file.lastModified()).toInstant());
                sout.append("\t\t").append(file.getName()).append("\n\t\t").append(file.length());
            }

            return sout;
        }

        swe_test_main(args.toString(), sout);

        return sout;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

        switch (ephemerisOption) {
            case moshier:
                menu.findItem(R.id.menu_action_ephe_mos).setChecked(true);
                break;
            case swiss:
                menu.findItem(R.id.menu_action_ephe_swe).setChecked(true);
                break;
            case jpl:
                menu.findItem(R.id.menu_action_ephe_jpl).setChecked(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();

        if (itemId == R.id.menu_action_import) {
            openDirToImportEphe();
            return true;
        } else if (itemId == R.id.menu_action_ephe_mos) {
            ephemerisOption = moshier;
            item.setChecked(true);
            return true;
        } else if (itemId == R.id.menu_action_ephe_swe) {
            ephemerisOption = swiss;
            item.setChecked(true);
            return true;
        } else if (itemId == R.id.menu_action_ephe_jpl) {
            ephemerisOption = jpl;
            item.setChecked(true);
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
            new CopyEpheFileTask(resultData.getData(), binding.cliOutput, config).execute();
        }
    }

    /**
     * CopyEpheFileTask
     */
    static final class CopyEpheFileTask extends AsyncTask<Void, Void, Void> {
        StringBuilder sout = new StringBuilder();
        final TextView cliOutput;
        final AppConfig config;
        String toastText;
        final Uri uri;

        CopyEpheFileTask(Uri uri, TextView cliOutput, AppConfig config) {
            this.cliOutput = cliOutput;
            toastText = "Failed to init...";
            this.config = config;
            this.uri = uri;

            sout.append("Started to copy... ").append(uri);
            cliOutput.setText(sout.toString());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Activity activity = config.getActivity();
            File assetsDest = config.appEpheFolder();

            toastText = "Failed to start...";
            ContentResolver contentResolver = activity.getContentResolver();
            String epheFileName = resolveFileName(uri, contentResolver);
            toastText = "Failed to import: " + epheFileName;

            try (InputStream epheIn = contentResolver.openInputStream(uri)) {
                File assetFileDest = new File(assetsDest, epheFileName);

                activity.runOnUiThread(() -> {
                    sout.append("\nFile destination: ").append(assetFileDest);
                    sout.append("\nResolved file name: ").append(epheFileName);
                    cliOutput.setText(sout.toString());
                });

                if (assetFileDest.isFile()) {
                    toastText = "Already imported: " + epheFileName;
                } else {
                    OutputStream out = new FileOutputStream(assetFileDest);

                    activity.runOnUiThread(() -> {
                        sout.append("\nThe file is being copied ...");
                        cliOutput.setText(sout.toString());
                    });

                    IOUtils.copyLarge(epheIn, out);
                    IOUtils.closeQuietly(epheIn);
                    IOUtils.closeQuietly(out);
                    toastText = "Imported: " + epheFileName;
                }
            } catch (Exception ex) {
                Log.e(SWE_CLI, "Failed to import: " + epheFileName, ex);
                toastText = "Failed to import: " + ex.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            config.getActivity().runOnUiThread(() -> {
                sout.append('\n').append(toastText);
                cliOutput.setText(sout.toString());
            });

            Toast.makeText(config.getActivity(), toastText, LENGTH_LONG).show();
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