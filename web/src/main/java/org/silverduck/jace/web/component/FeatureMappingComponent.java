package org.silverduck.jace.web.component;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.feature.Feature;
import org.silverduck.jace.domain.feature.FeatureMapping;
import org.silverduck.jace.domain.feature.MappingType;
import org.silverduck.jace.web.vaadin.WorkingBeanFieldGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * Created by Iiro on 3.11.2014.
 */
public class FeatureMappingComponent extends BaseComponent<FeatureMapping> {
    private static final Logger LOG = LoggerFactory.getLogger(FeatureMappingComponent.class);

    private ComboBox typeField;
    private TextField patternField;
    private TextField featureField;

    private List<Feature> features;

    public FeatureMappingComponent() {
        super();
        setCompositionRoot(createLayout());
    }

    @Override
    protected void bindFields(FeatureMapping featureMapping) {
        super.setFieldGroup(new WorkingBeanFieldGroup(FeatureMapping.class));
        super.getFieldGroup().setItemDataSource(new BeanItem<FeatureMapping>(featureMapping));
        super.getFieldGroup().setBuffered(true);
        super.getFieldGroup().bind(typeField, "mappingType");
        super.getFieldGroup().bind(patternField, "sourcePattern");
        super.getFieldGroup().bind(featureField, "featureName");
    }


    private Component createLayout() {
        Locale locale = UI.getCurrent().getLocale();

        typeField = new ComboBox(AppResources.getLocalizedString("label.featureMappingComponent.type", locale));
        for (MappingType type : MappingType.values()) {
            typeField.addItem(type);
            typeField.setItemCaption(type,
                    AppResources.getLocalizedString(type.getResourceKey(), UI.getCurrent().getLocale()));
        }
        typeField.setImmediate(true);
        patternField = new TextField(AppResources.getLocalizedString("label.featureMappingComponent.pattern", locale));
        patternField.setWidth(100, Unit.PERCENTAGE);

        featureField = new TextField(AppResources.getLocalizedString("label.featureMappingComponent.featureName", locale));
        featureField.setWidth(100, Unit.PERCENTAGE);
        FormLayout form = new FormLayout();
        form.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        form.setSizeFull();
        form.addComponent(typeField);
        form.addComponent(patternField);
        form.addComponent(featureField);

        return new VerticalLayout(form);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        typeField.setReadOnly(readOnly);
        patternField.setReadOnly(readOnly);
        featureField.setReadOnly(readOnly);
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
