package com.swarm.listeners;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.xdebugger.impl.actions.*;
import com.swarm.States;
import com.swarm.invokingMethod;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.NotNull;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class DebugActionListener implements AnActionListener {

    Project project;
    private long lastEventTime = -1;

    public static invokingMethod invokingMethod = new invokingMethod();

    public DebugActionListener(Project project) {
        this.project = project;
    }

    private int handleEvent(String eventName) {
                DebuggerManagerEx debuggerManagerEx = DebuggerManagerEx.getInstanceEx(project);
                var file = (PsiJavaFile) debuggerManagerEx.getContext().getSourcePosition().getFile();

                String typeName = file.getName();
                String typePath = file.getVirtualFile().getPath();
                String sourceCode = file.getText();
                String typeFullName;
                if (!(typeFullName = file.getPackageName()).equals("")) {
                    typeFullName += ".";
                }
                typeFullName += typeName;

                int lineNumber = debuggerManagerEx.getContext().getSourcePosition().getLine();
                final String[] methodName = {""}; //is this the best way???
                final String[] methodSignature = {""};
                debuggerManagerEx.getContext().getDebugProcess().getManagerThread().invokeAndWait(new DebuggerCommandImpl() {
                    @Override
                    protected void action() throws Exception {
                        try {
                            methodName[0] = debuggerManagerEx.getContext().getFrameProxy().location().method().name();
                            methodSignature[0] = debuggerManagerEx.getContext().getFrameProxy().location().method().signature();
                        } catch (EvaluateException e) {
                            e.printStackTrace();
                        }
                    }
                });

                int typeId = HTTPRequests.createType(States.currentSessionId, typeFullName, typeName, typePath, sourceCode);
                int methodId = HTTPRequests.createMethod(typeId, methodSignature[0], methodName[0]);
                HTTPRequests.createEvent(States.currentSessionId, lineNumber, eventName, methodId);
                return methodId;
    }

    @Override
    public void beforeActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext, @NotNull AnActionEvent event) {
        if(States.currentSessionId == -1) {
            return;
        }
        // The following is a hack to work around an issue with IDEA, where certain events arrive
        // twice. See https://youtrack.jetbrains.com/issue/IDEA-219133
        final InputEvent input = event.getInputEvent();
        if(input instanceof MouseEvent) {
            if(input.getWhen() != 0 && lastEventTime == input.getWhen()) {
                return;
            }
            lastEventTime = input.getWhen();
        }
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
