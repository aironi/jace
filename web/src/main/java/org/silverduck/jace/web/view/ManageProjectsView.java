package org.silverduck.jace.web.view;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.ui.*;
import org.silverduck.jace.common.exception.ExceptionHelper;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.services.project.ProjectService;
import org.silverduck.jace.web.component.ProjectComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Iiro Hietala 13.5.2014.
 */
@CDIView(ManageProjectsView.VIEW)
public class ManageProjectsView extends BaseView {

    private static final Logger LOG = LoggerFactory.getLogger(ManageProjectsView.class);

    public static final String VIEW = "ManageProjectsView";

    @EJB
    private ProjectService projectService;

    private JPAContainer<Project> projectsContainer = JPAContainerFactory.makeJndi(Project.class);

    Table projectsTable;
    private GridLayout contentLayout;

    public ManageProjectsView() {
        super();

    }

    private void addNewProjectButton(HorizontalLayout hl) {
        Locale locale = UI.getCurrent().getLocale();
        Button newButton = new Button(AppResources.getLocalizedString("label.newProject", locale));

        newButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                showNewProjectPopup();
            }
        });
        hl.addComponent(newButton);

    }

    private void addProjectsTable(Layout layout) {
        final Locale locale = UI.getCurrent().getLocale();
        projectsContainer.setFireContainerItemSetChangeEvents(true);
        projectsContainer.addNestedContainerProperty("pluginConfiguration.pluginType");
        projectsContainer.addNestedContainerProperty("pluginConfiguration.cloneUrl");
        projectsContainer.addNestedContainerProperty("pluginConfiguration.commitIdPattern");
        projectsContainer.addNestedContainerProperty("releaseInfo.versionFileType");
        projectsContainer.addNestedContainerProperty("releaseInfo.pathToVersionFile");
        projectsContainer.addNestedContainerProperty("releaseInfo.pattern");

        projectsTable = new Table();
        projectsTable.setContainerDataSource(projectsContainer);

        projectsTable.setVisibleColumns("name", "pluginConfiguration.pluginType", "pluginConfiguration.cloneUrl",
            "pluginConfiguration.commitIdPattern", "releaseInfo.versionFileType", "releaseInfo.pathToVersionFile",
            "releaseInfo.pattern");

        projectsTable.setColumnHeaders(AppResources.getLocalizedString("label.projectForm.name", locale),
            AppResources.getLocalizedString("label.projectForm.repositoryType", locale),
            AppResources.getLocalizedString("label.projectForm.cloneUrl", locale),
            AppResources.getLocalizedString("label.projectForm.commitIdPattern", locale),
            AppResources.getLocalizedString("label.projectForm.versionFileType", locale),
            AppResources.getLocalizedString("label.projectForm.pathToVersionFile", locale),
            AppResources.getLocalizedString("label.projectForm.versionPattern", locale));

        projectsTable.setImmediate(true);

        Table.ColumnGenerator columnGenerator = new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                final Object finalItemId = itemId;
                if ("Edit".equals(columnId)) {
                    Button editButton = new Button(AppResources.getLocalizedString("label.edit", locale));
                    editButton.addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            createProjectPopup((Long) finalItemId);
                        }
                    });
                    return editButton;
                } else if ("Remove".equals(columnId)) {
                    Button removeButton = new Button(AppResources.getLocalizedString("label.remove", locale));
                    removeButton.addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            projectService.removeProjectById((Long) finalItemId);
                            projectsContainer.refresh();
                        }
                    });
                    return removeButton;
                }
                return null;
            }
        };

        projectsTable.addGeneratedColumn("Edit", columnGenerator);
        projectsTable.addGeneratedColumn("Remove", columnGenerator);

        layout.addComponent(projectsTable);

    }

    private void createProjectPopup(Long projectId) {
        final Window projectPopUp = new Window();
        final Locale locale = UI.getCurrent().getLocale();
        // Configure the error handler for the UI
        projectPopUp.setErrorHandler(new DefaultErrorHandler() {
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                String cause = "";
                for (Throwable t = event.getThrowable(); t != null; t = t.getCause()) {
                    if (t.getCause() == null) {
                        cause = t.getMessage();
                    }
                }
                Notification.show("Creating of a project failed. Error message: " + cause,
                    Notification.Type.ERROR_MESSAGE);

                // Do the default error handling (optional)
                doDefault(event);
            }
        });

        projectPopUp.setModal(true);

        // Layout
        HorizontalLayout popupLayout = new HorizontalLayout();
        popupLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        popupLayout.setSpacing(true);
        popupLayout.setMargin(true);
        projectPopUp.setContent(popupLayout);
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        popupLayout.addComponent(formLayout);

        // Content

        Project project;
        if (projectId == null) {
            projectPopUp.setCaption(AppResources.getLocalizedString("caption.newProject", locale));
            project = Project.newProject();
        } else {
            projectPopUp.setCaption(AppResources.getLocalizedString("caption.editProject", locale));
            project = projectService.findProjectById(projectId);
        }


        final ProjectComponent projectComponent = new ProjectComponent(project);
        projectComponent.setProjectService(projectService);
        formLayout.addComponent(projectComponent);

        Button submitButton = new Button(AppResources.getLocalizedString("label.submit", locale));
        submitButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (projectComponent.isValid()) {
                    final Project project = projectComponent.commit();

                    // FIXME: Implement JCA compliant solution. For now the files go into config/<projectName>
                    final Future<Boolean> result;
                    boolean wasNew = false;
                    if (project.getId() == null) {
                        wasNew = true;
                        result = projectService.addProject(project);
                    } else {
                        result = projectService.updateProject(project);
                    }

                    final boolean finalWasNew = wasNew;
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                if (Boolean.TRUE.equals(result.get())) {
                                    UI.getCurrent().access(new Runnable() {
                                        @Override
                                        public void run() {
                                            projectsContainer.refresh();
                                            if (finalWasNew) {
                                                Notification.show(AppResources.getLocalizedString(
                                                        "notification.projectAdded", UI.getCurrent().getLocale(),
                                                        project.getName()), Notification.Type.TRAY_NOTIFICATION);
                                            }
                                        }
                                    });
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } catch (ExecutionException e) {
                                Notification.show("Error", "An error occurred when adding project.\nCause: "
                                        + ExceptionHelper.toHumanReadable(e), Notification.Type.ERROR_MESSAGE);
                            }
                        }
                    }.start();

                    projectPopUp.close();

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
                projectComponent.discard();
                projectPopUp.close();
            }
        });
        HorizontalLayout commandButtons = new HorizontalLayout();
        commandButtons.addComponent(submitButton);
        commandButtons.addComponent(cancelButton);
        formLayout.addComponent(commandButtons);
        projectPopUp.setWidth(800, Unit.PIXELS);
        projectPopUp.setHeight(600, Unit.PIXELS);
        projectComponent.edit(projectId == null);
        getUI().addWindow(projectPopUp);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

    @PostConstruct
    private void init() {
        HorizontalLayout hl = new HorizontalLayout();

        addProjectsTable(contentLayout);
        addNewProjectButton(hl);

        contentLayout.addComponent(hl);
    }

    /*
    public void observeProjectAdd(@Observes @Any AddingProjectCompleteEvent event) {
        LOG.fatal("observeProjectAdd called!");
        projectsContainer.refresh();
    }
    */

    private void showNewProjectPopup() {
        createProjectPopup(null);
    }

    @Override
    protected Layout getContentLayout() {
        if (contentLayout == null) {
            contentLayout = new GridLayout();
        }
        return contentLayout;
    }
}
