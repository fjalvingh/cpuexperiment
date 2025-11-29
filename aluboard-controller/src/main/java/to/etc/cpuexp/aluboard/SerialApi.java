package to.etc.cpuexp.aluboard;

import com.fazecast.jSerialComm.SerialPort;

import java.io.ByteArrayOutputStream;

final public class SerialApi {
	private final SerialPort m_port;

	private ByteArrayOutputStream m_pb;

	/** Response code that indicates an error */
	static private final int CMD_ERROR = 0x01;

	/** Get all register value */
	static private final int CMD_REGISTERS = 0x02;

	static private final int CMD_SETREGS = 0x03;

	static private final int CMD_ALUOPERATION = 0x04;

	private int m_packetLen;

	private int m_lastCommand;

	public SerialApi(SerialPort port) {
		m_port = port;
	}

	/**
	 * Tell the Arduino to read all registers and send their content.
	 */
	public int[] getRegisters() {
		startPacket(CMD_REGISTERS);
		transactPacket();
		int[] regs = new int[16];
		for(int i = 0; i < regs.length; i++) {
			regs[i] = readWord();
		}
		return regs;
	}

	public void writeRegisters(int[] registers) {
		startPacket(CMD_SETREGS);
		for(int i = 0; i < registers.length; i++) {
			writeWord(registers[i]);
		}
		transactPacket();								// The command has no data in the response
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Exchanging packets											*/
	/*----------------------------------------------------------------------*/

	/**
	 * Send a command, then wait for the result. Return the result packet.
	 */
	private void transactPacket() {
		byte[] packet = finishPacket();

		int tries = 0;
		long ets = System.currentTimeMillis() + RETRY_TIMEOUT;
		RuntimeException lastException = null;
		for(; ; ) {
			sendPacket(packet);

			try {
				receivePacket();

				//-- Read the command response code from the packet
				int responseCode = readWord();
				if(responseCode == CMD_ERROR) {
					String message = readString();
					throw new RemoteErrorException(message);
				} else if(responseCode != m_lastCommand) {
					throw new MessageException("Out-of-sync reply: received response code " + responseCode + " for command " + m_lastCommand);
				}
				return;
			} catch(RemoteErrorException e) {
				System.out.println("Remote error: " + e.getMessage());
				lastException = e;
			} catch(RuntimeException x) {
				System.out.println("SCC: " + x.toString());
				lastException = x;
			}

			if(tries++ > 5 || System.currentTimeMillis() > ets) {
				throw lastException;
			}
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {
				//-- Idiots.
			}
			System.out.println("Retrying transact packet: " + lastException.getMessage());
		}
	}

	private int readByte() {
		if(m_rdBufIndex >= m_rdBufLen) {
			throw new RxDataException("Read past end of packet");
		}
		int val = m_rdbuf[m_rdBufIndex++] & 0xff;
		return val;
	}

	private int readWord() {
		int high = readByte();
		int low = readByte();
		return high << 8 | low;
	}

	private String readString() {
		StringBuilder sb = new StringBuilder();
		while(m_rdBufIndex < m_rdBufLen) {
			int c = m_rdbuf[m_rdBufIndex++] & 0xff;
			if((c >= 0x20 && c < 0x80) || c == '\n') {
				sb.append((char) c);
			}
		}
		return sb.toString();
	}

	private byte[] m_rdbuf = new byte[256 + 4];

	private int m_rdBufIndex;

	private int m_rdBufLen;

	static private final long PACKET_READ_TIMEOUT = 5 * 1000L;

	static private final long RETRY_TIMEOUT = 30 * 1000L;

	private void receivePacket() {
		long ets = System.currentTimeMillis() + PACKET_READ_TIMEOUT;

		//-- Wait for the 0xaa 0x55
		int phase = 0;
		for(; ; ) {
			int rl = m_port.readBytes(m_rdbuf, 1, 0);
			if(rl == 1) {
				System.out.println("BYTE: " + (char) m_rdbuf[0] + " " + m_rdbuf[0]);
				if((m_rdbuf[0] & 0xff) == 0xaa) {
					phase = 1;
				} else if(m_rdbuf[0] == 0x55 && phase == 1) {
					break;
				}
			}

			if(System.currentTimeMillis() > ets) {
				throw new MessageException("Read timed out during header prefix read");
			}
		}

		//-- Prefix found. Read length
		int rl = m_port.readBytes(m_rdbuf, 1, 0);
		if(rl != 1)
			throw new MessageException("Length not received");
		int len = m_rdbuf[0] & 0xff;

		int offset = 0;
		int todo = len + 2;

		while(todo > 0) {
			rl = m_port.readBytes(m_rdbuf, todo, offset);
			if(rl < 0)
				throw new MessageException("eof during serial read");
			todo -= rl;
			offset += rl;
		}

		//-- Checksum the packet
		int sum = 0;
		for(int i = 0; i < len; i++) {
			sum += m_rdbuf[i] & 0xff;
		}
		int hilen = m_rdbuf[len] & 0xff;
		int lolen = m_rdbuf[len + 1] & 0xff;

		if(hilen != ((sum >> 8) & 0xff) || lolen != (sum & 0xff)) {
			throw new MessageException("Checksum error");
		}

		m_rdBufLen = len;
		m_rdBufIndex = 0;
	}


	/**
	 * Send the packet over the serial link.
	 */
	private void sendPacket(byte[] packet) {
		int rc = m_port.writeBytes(packet, packet.length);
		if(rc != packet.length) {
			throw new MessageException("Packet write error: wrote " + rc + " but expected " + packet.length);
		}
	}



	/*----------------------------------------------------------------------*/
	/*	CODING:	Primitive packet construction								*/
	/*----------------------------------------------------------------------*/

	private void startPacket(int command) {
		m_lastCommand = command;
		writeWord(command);
	}

	private void writeWord(int b) {
		ByteArrayOutputStream pb = pb();
		pb.write(b >> 8);
		pb.write(b & 0xff);
		m_packetLen += 2;
	}

	private void writeByte(int b) {
		pb().write(b);
		m_packetLen++;
	}

	/*
	 * Packet format:
	 * [aa 55] [len] [hi command] [lo command] [parameters]* [chksum hi] [chksum lo]
	 */
	private ByteArrayOutputStream pb() {
		ByteArrayOutputStream pb = m_pb;
		if(null == pb) {
			m_packetLen = 0;
			pb = m_pb = new ByteArrayOutputStream();
			pb.write(0xaa);
			pb.write(0x55);
			pb.write(0);                                    // Placeholder for length
		}
		return pb;
	}

	private byte[] finishPacket() {
		ByteArrayOutputStream pb = m_pb;
		if(null == pb) {
			throw new IllegalStateException("No packet in progress");
		}

		//-- Add two bytes for the checksum
		pb.write(0);
		pb.write(0);
		byte[] packet = pb.toByteArray();
		m_pb = null;

		//-- Calculate a checksum
		int sum = 0;
		for(int i = 3; i < packet.length; i++) {
			sum += packet[i];
		}
		packet[packet.length - 2] = (byte) ((sum >> 8) & 0xff);
		packet[packet.length - 1] = (byte) (sum & 0xff);
		packet[2] = (byte) m_packetLen;
		return packet;
	}


}
