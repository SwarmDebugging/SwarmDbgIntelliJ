package com.swarm.listeners;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import com.swarm.States;
import com.swarm.models.Breakpoint;
import com.swarm.models.Event;
import com.swarm.models.Method;
import com.swarm.models.Type;
import com.swarm.tools.HTTPUtils;
import org.jetbrains.annotations.NotNull;


public class BreakpointListener implements XBreakpointListener<XBreakpoint<?>>, DumbAware {

    private Project project;

    public BreakpointListener(Project project) {
        this.project = project;
    }

    public BreakpointListener(){};

    @Override
    public void breakpointAdded(@NotNull XBreakpoint xBreakpoint) {
        if(States.currentSession.getId() == 0) {
            return;
        }
        if(xBreakpoint.getSourcePosition() == null) {
            return;
        }
        int typeId = handleBreakpointEvent("Breakpoint Added", xBreakpoint);
        Breakpoint breakpoint = new Breakpoint();
        Type type = new Type();
        type.setId(typeId);
        breakpoint.setType(type);
        breakpoint.setLineNumber(xBreakpoint.getSourcePosition().getLine());
        breakpoint.create();
    }

    @Override
    public void breakpointRemoved(@NotNull XBreakpoint<?> breakpoint) {
        if(States.currentSession.getId() == 0) {
            return;
        }
        if(breakpoint.getSourcePosition() == null) {
            return;
        }
        handleBreakpointEvent("Breakpoint Removed", breakpoint);
    }

    private int handleBreakpointEvent(String eventKind, @NotNull XBreakpoint breakpoint) {
        PsiJavaFile file = (PsiJavaFile) PsiManager.getInstance(project).findFile(breakpoint.getSourcePosition().getFile());

        String typeName = file.getName();
        String typePath = file.getVirtualFile().getPath();
        String sourceCode = file.getText();
        String typeFullName;
        if (!(typeFullName = file.getPackageName()).equals("")) {
            typeFullName += ".";
        }
        typeFullName += typeName;

        Type type = new Type();
        type.setSession(States.currentSession);
        type.setFullName(typeFullName);
        type.setName(typeName);
        type.setFullPath(typePath);
        type.setSourceCode(sourceCode);
        type.create();

        int lineNumber = breakpoint.getSourcePosition().getLine();

        PsiMethod psiMethod = findMethodByBreakpointAndFile(breakpoint, file);
        if(psiMethod == null) {
            return type.getId();
        }

        String methodName = psiMethod.getName();
        String methodReturnType;
        if(psiMethod.getReturnType() == null){
            methodReturnType = "Constructor";
        } else {
            methodReturnType = psiMethod.getReturnType().getCanonicalText();
        }
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        String methodSignature = encodeSignature(parameters, methodReturnType);

        Method method = new Method();
        method.setType(type);
        method.setSignature(methodSignature);
        method.setName(methodName);
        method.create();

        Event event = new Event();
        event.setKind(eventKind);
        event.setLineNumber(lineNumber);
        event.setSession(States.currentSession);
        event.setMethod(method);
        event.create();

        return type.getId();
    }

    private PsiMethod findMethodByBreakpointAndFile(XBreakpoint breakpoint, PsiJavaFile file) {
        int offset = breakpoint.getSourcePosition().getOffset();
        var element = file.findElementAt(offset);

        if(element == null) {
            return null;
        }

        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if(method == null && element.getNextSibling() instanceof PsiMethod) {
            method = PsiTreeUtil.getNextSiblingOfType(element, PsiMethod.class);
        }

        return method;
    }

    private String encodeSignature(PsiParameter[] parameters, String returnType) {
        String result = "(";
        for (int i = 0; i < parameters.length; i++) {
            result += parameters[i].getType().getCanonicalText();
            if(i != parameters.length-1){
                result += ",";
            }
        }
        result += ")" + returnType;
        return result;
    }
}
