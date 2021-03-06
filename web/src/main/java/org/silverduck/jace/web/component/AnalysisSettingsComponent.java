package org.silverduck.jace.web.component;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.analysis.Granularity;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.project.ProjectBranch;
import org.silverduck.jace.web.vaadin.BeanComboBoxHelper;
import org.silverduck.jace.web.vaadin.WorkingBeanFieldGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Locale;

/**
 * @author Iiro Hietala 16.5.2014.
 */
public class AnalysisSettingsComponent extends BaseComponent<AnalysisSetting> {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisSettingsComponent.class);

    private AnalysisSetting analysisSetting;

    private CheckBox automaticFeatureMappingField;

    private ComboBox branchField;

    private CheckBox enabledField;

    private ComboBox granularityField;

    private ComboBox projectField;

    private List<Project> projects;

    public AnalysisSettingsComponent(List<Project> projects) {
        this.projects = projects;
        setCompositionRoot(createAnalysisSettingsLayout());
    }

    @Override
    protected void bindFields(AnalysisSetting analysisSetting) {
        this.analysisSetting = analysisSetting;
        super.setFieldGroup(new WorkingBeanFieldGroup(AnalysisSetting.class));
        super.getFieldGroup().setItemDataSource(new BeanItem<AnalysisSetting>(analysisSetting));
        super.getFieldGroup().setBuffered(true);
        super.getFieldGroup().bind(projectField, "project");
        super.getFieldGroup().bind(branchField, "branch");
        super.getFieldGroup().bind(granularityField, "granularity");
        super.getFieldGroup().bind(enabledField, "enabled");
        super.getFieldGroup().bind(automaticFeatureMappingField, "automaticFeatureMapping");
    }

    private Component createAnalysisSettingsLayout() {
        Locale locale = UI.getCurrent().getLocale();

        branchField = new ComboBox(AppResources.getLocalizedString("label.analysisForm.branch", locale));
        branchField.setImmediate(true);

        projectField = BeanComboBoxHelper.createComboBox("label.analysisForm.project", locale, Project.class, "name",
            projects);
        projectField.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Project project = (Project) event.getProperty().getValue();
                if (project != null) {
                    branchField.removeAllItems();
                    for (ProjectBranch projectBranch : project.getBranches()) {
                        branchField.addItem(projectBranch.getBranch());
                    }
                }
            }
        });

        if (analysisSetting != null && analysisSetting.getProject() != null) {
            projectField.setValue(analysisSetting.getProject().getId());
        }

        granularityField = new ComboBox(AppResources.getLocalizedString("label.analysisForm.granularity", locale));
        for (Granularity granularity : Granularity.values()) {
            granularityField.addItem(granularity);
            granularityField.setItemCaption(granularity,
                AppResources.getLocalizedString(granularity.getResourceKey(), locale));

        }
        granularityField.setImmediate(true);

        automaticFeatureMappingField = new CheckBox(AppResources.getLocalizedString(
            "label.analysisForm.automaticFeatureMapping", locale));
        automaticFeatureMappingField.setReadOnly(true); // Manual Mapping not yet supported, always on
        enabledField = new CheckBox(AppResources.getLocalizedString("label.analysisForm.enabled", locale));

        FormLayout basicDataForm = new FormLayout();
        basicDataForm.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        basicDataForm.setSizeFull();

        basicDataForm.addComponent(projectField);
        basicDataForm.addComponent(branchField);
        basicDataForm.addComponent(granularityField);
        basicDataForm.addComponent(automaticFeatureMappingField);
        basicDataForm.addComponent(enabledField);

        return new VerticalLayout(basicDataForm);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        projectField.setReadOnly(readOnly);
        branchField.setReadOnly(readOnly);
        // TODO: Always set Granularity read only until method level granularity is implemented
        granularityField.setReadOnly(true);
        enabledField.setReadOnly(readOnly);
    }

}
