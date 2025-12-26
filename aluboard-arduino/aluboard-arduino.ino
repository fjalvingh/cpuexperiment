#include <stdarg.h>

/********************************************************/
/*  ALU Board connections to Arduino                    */
/********************************************************/

#define ARDUINO_A0  54        // Pin number to use A0 as digital port

#define AD0         22              // AD0..15 ALU input/result

#define CTL_CA0     (AD0 + 16)      // Start of CA0 assignments (38)
#define CTL_CB0     (CTL_CA0 + 4)   // (42)
#define CTL_CI0     (CTL_CB0 + 4)   // Control signals (46) 

#define CTL_OE      (ARDUINO_A0 + 1)     // OE pin is on analog 1. Analog 0 is CTL.CI8, a control signal for the alu.

#define CTL_CLKAH   (ARDUINO_A0 + 2)
#define CTL_CLKAL   (ARDUINO_A0 + 3)

#define CTL_OP16    (ARDUINO_A0 + 4)
#define CTL_OP8H    (ARDUINO_A0 + 5)
#define CTL_OP8L    (ARDUINO_A0 + 6)

#define ACARRYIN    (ARDUINO_A0 + 7)      // ALU Carry in
#define FLAGSLATCH  (ARDUINO_A0 + 8)      // Flags register latch

#define CARRYSEL2   (ARDUINO_A0 + 9)      // Carry flag source. Watch out: reversed!
#define CARRYSEL1   (ARDUINO_A0 + 10)
#define CARRYSEL0   (ARDUINO_A0 + 11)

#define FLGCARRYL   (ARDUINO_A0 + 12)     // Carry flag, L active
#define FLGNEGL     (ARDUINO_A0 + 13)
#define FLGOVFL     (ARDUINO_A0 + 14)
#define FLGZEROL    (ARDUINO_A0 + 15)

#define CLKN        HIGH
#define CLKA        LOW

/********************************************************/
/*  Mode enums for both am2901 and ALU board.           */
/********************************************************/


enum CarrySel {
  Alu16,                  // 16 bit carry from ALU
  ARam0,                  // Carry from lhs shift on register
  AQ0,                    // Carry from low shift on Q register
  ARam0m,                 // Carry from shift on 8 bit middle of register
  AQ0m,                   // Carry from shift on middle of Q register
  ARam3,                  // Carry from high shift register
  AQ3,                    // Carry from high Q
  Alu8,                   // Alu carry from lower 8 bits
};

enum AluSource {
  SrcAQ,
  SrcAB,
  Src0Q,
  Src0B,
  Src0A,
  SrcDA,
  SrcDQ,
  SrcD0,
};

enum AluFunction {
  FnAdd,
  FnSubR,
  FnSubS,
  FnOr,
  FnAnd,
  FnNotRS,
  FnExOr,
  FnExNor,
};

enum AluDest {
  DstQReg,
  DstNop,
  DstRamA,
  DstRamF,
  DstRamQD,
  DstRamD,
  DstRamQU,
  DstRamU,
};

enum AluOpType {
  Op16,
  Op8L,
  Op8H
};

enum FlagsLatch {
  /** Keep values inside the latch, do not follow inputs */
  FlagLatchHold,
  /** Let input values flow to outputs */
  FlagLatchOpen             // Man, C is a shit language.
};

/**
 * Set pinMode for all AD lines to in- or out
 */
void setADMode(int mode) {
  for(int i = 0; i < 16; i++) {
    pinMode(AD0 + i, mode);
  }
}

void bitsWrite(int startPin, int bitCount, int value) {
  for(int i = 0; i < bitCount; i++) {
    if(value & (1 << i)) {
      digitalWrite(startPin + i, HIGH);
    } else {
      digitalWrite(startPin + i, LOW);
    }
  }
}

/**
 * Read data from AD0..AD15.
 */
int readData() {
  setADMode(INPUT);
  
  int result = 0;
  for(int i = 0; i < 16; i++) {
    int val = digitalRead(AD0 + i);
    if(val != 0) {
        result |= 1 << i;
    }
  }
  return result;
}

/**
 * Write data to AD0..15. The data stays after the call. The call ensures that OE is HIGH (negated).
 */
void writeData(int value) {
  digitalWrite(CTL_OE, HIGH);             // Make sure OE is negated
  setADMode(OUTPUT);
  bitsWrite(AD0, 16, value);
}

/**
 * Set a value for CA0..3, selecting ALU register port A
 */
void setAluPortA(int value) {
  bitsWrite(CTL_CA0, 4, value);
}
void setAluPortB(int value) {
  bitsWrite(CTL_CB0, 4, value);
}
/**
 * Set the alu source code (i0..2) 
 */
void setAluSrc(AluSource code) {
  bitsWrite(CTL_CI0, 3, code);
}
void setAluFunction(AluFunction code) {
  bitsWrite(CTL_CI0 + 3, 3, code);
}
void setAluDest(AluDest code) {
  bitsWrite(CTL_CI0 + 6, 3, code);
}

/**
 * Pulse the CLOCK line.
 * Description:
 * - when CLK is HIGH the A and B latches are open, providing 
 *   data from the registers to the ALU as-is. When CLK is LOW
 *   the latches are holding the last data, and data is WRITTEN
 *   into the register designated by B IF a RAM destination is chosen.
 * - Data gets clocked in Q on the upward edge of the clock.
 */
void pulseOperation(AluOpType op, bool latchFlags) {
  switch(op) {
    case Op16:
      digitalWrite(CTL_OP16, HIGH);
      digitalWrite(CTL_OP8H, LOW);
      digitalWrite(CTL_OP8L, LOW);
      break;

    case Op8H:
      digitalWrite(CTL_OP16, LOW);
      digitalWrite(CTL_OP8H, HIGH);
      digitalWrite(CTL_OP8L, LOW);
      break;

    case Op8L:
      digitalWrite(CTL_OP16, LOW);
      digitalWrite(CTL_OP8H, LOW);
      digitalWrite(CTL_OP8L, HIGH);
      break;
  }
  //-- Now pulse the required clock lines
  if(op == Op16 || op == Op8L)
    digitalWrite(CTL_CLKAL, LOW);              

  if(op == Op16 || op == Op8H)
    digitalWrite(CTL_CLKAH, LOW);
  delay(1);

  //-- We latch the flags while the register data is latched, because that represents the
  //-- real state. Once the clock is high the data passes through, with the same operation
  //-- code, so in effect the data on the bus would be the result of the same operation as
  //-- before, a second time.
  if(latchFlags) {
    setAluLatch(FlagLatchOpen);
    delayMicroseconds(100);
    setAluLatch(FlagLatchHold);
  }
  
  digitalWrite(CTL_CLKAL, HIGH);                 //  Low: keep data in latches
  digitalWrite(CTL_CLKAH, HIGH);
  // digitalWrite(CTL_OP16, LOW);
  // digitalWrite(CTL_OP8H, LOW);
  // digitalWrite(CTL_OP8L, LOW);
}

void pulseClockLH() {
    digitalWrite(CTL_CLKAL, LOW);               // Write data to RAM if DEST is RAM
    digitalWrite(CTL_CLKAH, LOW);
    digitalWrite(CTL_CLKAL, HIGH);            
    digitalWrite(CTL_CLKAH, HIGH);
}

/**
 * Set Alu register 0..15 to the specified value
 */
void setAluReg(int reg, int value) {
  setAluPortB(reg);
  writeData(value);
  setAluSrc(SrcD0);                     // Read data from D bus and other part is zero
  setAluFunction(FnOr);                 // Add D plus 0
  setAluDest(DstRamF);                  // Alu result (F) to B
  
  //-- Pulse clock to LOW to enter data into the register.
  pulseClockLH();
  setAluDest(DstNop);
  // pulseOperation(Op16, false);
  // delay(2);
  setADMode(INPUT);
}

/**
 * Set the Q reg to the spec'd
 */
void setQReg(int value) {
  writeData(value);
  setAluSrc(SrcD0);                     // Read data from D bus and other part is zero
  setAluFunction(FnOr);                 // Add D plus 0
  setAluDest(DstQReg);                  // Alu result (F) to Q
  pulseOperation(Op16, false);
  setAluDest(DstNop);
  // delay(2);
  setADMode(INPUT);
}


void setOE(boolean on) {
  digitalWrite(CTL_OE, on ? LOW : HIGH);
}

void setCarrySel(CarrySel sel) {
  digitalWrite(CARRYSEL0, sel & 0x1 ? HIGH : LOW);
  digitalWrite(CARRYSEL1, sel & 0x2 ? HIGH : LOW);
  digitalWrite(CARRYSEL2, sel & 0x4 ? HIGH : LOW);
}

/**
*/
void setAluLatch(FlagsLatch val) {
  digitalWrite(FLAGSLATCH, val == FlagLatchOpen);   // Open is a 1 on that chip
}

void setAluCarryIn(boolean val) {
  digitalWrite(ACARRYIN, val);
}

/*
 * Read the flag register values as a bit pattern.
*/
int readFlags() {
  int carry = digitalRead(FLGCARRYL) == 0;
  int neg = digitalRead(FLGNEGL) == 0;
  int ovf = digitalRead(FLGOVFL) == 0;
  int zero = digitalRead(FLGZEROL) == 0;

  char buf[60];
  sprintf(buf, "z=%d,c=%d,n=%d,o=%d\n", zero, carry, neg, ovf);
  Serial.print(buf);

  return (zero << 3)
    | (ovf << 2)
    | (neg << 1)
    | carry;
}


/********************************************************/
/*  Serial command reader                               */
/********************************************************/

#define CMD_ERROR     0x01
#define CMD_REGISTERS 0x02
#define CMD_SETREGS   0x03
#define CMD_ALUOP     0x04
#define CMD_INITREGS  0x05
#define CMD_ZEROREGS  0x06

uint8_t readBuffer[259];
int readIndex;
int readLen;

/**
 * Read one byte from the packet.
*/
int rdByte() {
  if(readIndex >= readLen)
    return -1;
  return readBuffer[readIndex++];
}

int rdWord() {
  if(readIndex + 1 >= readLen)
    return -1;
  int hi = readBuffer[readIndex++];
  int lo = readBuffer[readIndex++];
  return hi << 8 | lo;
}

boolean rdEof() {
  return readIndex >= readLen;
}


/********************************************************/
/*  Packet writer                                       */
/********************************************************/
#define WRITEINDEX_MAX  (2 + 1 + 255)         // Max. write size: lead-in, length, 255 bytes rest

uint8_t writeBuffer[WRITEINDEX_MAX + 2];      // Plus 2 bytes for checksum
int writeIndex;


void pktStart(int command) {
  writeBuffer[0] = 0xaa;
  writeBuffer[1] = 0x55;
  writeBuffer[2] = 0;           // Len will be patched in later
  writeBuffer[3] = (command >> 8) & 0xff;
  writeBuffer[4] = (command & 0xff);
  writeIndex = 5;               // First packet data byte
}

void pktByte(int v) {
  if(writeIndex >= WRITEINDEX_MAX)
    return;
  writeBuffer[writeIndex++] = v;
}

void pktWord(int v) {
  if(writeIndex + 1 >= WRITEINDEX_MAX)
    return;
  writeBuffer[writeIndex++] = v >> 8;
  writeBuffer[writeIndex++] = v & 0xff;
}

void pktString(char* str) {
  while(writeIndex < WRITEINDEX_MAX && *str != 0) {
    writeBuffer[writeIndex++] = *str++;
  }
}

void pktFinish() {
  int len = writeIndex - 3;           // -3 = lead-in + len
  writeBuffer[2] = len;               // Put it where it belongs
  int sum = 0;
  for(int i = 3; i < writeIndex; i++) {
    sum += writeBuffer[i];
  }
  writeBuffer[writeIndex++] = sum >> 8;
  writeBuffer[writeIndex++] = sum & 0xff;
}

void pktSend() {
  pktFinish();
  Serial.write(writeBuffer, writeIndex);
}


/********************************************************/
/*  Command functions                                   */
/********************************************************/

void replyError(char* message, ...) {
  va_list args;
  va_start(args, message);
  char buf[256];

  vsprintf(buf, message, args);
  pktStart(CMD_ERROR);
  pktString(buf);
  pktSend();
}

/**
 * Output Packet format: just one word per register.
 */
void replyRegisters() {
  pktStart(CMD_REGISTERS);
  
  //-- Clock needs to be HIGH to allow the A-B latches to pass data
  digitalWrite(CTL_CLKAL, HIGH);
  digitalWrite(CTL_CLKAH, HIGH);

  setAluFunction(FnOr);
  setAluDest(DstNop);
  setAluSrc(Src0A);               // 0, A as sources
  setOE(true);

  //-- Read all ALU registers one by one and output on F (data)
  for(int i = 0; i < 16; i++) {
    setAluPortA(i);
    setAluPortB(i);
    pulseOperation(Op16, false);
    int rv = readData();
    // delay(100);
    pktWord(rv);
  }

  //-- Read the Q register
  setAluSrc(Src0Q);
  pulseOperation(Op16, false);
  int rv = readData();
  pktWord(rv);

  //-- And read the flags
  int f = readFlags();
  pktWord(f);

  pktSend();
}

void replyRegistersOld() {
  pktStart(CMD_REGISTERS);
  
  //-- Clock needs to be HIGH to allow the A-B latches to pass data
  digitalWrite(CTL_CLKAL, HIGH);
  digitalWrite(CTL_CLKAH, HIGH);

  setAluFunction(FnOr);
  setAluDest(DstNop);
  setAluSrc(Src0A);               // 0, A as sources
  setOE(true);

  //-- Read all ALU registers one by one and output on F (data)
  for(int i = 0; i < 16; i++) {
    setAluPortA(i);
    setAluPortB(i);
    pulseOperation(Op16, false);
    int rv = readData();
    // delay(100);
    pktWord(rv);
  }

  //-- Read the Q register
  setAluSrc(Src0Q);
  pulseOperation(Op16, false);
  int rv = readData();
  pktWord(rv);

  //-- And read the flags
  int f = readFlags();
  pktWord(f);

  pktSend();
}


/**
 * Input packet: one word per register.
 * Output packet: empty.
 */
void cmdSetRegisters() {
  for(int i = 0; i < 16; i++) {
    int regval = rdWord();
    setAluReg(i, regval);
  }
  int qval = rdWord();
  setQReg(qval);

  pktStart(CMD_SETREGS);
  pktSend();
}

/**
 * Input packet: [porta][portb][i0..8][opsz][carrysel][carryin]{[datain]}.
 * Output packet: empty
 */
void cmdAluOperation() {
  int porta = rdByte();
  int portb = rdByte();
  int ictl = rdWord();
  int opsz = rdByte();
  int carrysel = rdByte();
  int carryin = rdByte();
  boolean withData = !rdEof();
  int data = 0;
  if(withData)
    data = rdWord();

  // all of the packet done.
  setAluPortA(porta);
  setAluPortB(portb);

  int src = ictl & 0x7;
  setAluSrc(src);
  int fn = (ictl >> 3) & 0x7;
  setAluFunction(fn);
  int dst = (ictl >> 6) & 0x7;
  setAluDest(dst);
  setCarrySel(carrysel);
  setAluCarryIn(carryin);

  if(withData)
    writeData(data);
  pulseOperation(opsz, true);         // Pulse clocks, do the op, latch flags

  char buf[128];
  sprintf(buf, "aluop: a=%d,b=%d,src=%d,fn=%d,dst=%d,ictl=%x", porta, portb, src, fn, dst, ictl);
  Serial.println(buf);

  pktStart(CMD_ALUOP);
  pktSend();
}

void cmdInitRegs() {
  testSetRegisters();
  pktStart(CMD_INITREGS);
  pktSend();
}

void cmdZeroRegisters() {
  for(int i = 0; i < 16; i++) {
    setAluReg(i, 0);
  }
  setQReg(0);
  pktStart(CMD_ZEROREGS);
  pktSend();
}

/********************************************************/
/*  Serial receive loop                                 */
/********************************************************/
/**
 * Read a serial packet, then execute the related command.
 */
void packetReader() {
  //-- Wait for the 0xaa 0x55 lead-in
  int phase = 0;
  while(true) {
    if(Serial.available()) {
      // Serial.println("Available");
      int r = Serial.readBytes(readBuffer, 3);
      if(r == 3) {
        if(readBuffer[0] == 0xaa && readBuffer[1] == 0x55)
          break;
      }
      // if(r > 0) {
      //   Serial.println("Gotcha");
      // }
    }
  }

  Serial.println("hdr");
  //-- Lead-in received, next is length
  int len = readBuffer[2];
  int rc = Serial.readBytes(readBuffer, len + 2);               // Read the packet and include the checksum
  if(rc != len + 2) {                                                // Incomplete?
    Serial.println("badlen");
    return;
  }

  //-- Check the sum
  int sum = 0;
  for(int i = 0; i < len; i++) {
    sum += readBuffer[i];
  }
  unsigned int psum = ((int)readBuffer[len] << 8) | (int)readBuffer[len + 1];
  if(sum != psum) {
    //-- Checksum error, bail out
    Serial.print("Badsum ");
    Serial.print(psum, 16);
    Serial.print(" exp ");
    Serial.println(sum, 16);
    return;
  }

  //-- All is well. We have a packet.. Initialize the read data.
  readIndex = 0;
  readLen = len;

  //-- Decode the command
  int command = rdWord();
  switch(command) {
    default:
      replyError("command %d??", command);
      break;

    case CMD_REGISTERS:
      replyRegisters();
      break;

    case CMD_SETREGS:
      cmdSetRegisters();
      break;

    case CMD_ALUOP:
      cmdAluOperation();
      break;  

    case CMD_INITREGS:
      cmdInitRegs();
      break;

    case CMD_ZEROREGS:
      cmdZeroRegisters();
      break;
  }
}

int vals[] = {
  0x1234, // 0
  0x2345,
  0x3456,
  0x4567,
  0x5678,
  0x6789,
  0x789a,
  0x89ab,
  0x9abc, // 8
  0xabcd,
  0xbcde,
  0xcdef,
  0xdef0,
  0xef01,
  0xf012,
  0x0123,
};

void testSetRegisters() {
  for(int i = 0; i < 16; i++) {
    setAluReg(i, vals[i]);
  }
}


void setup() {
  pinMode(CTL_OE, OUTPUT);
  digitalWrite(CTL_OE, HIGH);               // Disable AD0..15 outputs

  for(int i = 0; i < 4; i++)
    pinMode(CTL_CA0 + i, OUTPUT);
  for(int i = 0; i < 4; i++)
    pinMode(CTL_CB0 + i, OUTPUT);
  for(int i = 0; i < 9; i++)
    pinMode(CTL_CI0 + i, OUTPUT);
  pinMode(CTL_OP16, OUTPUT);
  pinMode(CTL_OP8L, OUTPUT);
  pinMode(CTL_OP8H, OUTPUT);

  pinMode(CTL_CLKAL, OUTPUT);
  pinMode(CTL_CLKAH, OUTPUT);
  digitalWrite(CTL_CLKAL, LOW);             // Latched A and B
  digitalWrite(CTL_CLKAH, LOW);

  pinMode(ACARRYIN, OUTPUT);
  digitalWrite(ACARRYIN, LOW);              // Carry in 0

  pinMode(FLAGSLATCH, OUTPUT);
  // digitalWrite(FLAGSLATCH, HIGH);           // Let flag values roll through (not latched)
  digitalWrite(FLAGSLATCH, LOW);

  pinMode(CARRYSEL0, OUTPUT);
  pinMode(CARRYSEL1, OUTPUT);
  pinMode(CARRYSEL2, OUTPUT);
  digitalWrite(CARRYSEL0, LOW);
  digitalWrite(CARRYSEL1, LOW);
  digitalWrite(CARRYSEL2, LOW);

  pinMode(FLGCARRYL, INPUT);
  pinMode(FLGNEGL, INPUT);
  pinMode(FLGOVFL, INPUT);
  pinMode(FLGZEROL, INPUT);

  Serial.begin(19200);
  Serial.setTimeout(5000);
}

void loop() {
  testSetRegisters();

  for(;;) {
    packetReader();
  }

}
