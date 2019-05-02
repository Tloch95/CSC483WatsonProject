/*
 * Author: Thomas Lochner
 * Description: Class for indexing for CSC 483 final project.
 * CSC 483
 * Spring 2019
 */

package main.java.edu.arizona.cs;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import java.io.*;
import java.nio.file.Paths;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
	public Directory index;
	public StandardAnalyzer analyzer;
	private final String INDEX_DIRECTORY = "src/main/resources/index";
	
	public Indexer() {
		try {
			this.index = FSDirectory.open(Paths.get(this.INDEX_DIRECTORY));
			this.analyzer = new StandardAnalyzer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void build() {
		// Initialize necessary index variables.
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = null;
        try {
			writer = new IndexWriter(index, config);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        // Build the index.
        try {
        	final String RESOURCES_DIRECTORY = "src/main/resources";
            File dir = new File(RESOURCES_DIRECTORY);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
            	// For every file in the resources directory.
            	for (File file : directoryListing) {
            		// Only get the Wiki files.
            		String fileName = file.getName();
            		if (!fileName.contains("enwiki")) {
            			continue;
            		}
                
            		// This is a Wiki file. Let's read and index it.
            		System.out.println("Indexing " + fileName + "...");
            		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            		String line;
            		String name = "";
            		StringBuilder content = new StringBuilder();
            		Document doc = null;
            		
            		while ((line = bufferedReader.readLine()) != null) {
            			line = line.trim();
            			
            			// Skip empty lines.
            			if (line.isEmpty()) {
            				continue;
            			}
            			
            			// Check if this is a title (indicating a new document).
            			if (line.startsWith("[[")) {
            				// If doc is not null, then we just finished a doc, so add it.
            				if (doc != null) {;
            					doc.add(new TextField("content", content.toString(), Field.Store.YES));
            					writer.addDocument(doc);
            				}
            				name = line.substring(2, line.length() - 2);
            				doc = new Document();
            				doc.add(new StringField("name", name, Field.Store.YES));
            				content = new StringBuilder();
            				continue;
            			}
            			
            			// Strip out equals signs from headers.
            			if (line.length() > 4) {
            				if (line.substring(0, 2).equals("==") && line.substring(line.length() - 2, line.length()).equals("==")) {
            					line = line.substring(2, line.length() - 2);
            					content.append(line + "\n");
            					continue;
            				}
            			}
            			
            			// Strip out hyperlinks.
            			if (line.contains("[tpl]")) {
            				while (line.contains("[tpl]")) {
            					content.append(line.substring(0, line.indexOf("[tpl]")));
            					content.append(line.substring(line.indexOf("[/tpl]") + 6) + " ");
            					line = line.substring(line.indexOf("[/tpl]") + 6);
            				}
            				content.append("\n");
            				continue;
            			}
            			
            			content.append(line + "\n");
            		}
            		bufferedReader.close();
            	}
            	System.out.println();
            } else {
              System.err.println("Could not find any files in the index!");
            }
            
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
