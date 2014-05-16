package org.silverduck.jace.web.component;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.project.VersionFileType;
import org.silverduck.jace.domain.vcs.PluginType;
import org.silverduck.jace.services.project.ProjectService;
import org.silverduck.jace.web.vaadin.WorkingBeanFieldGroup;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import javax.ejb.EJB;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * @author Iiro Hietala 16.5.2014.
 */
public class AnalysisSettingsComponent extends BaseComponent<AnalysisSetting> {

    private TextField branchField;

    private CheckBox enabledField;

    private ComboBox projectField;

    private List<Project> projects;

    public AnalysisSettingsComponent(List<Project> projects) {
        this.projects = projects;
        setCompositionRoot(createAnalysisSettingsLayout());
    }

    @Override
    protected void bindFields(AnalysisSetting analysisSetting) {

        super.setFieldGroup(new WorkingBeanFieldGroup(AnalysisSetting.class));
        super.getFieldGroup().setItemDataSource(new BeanItem<AnalysisSetting>(analysisSetting));
        super.getFieldGroup().setBuffered(true);
        super.getFieldGroup().bind(projectField, "project");
        super.getFieldGroup().bind(branchField, "branch");
        super.getFieldGroup().bind(enabledField, "enabled");
    }

    private Component createAnalysisSettingsLayout() {
        Locale locale = getUI().getCurrent().getLocale();

        projectField = new ComboBox(AppResources.getLocalizedString("label.analysisForm.project", locale), projects);
        branchField = new TextField(AppResources.getLocalizedString("label.analysisForm.branch", locale));
        branchField.setImmediate(true);

        enabledField = new CheckBox(AppResources.getLocalizedString("label.analysisForm.enabled", locale));

        FormLayout basicDataForm = new FormLayout();
        basicDataForm.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        basicDataForm.setSizeFull();

        basicDataForm.addComponent(projectField);
        basicDataForm.addComponent(branchField);
        basicDataForm.addComponent(enabledField);

        return new VerticalLayout(basicDataForm);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        projectField.setReadOnly(readOnly);
        branchField.setReadOnly(readOnly);
        enabledField.setReadOnly(readOnly);
    }

}
