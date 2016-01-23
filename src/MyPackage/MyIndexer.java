package MyPackage;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 * based on the input of the user.
 */
public class MyIndexer {
	

  public static final Version matchversion = Version.LUCENE_4_10_2;
  private static MyAnalyzer analyzer = new MyAnalyzer(matchversion);
  private IndexWriter writer;
  private ArrayList<File> queue = new ArrayList<File>();
  private static SplitFile sp = new SplitFile();
  private static String indexLocation = null;
  private static int total_rel_for_query = 0;
  private static ReadRlvAssFile rlv_ass = new ReadRlvAssFile();
  private static String query_number;
  private static double precision = 0.0;
  private static double recall = 0.0;

public static void main(String[] args) throws IOException {
	
	
	//Ask the user if he wants to split the file doc-text, if not, proceed
	System.out.println("Do you want to split file doc-text? Y or N:");

    BufferedReader br_split = new BufferedReader(
            new InputStreamReader(System.in));
    String split = br_split.readLine(); 
    if (!split.equalsIgnoreCase("N")){
		while (!(split.equalsIgnoreCase("Y"))){
		    	
		    	System.out.println("Please type one of the following answers: Y or N:");
		    	split = br_split.readLine();
		    	if (split.equalsIgnoreCase("N")) break;
		    	if (split.equalsIgnoreCase("Y")||split.equalsIgnoreCase("y")) break;
		        	
		    }
    }
    
    if (split.equalsIgnoreCase("Y")||split.equalsIgnoreCase("y")){
    	sp.SplitGivenFile();
    }
   
    System.out.println("Enter the path where the index will be created: (e.g. /tmp/index or c:\\temp\\index)");

    BufferedReader br = new BufferedReader(
            new InputStreamReader(System.in));
    String s = br.readLine();

    MyIndexer indexer = null;
    try {
      indexLocation = s;
      indexer = new MyIndexer(s);
    } catch (Exception ex) {
      System.out.println("Cannot create index..." + ex.getMessage());
      System.exit(-1);
    }

    //===================================================
    //read input from user until he enters q for quit
    //===================================================
    while (!s.equalsIgnoreCase("q")) {
      try {
        System.out.println("Enter the full path to add into the index (q=quit): (e.g. /home/ron/mydir or c:\\Users\\ron\\mydir)");
        System.out.println("[Acceptable file types: .txt]");
        s = br.readLine();
        if (s.equalsIgnoreCase("q")) {
          break;
        }

        //try to add file into the index
        indexer.indexFileOrDirectory(s);
      } catch (Exception e) {
        System.out.println("Error indexing " + s + " : " + e.getMessage());
      }
    }

    //===================================================
    //after adding, we always have to call the
    //closeIndex, otherwise the index is not created    
    //===================================================
    indexer.closeIndex();

    //=========================================================
    // Now search
    //=========================================================
    IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
    IndexSearcher searcher = new IndexSearcher(reader);
    
    s = "";
    while (!s.equalsIgnoreCase("q")) {
      try {
        System.out.println("Enter the path of the search query (q=quit):");
        s = br.readLine();
        if (s.equalsIgnoreCase("q")) {
          break;
        }
        Query q = new QueryParser("contents", analyzer).parse(ChooseQuery(s));
        System.out.println(q);
        // 10 top documents in decreasing relevance
        TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        List<String> retrieved_docs = new ArrayList<String>();
        List<String> true_relevant = new ArrayList<String>();
        true_relevant = rlv_ass.True_Rlv_docs(Integer.parseInt(query_number)-1);

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
          int docId = hits[i].doc;
          Document d = searcher.doc(docId);
          System.out.println((i + 1) + ". " + d.get("filename") + " score=" + hits[i].score);
          //add the retrieved_docs to a list
          retrieved_docs.add(d.get("filename").replaceAll("[^0-9]", ""));
          
          total_rel_for_query = rlv_ass.TotalRelForQuery(true_relevant, retrieved_docs, (Integer.parseInt(query_number)-1));
          
          System.out.println("\n");
          System.out.println("======= Now calculate the precision for retrieved document No "+(i+1)+" ========");
          precision = (double)total_rel_for_query/(i+1);
          System.out.println("Precision for query "+query_number+" is: "+precision);
          recall = (double)total_rel_for_query/true_relevant.size();
          System.out.println("Recall for query "+query_number+" is: "+recall);
          System.out.println("\n");
        }
                
        
      } catch (Exception e) {
        System.out.println("Error searching " + s + " : " + e.getMessage());
      }
    }

  }

  /**
   * Constructor
   * @param indexDir the name of the folder in which the index should be created
   * @throws java.io.IOException when exception creating index.
   */
  MyIndexer(String indexDir) throws IOException {
    // the boolean true parameter means to create a new index everytime, 
    // potentially overwriting any existing files there.
    FSDirectory dir = FSDirectory.open(new File(indexDir));


    IndexWriterConfig config = new IndexWriterConfig(matchversion, analyzer);

    writer = new IndexWriter(dir, config);
  }

  /**
   * Indexes a file or directory
   * @param fileName the name of a text file or a folder we wish to add to the index
   * @throws java.io.IOException when exception
   */
  public void indexFileOrDirectory(String fileName) throws IOException {
    //===================================================
    //gets the list of files in a folder (if user has submitted
    //the name of a folder) or gets a single file name (if user
    //has submitted only the file name) 
    //===================================================
    addFiles(new File(fileName));
    
    int originalNumDocs = writer.numDocs();
    for (File f : queue) {
      FileReader fr = null;
      try {
        Document doc = new Document();

        //===================================================
        // add contents of file
        //===================================================
        fr = new FileReader(f);
        doc.add(new TextField("contents", fr));
        doc.add(new StringField("path", f.getPath(), Field.Store.YES));
        doc.add(new StringField("filename", f.getName(), Field.Store.YES));

        writer.addDocument(doc);
        System.out.println("Added: " + f);
      } catch (Exception e) {
        System.out.println("Could not add: " + f);
      } finally {
        fr.close();
      }
    }
    
    int newNumDocs = writer.numDocs();
    System.out.println("");
    System.out.println("************************");
    System.out.println((newNumDocs - originalNumDocs) + " documents added.");
    System.out.println("************************");

    queue.clear();
  }

  private void addFiles(File file) {

    if (!file.exists()) {
      System.out.println(file + " does not exist.");
    }
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        addFiles(f);
      }
    } else {
      String filename = file.getName().toLowerCase();
      //===================================================
      // Only index text files
      //===================================================
      if (filename.endsWith(".htm") || filename.endsWith(".html") || 
              filename.endsWith(".xml") || filename.endsWith(".txt")) {
        queue.add(file);
      } else {
        System.out.println("Skipped " + filename);
      }
    }
  }
  
  /**
   * Choose the query(numbers 1-93) from file npl/query-text
   * @param filename
   * @return
   * @throws FileNotFoundException
   */
  @SuppressWarnings("resource")
private static String ChooseQuery(String filename) throws FileNotFoundException{
	  
	  String query_text;
	  Scanner in = new Scanner(System.in);
	  String content = new Scanner(new File(filename)).useDelimiter("\\Z").next();
	  String queries[] = content.split("/");
	  
	  
		
		System.out.println("Enter the query you want to use (1-93): ");
		query_number = in.nextLine();
		query_text = queries[Integer.parseInt(query_number)-1];
		System.out.println(query_text);
		
		return query_text;
  }

  /**
   * Close the index.
   * @throws java.io.IOException when exception closing
   */
  public void closeIndex() throws IOException {
    writer.close();
  }
  
  
  
  
}