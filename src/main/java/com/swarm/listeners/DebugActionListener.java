package com.swarm.listeners;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.xdebugger.impl.actions.*;
import com.sun.jdi.Location;
import com.swarm.States;
import com.swarm.invokingMethod;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.NotNull;

public class DebugActionListener implements AnActionListener {

    Project project;

    public static invokingMethod invokingMethod = new invokingMethod();

    public DebugActionListener(Project project) {
        this.project = project;
    }

    private int handleEvent(String eventName) {
        var debuggerManagerEx = DebuggerManagerEx.getInstanceEx(project);
        var file = (PsiJavaFile) debuggerManagerEx.getContext().getSourcePosition().getFile();

        String typeName = file.getName();
        String typePath = file.getVirtualFile().getPath();
        String sourceCode = file.getText();
        String typeFullName;
        if (!(typeFullName = file.getPackageName()).equals("")) {
            typeFullName += ".";
        }
        typeFullName += typeName;

        int typeId = HTTPRequests.createType(States.currentSessionId, typeFullName, typeName, typePath, sourceCode);

        int lineNumber = debuggerManagerEx.getContext().getSourcePosition().getLine();
        String methodName = "";
        String methodSignature = "";
        try {
            Location loc = debuggerManagerEx.getContext().getFrameProxy().location();
            methodName = debuggerManagerEx.getContext().getFrameProxy().location().method().name();
            methodSignature = debuggerManagerEx.getContext().getFrameProxy().location().method().signature();
        } catch (EvaluateException e) {
            e.printStackTrace();
        }

        int methodId = HTTPRequests.createMethod(typeId, methodSignature, methodName);
        HTTPRequests.createEvent(States.currentSessionId, lineNumber, eventName, methodId);
        return methodId;
    }

    @Override
    public void beforeActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext, @NotNull AnActionEvent event) {
        if (action instanceof StepIntoAction || action instanceof ForceStepIntoAction) {
            invokingMethod.setId(handleEvent("StepInto"));
            States.isSteppedInto = true;
        } else if (action instanceof StepOverAction || action instanceof ForceStepOverAction) {
            handleEvent("StepOver");
        } else if (action instanceof StepOutAction) {
            handleEvent("StepOut");
        } else if (action instanceof ResumeAction) {
            handleEvent("Resume");
        }
    }
}
