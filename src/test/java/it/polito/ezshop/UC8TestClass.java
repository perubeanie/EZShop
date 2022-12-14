package it.polito.ezshop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.polito.ezshop.data.EZShopInterface;
import it.polito.ezshop.exceptions.InvalidCreditCardException;
import it.polito.ezshop.exceptions.InvalidProductCodeException;
import it.polito.ezshop.exceptions.InvalidQuantityException;
import it.polito.ezshop.exceptions.InvalidRFIDException;
import it.polito.ezshop.exceptions.InvalidTransactionIdException;
import it.polito.ezshop.exceptions.UnauthorizedException;

public class UC8TestClass {

	static EZShopInterface ezShop;
	static Integer transactionId;
	static Integer transactionId2;
	static Integer transactionId3;
	static String barcode = "12637482635892";
	static String barcode2 = "6253478956438";
	static String creditCard = "4485370086510891";
	static String creditCard2 = "5100293991053009";
	int orderId;
	String RFID = "000000100000";

	@Before
	public void init() {

		ezShop = new it.polito.ezshop.data.EZShop();
		ezShop.reset();
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:creditCards.sqlite");) {
			String updateCard = "UPDATE creditCards SET balance = 150 WHERE creditCardNumber = 4485370086510891";
			PreparedStatement pstmt = conn.prepareStatement(updateCard);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			ezShop.createUser("admin", "admin", "Administrator");
			ezShop.login("admin", "admin");

			// Product type X exists and has enough units to complete the sale
			Integer productId = ezShop.createProductType("biscotti", "12637482635892", 1.5, "piccoli");
			ezShop.updatePosition(productId, "2-aisle-2");
			ezShop.updateQuantity(productId, 250);

			ezShop.recordBalanceUpdate(1000);
			orderId = ezShop.payOrderFor("12637482635892", 5, 3.0);
			assertTrue(ezShop.recordOrderArrivalRFID(orderId, RFID));

			Integer productId2 = ezShop.createProductType("insalata", "6253478956438", 2, "in busta");
			ezShop.updatePosition(productId2, "3-aisle-3");
			ezShop.updateQuantity(productId2, 300);

			ezShop.createUser("Casper", "casper101", "Cashier");
			ezShop.logout();

			// Cashier C exists and is logged in
			ezShop.login("Casper", "casper101");
			// ezShop.login("admin", "admin");

			transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			assertTrue(ezShop.addProductToSale(transactionId, barcode, 5));
			assertTrue(ezShop.addProductToSale(transactionId, barcode2, 10));

			assertTrue(ezShop.endSaleTransaction(transactionId));

			assertTrue(ezShop.receiveCreditCardPayment(transactionId, creditCard));

			transactionId2 = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(2), transactionId2);

			assertTrue(ezShop.addProductToSale(transactionId2, barcode, 8));

			assertTrue(ezShop.endSaleTransaction(transactionId2));

			assertEquals(3, ezShop.receiveCashPayment(transactionId2, 15), 0.001);

			transactionId3 = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(3), transactionId3);
			assertTrue(ezShop.addProductToSaleRFID(transactionId3, "000000100000"));
			assertTrue(ezShop.addProductToSaleRFID(transactionId3, "000000100001"));
			assertTrue(ezShop.endSaleTransaction(transactionId3));

			assertEquals(3, ezShop.receiveCashPayment(transactionId2, 15), 0.001);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@After
	public void teardown() {

		ezShop.reset();

	}

	@Test
	public void testCaseScenario8_1() {

		// Scenario 8-1 Return transaction of product type X completed, credit card
		try {

			Integer returnId = ezShop.startReturnTransaction(transactionId);
			assertEquals(Integer.valueOf(1), returnId);
			assertTrue(ezShop.returnProduct(returnId, barcode, 2));
			assertEquals(3, ezShop.returnCreditCardPayment(returnId, creditCard), 0.001);
			assertTrue(ezShop.endReturnTransaction(returnId, true));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_2() {

		// Scenario 8-2 Return transaction of product type X completed, cash
		try {

			Integer returnId = ezShop.startReturnTransaction(transactionId);
			assertEquals(Integer.valueOf(1), returnId);
			assertTrue(ezShop.returnProduct(returnId, barcode, 2));
			assertEquals(3, ezShop.returnCashPayment(returnId), 0.001);
			assertTrue(ezShop.endReturnTransaction(returnId, true));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_2b() {

		// Scenario 8-2 Return transaction of product type X completed, cash
		try {

			Integer returnId = ezShop.startReturnTransaction(transactionId3);
			assertEquals(Integer.valueOf(1), returnId);
			assertTrue(ezShop.returnProductRFID(returnId, RFID));
			assertEquals(1.5, ezShop.returnCashPayment(returnId), 0.001);
			assertTrue(ezShop.endReturnTransaction(returnId, true));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_3() {

		// Scenario 8-3 Delete return transaction
		try {

			Integer returnId = ezShop.startReturnTransaction(transactionId);
			assertEquals(Integer.valueOf(1), returnId);
			assertTrue(ezShop.returnProduct(returnId, barcode, 2));
			assertEquals(3, ezShop.returnCashPayment(returnId), 0.001);
			assertTrue(ezShop.endReturnTransaction(returnId, true));
			assertTrue(ezShop.deleteReturnTransaction(returnId));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_3b() {

		// Scenario 8-3 Delete return transaction (RFID)
		try {

			Integer returnId = ezShop.startReturnTransaction(transactionId3);
			assertEquals(Integer.valueOf(1), returnId);
			assertTrue(ezShop.returnProductRFID(returnId, RFID));
			assertEquals(1.5, ezShop.returnCashPayment(returnId), 0.001);
			assertTrue(ezShop.endReturnTransaction(returnId, true));
			assertTrue(ezShop.deleteReturnTransaction(returnId));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_4() {

		// Scenario 8-3 startReturnTransaction exceptions
		try {

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.startReturnTransaction(transactionId);
			});

			ezShop.login("admin", "admin");
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.startReturnTransaction(null);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.startReturnTransaction(0);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.startReturnTransaction(-1);
			});
			assertEquals(Integer.valueOf(-1), ezShop.startReturnTransaction(4));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_5() {

		// Scenario 8-4 returnProduct exceptions
		try {
			Integer returnId = ezShop.startReturnTransaction(transactionId);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.returnProduct(returnId, barcode, 2);
			});

			ezShop.login("admin", "admin");
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnProduct(null, barcode, 2);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnProduct(0, barcode, 2);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnProduct(-1, barcode, 2);
			});

			assertThrows(InvalidQuantityException.class, () -> {
				ezShop.returnProduct(returnId, barcode, 0);
			});
			assertThrows(InvalidQuantityException.class, () -> {
				ezShop.returnProduct(returnId, barcode, -1);
			});

			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.returnProduct(returnId, "", 2);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.returnProduct(returnId, null, 2);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.returnProduct(returnId, "123", 2);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.returnProduct(returnId, "12345678909aa", 2);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.returnProduct(returnId, "1234568890983", 2);
			});

			assertFalse(ezShop.returnProduct(returnId, "45637289084174", 2)); // the product to be returned does not
			// exists

			assertFalse(ezShop.returnProduct(2, barcode, 2)); // returnId != openReturnTransaction.getReturnId()

			ezShop.endReturnTransaction(returnId, true);
			ezShop.deleteReturnTransaction(returnId);
			assertFalse(ezShop.returnProduct(returnId, barcode, 2)); // openSaleTransaction.getTicketNumber()==

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_6() {

		// Scenario 8-5 endReturnTransaction exceptions
		try {
			Integer returnId = ezShop.startReturnTransaction(transactionId);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.endReturnTransaction(returnId, true);
			});

			ezShop.login("admin", "admin");
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.endReturnTransaction(null, true);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.endReturnTransaction(0, true);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.endReturnTransaction(-1, true);
			});

			// assertFalse(ezShop.returnProduct(returnId, "22637482635892", 2)); // the product to be returned does not
			// exists

			assertFalse(ezShop.endReturnTransaction(2, true)); // returnId != openReturnTransaction.getReturnId()
			assertTrue(ezShop.endReturnTransaction(returnId, false));

			Integer returnId2 = ezShop.startReturnTransaction(transactionId);
			assertTrue(ezShop.endReturnTransaction(returnId2, true));
			assertTrue(ezShop.deleteReturnTransaction(returnId2));
			assertFalse(ezShop.endReturnTransaction(returnId2, true)); // openSaleTransaction.getTicketNumber()==

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_7() {

		// Scenario 8-6 deleteReturnTransaction exceptions
		try {
			Integer returnId = ezShop.startReturnTransaction(transactionId);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.deleteReturnTransaction(returnId);
			});

			ezShop.login("admin", "admin");
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteReturnTransaction(null);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteReturnTransaction(0);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteReturnTransaction(-1);
			});

			// assertFalse(ezShop.returnProduct(returnId, "22637482635892", 2)); // the product to be returned does not
			// exists

			ezShop.endReturnTransaction(returnId, true);
			assertFalse(ezShop.deleteReturnTransaction(2)); // returnId != openReturnTransaction.getReturnId()

			ezShop.deleteReturnTransaction(returnId);
			assertFalse(ezShop.deleteReturnTransaction(returnId)); // openSaleTransaction.getTicketNumber()==

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_8() {
		// return cash payment exceptions

		try {
			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.returnCashPayment(-1);
			});
			ezShop.login("Casper", "casper101");

			Integer returnId = ezShop.startReturnTransaction(transactionId);
			assertEquals(Integer.valueOf(1), returnId);
			assertTrue(ezShop.returnProduct(returnId, barcode, 2));

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnCashPayment(null);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnCashPayment(0);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnCashPayment(-1);
			});

			assertEquals(-1, ezShop.returnCashPayment(2), 0.001);

			ezShop.endReturnTransaction(returnId, true);
			assertEquals(-1, ezShop.returnCashPayment(returnId), 0.001);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_9() {

		// return credit card payment exceptions
		try {
			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.returnCreditCardPayment(transactionId, creditCard);
			});
			ezShop.login("Casper", "casper101");

			Integer returnId = ezShop.startReturnTransaction(transactionId);
			assertEquals(Integer.valueOf(1), returnId);
			assertTrue(ezShop.returnProduct(returnId, barcode, 2));

			ezShop.addProductToSale(returnId, barcode, 100);

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnCreditCardPayment(null, creditCard);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnCreditCardPayment(0, creditCard);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnCreditCardPayment(-1, creditCard);
			});

			assertThrows(InvalidCreditCardException.class, () -> {
				ezShop.returnCreditCardPayment(transactionId, null);
			});

			assertThrows(InvalidCreditCardException.class, () -> {
				ezShop.returnCreditCardPayment(transactionId, "abc");
			});

			assertEquals(-1, ezShop.returnCreditCardPayment(transactionId, creditCard2), 0.001); // cash <

			assertEquals(-1, ezShop.returnCreditCardPayment(transactionId2, creditCard), 0.001);

			assertEquals(-1, ezShop.returnCreditCardPayment(2, creditCard), 0.001);

			ezShop.endReturnTransaction(returnId, true);
			assertEquals(-1, ezShop.returnCreditCardPayment(1, creditCard), 0.01);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario8_10() {

		// Scenario 8-10 returnProductRFID exceptions
		try {
			Integer returnId = ezShop.startReturnTransaction(transactionId3);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.returnProductRFID(returnId, RFID);
			});

			ezShop.login("admin", "admin");
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnProductRFID(null, RFID);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnProductRFID(0, RFID);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.returnProductRFID(-1, RFID);
			});

			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.returnProductRFID(returnId, "");
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.returnProductRFID(returnId, null);
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.returnProductRFID(returnId, "123");
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.returnProductRFID(returnId, "12345678909aa");
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.returnProductRFID(returnId, "1234568890983");
			});

			assertFalse(ezShop.returnProductRFID(returnId, "000000000009")); // the product does not exist

			assertFalse(ezShop.returnProductRFID(returnId, "000000100004")); // the product to be returned is not in the
																				// saleTransaction

			assertFalse(ezShop.returnProductRFID(2, RFID)); // returnId != openReturnTransaction.getReturnId()

			ezShop.endReturnTransaction(returnId, true);
			ezShop.deleteReturnTransaction(returnId);
			assertFalse(ezShop.returnProductRFID(returnId, RFID)); // openSaleTransaction.getTicketNumber()==

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
