package autocomplete;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class Autocomplete extends JFrame implements KeyListener, ActionListener {

private static SmartTrie trie;
private static JTextField field;
private static String[] data;
private static JTextArea textArea;
private static int selected;

private static Color gray = new Color(225, 225, 225);
private static Color red = new Color(240, 207, 207);
private static Color yellow = new Color(255, 255, 205);
private static Color green = new Color(210, 255, 210);
private static Color highlight = new Color(188, 210, 220);
private static Highlighter.HighlightPainter painter;
private static Color destColor;
private static Timer timer;

private Border line = BorderFactory.createLineBorder(Color.black);
	
	public Autocomplete() {
		init();
	}

	private void init() {
		trie = new SmartTrie(500);
		addLexicon("common.txt");
		
		field = new JTextField(24);
		field.addKeyListener(this);
		data = new String[10];
		textArea = new JTextArea();
		
		setSize(new Dimension(400, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setTitle("Autocomplete");
		
		textArea.setColumns(25);
		textArea.setRows(10);
		textArea.setEditable(false);
		textArea.setPreferredSize(new Dimension(80, 105));
		field.setFocusTraversalKeysEnabled(false);
		selected = -1;

		field.setBorder(BorderFactory.createCompoundBorder(line, 
		        BorderFactory.createEmptyBorder(1, 4, 1, 4)));
		textArea.setBorder(BorderFactory.createCompoundBorder(line, 
		        BorderFactory.createEmptyBorder(1, 4, 1, 4)));
		
		Container contentPane = getContentPane();
		SpringLayout layout = new SpringLayout();
		contentPane.setLayout(layout);
		contentPane.add(field);
		contentPane.add(textArea);
		contentPane.setBackground(gray);
		painter = new DefaultHighlighter.DefaultHighlightPainter(highlight);
		destColor = gray;
		
		timer = new Timer(30, this);
		
		layout.putConstraint(SpringLayout.WEST, field,
                5,
                SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, field,
                5,
                SpringLayout.NORTH, contentPane);
		layout.putConstraint(SpringLayout.EAST, contentPane,
                5,
                SpringLayout.EAST, field);
		
		// text area constraints
		layout.putConstraint(SpringLayout.WEST, textArea,
                5,
                SpringLayout.WEST, contentPane);
		layout.putConstraint(SpringLayout.NORTH, textArea,
                5,
                SpringLayout.SOUTH, field);
		layout.putConstraint(SpringLayout.SOUTH, contentPane,
                5,
                SpringLayout.SOUTH, textArea);
		
		validate();
		pack();
		
		setResizable(false);
		setVisible(true);
	}
	
	private void addLexicon(String path) {
		try {
			InputStream in = this.getClass().getClassLoader()
                    .getResourceAsStream(path);
			Scanner s = new Scanner(in);
			
			while (s.hasNext()) {
				trie.insert(s.next().trim(), true);
			}
			
		} catch (Exception e) {
			System.out.println("Could not load lexicon.");
		}
	}
	
	private void switchColorTo(Color c) {
		if (getContentPane().getBackground().getRGB() != c.getRGB()) {
			destColor = c;
			timer.start();
		}
	}
	
	public static void main(String[] args) {
		Autocomplete a = new Autocomplete();
	}

	@Override
	public void keyPressed(KeyEvent evt) {
		if (evt.getKeyCode() == KeyEvent.VK_DOWN ||
		    evt.getKeyCode() == KeyEvent.VK_UP) {
			String area = textArea.getText();
			if (!area.equals("")) {
				if (evt.getKeyCode() == KeyEvent.VK_DOWN) ++selected;
				else --selected;
				
				String[] div = area.split("\\s+");
				if (selected >= div.length) selected = div.length - 1;
				if (selected < -1) selected = -1;
				if (selected >= 0) {
					String text = field.getText();
					int idx = text.lastIndexOf(' ');
					
					if (idx >= 0) {
						field.setText(text.substring(0, idx+1) + div[selected]);
					} else {
						field.setText(div[selected]);
					}
					
					// remove highlights
					textArea.getHighlighter().removeAllHighlights();
					
					try {
						int sidx = textArea.getLineStartOffset(selected);
						int eidx = textArea.getLineEndOffset(selected);
						
						textArea.getHighlighter().addHighlight(sidx, eidx, painter);
					} catch (Exception e) {}
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (selected == -1) {
			String text = field.getText();
			String[] div = text.split("\\s+");
			
			if (text.length() == 0 || div.length == 0) {
				textArea.setText(trie.getPriority());
				switchColorTo(gray);
			} else {
				String word = div[div.length - 1].replaceAll("[^a-zA-Z]", "").toLowerCase();
		
				if (text.length() > 0 && text.charAt(text.length() - 1) == ' ') {
					textArea.setText("");
					if (trie.contains(word)) {
						switchColorTo(gray);
					} else {
						trie.insert(word, false);
						switchColorTo(yellow);
					}
					
				} else {
					textArea.setText(trie.getSuggested(word));
					if (textArea.getText().equals(""))
						switchColorTo(red);
					else switchColorTo(green);
				}
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent evt) {
		if (selected != -1) {
			selected = -1;
			if (evt.getKeyChar() == KeyEvent.VK_ENTER ||
			    evt.getKeyChar() == KeyEvent.VK_TAB) {
				field.setText(field.getText() + ' ');
			}
		} else if (evt.getKeyChar() == KeyEvent.VK_TAB) {
			String text = field.getText();
			String[] div = text.split("\\s+");
			int len = div.length > 0 ? div[div.length - 1].length() : 0;
			
			String fill = textArea.getText();
			div = fill.split("\\s+");
			if (fill.length() > 0 && div.length > 0) {
				field.setText(text + div[0].substring(len) + ' ');
			}
		} 
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Color cur = getContentPane().getBackground();
		int diffR = destColor.getRed() - cur.getRed();
		int diffG = destColor.getGreen() - cur.getGreen();
		int diffB = destColor.getBlue() - cur.getBlue();
		
		if (diffR < 2 || diffG < 2 || diffB < 2) {
			getContentPane().setBackground(destColor);
			timer.stop();
		} else {
			getContentPane().setBackground(new Color(
				(int)(cur.getRed() + diffR > 0 ? 1 : -1),
				(int)(cur.getGreen() + diffG > 0 ? 1 : -1),
				(int)(cur.getBlue())));
		}
	}
	
}
