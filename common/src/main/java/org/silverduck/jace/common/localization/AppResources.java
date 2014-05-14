package org.silverduck.jace.common.localization;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by Iiro on 29.12.2013.
 */
public class AppResources {

    /**
     * Returns a localized version of a string from the uiResources resource bundle with given locale
     * 
     * @param resourceKey
     *            Key
     * @param locale
     *            Target locale
     * @return Localized string.
     */
    public static final String getLocalizedString(String resourceKey, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("uiResources", locale);
        String resourceText;
        try {
            resourceText = bundle.getString(resourceKey);
        } catch (MissingResourceException e) {
            e.printStackTrace(); // TODO: Use LOG4J
            resourceText = "'" + resourceKey + "' (resource missing)";
        }
        return resourceText;
    }
}
