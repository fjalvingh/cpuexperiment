package to.etc.cpuexp.aluboard.ui;

import to.etc.cpuexp.aluboard.SerialApi;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final SerialApi m_api;

	private final RegisterPane m_registerPane;

	private final AluActionPane m_actionPane;

	/**
	 * Create the panel.
	 */
	public MainPanel(SerialApi api) {
		m_api = api;

		GridBagLayout gridBagLayout = new GridBagLayout();
		//gridBagLayout.columnWidths = new int[]{196, 267, 0};
		//gridBagLayout.rowHeights = new int[]{360, 0};
		//gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		//gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		RegisterPane rp = new RegisterPane(m_api);
		m_registerPane = rp;
		GridBagConstraints gbc_rp = new GridBagConstraints();
		gbc_rp.anchor = GridBagConstraints.NORTHWEST;
		gbc_rp.insets = new Insets(0, 0, 0, 5);
		gbc_rp.gridx = 0;
		gbc_rp.gridy = 0;
		add(rp, gbc_rp);

		AluActionPane ap = new AluActionPane(m_api);
		m_actionPane = ap;
		GridBagConstraints gbc_ap = new GridBagConstraints();
		gbc_ap.anchor = GridBagConstraints.NORTHWEST;
		gbc_ap.gridx = 1;
		gbc_ap.gridy = 0;
		add(ap, gbc_ap);
	}

	public RegisterPane getRegisterPane() {
		return m_registerPane;
	}

	public AluActionPane getActionPane() {
		return m_actionPane;
	}
}
