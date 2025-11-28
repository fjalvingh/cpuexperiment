#include <stdarg.h>


#define ARDUINO_A0  54        // Pin number to use A0 as digital port




#define AD0         22              // AD0..15 ALU input/result

#define CTL_CA0     (AD0 + 16)      // Start of CA0 assignments (38)
#define CTL_CB0     (CTL_CA0 + 4)   // (42)
#define CTL_CI0     (CTL_CB0 + 4)   // Control signals (46) 

#define CTL_CLKAH   (ARDUINO_A0 + 2)
#define CTL_CLKAL   (ARDUINO_A0 + 3)

#define CTL_OP16    (ARDUINO_A0 + 4)
#define CTL_OP8H    (ARDUINO_A0 + 5)
#define CTL_OP8L    (ARDUINO_A0 + 6)


#define CTL_OE      (ARDUINO_A0 + 1)     // OE pin is on analog 14

#define CLKN        HIGH
#define CLKA        LOW

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
  
  // for(int i = 0; i < 16; i++) {
  //   if(value & (1 << i)) {
  //     digitalWrite(AD0 + i, HIGH);
  //   } else {
  //     digitalWrite(AD0 + i, LOW);
  //   }
  // }
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
 * Pulse the CLOCK line, to latch data. The data gets latched
 * when the clock line is HIGH, and the last data read is retained
 * while clock is low.
 */
void pulseOperation(AluOpType op) {
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
    digitalWrite(CTL_CLKAL, CLKA);

  if(op == Op16 || op == Op8H)
    digitalWrite(CTL_CLKAH, CLKA);
  delay(10);
  digitalWrite(CTL_CLKAL, CLKN);
  digitalWrite(CTL_CLKAH, CLKN);
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
  pulseOperation(Op16);
  delay(200);

  setADMode(INPUT);
}

void setOE(boolean on) {
  digitalWrite(CTL_OE, on ? LOW : HIGH);
}


/********************************************************/
/*  Serial command reader                               */
/********************************************************/

#define CMD_ERROR     0x01
#define CMD_REGISTERS 0x02

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

void replyRegisters() {
  pktStart(CMD_REGISTERS);
  setAluFunction(FnOr);
  setAluDest(DstNop);
  setAluSrc(Src0A);               // 0, A as sources
  setOE(true);

  //-- Read all ALU registers one by one and output on F (data)
  for(int i = 0; i < 16; i++) {
    setAluPortA(i);
    setAluPortB(i);
    pulseOperation(Op16);
    int rv = readData();
    delay(100);
    pktWord(rv);
  }

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
  if(
    ((sum >> 8) & 0xff) != readBuffer[len]
    || (sum & 0xff) != readBuffer[len + 1]
  ) {
    //-- Checksum error, bail out
    Serial.println("Badsum");
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
  digitalWrite(CTL_CLKAL, LOW);
  digitalWrite(CTL_CLKAH, LOW);

  Serial.begin(19200);
  Serial.setTimeout(5000);
}

void block() {
  while(1)
    ;
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

  // setAluFunction(FnAdd);
  // setAluDest(DstNop);
  // setAluSrc(Src0A);               // 0, A as sources
  // setOE(true);

  // //-- Read all ALU registers one by one and output on F (data)
  // for(;;) {
  //   for(int i = 0; i < 16; i++) {
  //     setAluPortA(i);
  //     setAluPortB(i);
  //     pulseOperation(Op16);
  //     delay(1000);
  //   }
  // }
}





void loop() {
  testSetRegisters();

  for(;;) {
    packetReader();
  }

  setAluReg(7, 0x1234);
  setAluReg(5, 0x5678); // 
  setAluPortA(7);
  setAluPortB(6);
  setAluSrc(SrcAB);
  setAluFunction(FnAdd);
  setAluDest(DstRamF);
  digitalWrite(CTL_OE, LOW);
  
  block();

  // digitalWrite(CTL_OE, LOW);
  
  // put your main code here, to run repeatedly:
  // setAluReg(0, 0x1234);
  // setAluReg(1, 0x5678);

  // //-- Add reg A and B
  // setAluFunction(FnAdd);
  // setAluSrc(SrcAB);
  // setAluDest(DstNop);
  // digitalWrite(CTL_OE, LOW);

  // for(int i = 0; i < 16; i++) {
  //   setAluPortA(i);
  //   delay(1000);
  // }

  // for(int i = 0; i < 16; i++) {
  //   setAluPortB(i);
  //   delay(1000);
  // }

  // for(int i = 0; i < 8; i++) {
  //   setAluSrc(i);
  //   delay(1000);
  // }

  // for(int i = 0; i < 8; i++) {
  //   setAluFunction(i);
  //   delay(1000);
  // }
  // for(int i = 0; i < 8; i++) {
  //   setAluDest(i);
  //   delay(1000);
  // }








  // block();
}
