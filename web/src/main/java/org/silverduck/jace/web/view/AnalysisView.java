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

    private Panel analysisPanel;

    @EJB
    private AnalysisService analysisService;

    private Tree analysisTree;

    private JPAContainer<ChangedFeature> changedFeaturesContainer;

    private Table changedFeaturesTable;

    private Panel commitPanel;

    private Tree commitTree;

    Accordion detailsAccordion;

    private VerticalLayout detailsLayout;

    private Panel releasePanel;

    private ComboBox releaseSelect;

    private Tree releaseTree;

    public AnalysisView() {
        super();
    }

    private Component createAnalysisTab() {
        analysisTree = new Tree();
        analysisPanel = new Panel(AppResources.getLocalizedString("label.analyses", UI.getCurrent().getLocale()));
        analysisPanel.setContent(analysisTree);
        return analysisPanel;
    }

    private Component createCommitTab() {
        commitPanel = new Panel(AppResources.getLocalizedString("label.commits", UI.getCurrent().getLocale()));
        commitTree = new Tree();
        commitPanel.setContent(commitTree);
        return commitPanel;
    }

    private Component createDetails() {
        detailsLayout = new VerticalLayout();

        Locale locale = UI.getCurrent().getLocale();
        Panel detailsPanel = new Panel(AppResources.getLocalizedString("label.analysisView.details", locale));

        changedFeaturesTable = new Table(AppResources.getLocalizedString("label.analysisView.changedFeaturesTable",
            locale), changedFeaturesContainer);

        changedFeaturesTable.setVisibleColumns("feature.name", "slo.path", "slo.packageName", "slo.className",
            "diff.modificationType", "diff.commit.commitId", "diff.commit.message");

        changedFeaturesTable.setColumnHeaders(
            AppResources.getLocalizedString("label.changedFeatureTable.featureName", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.sloPath", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.sloPackageName", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.sloClassName", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.modificationType", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.commitId", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.commitMessage", locale));

        changedFeaturesTable.setImmediate(true);
        changedFeaturesTable.setSelectable(true);
        changedFeaturesTable.setSizeFull();

        // detailsLayout.addComponent(releaseSelect);
        detailsAccordion = new Accordion();
        detailsAccordion.addTab(changedFeaturesTable);
        detailsAccordion.addTab(new Label("All features"));

        detailsLayout.addComponent(detailsAccordion);
        detailsPanel.setContent(detailsLayout);

        return detailsPanel;

    }

    private Component createReleaseTab() {
        releasePanel = new Panel(AppResources.getLocalizedString("label.releases", UI.getCurrent().getLocale()));
        releaseTree = new Tree();
        releasePanel.setContent(releaseTree);
        return releasePanel;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

    @PostConstruct
    private void init() {
        setSizeFull();
        setDefaultComponentAlignment(Alignment.TOP_CENTER);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
        contentLayout.setSpacing(true);
        changedFeaturesContainer = JPAContainerFactory.makeJndi(ChangedFeature.class);
        changedFeaturesContainer.addContainerFilter(new Compare.Equal("analysis.releaseVersion", null));
        changedFeaturesContainer.applyFilters();
        changedFeaturesContainer.setFireContainerItemSetChangeEvents(true);
        changedFeaturesContainer.addNestedContainerProperty("feature.name");
        changedFeaturesContainer.addNestedContainerProperty("slo.path");
        changedFeaturesContainer.addNestedContainerProperty("slo.packageName");
        changedFeaturesContainer.addNestedContainerProperty("slo.className");
        changedFeaturesContainer.addNestedContainerProperty("diff.modificationType");
        changedFeaturesContainer.addNestedContainerProperty("diff.commit.commitId");
        changedFeaturesContainer.addNestedContainerProperty("diff.commit.message");

        TabSheet analysisTabs = new TabSheet();
        analysisTabs.setSizeFull();
        analysisTabs.addTab(createReleaseTab());
        analysisTabs.addTab(createCommitTab());
        analysisTabs.addTab(createAnalysisTab());
        analysisTabs.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                if (event.getTabSheet().getSelectedTab().equals(analysisPanel)) {
                    populateAnalysisTree();
                } else if (event.getTabSheet().getSelectedTab().equals(releasePanel)) {
                    populateReleaseTree();
                } else if (event.getTabSheet().getSelectedTab().equals(commitPanel)) {
                    populateCommitTree();
                }
            }
        });

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        hl.setSpacing(true);

        Component detailsPanel = createDetails();
        hl.addComponent(analysisTabs);
        hl.addComponent(detailsPanel);
        hl.setExpandRatio(analysisTabs, 3);
        hl.setExpandRatio(detailsPanel, 7);

        contentLayout.addComponent(hl);
        super.getContentLayout().addComponent(contentLayout);
    }

    private void populateAnalysisTree() {
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

            for (Analysis analysis : list) {
                Object aItem = hca.addItem();
                hca.setParent(aItem, parent);
                String caption = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(analysis.getCreated());
                if (Boolean.TRUE.equals(analysis.getInitialAnalysis())) {
                    caption += " (Initial)";
                }
                hca.getContainerProperty(aItem, "caption").setValue(caption);
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
                        populateDetailsForAnalysis(id);
                    }
                }
            }
        });

    }

    private void populateCommitTree() {

    }

    private void populateDetailsForAnalysis(Long analysisId) {
        // releaseSelect.removeAllItems();
        changedFeaturesContainer.removeAllContainerFilters();
        changedFeaturesContainer.addContainerFilter(new Compare.Equal("analysis.id", analysisId));
        changedFeaturesContainer.applyFilters();
        changedFeaturesContainer.refresh();
    }

    private void populateDetailsForRelease(String release) {
        changedFeaturesContainer.removeAllContainerFilters();
        changedFeaturesContainer.addContainerFilter(new Compare.Equal("analysis.releaseVersion", release));
        changedFeaturesContainer.applyFilters();
        changedFeaturesContainer.refresh();
    }

    private void populateReleaseTree() {
        HierarchicalContainer hca = new HierarchicalContainer();
        Map<Project, List<String>> projectReleases = new HashMap<Project, List<String>>();
        hca.addContainerProperty("caption", String.class, "");
        hca.addContainerProperty("id", String.class, null);

        List<Analysis> analyses = analysisService.listAllAnalyses();
        for (Analysis analysis : analyses) {
            List<String> list = projectReleases.get(analysis.getProject());
            if (list == null) {
                list = analysisService.listAllReleases(analysis.getProject().getId());
                projectReleases.put(analysis.getProject(), list);
            }
        }
        Iterator<Map.Entry<Project, List<String>>> iter = projectReleases.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Project, List<String>> item = iter.next();
            Project project = item.getKey();
            List<String> list = item.getValue();
            Object parent = hca.addItem();
            hca.getContainerProperty(parent, "caption").setValue(project.getName());
            // hca.getContainerProperty(parent, "id").setValue(project.getId());
            for (String release : list) {
                Object aItem = hca.addItem();
                hca.setParent(aItem, parent);
                hca.getContainerProperty(aItem, "caption").setValue(release);
                hca.getContainerProperty(aItem, "id").setValue(release);
            }
        }
        releaseTree.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        releaseTree.setItemCaptionPropertyId("caption");
        releaseTree.setContainerDataSource(hca);
        releaseTree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                Property idProperty = event.getItem().getItemProperty("id");
                if (idProperty != null) {
                    Object idValue = idProperty.getValue();
                    if (idValue != null) {
                        String release = (String) idValue;
                        populateDetailsForRelease(release);
                    }
                }
            }
        });

    }

}
