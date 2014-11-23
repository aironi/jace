package org.silverduck.jace.web.component;

import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.UserError;
import com.vaadin.ui.*;
import org.silverduck.jace.common.exception.ExceptionHelper;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.feature.FeatureMapping;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.project.VersionFileType;
import org.silverduck.jace.domain.vcs.PluginType;
import org.silverduck.jace.services.project.ProjectService;
import org.silverduck.jace.web.vaadin.WorkingBeanFieldGroup;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import java.net.URL;
import java.util.Locale;

/**
 * @author Iiro Hietala 14.5.2014.
 */
public class ProjectComponent extends BaseComponent<Project> {

    private Project project;

    BeanItemContainer<FeatureMapping> featureMappingContainer;

    private TextField cloneUrl;

    private TextField commitIdPattern;

    private TextField startPoint;

    @PropertyId("name")
    private TextField name;

    private PasswordField password;

    private TextField pathToVersionFile;

    private ComboBox repositoryType;

    private Button testCloneUrlButton;

    private TextField userName;

    private ComboBox versionFileType;

    private TextField versionPattern;

    private ProjectService projectService;


    public ProjectComponent(Project project) {
        this.project = project;
        featureMappingContainer = new BeanItemContainer<FeatureMapping>(FeatureMapping.class, project.getFeatureMappings());
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

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
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
        //super.getFieldGroup().bind(startPoint, "pluginConfiguration.startPoint");
        super.getFieldGroup().bind(userName, "pluginConfiguration.userName");
        super.getFieldGroup().bind(password, "pluginConfiguration.password");
        super.getFieldGroup().bind(versionFileType, "releaseInfo.versionFileType");
        super.getFieldGroup().bind(pathToVersionFile, "releaseInfo.pathToVersionFile");
        super.getFieldGroup().bind(versionPattern, "releaseInfo.pattern");

    }

    private Component createBasicDataLayout() {
        Locale locale = UI.getCurrent().getLocale();

        name = new TextField(AppResources.getLocalizedString("label.projectForm.name", locale));
        name.setImmediate(true);
        name.setWidth(100, Unit.PERCENTAGE);

        repositoryType = new ComboBox(AppResources.getLocalizedString("label.projectForm.repositoryType", locale));
        for (PluginType pluginType : PluginType.values()) {
            repositoryType.addItem(pluginType);
            repositoryType.setItemCaption(pluginType,
                AppResources.getLocalizedString(pluginType.getResourceKey(), UI.getCurrent().getLocale()));
        }
        repositoryType.setImmediate(true);
        repositoryType.setWidth(100, Unit.PERCENTAGE);

        cloneUrl = new TextField(AppResources.getLocalizedString("label.projectForm.cloneUrl", locale));
        cloneUrl.setImmediate(true);
        cloneUrl.setWidth(100, Unit.PERCENTAGE);

        userName = new TextField(AppResources.getLocalizedString("label.projectForm.userName", locale));
        userName.setImmediate(true);

        password = new PasswordField(AppResources.getLocalizedString("label.projectForm.passWord", locale));
        password.setImmediate(true);
        password.setWidth(100, Unit.PERCENTAGE);

        testCloneUrlButton = new Button(AppResources.getLocalizedString("label.projectForm.testCloneUrlButton", locale));
        testCloneUrlButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                testCloneUrl();
            }
        });

        commitIdPattern = new TextField(AppResources.getLocalizedString("label.projectForm.commitIdPattern", locale));
        commitIdPattern.setImmediate(true);
        commitIdPattern.setWidth(100, Unit.PERCENTAGE);

        startPoint = new TextField((AppResources.getLocalizedString("label.projectForm.startPoint", locale)));
        startPoint.setImmediate(true);
        startPoint.setWidth(100, Unit.PERCENTAGE);

        FormLayout basicDataForm = new FormLayout();

        basicDataForm.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        basicDataForm.setSizeFull();

        basicDataForm.addComponent(name);
        basicDataForm.addComponent(repositoryType);
        basicDataForm.addComponent(cloneUrl);
        basicDataForm.addComponent(new Label("Note: The password is stored in plain text to DB for now."));
        basicDataForm.addComponent(userName);
        basicDataForm.addComponent(password);
        basicDataForm.addComponent(testCloneUrlButton);
        basicDataForm.addComponent(commitIdPattern);
        //basicDataForm.addComponent(startPoint);

        return new VerticalLayout(basicDataForm);
    }

    private Component createFeatureMappingLayout() {

        final Locale locale = UI.getCurrent().getLocale();
        final VerticalLayout featureMappingLayout = new VerticalLayout();
        featureMappingLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        featureMappingLayout.setSizeFull();


        final Table featureMappingTable = new Table();
        featureMappingTable.setWidth(100, Unit.PERCENTAGE);
        featureMappingTable.setImmediate(true);
        featureMappingTable.setContainerDataSource(featureMappingContainer);
        featureMappingTable.setVisibleColumns("mappingType", "sourcePattern", "featureName");
        featureMappingTable.setColumnHeaders("Type", "Pattern", "To Feature");

        Table.ColumnGenerator columnGenerator = new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                final FeatureMapping bean = (FeatureMapping) itemId;
                if ("Edit".equals(columnId)) {
                    Button editButton = new Button(AppResources.getLocalizedString("label.edit", locale));
                    editButton.addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            showFeatureMappingPopup(bean);
                        }
                    });
                    return editButton;
                } else if ("Remove".equals(columnId)) {
                    Button removeButton = new Button(AppResources.getLocalizedString("label.remove", locale));
                    removeButton.addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            project.removeFeatureMapping(bean);
                        }
                    });
                    return removeButton;
                }
                return null;
            }
        };

        featureMappingTable.addGeneratedColumn("Edit", columnGenerator);
        featureMappingTable.addGeneratedColumn("Remove", columnGenerator);

        Button addButton = new Button("New");
        addButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                showFeatureMappingPopup(null);
            }
        });
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.addComponent(addButton);
        featureMappingLayout.addComponent(featureMappingTable);
        featureMappingLayout.addComponent(buttonsLayout);
        return featureMappingLayout;
    }

    private void showFeatureMappingPopup(FeatureMapping featureMapping) {
        final Locale locale = UI.getCurrent().getLocale();
        final Window featureMappingWindow = new Window();

        final FeatureMappingComponent featureMappingComponent = new FeatureMappingComponent();
        featureMappingComponent.setFeatures(project.getFeatures());
        if (featureMapping == null) {
            featureMappingWindow.setCaption(AppResources.getLocalizedString("caption.newFeatureMapping", locale));
            featureMapping = FeatureMapping.newFeatureMapping();
            featureMapping.setProject(project);
        } else {
            featureMappingWindow.setCaption(AppResources.getLocalizedString("caption.editFeatureMapping", locale));
        }

        featureMappingComponent.edit(featureMapping);

        Button submitButton = new Button(AppResources.getLocalizedString("label.submit", locale));
        submitButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (featureMappingComponent.isValid()) {
                    final FeatureMapping featureMapping = featureMappingComponent.commit();
                    featureMappingContainer.addBean(featureMapping);
                    if (featureMapping.getId() == null) {
                        project.addFeatureMapping(featureMapping);
                    }

                    featureMappingWindow.close();

                } else {
                    Notification.show(AppResources.getLocalizedString("form.validationErrorsNotification", locale),
                            Notification.Type.TRAY_NOTIFICATION);
                }
            }
        });

        Button cancelButton = new Button(AppResources.getLocalizedString("label.cancel", locale));
        cancelButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                featureMappingComponent.discard();
                featureMappingWindow.close();
            }
        });
        HorizontalLayout commandButtons = new HorizontalLayout();
        commandButtons.addComponent(submitButton);
        commandButtons.addComponent(cancelButton);
        FormLayout formLayout = new FormLayout();
        formLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        formLayout.addComponent(featureMappingComponent);
        formLayout.addComponent(commandButtons);

        HorizontalLayout popupLayout = new HorizontalLayout();
        popupLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        popupLayout.setSpacing(true);
        popupLayout.setMargin(true);
        popupLayout.addComponent(formLayout);

        featureMappingWindow.setContent(popupLayout);
        featureMappingWindow.setModal(true);

        getUI().addWindow(featureMappingWindow);
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
        versionFileType.setWidth(100, Unit.PERCENTAGE);

        pathToVersionFile = new TextField(
            AppResources.getLocalizedString("label.projectForm.pathToVersionFile", locale));
        pathToVersionFile.setImmediate(true);
        pathToVersionFile.setWidth(100, Unit.PERCENTAGE);

        versionPattern = new TextField(AppResources.getLocalizedString("label.projectForm.versionPattern", locale));
        versionPattern.setImmediate(true);
        versionPattern.setWidth(100, Unit.PERCENTAGE);

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
    public void edit(boolean isNew) {
        super.edit(project);

        if (!isNew) {
            // Once created, these may no longer be changed.
            name.setReadOnly(true);
            repositoryType.setReadOnly(true);
            cloneUrl.setReadOnly(true);
            startPoint.setReadOnly(true);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        repositoryType.setReadOnly(readOnly);
        name.setReadOnly(readOnly);
        cloneUrl.setReadOnly(readOnly);
        commitIdPattern.setReadOnly(readOnly);
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
