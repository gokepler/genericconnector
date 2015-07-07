package ch.maxant.jca_demo.bookingsystem;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(name="BookingSystem")
public class BookingSystemWebService {

	public BookingSystemWebService(){
	}
	
    private final Logger log = Logger.getLogger(this.getClass().getName());
    
    public String reserveTickets(@WebParam(name="txid") String txid, @WebParam(name="referenceNumber") String referenceNumber) throws Exception {

        log.log(Level.INFO, "EXECUTE: booking tickets with reference number " + referenceNumber + " for TXID " + txid);
        if("FAILWSBookingSystem".equals(referenceNumber)){
            throw new Exception("failed for test purposes");
        }else{
        	//TODO write persistently!
            return "RESERVED tickets: " + referenceNumber;
        }
    }

    public void bookTickets(@WebParam(name="txId") String txId) throws IOException{
    	
    	//TODO write persistently!
    	
        log.log(Level.INFO, "BOOK tickets: " + txId);
    }

    public void cancelTickets(@WebParam(name="txId") String txId) throws IOException {
    	
    	//TODO write persistently!

    	log.log(Level.INFO, "CANCEL tickets: " + txId);
    }
    
}
