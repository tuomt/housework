package com.example.housework.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {

    public static boolean isValidPassword(final String password) {
        int len = password.length();
        if (len < 8 || len > 85) {
            return false;
        } else return true;
    }

    public static boolean isValidUserName(final String userName) {
        int len = userName.length();
        if (len < 2 || len > 21) {
            return false;
        } else return true;
    }

    public static boolean isValidEmail(final String email) {
        int len = email.length();
        if (len < 5 || len > 85) {
            return false;
        }

        Pattern pattern = Pattern.compile("^.+@.+(\\.[^.]+)+$");
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            return true;
        } else return false;
    }

    public static boolean isValidGroupName(final String groupName) {
        int len = groupName.length();
        if (len < 3 || len > 21) {
            return false;
        } else return true;
    }

    public static boolean isValidGroupPassword(final String password) {
        int len = password.length();
        if (len < 5 || len > 85) {
            return false;
        } else return true;
    }
}
