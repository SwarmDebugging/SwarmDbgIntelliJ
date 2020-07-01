package com.swarm.listeners;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.intellij.xdebugger.impl.actions.*;
import com.swarm.States;
import com.swarm.models.Method;
import com.swarm.models.Type;
import com.swarm.tools.HTTPUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class DebugActionListener implements AnActionListener, DumbAware {

    Project project;
    private long lastEventTime = -1;

    public static int invokingMethodId;

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
        debuggerManagerEx.getContext().getDebugProcess().getManagerThread().invoke(new DebuggerCommandImpl() {
            @Override
            protected void action() throws Exception {
                try {
                    //TODO: bug here
                    methodName[0] = Objects.requireNonNull(debuggerManagerEx.getContext().getFrameProxy()).location().method().name();
                    methodSignature[0] = debuggerManagerEx.getContext().getFrameProxy().location().method().signature();
                } catch (EvaluateException e) {
                    e.printStackTrace();
                }
            }
        });

        Type type = new Type();
        type.setSession(States.currentSession);
        type.setFullName(typeFullName);
        type.setName(typeName);
        type.setFullPath(typePath);
        type.setSourceCode(sourceCode);
        type.create();

        Method method = new Method();
        method.setType(type);
        method.setSignature(methodSignature[0]);
        method.setName(methodName[0]);
        method.create();

        HTTPUtils.createEvent(States.currentSession.getId(), lineNumber, eventName, method.getId());
        return method.getId();
    }

    @Override
    public void beforeActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext, @NotNull AnActionEvent event) {
        if (States.currentSession.getId() == 0) {
            return;
        }
        // The following is a hack to work around an issue with IDEA, where certain events arrive
        // twice. See https://youtrack.jetbrains.com/issue/IDEA-219133
        final InputEvent input = event.getInputEvent();
        if (input instanceof MouseEvent) {
            if (input.getWhen() != 0 && lastEventTime == input.getWhen()) {
                return;
            }
            lastEventTime = input.getWhen();
        }
        if (action instanceof StepIntoAction || action instanceof ForceStepIntoAction) {
            invokingMethodId = handleEvent("StepInto");
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
