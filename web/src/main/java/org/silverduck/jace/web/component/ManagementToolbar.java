package org.silverduck.jace.web.component;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.web.JaceUI;
import org.silverduck.jace.web.view.ManageProjectsView;

/**
 * Common toolbar component
 */
public class ManagementToolbar extends CustomComponent {

    private Button analysisLink;

    private Image jaceLogo;

    private Button manageProjects;

    public ManagementToolbar() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setStyleName("light-green-background");
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        vl.addComponent(hl);

        createJaceLogo(hl);

        createManageProjectsLink(hl);
        setCompositionRoot(vl);
    }

    private void createJaceLogo(HorizontalLayout layout) {
        jaceLogo = new Image(AppResources.getLocalizedString("label.jaceDescription", getUI().getCurrent().getLocale()));
        jaceLogo.setIcon(new ThemeResource("jace.png"));
        jaceLogo.setWidth(150, Unit.PIXELS);

        layout.addComponent(jaceLogo);
        layout.setComponentAlignment(jaceLogo, Alignment.TOP_LEFT);

    }

    private void createManageProjectsLink(HorizontalLayout layout) {
        manageProjects = new Button(AppResources.getLocalizedString("label.manageProjects", getUI().getCurrent()
            .getLocale()));
        manageProjects.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                JaceUI.navigateTo(ManageProjectsView.VIEW);
            }
        });
        manageProjects.addStyleName(BaseTheme.BUTTON_LINK);
        layout.addComponent(manageProjects);
        layout.setComponentAlignment(manageProjects, Alignment.TOP_RIGHT);
    }

}
