package com.swarm.tools;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;

public class HTTPRequests {

    private static final String URL = "http://localhost:8080/graphql";

    public static String createDeveloper(String username) {

        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body("{\"query\":\"mutation developerCreate($username: String!) {\\n  developerCreate(developer: {username:$username}){\\n    id\\n    username\\n  }\\n}\"," +
                        "\"variables\":{\"username\":\"" + username + "\"},\"operationName\":\"developerCreate\"}")
                .asString();



        JSONObject jsonObject = new JSONObject(response.getBody());
        int id = jsonObject.getJSONObject("data").getJSONObject("developerCreate").getInt("id");

        return "id: " + id;
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
        int id = jsonObject.getJSONObject("data").getJSONObject("eventCreate").getInt("id");

        return id;
    }

    //just a test
    public static int createInvocation(String invokingFilePath, String invokedFilePath, int invokingId, String invokedName, int invokingLine, int invokedLine, int sessionId){

        int invokedTypeId = 56;

        int invokedId = createMethod(invokedTypeId,"signature", invokedName);

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
        int invocationId = jsonObject.getJSONObject("data").getJSONObject("invocationCreate").getInt("id");


        return invocationId;
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
        int methodId = jsonObject.getJSONObject("data").getJSONObject("methodCreate").getInt("id");

        return methodId;
    }

    public static String createType(int sessionId, String fullName, String name, String fullPath, String sourceCode) {
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
        int id = jsonObject.getJSONObject("data").getJSONObject("typeCreate").getInt("id");

        return "id: " + id;
    }
}
