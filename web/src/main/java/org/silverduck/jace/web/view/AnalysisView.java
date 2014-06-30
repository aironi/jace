package org.silverduck.jace.web.view;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.cdi.CDIView;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.silverduck.jace.common.localization.AppResources;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.analysis.impl.ScoredCommit;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The root view of the application.
 */
@CDIView
public class AnalysisView extends BaseView implements View {

    public static final String ALL_FEATURES = "All Features";

    private static final Log LOG = LogFactory.getLog(AnalysisView.class);

    public static final String VIEW = "";

    @EJB
    private AnalysisDao analysisDao;

    private Panel analysisPanel;

    @EJB
    private AnalysisService analysisService;

    private Tree analysisTree;

    private JPAContainer<ChangedFeature> changedFeaturesContainer;

    private Table changedFeaturesTable;

    private Panel commitPanel;

    // private Tree commitTree;

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

    private Component createDetails() {
        detailsLayout = new VerticalLayout();

        Locale locale = UI.getCurrent().getLocale();
        Panel detailsPanel = new Panel(AppResources.getLocalizedString("label.analysisView.details", locale));

        changedFeaturesTable = new Table(AppResources.getLocalizedString("label.analysisView.changedFeaturesTable",
            locale), changedFeaturesContainer);

        changedFeaturesTable.setVisibleColumns("feature.name", "slo.path", "slo.packageName", "slo.className",
            "diff.modificationType", "diff.commit.commitId", "diff.commit.message", "diff.commit.authorName",
            "diff.commit.authorEmail", "diff.commit.authorDateOfChange",
            // "diff.commit.authorTimeZone",
            "diff.commit.formattedTimeZoneOffset", "created");

        changedFeaturesTable.setColumnHeaders(
            AppResources.getLocalizedString("label.changedFeatureTable.featureName", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.sloPath", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.sloPackageName", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.sloClassName", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.modificationType", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.commitId", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.commitMessage", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.authorName", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.authorEmail", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.dateOfChange", locale),
            // AppResources.getLocalizedString("label.changedFeatureTable.timeZone", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.timeZoneOffSet", locale),
            AppResources.getLocalizedString("label.changedFeatureTable.created", locale));

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
        changedFeaturesContainer.addNestedContainerProperty("diff.commit.authorName");
        changedFeaturesContainer.addNestedContainerProperty("diff.commit.authorEmail");
        changedFeaturesContainer.addNestedContainerProperty("diff.commit.authorDateOfChange");
        // TODO: The table doesn't know how to show this field
        // changedFeaturesContainer.addNestedContainerProperty("diff.commit.authorTimeZone");
        changedFeaturesContainer.addNestedContainerProperty("diff.commit.formattedTimeZoneOffset");

        TabSheet analysisTabs = new TabSheet();
        analysisTabs.setSizeFull();
        analysisTabs.addTab(createReleaseTab());
        analysisTabs.addTab(createAnalysisTab());
        analysisTabs.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                if (event.getTabSheet().getSelectedTab().equals(analysisPanel)) {
                    populateAnalysisTree();
                } else if (event.getTabSheet().getSelectedTab().equals(releasePanel)) {
                    populateReleaseTree();
                }
            }
        });

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(100, Unit.PERCENTAGE);
        hl.setHeight(80, Unit.PERCENTAGE);
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
        SortedMap<Project, List<Analysis>> projectAnalyses = new TreeMap<Project, List<Analysis>>(
            new Comparator<Project>() {
                @Override
                public int compare(Project o1, Project o2) {
                    CompareToBuilder ctb = new CompareToBuilder();
                    ctb.append(o1.getName(), o2.getName());
                    return ctb.build();
                }
            });
        hca.addContainerProperty("caption", String.class, "");
        hca.addContainerProperty("id", Long.class, null);

        List<Analysis> analyses = analysisDao.listAllAnalyses();
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
            Collections.sort(list, new Comparator<Analysis>() {
                @Override
                public int compare(Analysis o1, Analysis o2) {
                    CompareToBuilder ctb = new CompareToBuilder();
                    ctb.append(o2.getCreated(), o1.getCreated()); // reversed
                    return ctb.build();
                }
            });
            for (Analysis analysis : list) {
                Object aItem = hca.addItem();
                hca.setParent(aItem, parent);
                String caption = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(analysis.getCreated());
                if (Boolean.TRUE.equals(analysis.getInitialAnalysis())) {
                    caption += " (Initial)";
                }
                caption += " - "
                    + AppResources.getLocalizedString(analysis.getAnalysisStatus().getResourceKey(), UI.getCurrent()
                        .getLocale());
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

        SortedMap<String, List<ScoredCommit>> releaseCommits = new TreeMap<String, List<ScoredCommit>>();

        SortedMap<Project, List<String>> projectReleases = new TreeMap<Project, List<String>>(
            new Comparator<Project>() {
                @Override
                public int compare(Project o1, Project o2) {
                    CompareToBuilder ctb = new CompareToBuilder();
                    ctb.append(o1.getName(), o2.getName());
                    return ctb.build();
                }
            });
        hca.addContainerProperty("caption", String.class, "");
        hca.addContainerProperty("id", String.class, null);
        hca.addContainerProperty("type", String.class, null);

        List<Analysis> analyses = analysisDao.listAllAnalyses();
        for (Analysis analysis : analyses) {
            List<String> list = projectReleases.get(analysis.getProject());
            if (list == null) {
                list = analysisDao.listAllReleases(analysis.getProject().getId());
                projectReleases.put(analysis.getProject(), list);
                for (String release : list) {
                    if (release != null) {
                        List<ScoredCommit> commits = analysisService.listScoredCommitsByRelease(analysis.getProject()
                            .getId(), release);
                        if (commits == null) {
                            commits = new ArrayList<>();
                        }

                        releaseCommits.put(release, commits);
                    }
                }
            }
        }
        Iterator<Map.Entry<Project, List<String>>> iter = projectReleases.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Project, List<String>> item = iter.next();
            Project project = item.getKey();
            List<String> list = item.getValue();
            Object projectItem = hca.addItem();
            hca.getContainerProperty(projectItem, "caption").setValue(project.getName());
            hca.getContainerProperty(projectItem, "type").setValue("project");
            hca.getContainerProperty(projectItem, "id").setValue(project.getId().toString());
            Collections.sort(list);
            for (String release : list) {
                Object releaseItem = hca.addItem();
                hca.setParent(releaseItem, projectItem);
                hca.getContainerProperty(releaseItem, "caption").setValue(release);
                hca.getContainerProperty(releaseItem, "id").setValue(release);
                hca.getContainerProperty(releaseItem, "type").setValue("release");
                if (release != null) {
                    for (ScoredCommit scoredCommit : releaseCommits.get(release)) {
                        Object commitItem = hca.addItem();
                        hca.setParent(commitItem, releaseItem);
                        String caption = scoredCommit.getCommitId() + " (Score: " + scoredCommit.getScore() + ")";
                        hca.getContainerProperty(commitItem, "caption").setValue(caption);
                        hca.getContainerProperty(commitItem, "id").setValue(scoredCommit.getCommitId());
                        hca.getContainerProperty(commitItem, "type").setValue("commit");
                        releaseTree.setChildrenAllowed(commitItem, false);
                    }
                } else {
                    releaseTree.setChildrenAllowed(releaseItem, false);
                }
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
                    Property typeProperty = item.getItemProperty("type");
                    if (idProperty != null && typeProperty != null) {
                        String type = (String) typeProperty.getValue();
                        String key = (String) idProperty.getValue();
                        if ("release".equals(type)) {
                            populateDetailsForRelease(key);
                        } else if ("commit".equals(type)) {
                            populateDetailsForCommit(key);
                        }
                    }
                }
            }
        });
    }

}
