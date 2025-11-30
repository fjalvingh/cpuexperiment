package to.etc.cpuexp.aluboard.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AluActionPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public AluActionPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{45, 68, 72, 67, 0};
		gridBagLayout.rowHeights = new int[]{207, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel lblSrc = new JLabel("Src");
		panel.add(lblSrc);

		JRadioButton rdbtnNewRadioButton = new JRadioButton("AQ");
		panel.add(rdbtnNewRadioButton);

		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("AB");
		panel.add(rdbtnNewRadioButton_1);

		JRadioButton rb0Q = new JRadioButton("0Q");
		panel.add(rb0Q);

		JRadioButton rb0B = new JRadioButton("0B");
		panel.add(rb0B);

		JRadioButton rb0A = new JRadioButton("0A");
		panel.add(rb0A);

		JRadioButton rbDA = new JRadioButton("DA");
		panel.add(rbDA);

		JRadioButton rbDQ = new JRadioButton("DQ");
		panel.add(rbDQ);

		JRadioButton rbD0 = new JRadioButton("D0");
		panel.add(rbD0);


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


		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.anchor = GridBagConstraints.WEST;
		gbc_panel_1.gridwidth = 4;
		gbc_panel_1.fill = GridBagConstraints.VERTICAL;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		add(panel_1, gbc_panel_1);

		JLabel lblPorta = new JLabel("PortA");
		panel_1.add(lblPorta);

		//-- Terrible.
		Integer[] regs = new Integer[16];
		for(int i = 0; i < 16; i++)
			regs[i] = i;

//		ComboBoxModel<Integer> regModel = new DefaultComboBoxModel<>(regs);
		JComboBox<RegName> portAC = new JComboBox<>();
		panel_1.add(portAC);
		portAC.setModel(new DefaultComboBoxModel<>(RegName.values()));

		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.gridwidth = 4;
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 2;
		add(panel_2, gbc_panel_2);

		JLabel lblPortb = new JLabel("PortB");
		panel_2.add(lblPortb);

		JComboBox<RegName> portBC = new JComboBox<>();
		panel_2.add(portBC);
		portBC.setModel(new DefaultComboBoxModel<>(RegName.values()));
		
		JButton btnExecute = new JButton("Execute");
		btnExecute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbc_btnExecute = new GridBagConstraints();
		gbc_btnExecute.gridwidth = 4;
		gbc_btnExecute.insets = new Insets(0, 0, 0, 5);
		gbc_btnExecute.gridx = 0;
		gbc_btnExecute.gridy = 3;
		add(btnExecute, gbc_btnExecute);

	}

}
