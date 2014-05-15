package org.silverduck.jace.web.component;

import com.sun.net.ssl.HttpsURLConnection;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.apache.http.HttpConnection;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.vcs.PluginType;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by ihietala on 14.5.2014.
 */
public class ProjectComponent extends CustomComponent {

    @PropertyId("pluginConfiguration.cloneUrl")
    private TextField cloneUrl;

    private BeanFieldGroup<Project> fieldGroup;

    @PropertyId("pluginConfiguration.localDirectory")
    private TextField localDirectory;

    @PropertyId("name")
    private TextField name;

    @PropertyId("pluginConfiguration.pluginType")
    private ComboBox repositoryType;

    private Button testCloneUrlButton;

    public ProjectComponent() {

        Locale locale = getUI().getCurrent().getLocale();

        name = new TextField(AppResources.getLocalizedString("label.projectForm.name", locale));
        name.setImmediate(true);

        repositoryType = new ComboBox(AppResources.getLocalizedString("label.projectForm.repositoryType", locale),
            Arrays.asList(PluginType.values()));

        cloneUrl = new TextField(AppResources.getLocalizedString("label.projectForm.cloneUrl", locale));
        cloneUrl.setImmediate(true);
        localDirectory = new TextField(AppResources.getLocalizedString("label.projectForm.localDirectory", locale));
        localDirectory.setImmediate(true);
        testCloneUrlButton = new Button(AppResources.getLocalizedString("label.projectForm.testCloneUrlButton", locale));
        testCloneUrlButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                testCloneUrl();
            }
        });

        FormLayout formLayout = new FormLayout();
        formLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        formLayout.setSizeFull();
        formLayout.addComponent(name);

        formLayout.addComponent(cloneUrl);
        formLayout.addComponent(testCloneUrlButton);
        formLayout.addComponent(repositoryType);
        formLayout.addComponent(cloneUrl);
        formLayout.addComponent(localDirectory);

        setCompositionRoot(formLayout);
    }

    private void bindFields(Project project) {
        fieldGroup = new BeanFieldGroup(Project.class);
        fieldGroup.setItemDataSource(new BeanItem<Project>(project));
        fieldGroup.setBuffered(true);
        fieldGroup.bindMemberFields(this);
    }

    /**
     * Commits the changes in the component and returns the Project object.
     * 
     * @return Project object if bound. Otherwise null.
     */
    public Project commit() {
        if (fieldGroup.getItemDataSource() != null) {
            try {
                fieldGroup.commit();
            } catch (FieldGroup.CommitException e) {
                Notification.show("Error", AppResources.getLocalizedString("errorMessages.commitFailed", getLocale()),
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
     * Edit a Project
     * 
     * @param project
     *            Project to Edit
     */
    public void edit(final Project project) {
        bindFields(project);
        setReadOnly(false);
    }

    /**
     * @return True, if the given data in the Form valid. Otherwise false.
     */
    public boolean isValid() {
        return fieldGroup.isValid();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        name.setReadOnly(readOnly);
        cloneUrl.setReadOnly(readOnly);
        localDirectory.setReadOnly(readOnly);
    }

    private void testCloneUrl() {
        try {
            URL url = new URL(cloneUrl.getValue());
            HttpsURLConnectionImpl connection = (HttpsURLConnectionImpl) url.openConnection();
            connection.disconnect();
            Notification.show(AppResources.getLocalizedString("notification.connectionSuccessful", getLocale()));
        } catch (Exception e) {
            cloneUrl.setComponentError(new UserError("Connection Failed: '" + e.getMessage() + "'"));
        }
    }

    /**
     * View an project
     * 
     * @param project
     *            Project to View
     */
    public void view(final Project project) {
        if (project != null) {
            bindFields(project);
        }
        setReadOnly(true);
    }
}
