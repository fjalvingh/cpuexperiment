package to.etc.cpuexp.aluboard;

import to.etc.cpuexp.aluboard.ui.MainPanel;

import javax.swing.*;

final public class MainWindow extends JFrame {

	private final SerialApi m_api;

	public MainWindow(SerialApi api) throws Exception {
		m_api = api;
		setTitle("AluController");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		//JButton button = new JButton("Click me");
		//getContentPane().add(button);
		setSize(1024, 768);

		MainPanel mainPanel = new MainPanel(m_api);
		getContentPane().add(mainPanel);

		//JDisasmPanel dp = new JDisasmPanel(m_source, infoModel, new PdpDisassembler());
		//dp.setSize(1024, 8192);
		//JScrollPane sp = new JScrollPane(dp);
		//getContentPane().add(sp);
		pack();
		setVisible(true);

		mainPanel.getRegisterPane().readFromRemote();
	}


}
