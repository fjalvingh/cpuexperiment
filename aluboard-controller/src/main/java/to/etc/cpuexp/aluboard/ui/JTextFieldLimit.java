package to.etc.cpuexp.aluboard.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * This whole piece of crap is needed to do something as
 * simple as to limit the max #chars in a text input. It
 * tells you that another term for architect is probably
 * just idiot.
 */
final public class JTextFieldLimit extends PlainDocument {
	private int limit;

	public JTextFieldLimit(int limit) {
		super();
		this.limit = limit;
	}

	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		if(str == null)
			return;

		if((getLength() + str.length()) <= limit) {
			super.insertString(offset, str, attr);
		}
	}
}
