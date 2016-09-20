package autocomplete;

import java.util.ArrayList;

public class SmartTrie {
	private ArrayList<Trie> roots;
	private int bucketSize;
	private int lexiconSize;
	
	public SmartTrie(int bucketSize) {
		roots = new ArrayList<Trie>();
		roots.add(new Trie());
		this.bucketSize = bucketSize;
		lexiconSize = 0;
	}
	
	public int getLexiconSize() { return lexiconSize; }
	
	public void insert(String word, boolean isLexicon) {
		int level = (lexiconSize / bucketSize) + 1;
		if (!isLexicon) level = 0;
		if (level >= roots.size()) roots.add(new Trie());
		
		boolean isNew = roots.get(level).insert(word);
		if (isLexicon && isNew) ++lexiconSize;
	}
	
	public boolean contains(String word) {
		for (int i = 0; i < roots.size(); ++i) {
			if (roots.get(i).contains(word)) return true;
		}
		return false;
	}
	
	public String getSuggested(String s) {
		ArrayList<String> list = new ArrayList<String>();
		
		for (int i = 0; i < roots.size(); ++i) {
			roots.get(i).getSuggested(s, list);
			if (list.size() >= 10) break;
		}
		
		String ret = "";
		for (String st : list) {
			ret += st + "\n";
		}
		
		return ret;
	}
	
	public String getPriority() {
		ArrayList<String> list = new ArrayList<String>();
		
		roots.get(0).getSuggested("", list);
		
		String ret = "";
		for (String st : list) {
			ret += st + "\n";
		}
		
		return ret;
	}
}
