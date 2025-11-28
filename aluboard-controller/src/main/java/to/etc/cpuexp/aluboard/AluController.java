package to.etc.cpuexp.aluboard;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.Arrays;
import java.util.stream.Collectors;

final public class AluController {
	@Option(name = "-serial", aliases = {"-s"})
	private String m_serialPort = "/dev/ttyUSB0";

	@Option(name = "-bitrate", aliases = {"-b"})
	private int m_bitRate = 19200;

	private SerialPort m_port;

	static public void main(String[] args) throws Exception {
		new AluController().run(args);
	}

	private void run(String[] args) throws Exception {
		CmdLineParser p = new CmdLineParser(this);
		try {
			//-- Decode the tasks's arguments
			p.parseArgument(args);
		} catch(CmdLineException x) {
			System.err.println("Invalid arguments: " + x.getMessage());
			p.printUsage(System.err);
			System.exit(10);
		}

		SerialPort port = m_port = open();

		SerialApi api = new SerialApi(port);

		new MainWindow(api);
	}

	/**
	 * Open serial port for use.
	 */
	private SerialPort open() throws Exception {
		try {
			SerialPort port = SerialPort.getCommPort(m_serialPort);
			port.setBaudRate(m_bitRate);
			port.setNumStopBits(1);
			port.setNumDataBits(8);
			port.setParity(SerialPort.NO_PARITY);
			port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
			port.clearDTR();
			port.clearRTS();
			//port.setFlowControl(SerialPort.FLOW_CONTROL_CTS_ENABLED | SerialPort.FLOW_CONTROL_RTS_ENABLED);
			port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 10_000, 0);
			//port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);
			port.openPort();
			return port;
		} catch(SerialPortInvalidPortException spx) {
			String ports = Arrays.stream(SerialPort.getCommPorts())
				.map(a -> "- " + a.getSystemPortName() + " " + a.getDescriptivePortName() + "\n")
				.collect(Collectors.joining());

			throw new MessageException(spx.getMessage() + "\nAvailable ports are: " + ports);
		}
	}

}
