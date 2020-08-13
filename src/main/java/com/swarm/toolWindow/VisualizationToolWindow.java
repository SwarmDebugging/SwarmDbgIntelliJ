package com.swarm.toolWindow;

import com.intellij.application.Topics;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.util.ui.JBUI;
import com.swarm.models.Product;
import com.swarm.models.Task;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class VisualizationToolWindow extends SimpleToolWindowPanel implements DumbAware, TreeSelectionProvider.Handler, Disposable {
    private final Project project;
    private boolean constantNodeSize = true;

    @Override
    public JComponent getContent() {
        return this.getComponent();
    }

    public VisualizationToolWindow(Project project){
        super(false, true);
        this.project = project;

        Topics.subscribe(TreeSelectionProvider.Handler.TREE_SELECTION_TOPIC, this, this);

        createToolBar();
        TreeSelectionProvider.setTreeNode(TreeSelectionProvider.getTreeNode());

        setContent(new JBLabel("Fetching visualisation..."));
    }

    private void createToolBar() {
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new ToggleNodeSizeAction());
        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("swarm", group, false);
        setToolbar(JBUI.Panels.simplePanel(actionToolbar.getComponent()));
    }

    @Override
    public void treeSelectionAction(Object treeNode) {
        if (treeNode == null) {
            setContent(new JBLabel("Select a task to view the visualisation"));

        } else if(treeNode instanceof Task) {
            Task task = (Task) treeNode;

            if(JBCefApp.isSupported()){
                setContent(new JBCefBrowser("http://localhost:8080/getInvocationGraph/" + task.getId() + "/" + constantNodeSize).getComponent());
            } else {
                BrowserUtil.browse("http://localhost:8080/getInvocationGraph/" + task.getId() + "/" + constantNodeSize);
            }
        } else {
            setContent(new JBLabel("Select a task to view the visualisation"));
        }
    }


    @Override
    public void dispose() {
    }

    private class ToggleNodeSizeAction extends DumbAwareAction {
        ToggleNodeSizeAction() {
            super("Toggle Node Sized to Number of Events");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            constantNodeSize = !constantNodeSize;
            TreeSelectionProvider.setTreeNode(TreeSelectionProvider.getTreeNode());
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(TreeSelectionProvider.getTreeNode() instanceof Task);
        }
    }
}
