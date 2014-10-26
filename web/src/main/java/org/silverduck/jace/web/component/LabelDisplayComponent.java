package org.silverduck.jace.web.component;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import org.silverduck.jace.common.localization.AppResources;

/**
 * Simple component that displays a caption and a value as label components.
 * Created by Iiro on 19.10.2014.
 */
public class LabelDisplayComponent extends com.vaadin.ui.CustomComponent {
    private String captionKey;
    private String value;

    Label captionLabel;
    Label valueLabel;
    private boolean init;

    public LabelDisplayComponent(String captionKey) {
        this(captionKey, null);
    }

    public LabelDisplayComponent(String captionKey, String value) {
        super();
        this.setCaptionKey(captionKey);
        this.setValue(value);
    }

    @Override
    public void attach() {
        super.attach();
        initLayout();
        refresh();
    }

    private void refresh() {
        if (init) {
            this.captionLabel.setValue(AppResources.getLocalizedString(getCaptionKey(), UI.getCurrent().getLocale()));
            this.valueLabel.setValue(getValue());
        }
    }

    private void initLayout() {
        if (!init) {
            HorizontalLayout hl = new HorizontalLayout();
            this.captionLabel = new Label();
            this.valueLabel = new Label();
            this.captionLabel.setWidth(300, Unit.PIXELS);
            hl.addComponent(this.captionLabel);
            hl.addComponent(this.valueLabel);
            setCompositionRoot(hl);
            init = true;
        }
    }

    public void setValue(String value) {
        this.value = value;
        refresh();
    }

    public String getValue() {
        return value;
    }

    public String getCaptionKey() {
        return captionKey;
    }

    public void setCaptionKey(String captionKey) {
        this.captionKey = captionKey;
        refresh();
    }
}
