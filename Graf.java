import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import com.opencsv.CSVWriter;

public class Graf {
	
	public static CSVWriter writer1;
	public static CSVWriter writer2;
	public static CSVWriter writer3;
	public static ArrayList<String> naredbe;
	public static ArrayList<Integer> vremena;
	
	public static void napraviGraf(int n, ArrayList<Integer>v) throws IOException {
		vremena=v;
		
		String osobe="";
		for(int i=1; i<=n; i++) {
			osobe+="P"+i;
		}
		osobe+="_";
		
		try {
			writer1 = new CSVWriter(new FileWriter(new File("C:\\Users\\teodo\\OneDrive\\Desktop\\cvorovi.csv")));
			writer2 = new CSVWriter(new FileWriter(new File("C:\\Users\\teodo\\OneDrive\\Desktop\\prelaze.csv")));
			writer3 = new CSVWriter(new FileWriter(new File("C:\\Users\\teodo\\OneDrive\\Desktop\\vracaju.csv")));

			String[] header1 = { "attribute"};
	        writer1.writeNext(header1);
	        String[] header2 = { "attribute", "id1", "id2"};
	        writer2.writeNext(header2);
	        String[] header3 = { "attribute", "id1", "id2"};
	        writer3.writeNext(header3);
			
			naredbe = new ArrayList<String>();
			String[] s = {osobe};
			writer1.writeNext(s);
			naredbe.add("MERGE (:InitialState:State{attribute:\""+osobe+"\"})\n");
			rekurzivna1(osobe);
			
			writer1.flush();
			writer2.flush();
			writer3.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<ArrayList<String>> getParovi(String[] strane) {
		ArrayList<ArrayList<String>> lista=new ArrayList<ArrayList<String>>();
		
		for(int i = 0; i<strane[0].length()-1; i+=2)
            for(int j = i+2; j<strane[0].length(); j+=2) {
            	String[] parovi= {strane[0].charAt(i)+""+strane[0].charAt(i+1),strane[0].charAt(j)+""+strane[0].charAt(j+1)};
            	lista.add(new ArrayList<String>(Arrays.asList(parovi)));
            }
		
		return lista;
	}
	
	public static ArrayList<String> getZasebni(String[] strane){
		ArrayList<String> lista=new ArrayList<String>();
		
		for(int i=0; i<strane[1].length(); i+=2) {
			String osoba=strane[1].charAt(i)+""+strane[1].charAt(i+1);
			lista.add(osoba);
		}
		
		return lista;
	}
	
	public static int nadjiIndeks(String drugaStrana, String osoba) {
		int broj=osoba.charAt(1)-'0';
		for(int i=drugaStrana.length()-1; i>0; i-=2) {
			if(drugaStrana.charAt(i)-'0'<broj)
				return i+1;
		}
		return 0;
	}
	
	public static void rekurzivna2(String s) throws IOException {
		String strane[]=s.split("_");
		ArrayList<String> lista=getZasebni(strane);
		
		for(String osoba: lista) {
			if(s.charAt(0)=='_')
				break;
			
			int i1=nadjiIndeks(strane[0],osoba);
			String novaStrana1=strane[0].substring(0,i1)+""+osoba+""+strane[0].substring(i1);
			
			String novaStrana2=strane[1];
			novaStrana2=novaStrana2.replace(osoba, "");
			
			String nova=novaStrana1+"_"+novaStrana2;
			String naredba1="MERGE (:State{attribute:\""+nova+"\"});\n";
			String[] red1= {nova};
			
			if(!naredbe.contains(naredba1)) {
				writer1.writeNext(red1);
				naredbe.add(naredba1);
			}
			
			int indeks=osoba.charAt(1)-'0';
        	int vrijeme=vremena.get(indeks-1);
			
        	String naredba2="MATCH (s1:State{attribute:\""+s+"\"}), (s2:State{attribute:\""+nova+"\"})\r\n"
					+ "MERGE (s1)-[:vraca_se_"+osoba+"{attribute:"+vrijeme+"}]->(s2);";
        	String[]red2= {vrijeme+"", s, nova};
        	
			if(!naredbe.contains(naredba2)) {
				writer3.writeNext(red2);
				naredbe.add(naredba2);
			}
			
			rekurzivna1(nova);
		}
	}
	
	public static void rekurzivna1(String s) throws IOException {
		String strane[]=s.split("_");
		
		ArrayList<ArrayList<String>> lista=getParovi(strane);
		
		for(ArrayList<String> l: lista) {
			
			String novaStrana2="";
			if(strane.length==1 && s.charAt(s.length()-1)=='_')
				novaStrana2=l.get(0)+""+l.get(1);
			else {
				int i1=nadjiIndeks(strane[1],l.get(0));
				novaStrana2=strane[1].substring(0,i1)+""+l.get(0)+""+strane[1].substring(i1);
				int i2=nadjiIndeks(novaStrana2,l.get(1));
				novaStrana2=novaStrana2.substring(0,i2)+""+l.get(1)+""+novaStrana2.substring(i2);
			}
			
			String novaStrana1=strane[0];
			novaStrana1=novaStrana1.replace(l.get(0), "");
			novaStrana1=novaStrana1.replace(l.get(1), "");
			
			String nova=novaStrana1+"_"+novaStrana2;
			String naredba1="MERGE (:State{attribute:\""+nova+"\"});\n";
			String naredba2="MERGE (:FinalState:State{attribute:\""+nova+"\"});\n";
			String[] red1= {nova};
			
			if(!naredbe.contains(naredba1) && nova.charAt(0)!='_') {
				writer1.writeNext(red1);
				naredbe.add(naredba1);
			}
			else if(!naredbe.contains(naredba2) && nova.charAt(0)=='_') {
				writer1.writeNext(red1);
				naredbe.add(naredba2);
			}
			
			int indeks1=l.get(0).charAt(1)-'0';
        	int indeks2=l.get(1).charAt(1)-'0';
        	int vrijeme=(vremena.get(indeks1-1)>=vremena.get(indeks2-1)) ? vremena.get(indeks1-1) : vremena.get(indeks2-1);
			
        	String naredba3="MATCH (s1:State{attribute:\""+s+"\"}), (s2:State{attribute:\""+nova+"\"})\r\n"
					+ "MERGE (s1)-[:prelaze_"+l.get(0)+""+l.get(1)+"{attribute:"+vrijeme+"}]->(s2);";
        	String[] red2= {vrijeme+"", s, nova};
        	
			if(!naredbe.contains(naredba3)) {
				writer2.writeNext(red2);
				naredbe.add(naredba3);
			}
			
			rekurzivna2(nova);
		}
		
	}
}