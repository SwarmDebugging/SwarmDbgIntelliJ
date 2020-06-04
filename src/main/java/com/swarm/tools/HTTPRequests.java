package com.swarm.tools;

import com.google.gson.JsonArray;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.swarm.States;
import com.swarm.models.Product;
import com.swarm.models.Task;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


//TODO: handle all case and errors
public class HTTPRequests {

    private static final String URL = "http://localhost:8080/graphql";

    public static int createDeveloper(String username) {

        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation developerCreate($username: String!) {\\n  developerCreate(developer: {username:$username}){\\n    id\\n    username\\n  }\\n}\"," +
                        "\"variables\":{\"username\":\"" + username + "\"},\"operationName\":\"developerCreate\"}")
                .asString();



        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getJSONObject("developerCreate").getInt("id");
    }

    public static ArrayList<Task> tasksByProductId(int productId) {
        HttpResponse<String> response = Unirest.post("http://localhost:8080/graphql")
                .header("content-type", "application/json")
                .body("{\"query\":\"{\\n  tasks(productId:" + productId +
                        ") {\\n    id\\n    title\\n  }\\n}\"}")
                .asString();

        JSONObject jsonObject = new JSONObject((response.getBody())).getJSONObject("data");

        if(jsonObject.isNull("tasks")){
            return null;
        }

        ArrayList<Task> productArray = new ArrayList<>();

        JSONArray jsonArray = jsonObject.getJSONArray("tasks");
        for (int i = 0; i < jsonArray.length(); i++) {
            productArray.add(new Task(jsonArray.getJSONObject(i).getInt("id"), jsonArray.getJSONObject(i).getString("title")));
        }
        return productArray;
    }

    public static ArrayList<Product> productsByDeveloperId(int developerId) {
        HttpResponse<String> response = Unirest.post("http://localhost:8080/graphql")
                .header("content-type", "application/json")
                .body("{\"query\":\"{\\n  products(developerId:" + developerId +
                        "){\\n    id\\n name \\n  }\\n}\"}")
                .asString();

        JSONObject jsonObject = new JSONObject((response.getBody())).getJSONObject("data");

        if(jsonObject.isNull("products")){
            return null;
        }

        ArrayList<Product> productArray = new ArrayList<>();

        JSONArray jsonArray = jsonObject.getJSONArray("products");
        for (int i = 0; i < jsonArray.length(); i++) {
            productArray.add(new Product(jsonArray.getJSONObject(i).getInt("id"), jsonArray.getJSONObject(i).getString("name")));
        }
        return productArray;
    }

    public static int login(String username) {
        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"{\\n  developer(username:\\\"" + username +
                        "\\\"){\\n    id\\n  }\\n}\"}")
                .asString();

        JSONObject jsonObject = new JSONObject(response.getBody());

        int developerId = -1;
        if(!(jsonObject.getJSONObject("data").isNull("developer"))) {
            developerId = jsonObject.getJSONObject("data").getJSONObject("developer").getInt("id");
        }

        return developerId;
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
        int invokedTypeId = createType(States.currentSessionId, typeFullName[0], typeName, typePath, sourceCode);

        int invokedId = createMethod(invokedTypeId,invokedSignature, invokedName);

        //TODO: must save methods before create the invocation

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

    public static int createType(int sessionId, String fullName, String name, String fullPath, String sourceCode) {
        String source = sourceCode.replace("\n", "\\\\n");
        source = source.replace("\"", "\\\"");
        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation typeCreate($sessionId: Long!,$name: String!, $fullPath: String!, $fullName: String!, $source:String){\\n  " +
                        "typeCreate(typeWrapper:{type:{session:{id:$sessionId},name:$name,fullPath:$fullPath,fullName:$fullName},source:$source}){\\n    " +
                        "id\\n  }\\n}\\n\",\"variables\":{\"sessionId\":\"" + sessionId +
                        "\",\"name\":\"" + name +
                        "\",\"fullPath\":\"" + fullPath +
                        "\",\"fullName\":\"" + fullName +
                        "\",\"source\":\"" + source +
                        "\"},\"operationName\":\"typeCreate\"}")
                .asString();

        JSONObject jsonObject = new JSONObject(response.getBody());

        return jsonObject.getJSONObject("data").getJSONObject("typeCreate").getInt("id");
    }

    public static int createBreakpoint(int lineNumber, int typeId) {
        HttpResponse<String> response = Unirest.post("http://localhost:8080/graphql")
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation breakpointCreate($typeId: Long!, $lineNumber: Int!) {\\n  breakpointCreate(breakpoint:{type:{id:$typeId}, lineNumber:$lineNumber}) {\\n " +
                        "   id\\n  }\\n}\",\"variables\":{\"typeId\":\"" + typeId +
                        "\",\"lineNumber\":" + lineNumber +
                        "},\"operationName\":\"breakpointCreate\"}")
                .asString();

        JSONObject jsonObject = new JSONObject((response.getBody()));
        return jsonObject.getJSONObject("data").getJSONObject("breakpointCreate").getInt("id");
    }
}
