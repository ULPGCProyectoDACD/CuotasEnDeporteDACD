package org.ulpgc.dacd;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class MatchNormalize {

    private static Gson gson = new Gson();

    public static List<Match> parseMatches(String rawJson) {
        JsonObject jsonObject = JsonParser.parseString(rawJson).getAsJsonObject();
        JsonArray matchesArray = jsonObject.getAsJsonArray("response");
        Type listType = new TypeToken<List<Match>>(){}.getType();
        return gson.fromJson(matchesArray, listType);
    }
}