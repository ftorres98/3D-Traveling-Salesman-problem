/*
Fernando Torres
 */

import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.Scanner;
import java.util.Set;
import java.util.Random;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.Math;
import java.io.FileWriter;
import java.io.IOException;

public class homework {
   /*
    * Global Vaariables
    */
   public static int INITIAL_SIZE = 2000;
   public static int NUM_MATING = 1000;
   public static Map<Double, ArrayList<Vector<Integer> > > paths = new TreeMap<>();

   public static void main(String[] args) {
      try{
         ArrayList<Vector<Integer> > list = readIN();

         ArrayList<ArrayList<Vector<Integer> > > init = CreateInitialPopulation(INITIAL_SIZE, list);

         Map<Double,Integer> rank = generateRank(init);

         ArrayList<ArrayList<Vector<Integer> > > matingPool = CreateMatingPool(init, rank);

         ArrayList<Vector<Integer> > optimalPath = findPath(matingPool);

         printPath(optimalPath);
         
      }
      catch (FileNotFoundException e){
         System.out.println("File does not exist.");
         System.exit(0);
      }

   }

   /*
    * Reads in the cordinates of the cities from the file and stores them in a Vector and stores that vector in an ArrayList
    */
   public  static ArrayList<Vector<Integer> > readIN() throws FileNotFoundException{
      ArrayList<Vector<Integer> > citiesList = new ArrayList<>();

      File inFile = new File("input.txt");
      try (Scanner in = new Scanner(inFile)) {
         in.nextLine();

         while(in.hasNextLine()){
            Scanner line = new Scanner(in.nextLine());
            Vector<Integer> point = new Vector<>();
            while(line.hasNextInt()){
               point.add(line.nextInt());
            }
            citiesList.add(point);
         }
      }

      return citiesList;
   }

   /*
    * Creates an initial population by randomly choosing city coordinates to create a path
    */
   public static ArrayList<ArrayList<Vector<Integer> > > CreateInitialPopulation(int size, ArrayList<Vector<Integer> > cities){
      ArrayList<ArrayList<Vector<Integer> > > initial_population = new ArrayList<>();
      while(initial_population.size()<size){
         ArrayList<Vector<Integer> > temp = new ArrayList<>(cities);
         ArrayList<Vector<Integer> > path = new ArrayList<>();
         Random rand = new Random();
         while(!temp.isEmpty()){
            int i = rand.nextInt(temp.size());
            path.add(temp.get(i));
            temp.remove(i);
         }
         path.add(path.get(0));
         initial_population.add(path);
      }

      return initial_population;
   }

   /*
    * Creates a map ranking the distances in incrasing order so the lowest distance is the first entry
    */
   public static Map<Double,Integer> generateRank(ArrayList<ArrayList<Vector<Integer> > > population){
      Map<Double,Integer> RankList = new TreeMap<>();

      for(int i = 0; i < population.size(); i++){
         double dist = 0;
         for(int j = 1; j < population.get(i).size(); j++){
            Vector<Integer> v1 = population.get(i).get(j-1);
            Vector<Integer> v2 = population.get(i).get(j);

            dist = dist + Math.sqrt(Math.pow((v1.get(0)-v2.get(0)),2) + 
                                    Math.pow((v1.get(1)-v2.get(1)),2) +
                                    Math.pow((v1.get(2)-v2.get(2)),2));
         }
         RankList.put(dist, i);
      }

      return RankList;
   }

   /*
    * Creates an arrayList of half the size of the initial population using the Roulette wheel selection
    */
   public static ArrayList<ArrayList<Vector<Integer> > > CreateMatingPool(ArrayList<ArrayList<Vector<Integer> > > population, Map<Double,Integer> RankList){
      ArrayList<ArrayList<Vector<Integer> > > matingPool = new ArrayList<>();
      Map<Integer, Double> probMap = new HashMap<>();
      Set<Integer> chosen = new HashSet<>();

      double total = 0;

      for(Map.Entry<Double, Integer> entry : RankList.entrySet()){
         total = total + entry.getKey();
      }

      for(Map.Entry<Double, Integer> entry : RankList.entrySet()){
         double prob = 1 - (entry.getKey()/total);
         probMap.put(entry.getValue(), prob);
      } 

      int num = 0;
      while(num<(population.size()/2)){
         Random rand = new Random();
         int rand_num = rand.nextInt((int)total) + 1;

         double S = 1 - ((double)rand_num/total);
         double partial_sum = 0;

         for(Map.Entry<Double, Integer> entry : RankList.entrySet()){
            if(!chosen.contains(entry.getValue())){
               partial_sum = partial_sum + probMap.get(entry.getValue());
               if(partial_sum>=S){
                  chosen.add(entry.getValue());
                  matingPool.add(population.get(entry.getValue()));
                  break;
               }
            }
         }

         num++;
      }
      
      return matingPool;
   }

   /*
    * Randomly generates to distinct parents from the population
    */
   public static String generateParents(ArrayList<ArrayList<Vector<Integer> > > population){
      String indices = "";
      Random rand = new Random();

      int i = rand.nextInt(population.size());

      int j = rand.nextInt(population.size());

      while(i == j ){
         j = rand.nextInt(population.size());
      }

      indices = i + " " + j;

      return indices;
   }

   /*
    * Randomly picks two cities from the path and swaps them to cause a mutation
    */
   public static ArrayList<Vector<Integer> > mutate(ArrayList<Vector<Integer> > parent){
      Random rand = new Random();

      ArrayList<Vector<Integer> > mutatedP = new ArrayList<>(parent);
      mutatedP.remove(mutatedP.size()-1);
      int i = rand.nextInt(mutatedP.size());

      int j = rand.nextInt(mutatedP.size());

      while(i == j ){
         j = rand.nextInt(mutatedP.size());
      }

      Vector<Integer> v = mutatedP.get(i);

      mutatedP.set(i, mutatedP.get(j));
      mutatedP.set(j,v);
      mutatedP.add(mutatedP.get(0));

      return mutatedP;
   }

   /*
    * Creates a child from the two parents using ordered crossover
    */
   public static ArrayList<Vector<Integer> > Crossover(ArrayList<Vector<Integer> > Parent1, ArrayList<Vector<Integer> > Parent2){
      Random rand = new Random();
      int i = 0;
      int j = 0;
      int p1 = Parent1.size()-1;
      if(p1%2 == 0){
         i = rand.nextInt((p1/2) -1) + 1;
         j = rand.nextInt((p1/2)) + (p1/2) - 1;
      }
      else{
         i = rand.nextInt((p1/2) -1) + 1;
         j = rand.nextInt((p1/2)) + (p1/2);
      }
      ArrayList<Vector<Integer> > mutatedP = mutate(Parent1);
      
      ArrayList<Vector<Integer> > subArr = new ArrayList<>(mutatedP.subList(i, j+1));
      Set<Vector<Integer> > contain = new HashSet<>(subArr);
      ArrayList<Vector<Integer> > child = new ArrayList<>(subArr);

      Map<Integer, Vector<Integer> > leftFromP2 = new HashMap<>();

      int indx = 0;
      for(int k = j+1; k < Parent2.size()-1; k++){
         Vector<Integer> v = Parent2.get(k);
         if(!contain.contains(v)){
            leftFromP2.put(indx, v);
            indx++;
         }
      }
      for(int k =0; k<j+1; k++){
         Vector<Integer> v = Parent2.get(k);
         if(!contain.contains(v)){
            leftFromP2.put(indx, v);
            indx++;
         }
      }

      int childInd = 0;
      int endBound = j+1;
      while(endBound < Parent1.size()-1){
         child.add(leftFromP2.get(childInd));
         endBound++;
         childInd++;
      }
      int frontBound = 0;
      while(frontBound < i){
         child.add(frontBound, leftFromP2.get(childInd));
         frontBound++;
         childInd++;
      }

      child.add(child.get(0));

      return child;
   }

   /*
    * Creates an arrayList of children by randomly selecting two parents from the population and using the Crossover method
    */
   public static  ArrayList<ArrayList<Vector<Integer> > > generateChildren(ArrayList<ArrayList<Vector<Integer> > > population){
      ArrayList<ArrayList<Vector<Integer> > > children = new ArrayList<>();

      int num = 0;

      while(num<population.size()){
         String parents = generateParents(population);
         Scanner in = new Scanner(parents);

         int i = in.nextInt();

         int j = in.nextInt();

         children.add(Crossover(population.get(i), population.get(j)));

         num++;
      }

      return children;
   }

   /*
    * 
    */
   public static ArrayList<Vector<Integer> > findPath(ArrayList<ArrayList<Vector<Integer> > > population){

      ArrayList<ArrayList<Vector<Integer> > > temp = population;
      int i = 0;
      while(i < NUM_MATING){
         ArrayList<ArrayList<Vector<Integer> > > children = generateChildren(temp);
         Map<Double,Integer> rank = generateRank(children);
         Map.Entry<Double,Integer> entry = rank.entrySet().iterator().next();
         paths.put(entry.getKey(), children.get(entry.getValue()));
         temp = children;

         i++;
      }

      Map.Entry<Double, ArrayList<Vector<Integer> > > entry = paths.entrySet().iterator().next();

      return entry.getValue();

   }

   /*
    * prints the city coordinates from a path
    */
   public static void printPath(ArrayList<Vector<Integer> > path){
      try (FileWriter outFile = new FileWriter("output.txt")) {
         for(Vector<Integer> v : path){
            String text = v.get(0) + " " + v.get(1) +  " " + v.get(2) + "\n";
            outFile.write(text);
         }

         outFile.close();
      } catch (IOException e) {
         e.printStackTrace();
      }


   }
}

