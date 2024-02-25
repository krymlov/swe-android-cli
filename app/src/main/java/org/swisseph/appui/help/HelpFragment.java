package org.swisseph.appui.help;

import static swisseph.SwissephTest.cli;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import swisseph.databinding.FragmentHelpBinding;


public class HelpFragment extends Fragment {
    private FragmentHelpBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHelpBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView cliOutput = binding.cliOutput;
        cliOutput.setMovementMethod(new ScrollingMovementMethod());
        cliOutput.setHorizontallyScrolling(true);
        cliOutput.setText(cli().help().toString());
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}