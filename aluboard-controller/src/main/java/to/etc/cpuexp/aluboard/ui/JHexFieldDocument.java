package to.etc.cpuexp.aluboard.ui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

final public class JHexFieldDocument extends PlainDocument {
	private int m_limit;

	public JHexFieldDocument(int limit) {
		m_limit = limit;
	}

	@Override
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		if(str == null)
			return;
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(!isHexDigit(c))
				return;
		}

		if((getLength() + str.length()) <= m_limit) {
			super.insertString(offset, str, attr);
		}
	}

	private boolean isHexDigit(char c) {
		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
	}
}
