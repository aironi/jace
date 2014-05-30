package org.silverduck.jace.web.view;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.vcs.Commit;
import org.silverduck.jace.services.analysis.AnalysisService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The root view of the application.
 */
@CDIView
public class AnalysisView extends BaseView implements View {

    public static final String ALL_FEATURES = "All Features";

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

    private ComboBox featureSelect;

    private Panel releasePanel;

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

        changedFeaturesTable.setVisibleColumns("created", "feature.name", "slo.path", "slo.packageName",
            "slo.className", "diff.modificationType", "diff.commit.commitId", "diff.commit.message");

        changedFeaturesTable.setColumnHeaders(
            AppResources.getLocalizedString("label.changedFeatureTable.created", locale),
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

        featureSelect = new ComboBox(AppResources.getLocalizedString("label.analysisView.features", locale));
        featureSelect.setImmediate(true);
        featureSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                changedFeaturesContainer.removeContainerFilters("feature.name");
                if (event.getProperty() != null && event.getProperty().getValue() != null) {
                    String value = event.getProperty().getValue().toString();
                    if (!ALL_FEATURES.equals(value)) {
                        changedFeaturesContainer.addContainerFilter("feature.name", value, false, false);
                    }
                }
            }
        });

        // detailsLayout.addComponent(releaseSelect);
        detailsLayout.addComponent(featureSelect);

        detailsLayout.addComponent(changedFeaturesTable);
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
        populateReleaseTree();
        populateCommitTree();
        populateAnalysisTree();
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
                String caption = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(analysis.getCreated());
                if (Boolean.TRUE.equals(analysis.getInitialAnalysis())) {
                    caption += " (Initial)";
                }
                hca.getContainerProperty(aItem, "caption").setValue(caption);
                hca.getContainerProperty(aItem, "id").setValue(analysis.getId());
                analysisTree.setChildrenAllowed(aItem, false);

            }

        }
        analysisTree.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        analysisTree.setItemCaptionPropertyId("caption");
        analysisTree.setContainerDataSource(hca);
        analysisTree.setImmediate(true);
        /*
         * Property idProperty = event.getItem().getItemProperty("id"); if (idProperty != null) { Object idValue =
         * idProperty.getValue(); if (idValue != null) { Long id = (Long) idValue; populateDetailsForAnalysis(id); } }
         */
        analysisTree.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object idValue = analysisTree.getValue();
                Item item = analysisTree.getItem(idValue);
                if (item != null) {
                    Property idProperty = item.getItemProperty("id");
                    if (idProperty != null) {
                        Long id = (Long) idProperty.getValue();
                        populateDetailsForAnalysis(id);
                    }
                }
            }
        });

    }

    private void populateCommitTree() {
        HierarchicalContainer hca = new HierarchicalContainer();
        Map<Project, List<String>> projectCommits = new HashMap<Project, List<String>>();
        hca.addContainerProperty("caption", String.class, "");
        hca.addContainerProperty("id", String.class, null);

        List<Analysis> analyses = analysisService.listAllAnalyses();
        for (Analysis analysis : analyses) {
            List<String> list = projectCommits.get(analysis.getProject());
            if (list == null) {
                list = analysisService.listAllCommitIds(analysis.getProject().getId());
                projectCommits.put(analysis.getProject(), list);
            }
        }
        Iterator<Map.Entry<Project, List<String>>> iter = projectCommits.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Project, List<String>> item = iter.next();
            Project project = item.getKey();
            List<String> list = item.getValue();
            Object parent = hca.addItem();
            hca.getContainerProperty(parent, "caption").setValue(project.getName());
            // hca.getContainerProperty(parent, "id").setValue(project.getId());
            for (String commitId : list) {
                Object aItem = hca.addItem();
                hca.setParent(aItem, parent);
                hca.getContainerProperty(aItem, "caption").setValue(commitId);
                hca.getContainerProperty(aItem, "id").setValue(commitId);
                commitTree.setChildrenAllowed(aItem, false);
            }
        }
        commitTree.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        commitTree.setItemCaptionPropertyId("caption");
        commitTree.setContainerDataSource(hca);
        commitTree.setImmediate(true);
        commitTree.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object idValue = commitTree.getValue();
                Item item = commitTree.getItem(idValue);
                if (item != null) {
                    Property idProperty = item.getItemProperty("id");
                    if (idProperty != null) {
                        String commitId = (String) idProperty.getValue();
                        populateDetailsForCommit(commitId);
                    }
                }
            }
        });
    }

    private void populateDetailsCommon() {
        featureSelect.removeAllItems();
        Set<String> featureNames = new HashSet<String>();
        for (Object id : changedFeaturesContainer.getItemIds()) {
            featureNames.add(changedFeaturesContainer.getItem(id).getEntity().getFeature().getName());
        }

        featureSelect.addItem(ALL_FEATURES);
        for (String featureName : featureNames) {
            featureSelect.addItem(featureName);
        }
    }

    private void populateDetailsForAnalysis(Long analysisId) {
        changedFeaturesContainer.removeAllContainerFilters();
        changedFeaturesContainer.addContainerFilter(new Compare.Equal("analysis.id", analysisId));
        changedFeaturesContainer.applyFilters();
        changedFeaturesContainer.refresh();
        populateDetailsCommon();
    }

    private void populateDetailsForCommit(String commitId) {
        changedFeaturesContainer.removeAllContainerFilters();
        changedFeaturesContainer.addContainerFilter(new Compare.Equal("diff.commit.commitId", commitId));
        changedFeaturesContainer.applyFilters();
        changedFeaturesContainer.refresh();
        populateDetailsCommon();
    }

    private void populateDetailsForRelease(String release) {
        changedFeaturesContainer.removeAllContainerFilters();
        changedFeaturesContainer.addContainerFilter(new Compare.Equal("analysis.releaseVersion", release));
        changedFeaturesContainer.applyFilters();
        changedFeaturesContainer.refresh();
        populateDetailsCommon();
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
                releaseTree.setChildrenAllowed(aItem, false);
            }
        }
        releaseTree.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        releaseTree.setItemCaptionPropertyId("caption");
        releaseTree.setContainerDataSource(hca);
        releaseTree.setImmediate(true);

        releaseTree.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Object idValue = releaseTree.getValue();
                Item item = releaseTree.getItem(idValue);

                if (item != null) {
                    Property idProperty = item.getItemProperty("id");
                    if (idProperty != null) {
                        String release = (String) idProperty.getValue();
                        populateDetailsForRelease(release);
                    }
                }
            }
        });
    }

}
