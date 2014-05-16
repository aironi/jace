package org.silverduck.jace.web.vaadin;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.Caption;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.util.ReflectTools;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.silverduck.jace.domain.project.Project;

import javax.persistence.Embedded;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Workaround for Vaadin bug
 * 
 * Copied from: https://vaadin.com/old-forum/-/message_boards/view_message/3566802
 */
public class WorkingBeanFieldGroup<T> extends BeanFieldGroup<T> {
    private static final Log LOG = LogFactory.getLog(WorkingBeanFieldGroup.class);

    public WorkingBeanFieldGroup(Class<T> beanType) {
        super(beanType);
    }



    @Override
    protected void configureField(Field<?> field) {
        // Workaround for a bug in Vaadin with validator and BeanFieldGroups
        // http://dev.vaadin.com/ticket/10752
        // We need to remove all validators first
        field.removeAllValidators();

        // Workaround for a bug in Vaadin and validation for nested properties
        // https://vaadin.com/forum#!/thread/3566803
        // We need to add a validator for nested properties manually
        if (getPropertyId(field).toString().contains(".") && isBeanValidationImplementationAvailable()) {
            String[] propertyId = getPropertyId(field).toString().split("\\.");
            Class<?> beanClass = getPropertyType(propertyId[0]);
            String propertyName = propertyId[1];

            BeanValidator validator = new BeanValidator(beanClass, propertyName);
            field.addValidator(validator);
            if (field.getLocale() != null) {
                validator.setLocale(field.getLocale());
            }
        } else {
            super.configureField(field);
        }
    }


}
