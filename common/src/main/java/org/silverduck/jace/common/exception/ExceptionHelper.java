package org.silverduck.jace.common.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Iiro Hietala on 21.5.2014.
 */
public class ExceptionHelper {
    /**
     * A simple Exception cause-walker that picks up the messages from Exceptions and makes a string out of them in
     * similar fashion as Stack Trace is printed.
     * 
     * @param t
     *            Throwable
     * @return String
     */
    public static String toHumanReadable(Throwable t) {
        Throwable e2 = t;
        StringBuilder sb = new StringBuilder();
        sb.append(t.getMessage()).append("\n");
        while (e2.getCause() != null) {
            if (e2.getCause().getMessage() != null) {
                sb.insert(0, e2.getCause().getMessage() + "\n");
            }
            e2 = e2.getCause();
        }
        return sb.toString();
    }
}
