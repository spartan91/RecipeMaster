package com.example.wojciech.recipemaster.utils;

import com.example.wojciech.recipemaster.model.Recipe;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Wojciech on 2015-09-04.
 */
public class DataParserUtil {

    public static Recipe parseToRecipe(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream, "UTF-8"), 8);
        Gson gson = new Gson();
        Recipe recipe = gson.fromJson(reader, Recipe.class);
        stream.close();
        return recipe;
    }
    public static Recipe parseToRecipe(String json) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(json, Recipe.class);
    }
}
