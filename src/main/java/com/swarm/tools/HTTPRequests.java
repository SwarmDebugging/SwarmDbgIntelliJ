package com.swarm.tools;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


//TODO: handle all case and errors
public class HTTPRequests {

    private static final String URL = "http://localhost:8080/graphql";

    public static int createDeveloper(String username) {

        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation developerCreate($username: String!) {\\n  developerCreate(developer: {username:$username}){\\n    id\\n    username\\n  }\\n}\"," +
                        "\"variables\":{\"username\":\"" + username + "\"},\"operationName\":\"developerCreate\"}")
                .asString();


//TODO add creation validation
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
            //productArray.add(new Task(jsonArray.getJSONObject(i).getInt("id"), jsonArray.getJSONObject(i).getString("title")));
        }
        return productArray;
    }

    public static int createProduct(String productTitle, int developerId) {
        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation{\\n  productCreate(product:{name:\\\"" + productTitle +
                        "\\\"}){\\n    id\\n  }\\n}\"}")
                .asString();

        JSONObject jsonProduct = new JSONObject(response.getBody()).getJSONObject("data");

        if(jsonProduct.isNull("productCreate")) {
            return -1;
        }

        int productId = jsonProduct.getJSONObject("productCreate").getInt("id");

        return createTask(productId, "productCreation", true, developerId);
    }

    public static int createTask(int productId, String taskTitle, boolean done, int developerId) {
        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation {\\n  taskCreate(task:{title: \\\"" + taskTitle +
                        "\\\", done:" + done +
                        ", product:{id:" + productId +
                        "}}){\\n    id\\n  }\\n}\"}")
                .asString();

        JSONObject jsonTask = new JSONObject(response.getBody()).getJSONObject("data");

        if(jsonTask.isNull("taskCreate")) {
            return -1;
        }

        int taskId =  jsonTask.getJSONObject("taskCreate").getInt("id");

        response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation {\\n  sessionCreate(session:{developer:{id:" + developerId +
                        "}, task:{id: " + taskId +
                        ", done: true}}){ \\n    id\\n  }\\n}\"}")
                .asString();

        JSONObject jsonSession = new JSONObject(response.getBody()).getJSONObject("data");

        if(jsonSession.isNull("sessionCreate")) {
            return -1;
        }

        return taskId;
    }

    public static int sessionFinish(int sessionId) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        String stringDate = simpleDateFormat.format(date);
        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation($sessionId: Long!, $finished:Date!) {\\n  sessionUpdate(id:$sessionId, finished:$finished){\\n" +
                        "    id\\n  }\\n}\",\"variables\":{\"sessionId\":" + sessionId +
                        ",\"finished\":\"" + stringDate +
                        "\"}}")
                .asString();

        JSONObject jsonSession = new JSONObject(response.getBody()).getJSONObject("data");

        if(jsonSession.isNull("sessionUpdate")){
            return -1;
        }

        return jsonSession.getJSONObject("sessionUpdate").getInt("id");
    }

    public static int sessionStart(int developerId, int taskId) {
        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation($developerId: Long!, $taskId: Long!) {\\n  sessionStart(session:{developer:{id:$developerId}, task:{id: $taskId, done: false}}){ \\n" +
                        "    id\\n  }\\n}\",\"variables\":{\"developerId\":" + developerId +
                        ",\"taskId\":" + taskId +
                        "}}")
                .asString();

        JSONObject jsonSession = new JSONObject(response.getBody()).getJSONObject("data");

        if(jsonSession.isNull("sessionStart")){
            return -1;
        }

        return jsonSession.getJSONObject("sessionStart").getInt("id");

    }

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
