package org.silverduck.jace.web.component;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.web.JaceUI;
import org.silverduck.jace.web.view.ManageAnalysisSettingsView;
import org.silverduck.jace.web.view.ManageProjectsView;

/**
 * Common toolbar component
 */
public class ManagementToolbar extends CustomComponent {

    private Button analysisLink;

    private Button analysisView;

    private Image jaceLogo;

    private Button manageProjects;

    public ManagementToolbar() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        vl.addComponent(hl);

        createJaceLogo(hl);

        createAnalysisLink(hl);
        createManageProjectsLink(hl);
        setCompositionRoot(hl);
    }

    private void createAnalysisLink(HorizontalLayout layout) {
        analysisView = new Button(AppResources.getLocalizedString("label.analysisView", UI.getCurrent().getLocale()));
        analysisView.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                JaceUI.navigateTo(ManageAnalysisSettingsView.VIEW);
            }
        });
        analysisView.addStyleName(BaseTheme.BUTTON_LINK);
        layout.addComponent(analysisView);
        layout.setComponentAlignment(analysisView, Alignment.TOP_RIGHT);
    }

    private void createJaceLogo(HorizontalLayout layout) {
        jaceLogo = new Image(AppResources.getLocalizedString("label.jaceDescription", UI.getCurrent().getLocale()));
        jaceLogo.setIcon(new ThemeResource("jace.png"));
        jaceLogo.setWidth(150, Unit.PIXELS);

        layout.addComponent(jaceLogo);
        layout.setComponentAlignment(jaceLogo, Alignment.TOP_LEFT);

    }

    private void createManageProjectsLink(HorizontalLayout layout) {
        manageProjects = new Button(
            AppResources.getLocalizedString("label.manageProjects", UI.getCurrent().getLocale()));
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
