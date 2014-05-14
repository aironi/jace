package org.silverduck.jace.web.view;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.vcs.PluginType;
import org.silverduck.jace.services.project.ProjectService;
import org.silverduck.jace.web.component.ProjectComponent;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.swing.*;

/**
 * Created by ihietala on 13.5.2014.
 */
@CDIView(ManageProjectsView.VIEW)
public class ManageProjectsView extends BaseView {
    public static final String VIEW = "ManageProjectsView";

    @EJB
    private ProjectService projectService;

    private JPAContainer<Project> projectsContainer = JPAContainerFactory.makeJndi(Project.class);

    public ManageProjectsView() {
        super();
        VerticalLayout vl = new VerticalLayout();
        HorizontalLayout hl = new HorizontalLayout();

        vl.setSizeFull();

        hl.setSizeFull();

        super.getContentLayout().addComponent(vl);

        addProjectsTable(vl);
        addNewButton(hl);

        vl.addComponent(hl);
    }

    private void addNewButton(HorizontalLayout hl) {
        Button newButton = new Button(AppResources.getLocalizedString("label.newProject", getUI().getCurrent()
            .getLocale()));

        newButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                showNewProjectPopup();
            }
        });
        hl.addComponent(newButton);

    }

    private void addProjectsTable(VerticalLayout layout) {

        Table projectsTable = new Table(AppResources.getLocalizedString("label.projectsTable", getUI().getCurrent()
            .getLocale()), projectsContainer);
        projectsTable.setVisibleColumns("name");
        projectsTable.setImmediate(true);
        /*
         * applicantsDebug.addItemClickListener(new ItemClickEvent.ItemClickListener() {
         * 
         * @Override public void itemClick(ItemClickEvent event) { applicantsContainer.getItem(event.getItemId()); } });
         */

        layout.addComponent(projectsTable);

    }

    private void createProjectPopup(Long projectId) {
        final Window projectPopUp = new Window();

        projectPopUp.setModal(true);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(true);

        projectPopUp.setContent(horizontalLayout);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        horizontalLayout.addComponent(contentLayout);
        final ProjectComponent projectComponent = new ProjectComponent();
        contentLayout.addComponent(projectComponent);

        Button submitButton = new Button(AppResources.getLocalizedString("label.submit", getUI().getCurrent()
            .getLocale()));
        submitButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (projectComponent.isValid()) {
                    Project project = projectComponent.commit();

                    // Since we're using transient entity up to this point (only adding), this may be done
                    // as follows. Still, it's not pretty...
                    // TODO: We should be using MVP pattern instead.
                    if (project.getId() == null) {
                        projectService.addProject(project);
                    } else {
                        projectService.updateProject(project);
                    }

                    projectPopUp.close();
                } else {
                    Notification.show(AppResources.getLocalizedString("applicantForm.validationErrorsNotification",
                        getUI().getCurrent().getLocale()), Notification.Type.TRAY_NOTIFICATION);
                }
            }
        });

        Button cancelButton = new Button(AppResources.getLocalizedString("label.cancel", getUI().getCurrent()
            .getLocale()));
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
            projectPopUp.setCaption(AppResources.getLocalizedString("caption.newProject", getLocale()));
            project = new Project();
            project.setName("");
            project.getPluginConfiguration().setPluginType(PluginType.GIT);
        } else {
            project = null; // FIXME: implement fetching
        }
        projectComponent.edit(project);

        getUI().addWindow(projectPopUp);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

    private void showNewProjectPopup() {
        createProjectPopup(null);
    }
}
