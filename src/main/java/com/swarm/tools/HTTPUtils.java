package com.swarm.tools;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.swarm.States;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.models.Type;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;


//TODO: handle all case and errors
public class HTTPUtils {

    private static final String URL = "http://localhost:8080/graphql";

    public static int taskDone(int taskId) {
        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation {\\n  taskDone(taskId:" + taskId +
                        "){\\n    id\\n  }\\n}\"}")
                .asString();

        JSONObject jsonTask = new JSONObject((response.getBody())).getJSONObject("data");

        if(jsonTask.isNull("taskDone")){
            return -1;
        }

        return taskId;
    }


    public static ArrayList<Product> productsByDeveloperId(int developerId) {
        HttpResponse<String> response = Unirest.post("http://localhost:8080/graphql")
                .header("content-type", "application/json")
                .body("{\"query\":\"{\\n  tasks(developerId:" + developerId +
                        ") {\\n    product{\\n      id\\n      name\\n    }\\n    id\\n    title\\n done \\n }\\n}\"}")
                .asString();

        JSONObject jsonObject = new JSONObject((response.getBody())).getJSONObject("data");

        if(jsonObject.isNull("tasks")){
            return null;
        }

        ArrayList<Product> productArray = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("tasks");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonTask = jsonArray.getJSONObject(i);
            Task newTask = new Task(jsonTask.getInt("id"), jsonTask.getString("title"), jsonTask.getBoolean("done"));
            int index = productIsInArray(productArray, jsonTask.getJSONObject("product").getInt("id"));
            if(index != -1) {
                productArray.get(index).addTask(newTask);
            } else {
                Product newProduct = new Product(jsonTask.getJSONObject("product").getInt("id"), jsonTask.getJSONObject("product").getString("name"));
                newProduct.addTask(newTask);
                productArray.add(newProduct);
            }
        }
        return productArray;
    }

    private static int productIsInArray(ArrayList<Product> productArrayList, int productId) {
        for (int i = 0; i < productArrayList.size(); i++) {
            if (productArrayList.get(i).getId() == productId) {
                return i;
            }
        }
        return -1;
    }

    public static int createEvent(int sessionId, int eventLineNumber, String eventKind, int methodId) {
        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation eventCreate($sessionId: Long!, $eventLineNumber: Int!, $eventKind: String!, $methodId: Long!){\\n  eventCreate(event:{session:{id:$sessionId}," +
                        "lineNumber:$eventLineNumber, kind:$eventKind, method:{id:$methodId}}){\\n    id\\n  }\\n}\\n\",\"variables\":{\"sessionId\":\"" + sessionId +
                        "\",\"eventLineNumber\":" + eventLineNumber +
                        ",\"eventKind\":\"" + eventKind +
                        "\",\"methodId\":\"" + methodId +
                        "\"},\"operationName\":\"eventCreate\"}")
                .asString();

        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getJSONObject("eventCreate").getInt("id");
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

        int invokedId = createMethod(invokedType.getId(),invokedSignature, invokedName);

        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation invocationCreate($sessionId: Long!, $invokingId: Long!, $invokedId: Long!){\\n  " +
                        "invocationCreate(invocation:{session:{id:$sessionId}, invoking:{id:$invokingId},invoked:{id:$invokedId}," +
                        "virtual:false}){\\nid\\n  }\\n}\\n\",\"variables\":{\"sessionId\":\"" + sessionId +
                        "\",\"invokingId\":\"" + invokingId +
                        "\",\"invokedId\":\"" + invokedId +
                        "\"},\"operationName\":\"invocationCreate\"}")
                .asString();

        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getJSONObject("invocationCreate").getInt("id");
    }

    public static int createMethod(int typeId, String signature, String name) {

        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation methodCreate($typeId: Long!, $signature:String!, $name:String!){\\n  methodCreate(method:{type:{id:$typeId}, signature:$signature, name:$name}){\\n" +
                        "    id\\n  }\\n}\\n\",\"variables\":{\"typeId\":\"" + typeId +
                        "\",\"signature\":\"" + signature +
                        "\",\"name\":\"" + name +
                        "\"},\"operationName\":\"methodCreate\"}")
                .asString();

        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getJSONObject("methodCreate").getInt("id");
    }
}
