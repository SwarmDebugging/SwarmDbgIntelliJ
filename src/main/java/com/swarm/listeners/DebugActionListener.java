package com.swarm.listeners;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.impl.actions.*;
import com.intellij.xdebugger.impl.frame.XDebuggerFramesList;
import com.intellij.xdebugger.impl.ui.tree.nodes.XDebuggerTreeNode;
import org.jetbrains.annotations.NotNull;

public class DebugActionListener implements AnActionListener{


    @Override
    public void afterActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext, @NotNull AnActionEvent event) {
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        XDebuggerFramesList y = new XDebuggerFramesList(project);
        XDebugSession session = XDebugSession.DATA_KEY.getData(dataContext);

        if (action instanceof StepIntoAction || action instanceof ForceStepIntoAction) {
            int i = 0;
            String x = "";
        } else if (action instanceof StepOverAction || action instanceof ForceStepOverAction) {
            int i = 1;
        } else if (action instanceof StepOutAction) {
            int i = 2;
        } else if(action instanceof ResumeAction) {
            int i = 3;
        }
    }
}
