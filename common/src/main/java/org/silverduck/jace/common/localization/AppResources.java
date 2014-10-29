package org.silverduck.jace.common.localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by Iiro Hietala on 29.12.2013.
 */
public class AppResources {

    private static final Logger LOG = LoggerFactory.getLogger(AppResources.class);

    /**
     * Returns a localized version of a string from the uiResources resource bundle with given locale
     * 
     * @param resourceKey
     *            Key
     * @param locale
     *            Target locale
     * @return Localized string.
     */
    public static final String getLocalizedString(String resourceKey, Locale locale, String... parameters) {
        ResourceBundle bundle = ResourceBundle.getBundle("uiResources", locale);
        String resourceText;
        try {
            resourceText = bundle.getString(resourceKey);
            if (parameters.length > 0) {
                resourceText = MessageFormat.format(resourceText, parameters);
            }
        } catch (MissingResourceException e) {
            resourceText = "'" + resourceKey + "' (resource missing)";
            LOG.warn("Resource '" + resourceKey + "' was not found!");
        }
        return resourceText;
    }
}
