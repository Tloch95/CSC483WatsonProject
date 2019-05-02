/*
 * Author: Thomas Lochner
 * Description: Class for running Watson queries/questions for CSC 483 final project.
 * CSC 483
 * Spring 2019
 */

package main.java.edu.arizona.cs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;

@SuppressWarnings("unused")
public class QueryEngine {
	public static void main(String args[]) {
		
		// Initialize some variables.
		Indexer indexer = new Indexer();
		float correct = 0;
		float incorrect = 0;
		float total = 0;
		
		////////////////////////////////////////////////
		// NOTE! COMMENT THIS OUT TO NOT BUILD INDEX. //
		////////////////////////////////////////////////
		
		// Build our index.
		//indexer.build();
		
		// Get our questions ready.
		File file = new File("src/main/resources/questions.txt");
        BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
        try {
        	// Ready our searchers.
        	IndexReader reader = DirectoryReader.open(indexer.index);
	        IndexSearcher searcher = new IndexSearcher(reader);
        	
	        // Build the queries, one by one.
        	StringBuilder query = new StringBuilder();
        	String line;
            while ((line = bufferedReader.readLine()) != null) {
            	if (line.isEmpty()) {
            		continue;
            	}
            	
            	// Get the category, clue, and answer for each question.
            	query = new StringBuilder();
            	String category = line.trim();
            	String clue = bufferedReader.readLine().trim();
            	String answer = bufferedReader.readLine().trim();
            	
            	// Build the query.
            	query.append("\"" + clue + "\"" + "\"" + category + "\"");
            	Query q = new QueryParser("content", indexer.analyzer).parse(QueryParser.escape(query.toString()));
            	
            	// Search the query.
            	//searcher.setSimilarity(new BM25Similarity());
    	        TopDocs docs = searcher.search(q, 5);
    	        ScoreDoc[] hits = docs.scoreDocs;
    	        
    	        // Get the top-scored doc.
    	        int docId = hits[0].doc;
	        	Document doc = searcher.doc(docId);
	        	String result = doc.get("name").trim();
	        	
	        	// Display some results for this query!
	        	if (result.equals(answer)) {
	        		System.out.println("Correct answer! Found answer: " + result);
	        		correct++;
	        		total++;
	        	} else {
	        		System.out.println("Incorrect answer. Found answer: " + result + "\t\t\tExpected answer: " + answer);
	        		incorrect++;
	        		total++;
	        	}
            }
            
            // Display the final correctness results for all questions.
            System.out.println();
            System.out.println("------------------------------------------");
            System.out.println("Correct: " + (int)correct);
            System.out.println("Incorrect: " + (int)incorrect);
            System.out.println("Total: " + (int)total);
            System.out.println("Got " + ((correct / total) * 100) + "% correct.");
            System.out.println("------------------------------------------");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
