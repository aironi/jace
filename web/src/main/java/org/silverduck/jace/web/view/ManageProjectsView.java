package org.silverduck.jace.web.view;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.silverduck.jace.common.exception.ExceptionHelper;
import org.silverduck.jace.common.exception.JaceRuntimeException;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.vcs.PluginType;
import org.silverduck.jace.services.project.ProjectService;
import org.silverduck.jace.services.project.impl.AddingProjectCompleteEvent;
import org.silverduck.jace.web.component.ProjectComponent;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Iiro Hietala 13.5.2014.
 */
@CDIView(ManageProjectsView.VIEW)
public class ManageProjectsView extends BaseView {

    private static final Log LOG = LogFactory.getLog(ManageProjectsView.class);

    public static final String VIEW = "ManageProjectsView";

    @EJB
    private ProjectService projectService;

    private JPAContainer<Project> projectsContainer = JPAContainerFactory.makeJndi(Project.class);

    Table projectsTable;

    public ManageProjectsView() {
        super();

    }

    private void addNewButton(HorizontalLayout hl) {
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

    private void addProjectsTable(VerticalLayout layout) {
        final Locale locale = UI.getCurrent().getLocale();
        projectsContainer.setFireContainerItemSetChangeEvents(true);
        projectsContainer.addNestedContainerProperty("pluginConfiguration.pluginType");
        projectsContainer.addNestedContainerProperty("pluginConfiguration.cloneUrl");
        projectsContainer.addNestedContainerProperty("pluginConfiguration.commitIdPattern");
        projectsContainer.addNestedContainerProperty("releaseInfo.versionFileType");
        projectsContainer.addNestedContainerProperty("releaseInfo.pathToVersionFile");
        projectsContainer.addNestedContainerProperty("releaseInfo.pattern");

        projectsTable = new Table(AppResources.getLocalizedString("label.projectsTable", locale), projectsContainer);

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
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(true);
        projectPopUp.setContent(horizontalLayout);
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        horizontalLayout.addComponent(contentLayout);

        // Content

        final ProjectComponent projectComponent = new ProjectComponent();
        contentLayout.addComponent(projectComponent);

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
        contentLayout.addComponent(commandButtons);

        Project project;
        if (projectId == null) {
            projectPopUp.setCaption(AppResources.getLocalizedString("caption.newProject", locale));
            project = Project.newProject();
        } else {
            projectPopUp.setCaption(AppResources.getLocalizedString("caption.editProject", locale));
            project = projectService.findProjectById(projectId);
        }

        projectComponent.edit(project, projectId == null);
        getUI().addWindow(projectPopUp);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

    @PostConstruct
    private void init() {
        VerticalLayout vl = new VerticalLayout();
        HorizontalLayout hl = new HorizontalLayout();

        vl.setSizeFull();

        hl.setSizeFull();

        addProjectsTable(vl);
        addNewButton(hl);

        vl.addComponent(hl);
        super.getContentLayout().addComponent(vl);
    }

    public void observeProjectAdd(@Observes @Any AddingProjectCompleteEvent event) {
        LOG.fatal("observeProjectAdd called!");
        projectsContainer.refresh();
    }

    private void showNewProjectPopup() {
        createProjectPopup(null);
    }
}
