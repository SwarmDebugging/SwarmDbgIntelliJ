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
import com.swarm.tools.HTTPUtils;
import org.jetbrains.annotations.NotNull;


public class BreakpointListener implements XBreakpointListener<XBreakpoint<?>>, DumbAware {

    private Project project;

    public BreakpointListener(Project project) {
        this.project = project;
    }

    public BreakpointListener(){};

    @Override
    public void breakpointAdded(@NotNull XBreakpoint breakpoint) {
        if(States.currentSessionId == -1) {
            return;
        }
        if(breakpoint.getSourcePosition() == null) {
            return;
        }
        int typeId = handleBreakpointEvent("Breakpoint Added", breakpoint);
        HTTPUtils.createBreakpoint(breakpoint.getSourcePosition().getLine(), typeId);
    }

    @Override
    public void breakpointRemoved(@NotNull XBreakpoint<?> breakpoint) {
        if(States.currentSessionId == -1) {
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

        //verification is made in the server for doubles
        int typeId = HTTPUtils.createType(States.currentSessionId, typeFullName, typeName, typePath, sourceCode);

        int lineNumber = breakpoint.getSourcePosition().getLine();

        PsiMethod method = findMethodByBreakpointAndFile(breakpoint, file);
        if(method == null) {
            return typeId;
        }

        String methodName = method.getName();
        String methodReturnType;
        if(method.getReturnType() == null){
            methodReturnType = "Constructor";
        } else {
            methodReturnType = method.getReturnType().getCanonicalText();
        }
        PsiParameter[] parameters = method.getParameterList().getParameters();
        String methodSignature = encodeSignature(parameters, methodReturnType);

        int methodId = HTTPUtils.createMethod(typeId, methodSignature, methodName);
        HTTPUtils.createEvent(States.currentSessionId, lineNumber, eventKind, methodId);
        return typeId;
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

    private void setProject(Project project) {
        this.project = project;
    }
}
