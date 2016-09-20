package autocomplete;

import java.util.ArrayList;

class TrieNode {
	TrieNode[] next;
	boolean isWord;
	int numChildren;

	public TrieNode() {
		next = new TrieNode[26];
		isWord = false;
		numChildren = 0;
	}
}

public class Trie {
	private TrieNode root;
	private int sz;
	
	public Trie() {
		root = new TrieNode();
		sz = 0;
	}
	
	public int size() { return sz; }
	
	/*
	 * returns true if new entry
	 */
	public boolean insert(String s) {
		boolean isNew = false;
		TrieNode cur = root;
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (cur.next[c - 'a'] == null) {
				cur.next[c - 'a'] = new TrieNode();
				++cur.numChildren;
			}
			cur = cur.next[c - 'a'];
		}
		
		if (!cur.isWord) {
			++sz;
			isNew = true;
		}
		
		cur.isWord = true;
		return isNew;
	}
	
	public boolean contains(String s) {
		TrieNode t = search(s);
		if (t == null) return false;
		return t.isWord;
	}
	
	public boolean isPrefix(String s) {
		return search(s) != null;
	}
	
	public void getSuggested(String s, ArrayList<String> list) {
		TrieNode cur = search(s);
		if (cur != null) recurseHelper(cur, s, list);
	}
	
	private void recurseHelper(TrieNode cur, String s, ArrayList<String> list) {
		if (list.size() == 10) return;
		if (cur.isWord && !list.contains(s)) list.add(s); 
		if (cur.numChildren == 0) return;
		
		for (int i = 0; i < 26; ++i) {
			if (cur.next[i] != null) {
				recurseHelper(cur.next[i], s + (char)('a' + i), list);
			}
		}
	}
	
	private TrieNode search(String s) {
		TrieNode cur = root;
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (cur.next[c - 'a'] == null) return null;
			cur = cur.next[c - 'a'];
		}
		return cur;
	}
}
