package com.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GitHubAnalyzer 
{
    private static List<Repository> repositories;
    public static void main( String[] args ){
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Github username: ");
        String username = sc.nextLine().trim();
        while(username.isEmpty()){
            System.out.println("Username can't be empty. Try again");
            System.out.println("Enter Github username: ");
            username = sc.nextLine().trim();
        }
        System.out.println("You entered: "+username);
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.github.com/users/"+username+"/repos";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful()){
                System.out.println("Error: API failed with code "+response.code());
                sc.close();
                return ;
            }
            String jsonResponse = response.body().string();
            if(jsonResponse.equals("[]")){
                System.out.println("No public repositories found for user: " + username);
                sc.close();
                return;
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                repositories = mapper.readValue(jsonResponse, new TypeReference<List<Repository>>(){});

                if (repositories.isEmpty()) {
                    System.out.println("No repositories parsed for user: "+username);
                    sc.close();
                    return ;                    
                }

                Collections.sort(repositories, (Repository r1, Repository r2) -> Integer.compare(r2.getStars(),r1.getStars()));
        
                System.out.println("Repositories:");
                for (Repository repo : repositories) {
                    System.out.println(repo);
                }
                
                int totalRepos = repositories.size();
                int totalStars=0;
                for(Repository repo:repositories){
                    totalStars+=repo.getStars();
                }
                double avgStars =totalRepos>0?(double)totalStars/totalRepos:0;
                System.out.println("\nStatistics");
                System.out.println("Total Repositories: " + totalRepos);
                System.out.println("Total Stars: " + totalStars);
                System.out.println("Average Stars: " + avgStars);

                System.out.println("\nLanguages Used:");
                Map<String, Integer> languageCount = new HashMap<>();
                for (Repository repo : repositories) {
                    String lang = repo.getLanguage();
                    languageCount.put(lang, languageCount.getOrDefault(lang, 0) + 1);
                }
                for(Map.Entry<String,Integer> entry: languageCount.entrySet()){
                    String lang = entry.getKey();
                    int count = entry.getValue();
                    double percentage =totalRepos>0?(double)count/totalRepos*100:0;
                    System.out.println(lang + ": " + count + " (" + String.format("%.2f%%", percentage) + ")");

                }

                System.out.println("\nSave data to csv? (y/n):");
                String saveString = sc.nextLine().trim();
                if(saveString.equals("y")){
                    try(FileWriter writer = new FileWriter(username+"_repos.csv")) {
                        writer.write("Name, Description,Language, Stars");
                        for(Repository repo:repositories){
                            String description =repo.getDescription().replace(","," ");
                            writer.write(String.format("%s,%s,%s,%d\n",repo.getName(), description,repo.getLanguage(),repo.getStars()));
                        }
                        System.out.println("Results saved to "+username+"_repos.csv");                        
                    } catch (IOException e) {
                        System.out.println("Error saving csv:"+e.getMessage());
                    }
                }
                System.out.println("Would you like to see the chart statistics? (y/n)");
                String showChart = sc.nextLine().trim().toLowerCase();
                if(showChart.equals("y")){
                    Application.launch(Chart.class,args);
                }

                

            } catch (IOException e) {
                System.out.println("Error: Failed to parse JSON - " + e.getMessage());
            }

        } catch (IOException e) {
            System.out.println("Error: Failed to fetch data-"+e.getMessage());
        }
        sc.close();
    }

    public static class Chart extends Application{
        @Override
        public void start(Stage stage){
            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Repositories");
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Stars");

            BarChart<String,Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Repository stars for users");

            XYChart.Series<String,Number> series = new XYChart.Series<>();
            series.setName("Stars");
            for (Repository repo : repositories) {
                series.getData().add(new XYChart.Data<>(repo.getName(), repo.getStars()));
            }
            barChart.getData().add(series);

            PieChart pieChart = new PieChart();
            pieChart.setTitle("Language Distribution");
            Map<String, Integer> languageCount = new HashMap<>();
            for (Repository repo : repositories) {
                String lang = repo.getLanguage();
                languageCount.put(lang, languageCount.getOrDefault(lang, 0) + 1);
            }
            for(Map.Entry<String,Integer> entry:languageCount.entrySet()){
                pieChart.getData().add(new PieChart.Data(entry.getKey(),entry.getValue()));
            }

            HBox hbox = new HBox(20);
            hbox.setPadding(new Insets(10));
            hbox.getChildren().addAll(barChart,pieChart);

            Scene scene = new Scene(hbox, 1200, 600);
            stage.setScene(scene);
            stage.setTitle("GitHub Repository Analysis");
            stage.show();
            
        }
    }





}
