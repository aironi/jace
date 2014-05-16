package org.silverduck.jace.web.view;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.ProjectService;
import org.silverduck.jace.web.component.AnalysisSettingsComponent;

import javax.ejb.EJB;
import java.util.Locale;

/**
 * @author Iiro Hietala 16.5.2014. @
 */
@CDIView(AnalysisView.VIEW)
public class AnalysisView extends BaseView {
    public static final String VIEW = "AnalysisView";

    @EJB
    private AnalysisService analysisService;

    private JPAContainer<AnalysisSetting> analysisSettingsJPAContainer = JPAContainerFactory
        .makeJndi(AnalysisSetting.class);

    Table analysisTable;

    @EJB
    private ProjectService projectService;

    public AnalysisView() {
        super();
        VerticalLayout vl = new VerticalLayout();
        HorizontalLayout hl = new HorizontalLayout();

        vl.setSizeFull();

        hl.setSizeFull();

        super.getContentLayout().addComponent(vl);

        addAnalysisSettingTable(vl);
        addNewButton(hl);

        vl.addComponent(hl);
    }

    private void addAnalysisSettingTable(VerticalLayout vl) {
        Locale locale = getUI().getCurrent().getLocale();
        analysisTable = new Table(AppResources.getLocalizedString("label.projectsTable", locale),
            analysisSettingsJPAContainer);

        analysisSettingsJPAContainer.addNestedContainerProperty("project.name");
        analysisTable.setVisibleColumns("project.name", "branch", "enabled");

        analysisTable.setColumnHeaders(AppResources.getLocalizedString("label.projectForm.name", locale),
            AppResources.getLocalizedString("label.analysisForm.branch", locale),
            AppResources.getLocalizedString("label.analysisForm.enabled", locale));

        analysisTable.setImmediate(true);
        vl.addComponent(analysisTable);

    }

    private void addNewButton(HorizontalLayout hl) {
        Locale locale = getUI().getCurrent().getLocale();
        Button newButton = new Button(AppResources.getLocalizedString("label.newAnalysis", locale));

        newButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                showAnalysisSettingPopup(null);
            }
        });
        hl.addComponent(newButton);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

    private void showAnalysisSettingPopup(Long analysisSettingsId) {
        final Window analysisPopup = new Window();
        final Locale locale = getUI().getCurrent().getLocale();
        // Configure the error handler for the UI
        analysisPopup.setErrorHandler(new DefaultErrorHandler() {
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                String cause = "";
                for (Throwable t = event.getThrowable(); t != null; t = t.getCause()) {
                    if (t.getCause() == null) {
                        cause = t.getMessage();
                    }
                }
                Notification.show("Creation of analysis settings failed. Error message: " + cause,
                    Notification.Type.ERROR_MESSAGE);

                // Do the default error handling (optional)
                doDefault(event);
            }
        });

        analysisPopup.setModal(true);

        // Layout
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(true);
        analysisPopup.setContent(horizontalLayout);
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        horizontalLayout.addComponent(contentLayout);

        // Content

        final AnalysisSettingsComponent analysisSettingsComponent = new AnalysisSettingsComponent(
            projectService.findAllProjects());
        contentLayout.addComponent(analysisSettingsComponent);

        Button submitButton = new Button(AppResources.getLocalizedString("label.submit", locale));
        submitButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (analysisSettingsComponent.isValid()) {
                    AnalysisSetting settings = analysisSettingsComponent.commit();
                    // FIXME: Implement JCA compliant solution. For now the files go into config/<projectName>

                    if (settings.getId() == null) {
                        analysisService.addAnalysisSetting(settings);
                    } else {
                        analysisService.updateAnalysisSetting(settings);
                    }

                    analysisPopup.close();
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
                analysisSettingsComponent.discard();
                analysisPopup.close();
            }
        });
        HorizontalLayout commandButtons = new HorizontalLayout();
        commandButtons.addComponent(submitButton);
        commandButtons.addComponent(cancelButton);
        contentLayout.addComponent(commandButtons);

        AnalysisSetting analysisSettings;
        if (analysisSettingsId == null) {
            analysisPopup.setCaption(AppResources.getLocalizedString("caption.newAnalysisSetting", locale));
            analysisSettings = AnalysisSetting.newAnalysisSetting();
        } else {
            analysisPopup.setCaption(AppResources.getLocalizedString("caption.editAnalysisSetting", locale));
            analysisSettings = analysisService.findAnalysisSettingById(analysisSettingsId);
        }

        analysisSettingsComponent.edit(analysisSettings);
        getUI().addWindow(analysisPopup);
    }
}
