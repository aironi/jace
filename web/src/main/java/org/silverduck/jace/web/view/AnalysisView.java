package org.silverduck.jace.web.view;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.services.analysis.AnalysisService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The root view of the application.
 */
@CDIView
public class AnalysisView extends BaseView implements View {
    public static final String VIEW = "";

    Accordion allFeaturesAccordion;

    @EJB
    private AnalysisService analysisService;

    private Tree analysisTree;

    Accordion changedFeaturesAccordion;

    private JPAContainer<ChangedFeature> changedFeaturesContainer;

    private Table changedFeaturesTable;

    private VerticalLayout detailsLayout;

    private ComboBox releaseSelect;

    public AnalysisView() {
        super();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        populateTree();
    }

    @PostConstruct
    private void init() {
        setSizeFull();
        setDefaultComponentAlignment(Alignment.TOP_CENTER);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
        contentLayout.setSpacing(true);

        analysisTree = new Tree(AppResources.getLocalizedString("label.analyses", UI.getCurrent().getLocale()));

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSizeFull();
        horizontalLayout.addComponent(analysisTree);

        detailsLayout = new VerticalLayout();

        Locale locale = UI.getCurrent().getLocale();
        // branchField = new ComboBox(AppResources.getLocalizedString("label.analysisForm.branch", locale));
        // branchField.setImmediate(true);
        // releaseSelect = new ComboBox(AppResources.getLocalizedString("label.analysisView.releaseSelect", locale));
        // releaseSelect.setImmediate(true);

        // releaseSelect.addValueChangeListener(new Property.ValueChangeListener() {
        // @Override
        // public void valueChange(Property.ValueChangeEvent event) {
        //
        // }
        // });

        changedFeaturesContainer = JPAContainerFactory.makeJndi(ChangedFeature.class);
        changedFeaturesContainer.addContainerFilter(new Compare.Equal("analysis.releaseVersion", null));
        changedFeaturesContainer.applyFilters();
        changedFeaturesContainer.setFireContainerItemSetChangeEvents(true);
        changedFeaturesContainer.addNestedContainerProperty("feature.name");
        changedFeaturesContainer.addNestedContainerProperty("slo.path");
        changedFeaturesContainer.addNestedContainerProperty("slo.packageName");
        changedFeaturesContainer.addNestedContainerProperty("slo.className");

        changedFeaturesTable = new Table(AppResources.getLocalizedString("label.analysisView.changedFeaturesTable",
            locale), changedFeaturesContainer);

        changedFeaturesTable.setVisibleColumns("feature.name", "slo.path", "slo.packageName", "slo.className");

        changedFeaturesTable.setColumnHeaders(
            AppResources.getLocalizedString("label.changedFeatureTable.featureName", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.sloPath", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.sloPackageName", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.sloClassName", locale));

        changedFeaturesTable.setImmediate(true);

        // detailsLayout.addComponent(releaseSelect);
        changedFeaturesAccordion = new Accordion();
        changedFeaturesAccordion.addComponent(changedFeaturesTable);
        changedFeaturesAccordion.setCaption(AppResources.getLocalizedString(
            "label.analysisView.changedFeaturesAccordion", locale));
        allFeaturesAccordion = new Accordion();
        allFeaturesAccordion.setCaption(AppResources.getLocalizedString("label.analysisView.allFeaturesAccordion",
            locale));
        detailsLayout.addComponent(changedFeaturesAccordion);

        horizontalLayout.addComponent(detailsLayout);
        horizontalLayout.setExpandRatio(analysisTree, 3);
        horizontalLayout.setExpandRatio(detailsLayout, 7);
        contentLayout.addComponent(horizontalLayout);

        super.getContentLayout().addComponent(contentLayout);
    }

    private void populateDetails(Analysis analysis) {
        // releaseSelect.removeAllItems();
        changedFeaturesContainer.removeAllContainerFilters();
        changedFeaturesContainer.addContainerFilter(new Compare.Equal("analysis.releaseVersion", analysis
            .getReleaseVersion()));
        changedFeaturesContainer.applyFilters();
        changedFeaturesContainer.refresh();
    }

    private void populateTree() {
        // TODO: Investigate if it is possible to implement with JPAContainer.
        HierarchicalContainer hca = new HierarchicalContainer();
        Map<Project, List<Analysis>> projectAnalyses = new HashMap<Project, List<Analysis>>();
        hca.addContainerProperty("caption", String.class, "");
        hca.addContainerProperty("id", Long.class, null);

        List<Analysis> analyses = analysisService.listAllAnalyses();
        for (Analysis analysis : analyses) {
            List<Analysis> list = projectAnalyses.get(analysis.getProject());
            if (list == null) {
                list = new ArrayList<Analysis>();
                projectAnalyses.put(analysis.getProject(), list);
            }
            list.add(analysis);
        }
        Iterator<Map.Entry<Project, List<Analysis>>> iter = projectAnalyses.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Project, List<Analysis>> item = iter.next();
            Project project = item.getKey();
            List<Analysis> list = item.getValue();
            Object parent = hca.addItem();
            hca.getContainerProperty(parent, "caption").setValue(project.getName());
            // hca.getContainerProperty(parent, "id").setValue(project.getId());
            for (Analysis analysis : list) {
                Object aItem = hca.addItem();
                hca.setParent(aItem, parent);
                hca.getContainerProperty(aItem, "caption").setValue(
                    new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(analysis.getCreated()));
                hca.getContainerProperty(aItem, "id").setValue(analysis.getId());

            }

        }
        analysisTree.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        analysisTree.setItemCaptionPropertyId("caption");
        analysisTree.setContainerDataSource(hca);
        analysisTree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                Property idProperty = event.getItem().getItemProperty("id");
                if (idProperty != null) {
                    Object idValue = idProperty.getValue();
                    if (idValue != null) {
                        Long id = (Long) idValue;
                        Analysis analysis = analysisService.findAnalysisById(id);
                        populateDetails(analysis);
                    }
                }
            }
        });

    }

}
