package com.swarm.tools;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.swarm.States;
import com.swarm.models.*;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;


//TODO: handle all case and errors
public class HTTPUtils {

    private static final String URL = "http://localhost:8080/graphql";

    //TODO: change for alltasks
    public static ArrayList<Product> fetchProductsByDeveloperId(int developerId) {
        HttpResponse<String> response = Unirest.post("http://localhost:8080/graphql")
                .header("content-type", "application/json")
                .body("{\"query\":\"{\\n  tasks(developerId:" + developerId +
                        ") {\\n    product{\\n      id\\n      name\\n    }\\n    id\\n    title\\n done \\n }\\n}\"}")
                .asString();

        JSONObject jsonObject = new JSONObject((response.getBody())).getJSONObject("data");

        if(jsonObject.isNull("tasks")){
            return null;
        }

        ArrayList<Product> productList = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("tasks");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonTask = jsonArray.getJSONObject(i);
            Task newTask = new Task(jsonTask.getInt("id"), jsonTask.getString("title"), jsonTask.getBoolean("done"));
            int index = productIsInArray(productList, jsonTask.getJSONObject("product").getInt("id"));
            if(index != -1) {
                productList.get(index).addTask(newTask);
            } else {
                Product newProduct = new Product(jsonTask.getJSONObject("product").getInt("id"), jsonTask.getJSONObject("product").getString("name"));
                newProduct.addTask(newTask);
                productList.add(newProduct);
            }
        }
        return productList;
    }

    private static int productIsInArray(ArrayList<Product> productArrayList, int productId) {
        for (int i = 0; i < productArrayList.size(); i++) {
            if (productArrayList.get(i).getId() == productId) {
                return i;
            }
        }
        return -1;
    }

    public static int createInvocation(int invokingId, String invokedName, String invokedSignature, int sessionId, Project project){
        var file = (PsiJavaFile) DebuggerManagerEx.getInstanceEx(project).getContext().getSourcePosition().getFile();
        String typeName = file.getName();
        final String[] typeFullName = new String[1]; //Is this the best way???
        ApplicationManager.getApplication().runReadAction(() -> {
            typeFullName[0] = file.getPackageName();
        });
        if(!typeFullName[0].equals("")) {
            typeFullName[0] += ".";
        }
        typeFullName[0] += typeName;

        var typePath = file.getVirtualFile().getPath();

        String sourceCode = file.getText();
        //maybe we could find the type instead of creating a new one?
        Type invokedType = new Type();
        invokedType.setSession(States.currentSession);
        invokedType.setFullName(typeFullName[0]);
        invokedType.setName(typeName);
        invokedType.setFullPath(typePath);
        invokedType.setSourceCode(sourceCode);
        invokedType.create();

        Method invokedMethod = new Method();
        invokedMethod.setType(invokedType);
        invokedMethod.setName(invokedName);
        invokedMethod.setSignature(invokedSignature);
        invokedMethod.create();

        Invocation invocation = new Invocation();
        Session session = new Session();
        session.setId(sessionId);
        invocation.setSession(session);

        invocation.setInvoked(invokedMethod);

        Method invokingMethod = new Method();
        invokingMethod.setId(invokingId);
        invocation.setInvoking(invokingMethod);

        invocation.create();

        return invocation.getId();
    }
}
