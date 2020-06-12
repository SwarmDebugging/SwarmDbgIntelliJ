package com.swarm.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import com.swarm.States;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.NotNull;


public class breakpointListener implements XBreakpointListener<XBreakpoint<?>> {

    private final Project project;

    public breakpointListener(Project project) {
        this.project = project;
    }

    @Override
    public void breakpointAdded(@NotNull XBreakpoint breakpoint) {
        if(States.currentSessionId == -1) { //There needs to be a session for the types
            return;
        }
        if(breakpoint.getSourcePosition() == null) {
            return;
        }
        int typeId = handleBreakpointEvent("Breakpoint Added", breakpoint);
        HTTPRequests.createBreakpoint(breakpoint.getSourcePosition().getLine(), typeId);
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
        int typeId = HTTPRequests.createType(States.currentSessionId, typeFullName, typeName, typePath, sourceCode);

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

        int methodId = HTTPRequests.createMethod(typeId, methodSignature, methodName);
        HTTPRequests.createEvent(States.currentSessionId, lineNumber, eventKind, methodId);
        return typeId;
    }

    private PsiMethod findMethodByBreakpointAndFile(XBreakpoint breakpoint, PsiJavaFile file) {
        int offset = breakpoint.getSourcePosition().getOffset();
        var element = file.findElementAt(offset);

        if(element == null) {
            //that means that there's no element where the breakpoint is(unlikely)
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
