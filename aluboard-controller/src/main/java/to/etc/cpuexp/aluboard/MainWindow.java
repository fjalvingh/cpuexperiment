package to.etc.cpuexp.aluboard;

import to.etc.cpuexp.aluboard.ui.RegisterPane;

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

		RegisterPane rp = new RegisterPane(m_api);
		getContentPane().add(rp);



		//JDisasmPanel dp = new JDisasmPanel(m_source, infoModel, new PdpDisassembler());
		//dp.setSize(1024, 8192);
		//JScrollPane sp = new JScrollPane(dp);
		//getContentPane().add(sp);
		pack();
		setVisible(true);

		rp.readFromRemote();
	}


}
