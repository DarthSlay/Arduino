// Arduino Due - CAN Sample 2
// Brief CAN example for Arduino Due
// Test the transmission from CAN0 Mailbox 0 to CAN1 Mailbox 0 using interruption
// By Thibaut Viard/Wilfredo Molina 2012

// Required libraries
#include "variant.h"
#include <CAN.h>

#define TEST1_CAN_COMM_MB_IDX    0
#define TEST1_CAN_TRANSFER_ID    0x07
#define TEST1_CAN0_TX_PRIO       15
#define CAN_MSG_DUMMY_DATA       0x55AAAA55

// CAN frame max data length
#define MAX_CAN_FRAME_DATA_LEN   8

// CAN class
CANRaw CAN;

//Message variable to be send
uint32_t CAN_MSG_1 = 0;

// CAN0 Transceiver
SSN65HVD234_Data can0_transceiver;

// CAN1 Transceiver
SSN65HVD234_Data can1_transceiver;

// Define the CAN0 and CAN1 Transfer mailbox structure:
can_mb_conf_t can0_mailbox;
can_mb_conf_t can1_mailbox;

//Define the receive flag that is changed in CAN1 ISR handler:
volatile uint32_t g_ul_recv_status = 0;

void setup() {

// start serial port at 9600 bps: 
  Serial.begin(9600);
  Serial.println("Type CAN message to send");
  while (Serial.available() == 0);  

}
// Define the CAN1 ISR handler in the application:
void CAN1_Handler(void)
    {
        uint32_t ul_status;
        //In CAN1_Handler(), get CAN1 mailbox 0 status:
        ul_status = can_mailbox_get_status(CAN1, 0);
        // check whether the mailbox 0 has received a data frame:
        // if mailbox 0 is ready, set up the receive flag:
        if ((ul_status & CAN_MSR_MRDY) == CAN_MSR_MRDY) {
            can1_mailbox.ul_mb_idx = 0;
            can1_mailbox.ul_status = ul_status;
            can_mailbox_read(CAN1, &can1_mailbox);
            g_ul_recv_status = 1;
        }
    }

// brief Decode CAN messages.
// param p_mailbox Pointer to CAN Mailbox structure
static void decode_can_msg(can_mb_conf_t *p_mailbox)
{
	uint32_t ul_led_Ctrl = p_mailbox->ul_datal;
}

// Test the transmission from CAN0 Mailbox 0 to CAN1 Mailbox 0
static void test_1(void)
{
  //Reset all CAN0 and CAN1 mailboxes:
  CAN.reset_all_mailbox(CAN0);
  CAN.reset_all_mailbox(CAN1);

  // Initialize CAN1 mailbox 0 as CONSUMER:
  can1_mailbox.ul_mb_idx = TEST1_CAN_COMM_MB_IDX;
  can1_mailbox.uc_obj_type = CAN_MB_CONSUMER_MODE;
  can1_mailbox.uc_tx_prio = TEST1_CAN0_TX_PRIO;
  can1_mailbox.ul_id_msk = CAN_MAM_MIDvA_Msk | CAN_MAM_MIDvB_Msk;
  can1_mailbox.ul_id = CAN_MID_MIDvA(TEST1_CAN_TRANSFER_ID);
  CAN.mailbox_init(CAN1, &can1_mailbox);
  
  //Initialize CAN0 mailbox 0 as PRODUCER:
  can0_mailbox.ul_mb_idx = 0;
  can0_mailbox.uc_obj_type = CAN_MB_PRODUCER_MODE;
  can0_mailbox.ul_id_msk = 0;
  can0_mailbox.ul_id = CAN_MID_MIDvA(TEST1_CAN_TRANSFER_ID);
  CAN.mailbox_init(CAN0, &can0_mailbox);

  // Prepare the response information when it receives a remote frame: 
  can0_mailbox.ul_datal = CAN_MSG_1;
  can0_mailbox.ul_datah = CAN_MSG_DUMMY_DATA;
  can0_mailbox.uc_length = MAX_CAN_FRAME_DATA_LEN;
  CAN.mailbox_write(CAN0, &can0_mailbox);

  // Enable CAN1 mailbox 0 interrupt
  CAN.enable_interrupt(CAN1, CAN_IER_MB0);

  // Send out the information in the mailbox
  CAN.global_send_transfer_cmd(CAN0, CAN_TCR_MB0);
  CAN.global_send_transfer_cmd(CAN1, CAN_TCR_MB0);
	// Wait for the communication to be completed.
	while (!g_ul_recv_status) {
	}
	if ((can1_mailbox.ul_datal == CAN_MSG_1) &&
  	(can1_mailbox.uc_length == 8)) {
		decode_can_msg(&can1_mailbox);
                CAN.mailbox_read(CAN1, &can1_mailbox);
                Serial.print("CAN message received= ");
                Serial.println(can1_mailbox.ul_datal);
                Serial.println("End of test");
	} else {
		
	}
}

// can_example application entry point
void loop()
{
  while (Serial.available() > 0) {
     CAN_MSG_1 = Serial.parseInt();
      if (Serial.read() == '\n') {      
      Serial.print("Sent value= ");
      Serial.println(CAN_MSG_1);
      delay(1000);
    }
  }

// Initialize CAN0 Transceiver
SN65HVD234_Init(&can0_transceiver);
SN65HVD234_SetRs(&can0_transceiver, 61);
SN65HVD234_SetEN(&can0_transceiver, 62);
// Enable CAN0 Transceiver
SN65HVD234_DisableLowPower(&can0_transceiver);
SN65HVD234_Enable(&can0_transceiver);

// Initialize CAN1 Transceiver
SN65HVD234_Init(&can1_transceiver);
SN65HVD234_SetRs(&can1_transceiver, 63);
SN65HVD234_SetEN(&can1_transceiver, 64);
// Enable CAN1 Transceiver
SN65HVD234_DisableLowPower(&can1_transceiver);
SN65HVD234_Enable(&can1_transceiver);

// Enable the module clock for CAN0 and CAN1:
pmc_enable_periph_clk(ID_CAN0);
pmc_enable_periph_clk(ID_CAN1);

// Verify CAN0 and CAN1 initialization, baudrate is 1Mb/s:
if (CAN.init(CAN0, SystemCoreClock, CAN_BPS_1000K) &&
CAN.init(CAN1, SystemCoreClock, CAN_BPS_1000K)) {

// Disable all CAN0 & CAN1 interrupts
CAN.disable_interrupt(CAN0, CAN_DISABLE_ALL_INTERRUPT_MASK);
CAN.disable_interrupt(CAN1, CAN_DISABLE_ALL_INTERRUPT_MASK);

// Configure and enable interrupt of CAN1, as the tests will use receiver interrupt
NVIC_EnableIRQ(CAN1_IRQn);

// Run test
test_1();
g_ul_recv_status = 0;

// Disable CAN0 Controller
CAN.disable(CAN0);
// Disable CAN1 Controller
CAN.disable(CAN1);
} else {
Serial.println("CAN initialization (sync) ERROR");
}

while (1) {
	}
}
