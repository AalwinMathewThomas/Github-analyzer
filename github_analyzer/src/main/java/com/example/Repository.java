package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Repository {
    private String name;
    private String description;
    private String language;
    private int stars;


    public Repository(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("language") String language,
        @JsonProperty("stargazers_count") int stars){
        this.name = name;
        this.description=description !=null?description:"None";
        this.language=language!=null?language:"None";
        this.stars=stars;
    }


    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }
     public String getLanguage() {
        return language;
    }
    public int getStars(){
        return stars;
    }

    @Override
    public String toString(){
        return " Repo : "+name+", Language: " + language + ", Description : "+description+", Stars : "+stars;
    }
}
