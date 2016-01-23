package MyPackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Scanner;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

public class MyAnalyzer extends Analyzer{
	
	
	private static CharArraySet stopwords = new CharArraySet(600, true);		

	/**
	 * Constructor
	 * @param matchversion
	 */
	public MyAnalyzer(Version matchversion) {
		// TODO Auto-generated constructor stub
		
	}

	/**
	 * Tokenize the given fieldname of docs or queries
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
	    Tokenizer source = new LetterTokenizer(reader);              
	    TokenStream filter = new LowerCaseFilter(source); 
		try {
			stopwords = readFromFile("common_words");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(stopwords);
		filter = new StopFilter(filter, stopwords);    
	    filter = new PorterStemFilter(filter);
	    return new TokenStreamComponents(source, filter);
	} 
	
	/**
	 * Read from file common_words the words that need to be ignored from the analyzer
	 * @param filepath
	 * @return
	 */
	@SuppressWarnings("resource")
	public CharArraySet readFromFile(String filepath) throws FileNotFoundException{
		
		Scanner s;
		InputStream is = getClass().getResourceAsStream("/common_words");
		s = new Scanner(new File(filepath));
		s = new Scanner(is);
		while (s.hasNext()){
		    stopwords.add(s.next());
		}
		s.close();
		
		return stopwords;
	}
}