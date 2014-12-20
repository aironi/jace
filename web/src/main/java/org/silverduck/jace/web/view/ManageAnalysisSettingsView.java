package org.silverduck.jace.web.view;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.ui.*;
import org.silverduck.jace.common.exception.ExceptionHelper;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.ProjectService;
import org.silverduck.jace.web.component.AnalysisSettingsComponent;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Iiro Hietala 16.5.2014.
 */
@CDIView(ManageAnalysisSettingsView.VIEW)
public class ManageAnalysisSettingsView extends BaseView {
    public static final String VIEW = "ManageAnalysisSettingsView";

    @EJB
    private AnalysisService analysisService;

    @EJB
    private ProjectService projectService;

    private JPAContainer<AnalysisSetting> analysisSettingsJPAContainer = JPAContainerFactory
        .makeJndi(AnalysisSetting.class);

    private Table analysisTable;

    private VerticalLayout contentLayout;

    public ManageAnalysisSettingsView() {
        super();

    }

    private void addAnalysisSettingTable(Layout layout) {
        final Locale locale = UI.getCurrent().getLocale();
        analysisTable = new Table();
        analysisTable.setContainerDataSource(analysisSettingsJPAContainer);

        analysisSettingsJPAContainer.addNestedContainerProperty("project.name");
        analysisTable.setVisibleColumns("project.name", "branch", "enabled");

        analysisTable.setColumnHeaders(AppResources.getLocalizedString("label.projectForm.name", locale),
            AppResources.getLocalizedString("label.analysisForm.branch", locale),
            AppResources.getLocalizedString("label.analysisForm.enabled", locale));

        analysisTable.setImmediate(true);
        Table.ColumnGenerator columnGenerator = new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                final Object finalItemId = itemId;
                if ("Edit".equals(columnId)) {
                    Button editButton = new Button(AppResources.getLocalizedString("label.edit", locale));
                    editButton.addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            createAnalysisSettingPopup((Long) finalItemId);
                        }
                    });
                    return editButton;
                } else if ("Remove".equals(columnId)) {
                    Button removeButton = new Button(AppResources.getLocalizedString("label.remove", locale));
                    removeButton.addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            analysisService.removeAnalysisSettingById(((Long) finalItemId));
                            analysisSettingsJPAContainer.refresh();
                        }
                    });
                    return removeButton;
                } else if ("Trigger".equals(columnId)) {
                    Button triggerButton = new Button(AppResources.getLocalizedString("label.dev.triggerAnalysis",
                        locale));
                    triggerButton.addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            analysisService.triggerAnalysis(((Long) finalItemId));
                        }
                    });
                    return triggerButton;
                } else if ("InitialTrigger".equals(columnId)) {
                    Button triggerButton = new Button(AppResources.getLocalizedString(
                        "label.dev.triggerInitialAnalysis", locale));
                    triggerButton.addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            analysisService.initialAnalysis(((Long) finalItemId));
                        }
                    });
                    return triggerButton;
                }
                return null;
            }
        };

        analysisTable.addGeneratedColumn("Edit", columnGenerator);
        analysisTable.addGeneratedColumn("Remove", columnGenerator);
        analysisTable.addGeneratedColumn("Trigger", columnGenerator);
        analysisTable.addGeneratedColumn("InitialTrigger", columnGenerator);

        layout.addComponent(analysisTable);

    }

    private void addNewAnalysisSettingButton(Layout layout) {
        Locale locale = UI.getCurrent().getLocale();
        Button newButton = new Button(AppResources.getLocalizedString("label.newAnalysis", locale));

        newButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                createAnalysisSettingPopup(null);
            }
        });
        layout.addComponent(newButton);
    }

    private void createAnalysisSettingPopup(Long analysisSettingsId) {
        final Window analysisPopup = new Window();
        final Locale locale = UI.getCurrent().getLocale();
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
                Notification.show("Error", "Creation of analysis settings failed. Error message: " + cause,
                    Notification.Type.ERROR_MESSAGE);

                doDefault(event);
            }
        });

        analysisPopup.setModal(true);

        // Layout
        HorizontalLayout popupLayout = new HorizontalLayout();
        popupLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        popupLayout.setSpacing(true);
        popupLayout.setMargin(true);
        analysisPopup.setContent(popupLayout);
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        popupLayout.addComponent(formLayout);

        // Content

        final AnalysisSettingsComponent analysisSettingsComponent = new AnalysisSettingsComponent(
            projectService.findAllProjects());
        formLayout.addComponent(analysisSettingsComponent);

        Button submitButton = new Button(AppResources.getLocalizedString("label.submit", locale));
        submitButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (analysisSettingsComponent.isValid()) {
                    final AnalysisSetting settings = analysisSettingsComponent.commit();
                    Notification.show("Initial Analysis Started.", Notification.Type.TRAY_NOTIFICATION);
                    final Future<Boolean> result;
                    if (settings.getId() == null) {
                        result = analysisService.addAnalysisSetting(settings);
                    } else {
                        result = analysisService.updateAnalysisSetting(settings);
                    }

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                if (Boolean.TRUE.equals(result.get())) {
                                    UI.getCurrent().access(new Runnable() {
                                        @Override
                                        public void run() {
                                            analysisSettingsJPAContainer.refresh();
                                            Notification.show(AppResources.getLocalizedString(
                                                            "notification.analysisSettingAdded", UI.getCurrent().getLocale(),
                                                            settings.getProject().getName(), settings.getBranch()),
                                                    Notification.Type.TRAY_NOTIFICATION);
                                        }
                                    });
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } catch (ExecutionException e) {
                                Notification.show("Error occurred when adding Analysis Setting.\nCause: "
                                        + ExceptionHelper.toHumanReadable(e));
                            }
                        }
                    }.start();
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
        formLayout.addComponent(commandButtons);

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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

    @PostConstruct
    private void init() {
        addNewAnalysisSettingButton(contentLayout);
        addAnalysisSettingTable(contentLayout);
        contentLayout.setExpandRatio(analysisTable, 1f);
    }

    @Override
    protected Layout getContentLayout() {
        if (contentLayout == null) {
            contentLayout = new VerticalLayout();
        }
        return contentLayout;
    }
}
