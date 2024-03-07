package org.swisseph.appui.cli;

public enum Ephemeris {
    moshier("-emos"), swiss("-eswe"), jpl("-ejpl");

    final String option;

    Ephemeris(String option) {
        this.option = option;
    }

    public String getOption() {
        return option;
    }
}
