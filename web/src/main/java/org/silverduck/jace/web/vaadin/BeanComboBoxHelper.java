package org.silverduck.jace.web.vaadin;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import org.apache.commons.beanutils.PropertyUtils;
import org.silverduck.jace.common.exception.JaceRuntimeException;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Iiro Hietala on 17.5.2014.
 */
public class BeanComboBoxHelper {

    private static final Logger LOG = LoggerFactory.getLogger(BeanComboBoxHelper.class);

    /**
     * Create a ComboBox with a Bean items
     * 
     * @param localizationKey
     *            Localization Key of the Combo Box label
     * @param locale
     *            Locale
     * @param nameProperty
     *            The property in bean containing the name to be shown in the list
     * @param items
     *            List of items to be shown in ComboBox
     * @return
     */
    public static final ComboBox createComboBox(String localizationKey, Locale locale, Class<?> beanClass,
        String nameProperty, List<? extends AbstractDomainObject> items) {

        BeanItemContainer bic = new BeanItemContainer(beanClass);
        if (items != null) {
            bic.addAll(items);
        }
        ComboBox comboBox = new ComboBox(AppResources.getLocalizedString(localizationKey, locale), bic);
        comboBox.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        comboBox.setItemCaptionPropertyId(nameProperty);

        return comboBox;

    }

    /**
     * Update a given ComboBox with items defined in list of items
     * 
     * @param comboBox
     *            The ComboBox to update
     * @param nameProperty
     *            The property in bean containing the name to be shown in the list
     * @param items
     *            List of items to be shown in ComboBox
     */
    public static final void updateComboBoxItems(ComboBox comboBox, Class<?> beanClass, String nameProperty,
        List<? extends AbstractDomainObject> items) {
        comboBox.removeAllItems();
        BeanItemContainer bic = new BeanItemContainer(beanClass);
        bic.addAll(items);
        comboBox.setContainerDataSource(bic);
        comboBox.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        comboBox.setItemCaptionPropertyId(nameProperty);
    }
}
