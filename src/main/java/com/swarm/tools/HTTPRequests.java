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

    public static String createMethod(){


        return "yo";
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
