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
import it.polito.ezshop.exceptions.InvalidDiscountRateException;
import it.polito.ezshop.exceptions.InvalidPaymentException;
import it.polito.ezshop.exceptions.InvalidProductCodeException;
import it.polito.ezshop.exceptions.InvalidQuantityException;
import it.polito.ezshop.exceptions.InvalidRFIDException;
import it.polito.ezshop.exceptions.InvalidTransactionIdException;
import it.polito.ezshop.exceptions.UnauthorizedException;

public class UC6TestClass {

	static EZShopInterface ezShop;

	String barcode = "12637482635892";
	String barcode2 = "6253478956438";
	String customerCard;
	String creditCard = "4485370086510891";
	String creditCard2 = "5100293991053009";
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

			Integer productId2 = ezShop.createProductType("insalata", "6253478956438", 1.99, "in busta");
			ezShop.updatePosition(productId2, "3-aisle-3");
			ezShop.updateQuantity(productId2, 300);

			Integer idCustomer = ezShop.defineCustomer("Andrea");
			assertEquals(Integer.valueOf(1), idCustomer);

			customerCard = ezShop.createCard();
			assertTrue(customerCard != null && !customerCard.isEmpty());
			assertTrue(ezShop.attachCardToCustomer(customerCard, idCustomer));

			ezShop.createUser("Casper", "casper101", "Cashier");
			ezShop.logout();

			// Cashier C exists and is logged in
			ezShop.login("Casper", "casper101");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@After
	public void teardown() {

		ezShop.reset();

	}

	@Test
	public void testCaseScenario6_1() {
		// Scenario 6-1 Sale of product type X completed (credit card)

		try {
			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			assertTrue(ezShop.addProductToSale(transactionId, barcode, 5));
			assertTrue(ezShop.addProductToSale(transactionId, barcode, 13));

			assertTrue(ezShop.endSaleTransaction(transactionId));

			assertTrue(ezShop.receiveCreditCardPayment(transactionId, "4485370086510891"));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_1b() {
		// Scenario 6-1b Sale of product type X using RFID completed (credit card)

		try {
			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			assertTrue(ezShop.addProductToSaleRFID(transactionId, "000000100000"));
			assertTrue(ezShop.addProductToSaleRFID(transactionId, "000000100001"));

			assertTrue(ezShop.endSaleTransaction(transactionId));

			assertTrue(ezShop.receiveCreditCardPayment(transactionId, "4485370086510891"));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_2() {
		// Scenario 6-2 Sale of product type X with product discount

		try {
			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			assertTrue(ezShop.addProductToSale(transactionId, barcode, 4)); // 6 euro

			assertTrue(ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5)); // 3 euro

			assertTrue(ezShop.endSaleTransaction(transactionId));

			assertTrue(ezShop.receiveCreditCardPayment(transactionId, "4485370086510891"));

			assertEquals(3.00, ezShop.getSaleTransaction(transactionId).getPrice(), 0.001);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_3() {
		// Scenario 6-3 Sale of product type X with sale discount

		try {
			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			assertTrue(ezShop.addProductToSale(transactionId, barcode, 4)); // 6 euro

			assertTrue(ezShop.applyDiscountRateToSale(transactionId, 0.1)); // 5.40

			assertTrue(ezShop.endSaleTransaction(transactionId));

			assertTrue(ezShop.receiveCreditCardPayment(transactionId, "4485370086510891"));

			assertEquals(5.40, ezShop.getSaleTransaction(transactionId).getPrice(), 0.001);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_4() {
		// Scenario 6-4 Sale of product type X with Loyalty Card update

		try {
			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			assertTrue(ezShop.addProductToSale(transactionId, barcode, 32)); // 48 euro

			assertTrue(ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5)); // 24 euro

			assertTrue(ezShop.applyDiscountRateToSale(transactionId, 0.25)); // 18 euro

			assertTrue(ezShop.endSaleTransaction(transactionId));

			assertTrue(ezShop.receiveCreditCardPayment(transactionId, "4485370086510891"));

			int points = ezShop.computePointsForSale(transactionId);
			assertEquals(1, points);

			assertTrue(ezShop.modifyPointsOnCard(customerCard, points));

			assertEquals(18, ezShop.getSaleTransaction(transactionId).getPrice(), 0.001);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_5() {
		// Scenario 6-5 Sale of product type X cancelled

		try {
			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			assertTrue(ezShop.addProductToSale(transactionId, barcode, 16)); // 24 euro

			assertTrue(ezShop.deleteProductFromSale(transactionId, barcode, 4)); // 18 euro

			// assertTrue(ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5)); // 3 euro

			// assertTrue(ezShop.applyDiscountRateToSale(transactionId, 0.1));

			assertTrue(ezShop.endSaleTransaction(transactionId));

			assertTrue(ezShop.receiveCreditCardPayment(transactionId, "4485370086510891"));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_5b() {
		// Scenario 6-5b Sale of RFID cancelled

		try {
			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			assertTrue(ezShop.addProductToSaleRFID(transactionId, RFID));
			assertTrue(ezShop.addProductToSaleRFID(transactionId, "000000100001"));

			assertTrue(ezShop.deleteProductFromSaleRFID(transactionId, RFID));

			assertTrue(ezShop.endSaleTransaction(transactionId));

			assertTrue(ezShop.receiveCreditCardPayment(transactionId, "4485370086510891"));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_6() {
		// Scenario 6-6 Sale of product type X completed (Cash)

		try {
			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			assertTrue(ezShop.addProductToSale(transactionId, barcode, 16)); // 24 euro

			assertTrue(ezShop.deleteProductFromSale(transactionId, barcode, 4)); // 18 euro

			// assertTrue(ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5));

			// assertTrue(ezShop.applyDiscountRateToSale(transactionId, 0.1));

			assertTrue(ezShop.endSaleTransaction(transactionId));

			assertEquals(2, ezShop.receiveCashPayment(transactionId, 20), 0.01); // 2 euro back

			int points = ezShop.computePointsForSale(transactionId);
			assertEquals(1, points);

			// assertTrue(ezShop.modifyPointsOnCard("1000000000", points));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_7() {
		// Scenario 6-7 Delete of a SaleTransaction

		try {
			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			assertTrue(ezShop.addProductToSale(transactionId, barcode, 16)); // 24 euro

			assertTrue(ezShop.deleteProductFromSale(transactionId, barcode, 4)); // 18 euro

			assertTrue(ezShop.endSaleTransaction(transactionId));

			assertTrue(ezShop.deleteSaleTransaction(transactionId));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_8() {
		// startSaleTransaction exceptions

		try {
			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.startSaleTransaction();
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ezShop.login("Casper", "casper101");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Test
	public void testCaseScenario6_9() {
		// addProductToSale exceptions

		try {

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.addProductToSale(transactionId, barcode, 16);
			});
			ezShop.login("Casper", "casper101");

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.addProductToSale(null, barcode, 16);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.addProductToSale(0, barcode, 16);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.addProductToSale(-1, barcode, 16);
			});

			assertThrows(InvalidQuantityException.class, () -> {
				ezShop.addProductToSale(transactionId, barcode, 0);
			});
			assertThrows(InvalidQuantityException.class, () -> {
				ezShop.addProductToSale(transactionId, barcode, -1);
			});

			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.addProductToSale(transactionId, "", 16);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.addProductToSale(transactionId, null, 16);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.addProductToSale(transactionId, "123", 16);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.addProductToSale(transactionId, "12345678909aa", 16);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.addProductToSale(transactionId, "1234568890983", 16);
			});

			ezShop.addProductToSale(transactionId, barcode2, 16);
			assertTrue(ezShop.addProductToSale(transactionId, "12637482635892", 16));

			assertFalse(ezShop.addProductToSale(transactionId, barcode, 260)); // amount > productType.getQuantity()

			assertFalse(ezShop.addProductToSale(2, barcode, 16)); // transactionId !=
																	// openSaleTransaction.getTicketNumber()

			assertFalse(ezShop.addProductToSale(transactionId, "45637289084174", 5));

			assertTrue(ezShop.endSaleTransaction(transactionId));
			assertFalse(ezShop.addProductToSale(transactionId, barcode, 16));

			// assertTrue(ezShop.deleteProductFromSale(transactionId, barcode, 4)); // 18 euro

			// assertTrue(ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5));

			// assertTrue(ezShop.applyDiscountRateToSale(transactionId, 0.1));

			// assertTrue(ezShop.deleteSaleTransaction(transactionId));

			// assertEquals(2, ezShop.receiveCashPayment(transactionId, 20), 0.01); // 2 euro back
			//
			// int points = ezShop.computePointsForSale(transactionId);
			// assertEquals(1, points);
			//
			// assertTrue(ezShop.modifyPointsOnCard("1000000000", points));
			//
			// assertTrue(ezShop.endSaleTransaction(transactionId));
			//
			//
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_10() {
		// deleteProductFromSale exceptions

		try {

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.addProductToSale(transactionId, barcode, 16);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.deleteProductFromSale(transactionId, barcode, 16);
			});
			ezShop.login("Casper", "casper101");

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteProductFromSale(null, barcode, 16);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteProductFromSale(0, barcode, 16);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteProductFromSale(-1, barcode, 16);
			});

			assertThrows(InvalidQuantityException.class, () -> {
				ezShop.deleteProductFromSale(transactionId, barcode, 0);
			});
			assertThrows(InvalidQuantityException.class, () -> {
				ezShop.deleteProductFromSale(transactionId, barcode, -1);
			});

			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.deleteProductFromSale(transactionId, "", 16);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.deleteProductFromSale(transactionId, null, 16);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.deleteProductFromSale(transactionId, "123", 16);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.deleteProductFromSale(transactionId, "12345678909aa", 16);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.deleteProductFromSale(transactionId, "1234568890983", 16);
			});

			assertFalse(ezShop.deleteProductFromSale(transactionId, "45637289084174", 16)); // !productType.getBarCode().equals(productCode)
			assertFalse(ezShop.deleteProductFromSale(transactionId, barcode, 20)); // amount > previousAmount

			assertTrue(ezShop.deleteProductFromSale(transactionId, barcode, 10)); // productType.getBarCode().equals(productCode),
																					// amount < previousAmount
			assertTrue(ezShop.deleteProductFromSale(transactionId, barcode, 6)); // productType.getBarCode().equals(productCode),
			// amount == previousAmount
			assertFalse(ezShop.deleteProductFromSale(2, barcode, 16)); // transactionId !=
																		// openSaleTransaction.getTicketNumber()

			assertTrue(ezShop.endSaleTransaction(transactionId));
			assertFalse(ezShop.deleteProductFromSale(transactionId, barcode, 16)); // openSaleTransaction.getTicketNumber()
																					// == -1

			// assertTrue(ezShop.deleteProductFromSale(transactionId, barcode, 4)); // 18 euro

			// assertTrue(ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5));

			// assertTrue(ezShop.applyDiscountRateToSale(transactionId, 0.1));

			// assertTrue(ezShop.deleteSaleTransaction(transactionId));

			// assertEquals(2, ezShop.receiveCashPayment(transactionId, 20), 0.01); // 2 euro back
			//
			// int points = ezShop.computePointsForSale(transactionId);
			// assertEquals(1, points);
			//
			// assertTrue(ezShop.modifyPointsOnCard("1000000000", points));
			//
			// assertTrue(ezShop.endSaleTransaction(transactionId));
			//
			//
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_11() {
		// applyDiscountRateToProduct exceptions

		try {

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.addProductToSale(transactionId, barcode, 16);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5);
			});
			ezShop.login("Casper", "casper101");

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.applyDiscountRateToProduct(null, barcode, 0.5);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.applyDiscountRateToProduct(0, barcode, 0.5);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.applyDiscountRateToProduct(-1, barcode, 0.5);
			});

			assertThrows(InvalidDiscountRateException.class, () -> {
				ezShop.applyDiscountRateToProduct(transactionId, barcode, -0.1);
			});
			assertThrows(InvalidDiscountRateException.class, () -> {
				ezShop.applyDiscountRateToProduct(transactionId, barcode, 1);
			});

			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.applyDiscountRateToProduct(transactionId, "", 0.5);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.applyDiscountRateToProduct(transactionId, null, 0.5);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.applyDiscountRateToProduct(transactionId, "123", 0.5);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.applyDiscountRateToProduct(transactionId, "12345678909aa", 0.5);
			});
			assertThrows(InvalidProductCodeException.class, () -> {
				ezShop.applyDiscountRateToProduct(transactionId, "1234568890983", 0.5);
			});

			assertFalse(ezShop.applyDiscountRateToProduct(transactionId, "45637289084174", 0.5)); // !productType.getBarCode().equals(productCode)

			assertFalse(ezShop.applyDiscountRateToProduct(2, barcode, 0.5)); // transactionId !=
			// openSaleTransaction.getTicketNumber()

			assertTrue(ezShop.endSaleTransaction(transactionId));
			assertFalse(ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5)); // openSaleTransaction.getTicketNumber()
			// == -1

			// assertTrue(ezShop.deleteProductFromSale(transactionId, barcode, 4)); // 18 euro

			// assertTrue(ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5));

			// assertTrue(ezShop.applyDiscountRateToSale(transactionId, 0.1));

			// assertTrue(ezShop.deleteSaleTransaction(transactionId));

			// assertEquals(2, ezShop.receiveCashPayment(transactionId, 20), 0.01); // 2 euro back
			//
			// int points = ezShop.computePointsForSale(transactionId);
			// assertEquals(1, points);
			//
			// assertTrue(ezShop.modifyPointsOnCard("1000000000", points));
			//
			// assertTrue(ezShop.endSaleTransaction(transactionId));
			//
			//
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_12() {
		// applyDiscountRateToSale exceptions

		try {

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.addProductToSale(transactionId, barcode, 16);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.applyDiscountRateToSale(transactionId, 0.5);
			});
			ezShop.login("Casper", "casper101");

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.applyDiscountRateToSale(null, 0.5);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.applyDiscountRateToSale(0, 0.5);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.applyDiscountRateToSale(-1, 0.5);
			});

			assertThrows(InvalidDiscountRateException.class, () -> {
				ezShop.applyDiscountRateToSale(transactionId, -0.1);
			});
			assertThrows(InvalidDiscountRateException.class, () -> {
				ezShop.applyDiscountRateToSale(transactionId, 1);
			});

			assertFalse(ezShop.applyDiscountRateToSale(2, 0.5)); // transactionId !=
			// openSaleTransaction.getTicketNumber()

			assertTrue(ezShop.endSaleTransaction(transactionId));
			assertFalse(ezShop.applyDiscountRateToSale(transactionId, 0.5)); // openSaleTransaction.getTicketNumber()==
																				// -1
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_13() {
		// computePointsForSale exceptions

		try {

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.addProductToSale(transactionId, barcode, 16);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.computePointsForSale(transactionId);
			});
			ezShop.login("Casper", "casper101");

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.computePointsForSale(null);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.computePointsForSale(0);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.computePointsForSale(-1);
			});

			assertEquals(-1, ezShop.computePointsForSale(transactionId));

			assertEquals(-1, ezShop.computePointsForSale(2));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_14() {
		// endSaleTransaction exceptions

		try {

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.addProductToSale(transactionId, barcode, 16);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.endSaleTransaction(transactionId);
			});
			ezShop.login("Casper", "casper101");

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.endSaleTransaction(null);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.endSaleTransaction(0);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.endSaleTransaction(-1);
			});

			assertFalse(ezShop.endSaleTransaction(2)); // transactionId !=
			// openSaleTransaction.getTicketNumber()
			assertTrue(ezShop.endSaleTransaction(transactionId));
			assertFalse(ezShop.endSaleTransaction(transactionId));

			ezShop.deleteSaleTransaction(transactionId);
			assertFalse(ezShop.endSaleTransaction(transactionId)); // openSaleTransaction.getTicketNumber()==
																	// -1
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_15() {
		// deleteSaleTransaction exceptions

		try {

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.addProductToSale(transactionId, barcode, 16);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.deleteSaleTransaction(transactionId);
			});
			ezShop.login("Casper", "casper101");

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteSaleTransaction(null);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteSaleTransaction(0);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteSaleTransaction(-1);
			});

			assertFalse(ezShop.deleteSaleTransaction(1)); // transactionId == openSaleTransaction.getTicketNumber()
			assertFalse(ezShop.deleteSaleTransaction(2)); // saleTransactionImpl == null

			assertTrue(ezShop.endSaleTransaction(transactionId));
			assertTrue(ezShop.receiveCreditCardPayment(transactionId, creditCard));
			assertFalse(ezShop.deleteSaleTransaction(transactionId)); // saleTransactionImpl.getBalanceId() != -1

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_16() {
		// getSaleTransaction exceptions

		try {

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.addProductToSale(transactionId, barcode, 16);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.getSaleTransaction(transactionId);
			});
			ezShop.login("Casper", "casper101");

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.getSaleTransaction(null);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.getSaleTransaction(0);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.getSaleTransaction(-1);
			});

			assertEquals(null, ezShop.getSaleTransaction(transactionId)); // transactionId has not been ended yet

			// TODO: assertTrue(ezShop.recordBalanceUpdate(price));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_17() {
		// receive cash payment exceptions

		try {
			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.receiveCashPayment(-1, 10.0);
			});
			ezShop.login("Casper", "casper101");

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.addProductToSale(transactionId, barcode, 16);

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.receiveCashPayment(null, 10.0);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.receiveCashPayment(0, 10.0);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.receiveCashPayment(-1, 10.0);
			});

			assertThrows(InvalidPaymentException.class, () -> {
				ezShop.receiveCashPayment(transactionId, -1);
			});

			assertEquals(-1, ezShop.receiveCashPayment(2, 1.0), 0.001); // cash <

			assertEquals(-1, ezShop.receiveCashPayment(2, 10.0), 0.001);

			assertEquals(-1, ezShop.receiveCashPayment(transactionId, 10.0), 0.001);

			ezShop.endSaleTransaction(transactionId);
			assertEquals(-1, ezShop.receiveCashPayment(transactionId, 10.0), 0.001);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_18() {

		// receive credit card payment
		try {
			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.receiveCreditCardPayment(-1, creditCard);
			});
			ezShop.login("Casper", "casper101");

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.addProductToSale(transactionId, barcode, 100);

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.receiveCreditCardPayment(null, creditCard);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.receiveCreditCardPayment(0, creditCard);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.receiveCreditCardPayment(-1, creditCard);
			});

			assertThrows(InvalidCreditCardException.class, () -> {
				ezShop.receiveCreditCardPayment(transactionId, null);
			});

			assertThrows(InvalidCreditCardException.class, () -> {
				ezShop.receiveCreditCardPayment(transactionId, "abc");
			});

			assertFalse(ezShop.receiveCreditCardPayment(transactionId, creditCard2)); // cash <

			assertFalse(ezShop.receiveCreditCardPayment(2, creditCard));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_19() {
		// addProductToSaleRFID exceptions

		try {

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.addProductToSaleRFID(transactionId, RFID);
			});
			ezShop.login("Casper", "casper101");

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.addProductToSaleRFID(null, RFID);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.addProductToSaleRFID(0, RFID);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.addProductToSaleRFID(-1, RFID);
			});

			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.addProductToSaleRFID(transactionId, "");
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.addProductToSaleRFID(transactionId, null);
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.addProductToSaleRFID(transactionId, "123");
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.addProductToSaleRFID(transactionId, "123456789a");
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.addProductToSaleRFID(transactionId, "12345678901");
			});

			assertTrue(ezShop.addProductToSaleRFID(transactionId, "000000100000"));
			assertTrue(ezShop.addProductToSaleRFID(transactionId, "000000100001"));
			assertTrue(ezShop.addProductToSaleRFID(transactionId, "000000100002"));
			assertTrue(ezShop.addProductToSaleRFID(transactionId, "000000100003"));
			assertTrue(ezShop.addProductToSaleRFID(transactionId, "000000100004"));
			assertFalse(ezShop.addProductToSaleRFID(transactionId, "000000100005"));

			assertFalse(ezShop.addProductToSaleRFID(2, RFID)); // transactionId !=
																// openSaleTransaction.getTicketNumber()

			assertTrue(ezShop.endSaleTransaction(transactionId));
			assertFalse(ezShop.addProductToSaleRFID(transactionId, RFID));

			// assertTrue(ezShop.deleteProductFromSale(transactionId, barcode, 4)); // 18 euro

			// assertTrue(ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5));

			// assertTrue(ezShop.applyDiscountRateToSale(transactionId, 0.1));

			// assertTrue(ezShop.deleteSaleTransaction(transactionId));

			// assertEquals(2, ezShop.receiveCashPayment(transactionId, 20), 0.01); // 2 euro back
			//
			// int points = ezShop.computePointsForSale(transactionId);
			// assertEquals(1, points);
			//
			// assertTrue(ezShop.modifyPointsOnCard("1000000000", points));
			//
			// assertTrue(ezShop.endSaleTransaction(transactionId));
			//
			//
		} catch (

		Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCaseScenario6_20() {
		// deleteProductFromSaleRFID exceptions

		try {

			Integer transactionId = ezShop.startSaleTransaction();
			assertEquals(Integer.valueOf(1), transactionId);

			ezShop.addProductToSaleRFID(transactionId, RFID);

			ezShop.logout();
			assertThrows(UnauthorizedException.class, () -> {
				ezShop.deleteProductFromSaleRFID(transactionId, RFID);
			});
			ezShop.login("Casper", "casper101");

			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteProductFromSaleRFID(null, RFID);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteProductFromSaleRFID(0, RFID);
			});
			assertThrows(InvalidTransactionIdException.class, () -> {
				ezShop.deleteProductFromSaleRFID(-1, RFID);
			});

			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.deleteProductFromSaleRFID(transactionId, "");
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.deleteProductFromSaleRFID(transactionId, null);
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.deleteProductFromSaleRFID(transactionId, "123");
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.deleteProductFromSaleRFID(transactionId, "12345678909aa");
			});
			assertThrows(InvalidRFIDException.class, () -> {
				ezShop.deleteProductFromSaleRFID(transactionId, "1234568890983");
			});

			assertFalse(ezShop.deleteProductFromSaleRFID(transactionId, "000000100001"));

			// assertFalse(ezShop.deleteProductFromSaleRFID(transactionId, barcode, 20)); // amount > previousAmount
			//
			// assertTrue(ezShop.deleteProductFromSaleRFID(transactionId, barcode, 10)); //
			// productType.getBarCode().equals(productCode),
			// // amount < previousAmount
			// assertTrue(ezShop.deleteProductFromSaleRFID(transactionId, barcode, 6)); //
			// productType.getBarCode().equals(productCode),
			// // amount == previousAmount
			assertFalse(ezShop.deleteProductFromSaleRFID(2, RFID)); // transactionId !=
			// openSaleTransaction.getTicketNumber()

			assertTrue(ezShop.endSaleTransaction(transactionId));
			assertFalse(ezShop.deleteProductFromSaleRFID(transactionId, RFID)); //
			// openSaleTransaction.getTicketNumber() == -1

			// assertTrue(ezShop.deleteProductFromSale(transactionId, barcode, 4)); // 18 euro

			// assertTrue(ezShop.applyDiscountRateToProduct(transactionId, barcode, 0.5));

			// assertTrue(ezShop.applyDiscountRateToSale(transactionId, 0.1));

			// assertTrue(ezShop.deleteSaleTransaction(transactionId));

			// assertEquals(2, ezShop.receiveCashPayment(transactionId, 20), 0.01); // 2 euro back
			//
			// int points = ezShop.computePointsForSale(transactionId);
			// assertEquals(1, points);
			//
			// assertTrue(ezShop.modifyPointsOnCard("1000000000", points));
			//
			// assertTrue(ezShop.endSaleTransaction(transactionId));
			//
			//
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
