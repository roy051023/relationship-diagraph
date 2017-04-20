/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// Step1: Hero and Couple Novel relationship
// Step2: Average degree, Average clustering coefficient, Diameter, Degree Distribution.
package assignment2;

import java.io.*;
import java.util.*;
import org.json.*;

/**
 *
 * @author User
 */
public class compareRelation {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)  throws IOException{
        // Read hero's and couple's name file
        Map<String,Set<String>> heroNamesData;
        heroNamesData = readNameFile("HeroNames.txt");
        Map<String,Set<String>> coupleNamesData;
        coupleNamesData = readNameFile("CoupleNames.txt");
        
        // Compare name's and novel's relation
        ArrayList<Set<String>> heroRelationship;
        heroRelationship = compareRelation("HeroNovel.txt", heroNamesData);
        ArrayList<Set<String>> coupleRelationship;
        coupleRelationship = compareRelation("CoupleNovel.txt", coupleNamesData);

        // Calculate all(hero and couple) weights
        Map<Set<String>,Integer> relationshipWeight;
        relationshipWeight = calculateWeight(heroRelationship, coupleRelationship);
        
        // Create node size hashmap
        Map<String,Integer> sizeHash;
        sizeHash = createNodeSizeHash(heroNamesData, relationshipWeight);
        
        // Write to JSON
        writeToJSON(relationshipWeight, sizeHash);
        
        /*--------------------------------------------------------------------------------------*/
        // Average degree(sizeHash has all nodes, relationshipWeight has single edge)
        double AverDegree = 0;
        AverDegree = ((double)relationshipWeight.size() * 2) / sizeHash.size();
        System.out.println("Average degree : " + AverDegree);
        
        // Average clustering coefficient
        Map<String,Set<String>> nameHash;
        nameHash = createHash(sizeHash, relationshipWeight); // Create name node hash map
        ClusterCoefficient(nameHash);
        
        // Diameter
        Diameter(nameHash);
        
        // Degree Distribution
        Distribution(sizeHash);
    }
    
    public static Map<String,Set<String>> readNameFile(String namesTxt){
        // Create hero names hashmap
        Map<String,Set<String>> namesData = new HashMap<>();
        try{
            FileReader heroFile = new FileReader(namesTxt);
            BufferedReader heroBr = new BufferedReader(heroFile);
            heroBr.readLine();
            while (heroBr.ready()) {
                    String heroNameString  = heroBr.readLine(); 
                    String[] AfterSplit = heroNameString.split("	");
                    Set<String> heroNickname = new HashSet<>();
                    // Ex: a aa aaa, use a for key name
                    String heroName = AfterSplit[0];
                    for(int i = 0;i < AfterSplit.length;i++){
                        heroNickname.add(AfterSplit[i]);
                    }
                    // In namesData is a=[a aa aaa]
                    namesData.put(heroName, heroNickname);
                }
        }catch (Exception e){
            System.out.println("No name file!");
        }
        return namesData;
    }
    
    public static ArrayList<Set<String>> compareRelation(String novelTxt, Map<String,Set<String>> namesData){
        ArrayList<Set<String>> relationship = new ArrayList<Set<String>>();
        // Create novel string and compare relation set
        try{
            FileReader novelFile = new FileReader(novelTxt);
            BufferedReader novelBr = new BufferedReader(novelFile);
            while (novelBr.ready()) {
                    String sentence  = novelBr.readLine(); 
                    // Compare relation set
                    Set<String> relation = new HashSet<>();
                    for(String key : namesData.keySet()){
                        for(String value : namesData.get(key)){
                            // Whether value(every kind of name, like nickname) in the sentense, save origin
                            if(sentence.indexOf(value) != -1){
                                relation.add(key);
                                break;
                            }
                        }
                    }
                    if(!relation.isEmpty() && relation.size() > 1){
                        // If more than 2 componenet
                        if(relation.size() > 2){
                            Object[] temp = relation.toArray();
                            // Bubble sort for add relation
                            for(int value1 = 0;value1 < temp.length;value1++){
                                for(int value2 = value1;value2 < temp.length;value2++){
                                    Set<String> tempRelation = new HashSet<>();
                                    tempRelation.add(temp[value1].toString());
                                    tempRelation.add(temp[value2].toString());
                                    if(tempRelation.size() > 1)
                                        relationship.add(tempRelation);
                                }
                            }
                        }
                        else
                            relationship.add(relation);
                    }
                }
        }catch (Exception e){
            System.out.println("No novel file!");
        }
        return relationship;
    }
    
    public static Map<Set<String>,Integer> calculateWeight(ArrayList<Set<String>> heroRelationship, ArrayList<Set<String>> coupleRelationship){
        Map<Set<String>,Integer> relationshipWeight = new HashMap<>();
        // Add heroRelationship
        for(int i = 0;i < heroRelationship.size();i++){
            if(!relationshipWeight.containsKey(heroRelationship.get(i)))
                relationshipWeight.put(heroRelationship.get(i), 1);
            else{
                int count = relationshipWeight.get(heroRelationship.get(i));
                relationshipWeight.replace(heroRelationship.get(i), count+1);
            }
        }
        // Add coupleRelationship
        for(int i = 0;i < coupleRelationship.size();i++){
            if(!relationshipWeight.containsKey(coupleRelationship.get(i)))
                relationshipWeight.put(coupleRelationship.get(i), 1);
            else{
                int count = relationshipWeight.get(coupleRelationship.get(i));
                relationshipWeight.replace(coupleRelationship.get(i), count+1);
            }
        }
        return relationshipWeight;
    }
    
    public static Map<String,Integer> createNodeSizeHash(Map<String,Set<String>> namesData, Map<Set<String>,Integer> relationshipWeight){
        Map<String,Integer> sizeHash = new HashMap<>();
        // Whether certain name in relationshipWeight
        for(Set<String> relationship : relationshipWeight.keySet()){
            for(String key : namesData.keySet()){
                if(relationship.contains(key) && !sizeHash.containsKey(key)){
                    sizeHash.put(key, 1);
                }else if(relationship.contains(key) && sizeHash.containsKey(key)){
                    int count = sizeHash.get(key);
                    sizeHash.replace(key, count+1);
                }
            }
        }
        // Ensure Node's size
        /*
        try{
            File file = new File("Size.txt");
            Writer write = new FileWriter(file);  
            write.write(sizeHash.entrySet().toString());  
            write.flush();  
            write.close();
        }catch (Exception e){
            System.out.println("Write file error!");
        }
        */
        return sizeHash;
    }
    
    public static void writeToJSON(Map<Set<String>,Integer> relationshipWeight, Map<String,Integer> sizeHash){
        JSONObject tempJsonObj;
        JSONObject allJsonObj = new JSONObject();
        
        // Nodes
        JSONObject nodeJsonObj = new JSONObject();
        for(String nameKey : sizeHash.keySet()){
            tempJsonObj = new JSONObject();
            tempJsonObj.put("shape", "dot");
            tempJsonObj.put("label", nameKey);
            tempJsonObj.put("size", sizeHash.get(nameKey));
            nodeJsonObj.put(nameKey, tempJsonObj);
        }
        allJsonObj.put("nodes", nodeJsonObj);
        
        // Edges
        JSONObject edgesJsonObj = new JSONObject();
        JSONObject weightJsonObj;
        for(String nameKey : sizeHash.keySet()){
            tempJsonObj = new JSONObject();
            Set<Set<String>> bufferSet = new HashSet<>();
            for(Set<String> namesSet : relationshipWeight.keySet()){
                if(namesSet.contains(nameKey)){
                    weightJsonObj = new JSONObject();
                    weightJsonObj.put("weight", relationshipWeight.get(namesSet));
                    Object[] set = namesSet.toArray();
                    if( nameKey.equals(set[0].toString()))
                        tempJsonObj.put(set[1].toString(), weightJsonObj);
                    else
                        tempJsonObj.put(set[0].toString(), weightJsonObj);
                    bufferSet.add(namesSet);
                }
            }
            /*
            // For single line
            for(Set<String> buffer : bufferSet){
                relationshipWeight.remove(buffer);
            }
            */
            if(tempJsonObj.length() != 0)
                edgesJsonObj.put(nameKey,tempJsonObj);
        }
        allJsonObj.put("edges", edgesJsonObj);
        
        // Write json file
        try{
            File file = new File("DoubleWithSize.txt");
            //File file = new File("SingleWithSize.txt");
            //File file = new File("Double.txt");
            Writer write = new FileWriter(file);  
            write.write(allJsonObj.toString());  
            write.flush();  
            write.close();
        }catch (Exception e){
            System.out.println("Write file error!");
        }
    }

    public static  Map<String,Set<String>> createHash(Map<String,Integer> sizeHash, Map<Set<String>,Integer> relationshipWeight){
        Map<String,Set<String>> nameHash = new HashMap();
        for(Set<String> relation : relationshipWeight.keySet()){
            for(String node : sizeHash.keySet()){
                if(relation.contains(node)){
                    Object[] temp = relation.toArray();
                    String value = "";
                    Set<String> NodeIdList = new HashSet<>();
                    if(temp[0].toString() != node)
                        value = temp[0].toString();
                    else
                        value = temp[1].toString();
                    if(nameHash.containsKey(node)){
                        NodeIdList = nameHash.get(node);   
                        NodeIdList.add(value);
                        nameHash.put(node, NodeIdList);
                    }else{
                        NodeIdList.add(value);
                        nameHash.put(node, NodeIdList);
                    }
                }
            }
        }
        // For check hash
        /*
        try{
            File file = new File("hash.txt");
            //File file = new File("Double.txt");
            Writer write = new FileWriter(file); 
            for(String key : nameHash.keySet()){
                write.write(key+":"+nameHash.get(key).toString()+"\n"); 
            }
            write.flush();  
            write.close();
        }catch (Exception e){
            System.out.println("Write file error!");
        }
        */
        return nameHash;
    }
    
    public static void ClusterCoefficient(Map<String,Set<String>> nameHash){
        double coefficientSum = 0;
        for(Object ikey : nameHash.keySet()){
            Set<String> ikeyvalue = new HashSet<>();
            ikeyvalue = nameHash.get(ikey);
            int maxConnectNumber = 0;
            if(ikeyvalue.size() != 1){
                for(String oneikeyvalue : ikeyvalue){
                    Set<String> jkeyvalue = new HashSet<>();
                    jkeyvalue = nameHash.get(oneikeyvalue.toString());
                    for(String i : ikeyvalue){
                        if(jkeyvalue.contains(i)){
                            maxConnectNumber += 1;
                        }                
                    }
                }
            }
            double calculate = 0;
            if(maxConnectNumber != 0){
                //System.out.println(ikey+":"+maxConnectNumber);
                maxConnectNumber=maxConnectNumber / 2;
                calculate = (double)maxConnectNumber / (double)((ikeyvalue.size() * (ikeyvalue.size() - 1)) / 2);
            }
            coefficientSum += calculate;            
        }
        coefficientSum = coefficientSum / nameHash.size();
        System.out.println("Average clustering coefficient : " + coefficientSum);
    }
    
    public static void Diameter(Map<String,Set<String>> nameHash){
        int diameter = 0;
        for(String startName : nameHash.keySet()){
            // Have new distance, because I suppose no single nodes
            boolean haveNew = true;
            Set<String> selectedName = new HashSet();
            selectedName.add(startName);
            Set<String> selectedValue = new HashSet();
            // The first time search distance
            int distance = 1;
            // Try addAll V.S. one-by-one speed
            // A ABCD, ABCD, ABCDEF...
            selectedValue.add(startName);
            for(String firstValue : nameHash.get(startName)){
                selectedValue.add(firstValue);
            }
            // run startName(from second distance start) to the farest node
            while(haveNew){
                haveNew = false; // default
                // Put selectedValue to selectedName, A ABCD become ABCD ABCD
                for(String value : selectedValue){
                    selectedName.add(value);
                }
                // Select selectedName set whether have new node can add to selectedValue
                for(String value : selectedName){
                    for(String select : nameHash.get(value)){
                        if(!selectedValue.contains(select) & !haveNew){
                            selectedValue.add(select);
                            distance += 1;
                            haveNew = true;
                        }else{
                            selectedValue.add(select);
                        }
                    }
                }
            }
            if(distance > diameter)
                diameter = distance;
        }
        System.out.println("Diameter : " + diameter);
    }
    
    public static void Distribution(Map<String,Integer> sizeHash){
        List<Integer> arrangement = new ArrayList();
        for(String key : sizeHash.keySet()){
            arrangement.add(sizeHash.get(key));
        }
        Collections.sort(arrangement);
        // System.out.println(arrangement);
        int counter = 1;
        
        try{
            File file = new File("Distribution.txt");
            Writer write = new FileWriter(file);  
            for(int i = 1;i < arrangement.size();i++){
                if(arrangement.get(i) == arrangement.get(i-1)){
                    counter += 1;
                }else{
                    write.write(arrangement.get(i-1)+"\t"+counter+"\n");  
                    counter = 1;
                }
            }
            write.flush();  
            write.close();
        }catch(Exception e){
            System.out.println("Can't write file");
        }
    }
}
