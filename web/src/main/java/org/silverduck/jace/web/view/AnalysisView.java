package org.silverduck.jace.web.view;

import com.hs18.vaadin.addon.graph.GraphJSComponent;
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
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.silverduck.jace.common.localization.AppResources;

import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.slo.SLO;
import org.silverduck.jace.domain.slo.SLOStatus;
import org.silverduck.jace.domain.slo.SLOType;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.analysis.impl.ScoredCommit;
import org.silverduck.jace.web.component.LabelDisplayComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The root view of the application.
 */
@CDIView(AnalysisView.VIEW)
public class AnalysisView extends BaseView implements View {

    public static final String ALL_FEATURES = "All Features";

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisView.class);

    public static final String VIEW = "";
    public static final String PROJECT_TYPE = "project";
    public static final String RELEASE_TYPE = "release";
    public static final String COMMIT_TYPE = "commit";
    public static final String FEATURE_NAME_FILTER = "feature.name";
    public static final int FEATURE_COLUMNS = 6;
    private static final int MAX_DEP_LEVELS = 9;

    @EJB
    private AnalysisService analysisService;

    private JPAContainer<ChangedFeature> changedFeaturesContainer;

    private GridLayout contentLayout;
    private Tree analysisTree;
    private Tree releaseTree;
    private VerticalLayout detailsLayout;
    private VerticalLayout dependencyLevelsLayout;
    private LabelDisplayComponent scoreComponent;
    private LabelDisplayComponent commitIdComponent;
    private GridLayout changedFeaturesGrid;
    private Table changedFeaturesTable;
    private Window dependencyWindow;
    private GraphJSComponent graphComponent;

    public AnalysisView() {
        super();
    }

    private Component createAnalysisTab() {
        Panel panel = new Panel();
        analysisTree = new Tree();
        panel.setContent(analysisTree);
        return panel;

    }

    private Component createDetails() {
        detailsLayout = new VerticalLayout();

        createChangesPanel(detailsLayout);
        createChangedFeaturesGrid(detailsLayout);
        createChangedFeaturesTable(detailsLayout);

        Locale locale = UI.getCurrent().getLocale();
        Panel detailsPanel = new Panel(AppResources.getLocalizedString("label.analysisView.changedFeatures", locale));
        detailsPanel.setContent(detailsLayout);
        return detailsPanel;
    }

    private void createChangedFeaturesGrid(Layout layout) {
        changedFeaturesGrid = new GridLayout();
        changedFeaturesGrid.setSizeFull();
        layout.addComponent(changedFeaturesGrid);
    }

    private void createChangedFeaturesTable(Layout layout) {
        Locale locale = UI.getCurrent().getLocale();
        changedFeaturesTable = new Table();
        changedFeaturesTable.setContainerDataSource(changedFeaturesContainer);

        changedFeaturesTable.setVisibleColumns("feature.name", "slo.path", "slo.packageName", "slo.className",
                "diff.modificationType", "diff.commit.commitId", "diff.commit.message", "diff.commit.authorName",
                "diff.commit.authorEmail", "diff.commit.authorDateOfChange",
                // "diff.commit.authorTimeZone",
                "diff.commit.formattedTimeZoneOffset", "created");
        changedFeaturesTable.setColumnReorderingAllowed(true);
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

        changedFeaturesTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                ChangedFeature cf = changedFeaturesContainer.getItem(event.getItemId()).getEntity();
                if (cf.getSlo().getSloType() == SLOType.SOURCE) {
                    showDependencyPopup(cf);
                }
            }
        });
        changedFeaturesTable.setImmediate(true);
        changedFeaturesTable.setSelectable(true);
        changedFeaturesTable.setWidth(100, Unit.PERCENTAGE);
        layout.addComponent(changedFeaturesTable);
    }

    private void showDependencyPopup(ChangedFeature changedFeature) {
        if (dependencyWindow == null) {
            dependencyWindow = new Window();
            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();
            layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
            Panel panel = new Panel();
            graphComponent = new GraphJSComponent();
            graphComponent.setNodesSize(120, 50);
            graphComponent.setImmediate(true);
            String lhtml = "<div id='graph' class='graph' ></div>";//add style='overflow:scroll' if required
            Label graphLabel = new Label(lhtml, ContentMode.HTML);
            layout.addComponent(graphLabel);
            layout.addComponent(graphComponent);
            layout.setComponentAlignment(graphComponent, Alignment.MIDDLE_CENTER);
            panel.setContent(layout);
            dependencyWindow.setContent(panel);
            dependencyWindow.setResizable(true);
            dependencyWindow.setModal(true);
            dependencyWindow.setWidth(800, Unit.PIXELS);
            dependencyWindow.setHeight(600, Unit.PIXELS);
        }
        dependencyWindow.setCaption("Classes that depend on " + changedFeature.getSlo().getClassName());
        populateGraph(changedFeature);
        getUI().getCurrent().addWindow(dependencyWindow);
    }

    private void populateGraph(ChangedFeature changedFeature) {
        graphComponent.clear();
        Map<Integer, Set<String>> nodesOnLevel = new HashMap<>();
        populateGraph(changedFeature.getSlo(), null, 1, nodesOnLevel);
        graphComponent.refresh();
    }

    protected void populateGraph(SLO slo, String parentId, Integer level, Map<Integer, Set<String>> nodesOnLevel) {
        LOG.debug("Populating graph for {} ({}, {}) that is a dependency for {} classes", slo.getClassName(), slo.getId(), slo.getSloStatus(), slo.getDependantOf().size());

        if (nodesOnLevel.get(level) == null) {
            nodesOnLevel.put(level, new HashSet<String>());
        }

        String nodeId = slo.getId().toString();
        if (parentId != null) {
            nodeId += "_" + parentId.toString();
        }

        try {
            String title = slo.getClassName();
            if (title == null) {
                int i = slo.getPath().lastIndexOf("/");
                if (i != -1) {
                    title = slo.getPath().substring(i + 1);
                } else {
                    title = "Unknown - " + nodeId;
                }
            }
            if (!hasNodeOnUpperOrEqualLevel(nodesOnLevel, title, level)) {
                LOG.debug("Adding node with id {} and name {} at level {} with parent {}", slo.getId(), slo.getClassName(), level, parentId);
                nodesOnLevel.get(level).add(title);
                graphComponent.addNode(nodeId, title, level.toString(), null, parentId);
                graphComponent.getNodeProperties(nodeId).put("title", title);
                setGraphNodeColor(level, nodeId);
            } else {
                return;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create graph", e);
        }

        for (SLO dependency : slo.getDependantOf()) {
            populateGraph(dependency, nodeId, level + 1, nodesOnLevel);
        }
    }

    private boolean hasNodeOnUpperOrEqualLevel(Map<Integer, Set<String>> nodes, String node, int level) {
        for (int i = level; i >= 1; i--) {
            if (nodes.get(i).contains(node)) {
                return true;
            }
        }
        return false;
    }

    private void setGraphNodeColor(Integer level, String nodeId) throws Exception {
        String color;
        switch (level) {
            case 1:
                color = "#0099cc";
                break;
            case 2:
                color = "#32add6";
                break;
            case 3:
                color = "#7fcce5";
                break;
            case 4:
                color = "#b2e0ef";
                break;
            case 5:
                color = "#e5f4f9";
                break;
            default:
                color = "#ffffff";
                break;
        }
        graphComponent.getNodeProperties(nodeId).put("fill", color);
    }

    private void createChangesPanel(Layout layout) {
        commitIdComponent = new LabelDisplayComponent("label.analysisView.changesPanel.commitId");
        scoreComponent = new LabelDisplayComponent("label.analysisView.changesPanel.score");
        dependencyLevelsLayout = new VerticalLayout();

        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setMargin(true);
        detailsLayout.setSpacing(true);
        detailsLayout.addComponent(commitIdComponent);
        detailsLayout.addComponent(scoreComponent);
        detailsLayout.addComponent(dependencyLevelsLayout);

        Panel changesPanel = new Panel();
        changesPanel.setWidth(100, Unit.PERCENTAGE);
        changesPanel.setContent(detailsLayout);
        layout.addComponent(changesPanel);
    }


    private Component createReleaseTab() {
        Panel releasePanel = new Panel();
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
        setDefaultComponentAlignment(Alignment.TOP_CENTER);

        contentLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);
        contentLayout.setSpacing(true);
        createChangedFeaturesContainer();

        final TabSheet analysisTabs = createTabs();

        HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);

        Component detailsPanel = createDetails();
        detailsPanel.setHeight("100%");

        rootLayout.addComponent(analysisTabs);
        rootLayout.addComponent(detailsPanel);
        rootLayout.setExpandRatio(analysisTabs, 3);
        rootLayout.setExpandRatio(detailsPanel, 7);

        contentLayout.addComponent(rootLayout);
    }

    private TabSheet createTabs() {
        final TabSheet analysisTabs = new TabSheet();
        // analysisTabs.setSizeFull();
        analysisTabs.addTab(createReleaseTab(), AppResources.getLocalizedString("label.releases", UI.getCurrent().getLocale()));
        analysisTabs.addTab(createAnalysisTab(), AppResources.getLocalizedString("label.analyses", UI.getCurrent().getLocale()));
        analysisTabs.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                if (event.getTabSheet().getSelectedTab().equals(analysisTree)) {
                    populateAnalysisTree();
                } else if (event.getTabSheet().getSelectedTab().equals(releaseTree)) {
                    populateReleaseTree();
                }
            }
        });
        analysisTabs.setHeight("100%");
        return analysisTabs;
    }

    private void createChangedFeaturesContainer() {
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
    }

    @Override
    protected Layout getContentLayout() {
        if (contentLayout == null) {
            contentLayout = new GridLayout();
        }
        return contentLayout;
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

    private void populateChangedFeaturesGrid() {
        changedFeaturesGrid.removeAllComponents();
        Set<String> featureNames = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String string1, String string2) {
                if (string1.equals(ALL_FEATURES)) {
                    return -1;
                } else {
                    return string1.compareTo(string2);
                }
            }
        });
        for (Object id : changedFeaturesContainer.getItemIds()) {
            featureNames.add(changedFeaturesContainer.getItem(id).getEntity().getFeature().getName());
        }

        featureNames.add(ALL_FEATURES);
        int amountOfFeatures = featureNames.size();
        int cols = FEATURE_COLUMNS;
        int rows = amountOfFeatures / cols ;
        if (rows == 0) {
            rows++;
        }

        changedFeaturesGrid.setColumns(cols);
        changedFeaturesGrid.setRows(rows);

        Iterator<String> iterator = featureNames.iterator();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols && iterator.hasNext(); col++) {
                String feature = iterator.next();
                Button featureButton = new Button();
                featureButton.setStyleName(BaseTheme.BUTTON_LINK);
                featureButton.setCaption(feature);
                featureButton.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        changedFeaturesContainer.removeContainerFilters(FEATURE_NAME_FILTER);
                        String feature = event.getButton().getCaption();
                        if (!ALL_FEATURES.equals(feature)) {
                            changedFeaturesContainer.addContainerFilter(FEATURE_NAME_FILTER, feature, false, false);
                        }
                        changedFeaturesContainer.applyFilters();
                    }
                });
                changedFeaturesGrid.addComponent(featureButton, col, row);
            }
        }
    }

    private void populateDetailsForAnalysis(Long analysisId) {
        populateChangedFeaturesByProperty("analysis.id", analysisId);
    }

    private void populateDetailsForCommit(ScoredCommit scoredCommit) {
        Map<String, Object> params = new HashMap();
        params.put("diff.commit.commitId", scoredCommit.getCommitId());
        params.put("analysis.releaseVersion", scoredCommit.getReleaseVersion());
        populateChangedFeaturesByProperty(params);
        populateDetailsPanel(scoredCommit);
    }

    private void populateDetailsPanel(ScoredCommit scoredCommit) {
        commitIdComponent.setValue(scoredCommit.getCommitId());
        scoreComponent.setValue(scoredCommit.getRoundedScore().toString());
        dependencyLevelsLayout.removeAllComponents();
        LabelDisplayComponent baseLevelLabel = new LabelDisplayComponent("label.analysisView.changesPanel.dependencies.baseLevel");
        baseLevelLabel.setValue(scoredCommit.getDirectChanges().toString());
        dependencyLevelsLayout.addComponent(baseLevelLabel);
        if (scoredCommit.getDependenciesPerLevel() != null) {
            for (int i = 0; i < Math.min(scoredCommit.getDependenciesPerLevel().size(), MAX_DEP_LEVELS); i++) {
                LabelDisplayComponent labelDisplayComponent = new LabelDisplayComponent("label.analysisView.changesPanel.dependencies.level." + (i + 1));
                labelDisplayComponent.setValue(scoredCommit.getDependenciesPerLevel().get(i).toString());
                dependencyLevelsLayout.addComponent(labelDisplayComponent);
            }
        }
    }

    private void populateDetailsForRelease(String release) {
        populateChangedFeaturesByProperty("analysis.releaseVersion", release);
    }

    private void populateChangedFeaturesByProperty(String propertyName, Object propertyValue) {
        populateChangedFeaturesByProperty(Collections.singletonMap(propertyName, propertyValue));
    }

    private void populateChangedFeaturesByProperty(Map<String, Object> propertyNameValueMap) {
        changedFeaturesContainer.removeAllContainerFilters();
        for (Map.Entry<String, Object> entry : propertyNameValueMap.entrySet()) {
            changedFeaturesContainer.addContainerFilter(new Compare.Equal(entry.getKey(), entry.getValue()));
        }

        changedFeaturesContainer.applyFilters();
        changedFeaturesContainer.refresh();
        changedFeaturesContainer.sort(new String[]{FEATURE_NAME_FILTER}, new boolean[]{true});
        populateChangedFeaturesGrid();
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
        hca.addContainerProperty("extra", Object.class, null);

        List<Analysis> analyses = analysisService.listAllAnalyses();
        for (Analysis analysis : analyses) {
            List<String> list = projectReleases.get(analysis.getProject());
            if (list == null) {
                list = analysisService.listAllReleases(analysis.getProject().getId());
                if (list.isEmpty()) {
                    list = Collections.singletonList("Unknown");
                }
                LOG.debug("All releases: " + list);
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
            hca.getContainerProperty(projectItem, "type").setValue(PROJECT_TYPE);
            hca.getContainerProperty(projectItem, "id").setValue(project.getId().toString());
            Collections.sort(list);
            for (String release : list) {
                Object releaseItem = hca.addItem();
                hca.setParent(releaseItem, projectItem);
                hca.getContainerProperty(releaseItem, "caption").setValue(release);
                hca.getContainerProperty(releaseItem, "id").setValue(release);
                hca.getContainerProperty(releaseItem, "type").setValue(RELEASE_TYPE);
                if (release != null) {
                    for (ScoredCommit scoredCommit : releaseCommits.get(release)) {
                        Object commitItem = hca.addItem();
                        hca.setParent(commitItem, releaseItem);
                        String caption = scoredCommit.getCommitId() + " (Score: " + scoredCommit.getRoundedScore() + ")";
                        hca.getContainerProperty(commitItem, "caption").setValue(caption);
                        hca.getContainerProperty(commitItem, "id").setValue(scoredCommit.getCommitId());
                        hca.getContainerProperty(commitItem, "type").setValue(COMMIT_TYPE);
                        hca.getContainerProperty(commitItem, "extra").setValue(scoredCommit);
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
                    Property extraProperty = item.getItemProperty("extra");
                    if (idProperty != null && typeProperty != null) {
                        String type = (String) typeProperty.getValue();
                        String key = (String) idProperty.getValue();

                        if (RELEASE_TYPE.equals(type)) {
                            populateDetailsForRelease(key);
                        } else if (COMMIT_TYPE.equals(type)) {
                            ScoredCommit scoredCommit = null;
                            if (extraProperty != null) {
                                scoredCommit = (ScoredCommit) extraProperty.getValue();
                            }
                            populateDetailsForCommit(scoredCommit);
                        }
                    }
                }
            }
        });
    }

}
