package com.swarm.listeners;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.DebuggerManagerThreadImpl;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.managerThread.DebuggerManagerThread;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerContextListener;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.execution.ExecutorRegistryImpl;
import com.intellij.execution.dashboard.actions.DebugAction;
import com.intellij.execution.dashboard.actions.ExecutorAction;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.DocumentUtil;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.impl.actions.*;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;
import com.swarm.States;
import com.swarm.invokingMethod;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.NotNull;

public class DebugActionListener implements AnActionListener{

    Project project;

    public static invokingMethod invokingMethod = new invokingMethod();

    public DebugActionListener(Project project) {
        this.project = project;
    }

    private void handleStepInto() throws EvaluateException {
        //This works perfectly but only for java code(not tested)
        var debuggerManagerEx = DebuggerManagerEx.getInstanceEx(project);
        var location = debuggerManagerEx.getContext().getFrameProxy().location();

        var position = debuggerManagerEx.getContext().getSourcePosition();

        invokingMethod.setMethod(location.method());
        States.eventType = "StepInto";

        var file = (PsiJavaFile) position.getFile();
        String typeName = file.getName();
        String typeFullName;
        if(!(typeFullName = file.getPackageName()).equals("")){
            typeFullName += ".";
        }
        typeFullName += typeName;

        var typePath = file.getVirtualFile().getPath();

        String sourceCode = file.getText();

        String methodSignature = location.method().signature();
        String methodname = location.method().name();

        int typeId = HTTPRequests.createType(20, typeFullName, typeName, typePath, sourceCode);
        int methodId = HTTPRequests.createMethod(typeId, methodSignature, methodname);
        int eventId = HTTPRequests.createEvent(20, position.getLine(),"StepInto" , methodId);
        invokingMethod.setId(methodId);
    }

    @Override
    public void afterActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext, @NotNull AnActionEvent event) {
        String y = action.getTemplateText();
        if (action instanceof StepIntoAction || action instanceof ForceStepIntoAction) {
            try {
                handleStepInto();
            } catch (EvaluateException e) {
                e.printStackTrace();
            }
        } else if (action instanceof StepOverAction || action instanceof ForceStepOverAction) {
            int i = 1;
        } else if (action instanceof StepOutAction) {
            int i = 2;
        } else if(action instanceof ResumeAction) {
            int i = 3;
        }
    }
}
