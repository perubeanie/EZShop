package it.polito.ezshop.data.Implementations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import it.polito.ezshop.data.ProductType;
import it.polito.ezshop.data.SaleTransaction;
import it.polito.ezshop.data.TicketEntry;

public class SaleTransactionImpl implements SaleTransaction {

	private Integer ticketNumber;
	private double price;
	private LinkedList<TicketEntry> entries;
	private double discountRate;
	private String creditCard;
	private Integer balanceId;
	private HashMap<String, String> RFIDs;

	public SaleTransactionImpl(Integer ticketNumber) {

		this.ticketNumber = ticketNumber;
		this.price = 0;
		this.entries = new LinkedList<TicketEntry>();
		this.discountRate = 0;
		this.creditCard = "";
		this.balanceId = -1;
		this.RFIDs = new HashMap<String, String>();

	}

	public void addProductToSale(ProductType productType, int amount) {

		String barCode = productType.getBarCode();

		// update in entry or insert new entry if it doesn't exist
		String productDescription = productType.getProductDescription();
		double pricePerUnit = productType.getPricePerUnit();
		for (TicketEntry entry : entries) {
			if (entry.getBarCode().equals(barCode)) {
				entry.setAmount(entry.getAmount() + amount);
				this.setPrice(this.price + amount * pricePerUnit * (1 - entry.getDiscountRate()));
				return;
			}
		}

		TicketEntry newEntry = new TicketEntryImpl(barCode, productDescription, pricePerUnit, 0, amount);
		entries.add(newEntry);
		this.setPrice(this.price + amount * pricePerUnit); // discountRate=0 until applyDiscountRateToProduct is called

		return;

	}

	public void addProductToSaleRFID(ProductType productType, String RFID) {

		String barCode = productType.getBarCode();

		// update in entry or insert new entry if it doesn't exist
		String productDescription = productType.getProductDescription();
		double pricePerUnit = productType.getPricePerUnit();
		for (TicketEntry entry : entries) {
			if (entry.getBarCode().equals(barCode)) {
				this.RFIDs.put(RFID, barCode);
				entry.setAmount(entry.getAmount() + 1);
				this.setPrice(this.price + 1 * pricePerUnit * (1 - entry.getDiscountRate()));
				return;
			}
		}

		TicketEntry newEntry = new TicketEntryImpl(barCode, productDescription, pricePerUnit, 0, 1);
		this.RFIDs.put(RFID, barCode);
		entries.add(newEntry);
		this.setPrice(this.price + 1 * pricePerUnit); // discountRate=0 until applyDiscountRateToProduct is called

		return;

	}

	public boolean deleteProductFromSale(String barCode, int amount) {
		// amount to remove from sale, amount to add to store

		// remove amount from entry if amount<previous amount, deletes entry if
		// amount==previous amount, return false if amount>previous amount

		boolean result = false;
		Iterator<TicketEntry> iter = entries.iterator();
		while (iter.hasNext()) {
			TicketEntry entry = iter.next();
			if (entry.getBarCode().equals(barCode)) { // product present in the saleTransaction
				int previousAmount = entry.getAmount();
				if (amount < previousAmount) {
					entry.setAmount(previousAmount - amount);
					this.setPrice(this.price - (amount * entry.getPricePerUnit() * (1 - entry.getDiscountRate())));
					result = true;
				} else if (amount == previousAmount) {
					iter.remove();
					this.setPrice(this.price - (amount * entry.getPricePerUnit() * (1 - entry.getDiscountRate())));
					result = true;
				} else
					break;
				// else if (amountToRemove > previousAmount) result=false;
				// System.out.println("Found item to remove" + entry);
			}
		}
		// if product not present in the saleTransaction result==false

		return result;

	}

	public boolean deleteProductFromSaleRFID(String barCode, String RFID) {
		// amount to remove from sale, amount to add to store

		// remove amount from entry if amount<previous amount, deletes entry if
		// amount==previous amount, return false if amount>previous amount
		boolean result = this.RFIDs.remove(RFID, barCode);

		if (result) {
			Iterator<TicketEntry> iter = entries.iterator();
			while (iter.hasNext()) {
				TicketEntry entry = iter.next();
				if (entry.getBarCode().equals(barCode)) { // product present in the saleTransaction
					int previousAmount = entry.getAmount();
					if (1 < previousAmount) {
						entry.setAmount(previousAmount - 1);
						this.setPrice(this.price - (1 * entry.getPricePerUnit() * (1 - entry.getDiscountRate())));
						result = true;
					} else if (1 == previousAmount) {
						iter.remove();
						this.setPrice(this.price - (1 * entry.getPricePerUnit() * (1 - entry.getDiscountRate())));
						result = true;
					} else
						break;
					// else if (amountToRemove > previousAmount) result=false;
					// System.out.println("Found item to remove" + entry);
				}
			}
			// if product not present in the saleTransaction result==false
		}
		return result;

	}

	public boolean applyDiscountRateToProduct(String barCode, double discountRate) {

		for (TicketEntry entry : entries) {
			if (entry.getBarCode().equals(barCode)) {
				double entryCost = entry.getAmount() * entry.getPricePerUnit() * (1 - entry.getDiscountRate());
				this.price = this.price - entryCost; // removed entry cost
				entry.setDiscountRate(discountRate);
				this.price = entry.getAmount() * entry.getPricePerUnit() * (1 - entry.getDiscountRate()); // new entry
																											// cost
				// System.out.println(entry);
				return true;
			}
		}
		return false;

	}

	public void applyDiscountRateToSale(double discountRate) {

		this.price = this.price / (1 - this.discountRate);// full price
		this.discountRate = discountRate;
		this.price = this.price * (1 - this.discountRate);// new discounted price

	}

	@Override
	public Integer getTicketNumber() {

		return ticketNumber;

	}

	@Override
	public void setTicketNumber(Integer ticketNumber) {

		this.ticketNumber = ticketNumber;

	}

	@Override
	public double getPrice() {

		return price;

	}

	@Override
	public void setPrice(double price) {

		this.price = price;

	}

	@Override
	public List<TicketEntry> getEntries() {

		return entries;

	}

	@Override
	public void setEntries(List<TicketEntry> entries) {

		this.entries = (LinkedList<TicketEntry>) entries;

	}

	@Override
	public double getDiscountRate() {

		return discountRate;

	}

	@Override
	public void setDiscountRate(double discountRate) {

		this.discountRate = discountRate;

	}

	public String getCreditCard() {

		return creditCard;

	}

	public void setCreditCard(String creditCard) {

		this.creditCard = creditCard;

	}

	public Integer getBalanceId() {

		return balanceId;

	}

	public void setBalanceId(Integer balanceId) {

		this.balanceId = balanceId;

	}

	public HashMap<String, String> getRFIDs() {

		return RFIDs;

	}

	public void setRFIDs(HashMap<String, String> rFIDs) {

		RFIDs = rFIDs;

	}

	@Override
	public String toString() {

		return "SaleTransactionImpl [ticketNumber=" + ticketNumber + ", price=" + price + ", entries=" + entries
				+ ", discountRate=" + discountRate + ", creditCard=" + creditCard + ", balanceId=" + balanceId
				+ ", RFIDs=" + RFIDs + "]";

	}

}
