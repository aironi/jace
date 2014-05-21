package org.silverduck.jace.web.component;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.web.vaadin.WorkingBeanFieldGroup;

/**
 * @author Iiro Hietala 17.5.2014.
 */
public abstract class BaseComponent<T> extends CustomComponent {

    private WorkingBeanFieldGroup<T> fieldGroup;

    protected abstract void bindFields(T item);

    /**
     * Commits the changes in the component and returns the bound object.
     * 
     * @return Object if bound. Otherwise null.
     */
    public T commit() {
        if (fieldGroup.getItemDataSource() != null) {
            try {
                fieldGroup.commit();
                T bean = fieldGroup.getItemDataSource().getBean();
            } catch (FieldGroup.CommitException e) {
                Notification.show("Error",
                    AppResources.getLocalizedString("errorMessages.commitFailed", UI.getCurrent().getLocale()),
                    Notification.Type.ERROR_MESSAGE);
            }
            return fieldGroup.getItemDataSource().getBean();
        }

        return null;
    }

    /**
     * Discard given changes in Form to the bound Applicant
     */
    public void discard() {
        if (fieldGroup.getItemDataSource() != null) {
            fieldGroup.discard();
        }
    }

    /**
     * Edit an Item
     * 
     * @param item
     *            Item to Edit
     */
    public void edit(final T item) {
        bindFields(item);
        setReadOnly(false);
    }

    public WorkingBeanFieldGroup<T> getFieldGroup() {
        return fieldGroup;
    }

    /**
     * @return True, if the given data in the Form valid. Otherwise false.
     */
    public boolean isValid() {
        return fieldGroup.isValid();
    }

    public void setFieldGroup(WorkingBeanFieldGroup fieldGroup) {
        this.fieldGroup = fieldGroup;
    }

    public abstract void setReadOnly(boolean readOnly);

    /**
     * View an Item
     * 
     * @param item
     *            Item to View
     */
    public void view(final T item) {
        if (item != null) {
            bindFields(item);
        }
        setReadOnly(true);
    }

}
