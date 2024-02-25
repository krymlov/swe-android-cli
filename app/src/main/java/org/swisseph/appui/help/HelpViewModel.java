package org.swisseph.appui.help;

import static swisseph.SwissephTest.cli;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HelpViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public HelpViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue(cli().help().toString());
    }

    LiveData<String> getText() {
        return mText;
    }
}