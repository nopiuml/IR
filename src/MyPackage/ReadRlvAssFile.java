package MyPackage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class ReadRlvAssFile {

    /**
     * Compute total relevant for the query given. Returns the number of relevant documents.
     * @param true_rlv_docs
     * @param retrieved_docs
     * @param num_query
     * @return
     * @throws Exception
     */
	public int TotalRelForQuery(List<String> true_rlv_docs, List<String> retrieved_docs ,int num_query) throws Exception{
		
		 int relevant = 0;
				for (int k=0;k<retrieved_docs.size();k++){ // for every retrieved doc check if it matches the relevant docs of rlv-ass file
					for (int i=1;i<true_rlv_docs.size();i++){
						
							if (true_rlv_docs.get(i).equalsIgnoreCase(retrieved_docs.get(k))){
								relevant++;
								System.out.println("Doc "+true_rlv_docs.get(i)+" is relevant with "+retrieved_docs.get(k));
								
							}
					}
				}
			System.out.println("\n");
			System.out.println(relevant + " relevant for the query "+(num_query+1));
		
		return relevant;
		
	}
	
	/**
	 * Read from file rlv-ass the true relevant for each query. Returns a list.
	 * @param num_of_query
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public List<String> True_Rlv_docs(int num_of_query) throws Exception{
		
		List<String> docs= new ArrayList<String>();
		InputStream is = getClass().getResourceAsStream("/rlv-ass");
	    //InputStreamReader isr = new InputStreamReader(is);
	//	String content = new Scanner(new File("rlv-ass")).useDelimiter("\\Z").next();
		String content = new Scanner(is).useDelimiter("\\Z").next();
		String queries[] = content.split("/");
		
		String rlv_doc[] = queries[num_of_query].split(" ");
		
		for (int j=0;j<rlv_doc.length;j++){
			if (!rlv_doc[j].equalsIgnoreCase("")){
				docs.add(rlv_doc[j]);
			
			}
		}
		return docs;	
	}
}
