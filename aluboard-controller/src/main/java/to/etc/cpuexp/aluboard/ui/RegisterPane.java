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

		//-- Buttons
		JButton btnReadFromAlu = new JButton("Read from ALU");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 10;
		gbc_btnNewButton.gridwidth = 4;
		add(btnReadFromAlu, gbc_btnNewButton);

		btnReadFromAlu.addActionListener(e -> readFromRemote());

		JButton btnSendToAlu = new JButton("Send to ALU");
		GridBagConstraints gbc_btnSendToAlu = new GridBagConstraints();
		gbc_btnSendToAlu.insets = new Insets(0, 0, 0, 5);
		gbc_btnSendToAlu.gridx = 1;
		gbc_btnSendToAlu.gridy = 11;
		gbc_btnSendToAlu.gridwidth = 4;
		add(btnSendToAlu, gbc_btnSendToAlu);
		btnSendToAlu.addActionListener(e -> sendToRemote());

		JButton btnInitRegs = new JButton("Init regs");
		GridBagConstraints gbc_btnInitRegs = new GridBagConstraints();
		gbc_btnInitRegs.insets = new Insets(0, 0, 0, 5);
		gbc_btnInitRegs.gridx = 1;
		gbc_btnInitRegs.gridy = 12;
		gbc_btnInitRegs.gridwidth = 4;
		add(btnInitRegs, gbc_btnInitRegs);
		btnInitRegs.addActionListener(e -> m_serialApi.registersInit());

		JButton btnZeroRegs = new JButton("Zero regs");
		GridBagConstraints gbc_btnZeroRegs = new GridBagConstraints();
		gbc_btnZeroRegs.insets = new Insets(0, 0, 0, 5);
		gbc_btnZeroRegs.gridx = 1;
		gbc_btnZeroRegs.gridy = 13;
		gbc_btnZeroRegs.gridwidth = 4;
		add(btnZeroRegs, gbc_btnZeroRegs);
		btnZeroRegs.addActionListener(e -> m_serialApi.registersZero());
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
		textField.setColumns(4);
		textField.setDocument(new JHexFieldDocument(4));
	}

	public void readFromRemote() {
		int[] registers = m_serialApi.getRegisters();
		for(int i = 0; i < 16; i++) {
			String s = Integer.toHexString(registers[i]);
			String res = "0000".substring(s.length()) + s.toUpperCase();
			m_registerOut[i].setText(res);
		}
	}

	public void sendToRemote() {
		int[] regs = new int[16];

		for(int i = 0; i < 16; i++) {
			String str = m_registerOut[i].getText();
			if(str.isBlank()) {
				regs[i] = 0;
			} else {
				regs[i] = Integer.parseInt(str, 16);
			}
		}
		m_serialApi.writeRegisters(regs);
	}

}
