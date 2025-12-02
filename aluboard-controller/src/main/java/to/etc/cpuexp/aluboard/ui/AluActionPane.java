package to.etc.cpuexp.aluboard.ui;

import to.etc.cpuexp.aluboard.SerialApi;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class AluActionPane extends JPanel {
	private static final long serialVersionUID = 1L;

	private final SerialApi m_api;

	private List<JRadioButton> m_srcButtonList = new ArrayList<>();

	private List<JRadioButton> m_fnButtonList = new ArrayList<>();

	private List<JRadioButton> m_dstButtonList = new ArrayList<>();

	private List<JRadioButton> m_szButtonList = new ArrayList<>();

	private List<JRadioButton> m_carryButtonList = new ArrayList<>();

	private JCheckBox m_carryCB;

	/**
	 * Create the panel.
	 */
	public AluActionPane(SerialApi api) {
		m_api = api;

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{45, 68, 72, 67, 0};
		gridBagLayout.rowHeights = new int[]{207, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		createSrcPanel();

		createFunctionPanel();

		//-- Destination
		createDestPanel();

		createOpSizePanel();

		createCarryPanel();

		//-- Port selection A panel
		JPanel portAPanel = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.anchor = GridBagConstraints.WEST;
		gbc_panel_1.gridwidth = 2;
		gbc_panel_1.fill = GridBagConstraints.VERTICAL;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		add(portAPanel, gbc_panel_1);

		JLabel lblPorta = new JLabel("PortA");
		portAPanel.add(lblPorta);

		////-- Terrible.
		//Integer[] regs = new Integer[16];
		//for(int i = 0; i < 16; i++)
		//	regs[i] = i;
		//
//		ComboBoxModel<Integer> regModel = new DefaultComboBoxModel<>(regs);
		JComboBox<RegName> portAC = new JComboBox<>();
		portAPanel.add(portAC);
		portAC.setModel(new DefaultComboBoxModel<>(RegName.values()));
		portAC.setSelectedIndex(0);

		JPanel portBPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) portBPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.gridwidth = 2;
		gbc_panel_2.insets = new Insets(0, 0, 0, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 2;
		add(portBPanel, gbc_panel_2);

		JLabel lblPortb = new JLabel("PortB");
		portBPanel.add(lblPortb);

		JComboBox<RegName> portBC = new JComboBox<>();
		portBPanel.add(portBC);
		portBC.setModel(new DefaultComboBoxModel<>(RegName.values()));
		portBC.setSelectedIndex(1);

		JPanel execPanel = new JPanel();
		GridBagConstraints gbc_execPanel = new GridBagConstraints();
		gbc_execPanel.gridwidth = 2;
		gbc_execPanel.insets = new Insets(0, 0, 0, 5);
		gbc_execPanel.fill = GridBagConstraints.BOTH;
		gbc_execPanel.gridx = 2;
		gbc_execPanel.gridy = 1;
		add(execPanel, gbc_execPanel);

		JButton btnExecute = new JButton("Execute");
		btnExecute.addActionListener(e -> runAction(portAC.getSelectedIndex(), portBC.getSelectedIndex()));
		//GridBagConstraints gbc_btnExecute = new GridBagConstraints();
		//gbc_btnExecute.gridwidth = 4;
		//gbc_btnExecute.insets = new Insets(0, 0, 0, 5);
		//gbc_btnExecute.gridx = 0;
		//gbc_btnExecute.gridy = 3;
		execPanel.add(btnExecute);
	}

	private void createCarryPanel() {
		JPanel carryPanel = new JPanel();
		GridBagConstraints gbc_cPanel = new GridBagConstraints();
		gbc_cPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_cPanel.insets = new Insets(0, 0, 5, 5);
		gbc_cPanel.gridx = 4;
		gbc_cPanel.gridy = 0;
		add(carryPanel, gbc_cPanel);
		carryPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel carryLbl = new JLabel("CarrySel");
		carryPanel.add(carryLbl);

		//Alu16,                  // 16 bit carry from ALU
		//	ARam0,                  // Carry from lhs shift on register
		//	AQ0,                    // Carry from low shift on Q register
		//	ARam0m,                 // Carry from shift on 8 bit middle of register
		//	AQ0m,                   // Carry from shift on middle of Q register
		//	ARam3,                  // Carry from high shift register
		//	AQ3,                    // Carry from high Q
		//	Alu8,                   // Alu carry from lower 8 bits

		//-- A shitload of work for a list of buttons, sigh. Swing sucks.
		JRadioButton rb0 = new JRadioButton("Alu16");
		carryPanel.add(rb0);

		JRadioButton rb1 = new JRadioButton("ARam0");
		carryPanel.add(rb1);

		JRadioButton rb2 = new JRadioButton("AQ0");
		carryPanel.add(rb2);

		JRadioButton rb3 = new JRadioButton("ARam0m");
		carryPanel.add(rb3);

		JRadioButton rb4 = new JRadioButton("AQ0m");
		carryPanel.add(rb4);

		JRadioButton rb5 = new JRadioButton("ARam3");
		carryPanel.add(rb5);

		JRadioButton rb6 = new JRadioButton("AQ3");
		carryPanel.add(rb6);

		JRadioButton rb7 = new JRadioButton("AluB");
		carryPanel.add(rb7);

		m_carryButtonList.add(rb0);
		m_carryButtonList.add(rb1);
		m_carryButtonList.add(rb2);
		m_carryButtonList.add(rb3);
		m_carryButtonList.add(rb4);
		m_carryButtonList.add(rb5);
		m_carryButtonList.add(rb6);
		m_carryButtonList.add(rb7);
		ButtonGroup carryGroup = new ButtonGroup();
		m_carryButtonList.forEach(rb -> carryGroup.add(rb));

		//-- Add a carry flag input indicator
		JCheckBox cb = m_carryCB = new JCheckBox("Carry-in");
		carryPanel.add(cb);

		rb0.setSelected(true);

	}

	private void createSrcPanel() {
		JPanel srcPanel = new JPanel();
		GridBagConstraints gbc_srcPanel = new GridBagConstraints();
		gbc_srcPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_srcPanel.insets = new Insets(0, 0, 5, 5);
		gbc_srcPanel.gridx = 0;
		gbc_srcPanel.gridy = 0;
		add(srcPanel, gbc_srcPanel);
		srcPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel lblSrc = new JLabel("Src");
		srcPanel.add(lblSrc);

		//-- A shitload of work for a list of buttons, sigh. Swing sucks.
		JRadioButton rbAQ = new JRadioButton("AQ");
		srcPanel.add(rbAQ);

		JRadioButton rbAB = new JRadioButton("AB");
		srcPanel.add(rbAB);

		JRadioButton rb0Q = new JRadioButton("0Q");
		srcPanel.add(rb0Q);

		JRadioButton rb0B = new JRadioButton("0B");
		srcPanel.add(rb0B);

		JRadioButton rb0A = new JRadioButton("0A");
		srcPanel.add(rb0A);

		JRadioButton rbDA = new JRadioButton("DA");
		srcPanel.add(rbDA);

		JRadioButton rbDQ = new JRadioButton("DQ");
		srcPanel.add(rbDQ);

		JRadioButton rbD0 = new JRadioButton("D0");
		srcPanel.add(rbD0);

		m_srcButtonList.add(rbAQ);
		m_srcButtonList.add(rbAB);
		m_srcButtonList.add(rb0Q);
		m_srcButtonList.add(rb0B);
		m_srcButtonList.add(rb0A);
		m_srcButtonList.add(rbDA);
		m_srcButtonList.add(rbDQ);
		m_srcButtonList.add(rbD0);
		ButtonGroup srcGroup = new ButtonGroup();
		m_srcButtonList.forEach(rb -> srcGroup.add(rb));

		rbAB.setSelected(true);
		//
		//Enumeration<AbstractButton> srcElements = srcGroup.getElements();
		//int index = 0;
		//while(srcElements.hasMoreElements()) {
		//	AbstractButton crap = srcElements.nextElement();
		//	crap.setActionCommand(Integer.toString(index++));        // What an incredible piece of crap
		//}
	}

	private void createFunctionPanel() {
		int index;
		JPanel fnPanel = new JPanel();
		GridBagConstraints gbc_fnPanel = new GridBagConstraints();
		gbc_fnPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_fnPanel.insets = new Insets(0, 0, 5, 5);
		gbc_fnPanel.gridx = 1;
		gbc_fnPanel.gridy = 0;
		add(fnPanel, gbc_fnPanel);
		fnPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel fnLbl = new JLabel("Function");
		fnPanel.add(fnLbl);
		JRadioButton fnAdd = new JRadioButton("Add");
		fnPanel.add(fnAdd);
		JRadioButton fnSubR = new JRadioButton("SubR");
		fnPanel.add(fnSubR);
		JRadioButton fnSubS = new JRadioButton("SubS");
		fnPanel.add(fnSubS);
		JRadioButton fnOr = new JRadioButton("Or");
		fnPanel.add(fnOr);
		JRadioButton fnAnd = new JRadioButton("And");
		fnPanel.add(fnAnd);
		JRadioButton fnNotRS = new JRadioButton("NotRS");
		fnPanel.add(fnNotRS);
		JRadioButton fnExOr = new JRadioButton("ExOr");
		fnPanel.add(fnExOr);
		JRadioButton fnExNor = new JRadioButton("ExNor");
		fnPanel.add(fnExNor);

		m_fnButtonList.add(fnAdd);
		m_fnButtonList.add(fnSubR);
		m_fnButtonList.add(fnSubS);
		m_fnButtonList.add(fnOr);
		m_fnButtonList.add(fnAnd);
		m_fnButtonList.add(fnNotRS);
		m_fnButtonList.add(fnExOr);
		m_fnButtonList.add(fnExNor);
		ButtonGroup fnGroup = new ButtonGroup();
		m_fnButtonList.forEach(rb -> fnGroup.add(rb));

		fnAdd.setSelected(true);
		//
		//Enumeration<AbstractButton> fnElements = srcGroup.getElements();
		//index = 0;
		//while(fnElements.hasMoreElements()) {
		//	AbstractButton crap = fnElements.nextElement();
		//	crap.setActionCommand(Integer.toString(index++));        // What an incredible piece of crap
		//}
	}

	private void createDestPanel() {
		JPanel dstPanel = new JPanel();
		GridBagConstraints gbc_dstPanel = new GridBagConstraints();
		gbc_dstPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_dstPanel.insets = new Insets(0, 0, 5, 5);
		gbc_dstPanel.gridx = 2;
		gbc_dstPanel.gridy = 0;
		add(dstPanel, gbc_dstPanel);
		dstPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel dstLbl = new JLabel("Dest");
		dstPanel.add(dstLbl);
		JRadioButton dstQReg = new JRadioButton("Q");
		dstPanel.add(dstQReg);
		JRadioButton dstNop = new JRadioButton("nop");
		dstPanel.add(dstNop);
		JRadioButton dstRamA = new JRadioButton("ramA");
		dstPanel.add(dstRamA);
		JRadioButton dstRamF = new JRadioButton("ramF");
		dstPanel.add(dstRamF);
		JRadioButton dstRamQD = new JRadioButton("ramQD");
		dstPanel.add(dstRamQD);
		JRadioButton dstRamD = new JRadioButton("ramD");
		dstPanel.add(dstRamD);
		JRadioButton dstRamQU = new JRadioButton("ramQU");
		dstPanel.add(dstRamQU);
		JRadioButton dstRamU = new JRadioButton("ramU");
		dstPanel.add(dstRamU);

		m_dstButtonList.add(dstQReg);
		m_dstButtonList.add(dstNop);
		m_dstButtonList.add(dstRamA);
		m_dstButtonList.add(dstRamF);
		m_dstButtonList.add(dstRamQD);
		m_dstButtonList.add(dstRamD);
		m_dstButtonList.add(dstRamQU);
		m_dstButtonList.add(dstRamU);
		ButtonGroup dstGroup = new ButtonGroup();
		m_dstButtonList.forEach(a -> {
			dstGroup.add(a);
		});

		dstRamF.setSelected(true);
		//
		//Enumeration<AbstractButton> dstElements = srcGroup.getElements();
		//int index = 0;
		//while(dstElements.hasMoreElements()) {
		//	AbstractButton crap = dstElements.nextElement();
		//	crap.setActionCommand(Integer.toString(index++));        // What an incredible piece of crap
		//}
	}

	private void createOpSizePanel() {
		//-- Op size panel
		JPanel szPanel = new JPanel();
		GridBagConstraints gbc_szPanel = new GridBagConstraints();
		gbc_szPanel.insets = new Insets(0, 0, 5, 0);
		gbc_szPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_szPanel.gridx = 3;
		gbc_szPanel.gridy = 0;
		add(szPanel, gbc_szPanel);
		GridBagLayout gbl_szPanel = new GridBagLayout();
		gbl_szPanel.columnWidths = new int[]{67, 0};
		gbl_szPanel.rowHeights = new int[]{23, 23, 23, 23};
		gbl_szPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_szPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0};
		szPanel.setLayout(gbl_szPanel);

		JLabel szLbl = new JLabel("OpSize");
		GridBagConstraints gbc_szLbl = new GridBagConstraints();
		gbc_szLbl.fill = GridBagConstraints.BOTH;
		gbc_szLbl.insets = new Insets(0, 0, 5, 0);
		gbc_szLbl.gridx = 0;
		gbc_szLbl.gridy = 0;
		szPanel.add(szLbl, gbc_szLbl);
		JRadioButton sz16 = new JRadioButton("16bit");
		GridBagConstraints gbc_sz16 = new GridBagConstraints();
		gbc_sz16.fill = GridBagConstraints.BOTH;
		gbc_sz16.insets = new Insets(0, 0, 5, 0);
		gbc_sz16.gridx = 0;
		gbc_sz16.gridy = 1;
		szPanel.add(sz16, gbc_sz16);
		JRadioButton sz8l = new JRadioButton("8bit-L");
		GridBagConstraints gbc_sz8l = new GridBagConstraints();
		gbc_sz8l.fill = GridBagConstraints.BOTH;
		gbc_sz8l.insets = new Insets(0, 0, 5, 0);
		gbc_sz8l.gridx = 0;
		gbc_sz8l.gridy = 2;
		szPanel.add(sz8l, gbc_sz8l);

		JRadioButton sz8h = new JRadioButton("8bit-H");
		GridBagConstraints gbc_sz8h = new GridBagConstraints();
		gbc_sz8h.fill = GridBagConstraints.BOTH;
		gbc_sz8h.gridx = 0;
		gbc_sz8h.gridy = 3;
		szPanel.add(sz8h, gbc_sz8h);

		m_szButtonList.add(sz16);
		m_szButtonList.add(sz8l);
		m_szButtonList.add(sz8h);
		ButtonGroup szGroup = new ButtonGroup();
		m_szButtonList.forEach(e -> szGroup.add(e));
		sz16.setSelected(true);

		ButtonGroup srcGroup = new ButtonGroup();
		Enumeration<AbstractButton> szElements = srcGroup.getElements();
		int index = 0;
		while(szElements.hasMoreElements()) {
			AbstractButton crap = szElements.nextElement();
			crap.setActionCommand(Integer.toString(index++));        // What an incredible piece of crap
		}
	}

	private void runAction(int portA, int portB) {
		int srcCode = findSelectedIndex(m_srcButtonList);
		int dstCode = findSelectedIndex(m_dstButtonList);
		int fnCode = findSelectedIndex(m_fnButtonList);
		int szCode = findSelectedIndex(m_szButtonList);
		int carrySelCode = findSelectedIndex(m_carryButtonList);

		if(srcCode == -1 || dstCode == -1 || fnCode == -1 || szCode == -1 || carrySelCode == -1) {
			JOptionPane.showMessageDialog(this, "Some value is not selected");
			return;
		}

		m_api.aluAction(portA, portB, srcCode, fnCode, dstCode, szCode, carrySelCode, m_carryCB.isSelected() ? 1 : 0);
	}

	private int findSelectedIndex(List<JRadioButton> list) {
		for(int i = 0; i < list.size(); i++) {
			JRadioButton btn = list.get(i);
			if(btn.isSelected()) {
				return i;
			}
		}
		return -1;
	}

}
