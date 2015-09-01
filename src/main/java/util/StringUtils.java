package util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class StringUtils {
    protected static final char SEPARATOR = '-';
    
    public static String parameterize(String input) {
        return parameterize(input, SEPARATOR);
    }

    public static String parameterize(String input, char separator) {
        Splitter splitter = Splitter.on(CharMatcher.JAVA_LETTER_OR_DIGIT.negate()).omitEmptyStrings();
        Joiner joiner = Joiner.on(separator);
        return joiner.join(splitter.split(input)).toLowerCase();
    }
}
