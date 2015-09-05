package com.example.wojciech.recipemaster.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wojciech on 2015-09-04.
 */
public class Recipe implements Parcelable{
    public String title;
    public String description;
    public List<String> ingredients;
    public List<String> preparing;
    public List<String> imgs;


    public Recipe() {
        super();
    }
    public Recipe(Parcel in) {
        super();
        this.title=in.readString();
        this.description=in.readString();
        in.readStringList(this.ingredients);
        in.readStringList(this.preparing);
        in.readStringList(this.imgs);
    }
    public Recipe(String title,String description,ArrayList<String> ingredients,ArrayList<String> preparing,ArrayList<String> imgs) {
        super();
        this.title=title;
        this.description=description;
        this.ingredients=ingredients;
        this.preparing=preparing;
        this.imgs=imgs;

    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeStringList(ingredients);
        dest.writeStringList(preparing);
        dest.writeStringList(imgs);

    }
    public static final Parcelable.Creator<Recipe> CREATOR = new Parcelable.Creator<Recipe>() {

        @Override
        public Recipe createFromParcel(Parcel source) {
            return new Recipe(source);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };

}
