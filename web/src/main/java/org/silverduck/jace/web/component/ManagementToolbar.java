package org.silverduck.jace.web.component;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.web.JaceUI;
import org.silverduck.jace.web.view.AnalysisView;
import org.silverduck.jace.web.view.ManageAnalysisSettingsView;
import org.silverduck.jace.web.view.ManageProjectsView;

/**
 * Common toolbar component
 */
public class ManagementToolbar extends CustomComponent {

    private Button analysisLink;

    private Button analysisView;

    private Image jaceLogo;

    private Button manageAnalysisButton;

    private Button manageProjectsButton;

    private MenuBar menuBar;

    public ManagementToolbar() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setDefaultComponentAlignment(Alignment.TOP_LEFT);

        createJaceLogo(vl);

        createMenuBar(vl);

        setCompositionRoot(vl);
    }

    private void createJaceLogo(VerticalLayout layout) {
        jaceLogo = new Image(AppResources.getLocalizedString("label.jaceDescription", UI.getCurrent().getLocale())
            + " - Version " + getClass().getPackage().getImplementationVersion());
        jaceLogo.setIcon(new ThemeResource("jace.png"));
        jaceLogo.setWidth(150, Unit.PIXELS);

        layout.addComponent(jaceLogo);
        layout.setComponentAlignment(jaceLogo, Alignment.TOP_LEFT);

    }

    private void createMenuBar(VerticalLayout hl) {
        menuBar = new MenuBar();

        hl.addComponent(menuBar);
        menuBar.addItem(AppResources.getLocalizedString("label.analysisView", UI.getCurrent().getLocale()),
            new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    JaceUI.navigateTo(AnalysisView.VIEW);
                }
            });
        menuBar.addItem(
            AppResources.getLocalizedString("label.manageAnalysisSettingsView", UI.getCurrent().getLocale()),
            new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    JaceUI.navigateTo(ManageAnalysisSettingsView.VIEW);
                }
            });

        menuBar.addItem(AppResources.getLocalizedString("label.manageProjects", UI.getCurrent().getLocale()),
            new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    JaceUI.navigateTo(ManageProjectsView.VIEW);
                }
            });
    }
}
