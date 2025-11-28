package to.etc.cpuexp.aluboard.ui;

import to.etc.cpuexp.aluboard.SerialApi;

import javax.swing.*;
import java.awt.*;

public class RegisterPane extends JPanel {
	private static final long serialVersionUID = 1L;

	private final SerialApi m_serialApi;

	private JTextField[] m_registerOut = new JTextField[16];

	/**
	 * Create the panel.
	 */
	public RegisterPane(SerialApi serialApi) {
		m_serialApi = serialApi;
		GridBagLayout gridBagLayout = new GridBagLayout();
//		gridBagLayout.columnWidths = new int[]{14, 196, 14, 196, 0};
//		gridBagLayout.rowHeights = new int[]{19, 0};
//		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
//		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JLabel lblRegisters = new JLabel("Registers");
//		lblRegisters.setBackground(UIManager.getColor("TabbedPane.background"));
		GridBagConstraints gbc_lblRegisters = new GridBagConstraints();
		gbc_lblRegisters.gridwidth = 4;
		gbc_lblRegisters.insets = new Insets(0, 0, 5, 5);
		gbc_lblRegisters.gridx = 1;
		gbc_lblRegisters.gridy = 0;
		add(lblRegisters, gbc_lblRegisters);

		for(int i = 0; i < 16; i++) {
			createPair(i, 1);
		}
	}

	private void createPair(int i, int yOffset) {
		int loffset = i >= 8 ? 2 : 0;

		JLabel label = new JLabel("r" + i);
		GridBagConstraints labelCs = new GridBagConstraints();
		labelCs.anchor = GridBagConstraints.WEST;
		labelCs.insets = new Insets(0, 0, 0, 5);
		labelCs.gridx = loffset;
		labelCs.gridy = i % 8 + yOffset;
		add(label, labelCs);

		JTextField textField = new JTextField();
		m_registerOut[i] = textField;

		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.anchor = GridBagConstraints.NORTH;
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.gridx = loffset + 1;
		gbc_textField.gridy = i % 8 + yOffset;
		add(textField, gbc_textField);
		textField.setColumns(10);
	}

	public void refresh() {
		int[] registers = m_serialApi.getRegisters();
		for(int i = 0; i < 16; i++) {
			String s = Integer.toHexString(registers[i]);
			String res = "0000".substring(s.length()) + s.toUpperCase();
			m_registerOut[i].setText(res);
		}
	}

}
