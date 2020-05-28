package com.swarm.listeners;

import com.intellij.lang.jvm.JvmParameter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
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
        if(breakpoint.getSourcePosition() == null) {
            return;
        }
        int typeId = handleBreakpointEvent("Breakpoint Added", breakpoint);
        HTTPRequests.createBreakpoint(breakpoint.getSourcePosition().getLine(), typeId);
    }

    @Override
    public void breakpointRemoved(@NotNull XBreakpoint<?> breakpoint) {
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

        int typeId = HTTPRequests.createType(States.currentSessionId, typeFullName, typeName, typePath, sourceCode);

        int lineNumber = breakpoint.getSourcePosition().getLine();
        int offset = breakpoint.getSourcePosition().getOffset();
        var element = file.findElementAt(offset);

        if(element == null) {
            //that means that there's no element where the breakpoint is(unlikely)
            return typeId;
        }

        var method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if(method == null && element.getNextSibling() instanceof PsiMethod) {
            method = PsiTreeUtil.getNextSiblingOfType(element, PsiMethod.class);
        } else if(method == null){
            //the breakpoint is not a method(could be on a class or a field)
            return typeId;
        }

        String methodName = method.getName();
        String methodReturnType;

        if(method.getReturnType() == null){
            methodReturnType = "Constructor";
        } else {
            methodReturnType = method.getReturnType().getCanonicalText();
        }

        //var parameters = method.getParameters();

        //TODO: get right signature
        //MethodSignature methodSignature = method.getSignature(PsiSubstitutor.EMPTY);

        int methodId = HTTPRequests.createMethod(typeId, methodReturnType, methodName);
        HTTPRequests.createEvent(States.currentSessionId, lineNumber, eventKind, methodId);
        return typeId;
    }

    private String encodeSignature(JvmParameter[] parameters, String returnType) {
        //TODO Is it better to encode signature or to decode in the stepping events???
        return "Encoded signature";
    }
}
