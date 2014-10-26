package org.silverduck.jace.web.component;

import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.silverduck.jace.common.exception.ExceptionHelper;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.project.VersionFileType;
import org.silverduck.jace.domain.vcs.PluginType;
import org.silverduck.jace.web.vaadin.WorkingBeanFieldGroup;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import java.net.URL;
import java.util.Locale;

/**
 * @author Iiro Hietala 14.5.2014.
 */
public class ProjectComponent extends BaseComponent<Project> {

    private TextField cloneUrl;

    private TextField commitIdPattern;

    @PropertyId("name")
    private TextField name;

    private PasswordField passWord;

    private TextField pathToVersionFile;

    private ComboBox repositoryType;

    private Button testCloneUrlButton;

    private TextField userName;

    private ComboBox versionFileType;

    private TextField versionPattern;

    public ProjectComponent() {
        Locale locale = UI.getCurrent().getLocale();

        TabSheet tabSheet = new TabSheet();

        tabSheet.addTab(createBasicDataLayout(),
            AppResources.getLocalizedString("label.projectForm.basicDataTab", locale));
        tabSheet.addTab(createReleaseInfoLayout(),
            AppResources.getLocalizedString("label.projectForm.releaseInfoTab", locale));
        tabSheet.addTab(createFeatureMappingLayout(),
            AppResources.getLocalizedString("label.projectForm.featureMappingTab", locale));

        setCompositionRoot(tabSheet);

    }

    @Override
    protected void bindFields(Project project) {
        super.setFieldGroup(new WorkingBeanFieldGroup(Project.class));
        super.getFieldGroup().setItemDataSource(new BeanItem<Project>(project));
        super.getFieldGroup().setBuffered(true);
        super.getFieldGroup().bind(name, "name");
        super.getFieldGroup().bind(cloneUrl, "pluginConfiguration.cloneUrl");
        super.getFieldGroup().bind(repositoryType, "pluginConfiguration.pluginType");
        super.getFieldGroup().bind(commitIdPattern, "pluginConfiguration.commitIdPattern");
        super.getFieldGroup().bind(userName, "pluginConfiguration.userName");
        super.getFieldGroup().bind(passWord, "pluginConfiguration.password");
        super.getFieldGroup().bind(versionFileType, "releaseInfo.versionFileType");
        super.getFieldGroup().bind(pathToVersionFile, "releaseInfo.pathToVersionFile");
        super.getFieldGroup().bind(versionPattern, "releaseInfo.pattern");
        // fieldGroup.bind(localDirectory, "pluginConfiguration.localDirectory");
    }

    private Component createBasicDataLayout() {
        Locale locale = UI.getCurrent().getLocale();

        name = new TextField(AppResources.getLocalizedString("label.projectForm.name", locale));
        name.setImmediate(true);

        repositoryType = new ComboBox(AppResources.getLocalizedString("label.projectForm.repositoryType", locale));
        for (PluginType pluginType : PluginType.values()) {
            repositoryType.addItem(pluginType);
            repositoryType.setItemCaption(pluginType,
                AppResources.getLocalizedString(pluginType.getResourceKey(), UI.getCurrent().getLocale()));
        }
        repositoryType.setImmediate(true);

        cloneUrl = new TextField(AppResources.getLocalizedString("label.projectForm.cloneUrl", locale));
        cloneUrl.setImmediate(true);

        commitIdPattern = new TextField(AppResources.getLocalizedString("label.projectForm.commitIdPattern", locale));
        commitIdPattern.setImmediate(true);

        userName = new TextField(AppResources.getLocalizedString("label.projectForm.userName", locale));
        userName.setImmediate(true);

        passWord = new PasswordField(AppResources.getLocalizedString("label.projectForm.passWord", locale));
        passWord.setImmediate(true);

        testCloneUrlButton = new Button(AppResources.getLocalizedString("label.projectForm.testCloneUrlButton", locale));
        testCloneUrlButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                testCloneUrl();
            }
        });

        FormLayout basicDataForm = new FormLayout();
        basicDataForm.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        basicDataForm.setSizeFull();

        basicDataForm.addComponent(name);
        basicDataForm.addComponent(repositoryType);
        basicDataForm.addComponent(cloneUrl);
        basicDataForm.addComponent(new Label("Note: The password is stored in plain text to DB for now."));
        ;
        basicDataForm.addComponent(userName);
        basicDataForm.addComponent(passWord);
        basicDataForm.addComponent(testCloneUrlButton);
        basicDataForm.addComponent(commitIdPattern);

        return new VerticalLayout(basicDataForm);
    }

    private Component createFeatureMappingLayout() {
        return null;
    }

    private Component createReleaseInfoLayout() {
        Locale locale = UI.getCurrent().getLocale();
        FormLayout releaseInfoForm = new FormLayout();

        versionFileType = new ComboBox(AppResources.getLocalizedString("label.projectForm.versionFileType", locale));
        for (VersionFileType type : VersionFileType.values()) {
            versionFileType.addItem(type);
            versionFileType.setItemCaption(type,
                AppResources.getLocalizedString(type.getResourceKey(), UI.getCurrent().getLocale()));
        }
        versionFileType.setImmediate(true);

        pathToVersionFile = new TextField(
            AppResources.getLocalizedString("label.projectForm.pathToVersionFile", locale));
        pathToVersionFile.setImmediate(true);

        versionPattern = new TextField(AppResources.getLocalizedString("label.projectForm.versionPattern", locale));
        versionPattern.setImmediate(true);

        releaseInfoForm.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        releaseInfoForm.addComponent(versionFileType);
        releaseInfoForm.addComponent(pathToVersionFile);
        releaseInfoForm.addComponent(versionPattern);

        return new VerticalLayout(releaseInfoForm);
    }

    /**
     * Edit a Project
     * 
     * @param project
     *            Project to Edit
     */
    public void edit(final Project project, boolean isNew) {
        super.edit(project);
        if (!isNew) {
            // Once created, these may no longer be changed.
            name.setReadOnly(true);
            repositoryType.setReadOnly(true);
            cloneUrl.setReadOnly(true);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        repositoryType.setReadOnly(readOnly);
        name.setReadOnly(readOnly);
        cloneUrl.setReadOnly(readOnly);
        commitIdPattern.setReadOnly(readOnly);
        // localDirectory.setReadOnly(readOnly);
    }

    private void testCloneUrl() {
        try {
            URL url = new URL(cloneUrl.getValue());
            HttpsURLConnectionImpl connection = (HttpsURLConnectionImpl) url.openConnection();
            connection.disconnect();
            Notification.show(AppResources.getLocalizedString("notification.connectionSuccessful", getLocale()));
        } catch (Exception e) {
            cloneUrl
                .setComponentError(new UserError("Connection Failed.\nCause: " + ExceptionHelper.toHumanReadable(e)));
        }
    }
}
