package it.polito.ezshop.data.Implementations;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import it.polito.ezshop.data.BalanceOperation;
import it.polito.ezshop.data.SaleTransaction;
import it.polito.ezshop.data.TicketEntry;

public class SaleTransactionImpl implements SaleTransaction {

	Integer ticketNumber;
	double price;
	LinkedList<TicketEntry> entries;
	double discountRate;
	private BalanceOperation balanceOperation;

	public SaleTransactionImpl(Integer ticketNumber) {

		this.ticketNumber = ticketNumber;
		this.price = 0;
		this.entries = new LinkedList<TicketEntry>();
		this.discountRate = 0;
		this.balanceOperation = null;

	}

	public TicketEntry upsertEntry(String barCode, String productDescription, double pricePerUnit, double discountRate,
			int amount) { // update in entry or insert new entry if it doesn't exist

		for (TicketEntry entry : entries) {
			if (entry.getBarCode() == barCode) {
				entry.setAmount(entry.getAmount() + amount);
				entry.setDiscountRate(discountRate);
				return entry;
			}
		}

		TicketEntry newEntry = new TicketEntryImpl(barCode, productDescription, pricePerUnit, discountRate, amount);
		entries.add(newEntry);

		// this.setPrice(this.price + amount * pricePerUnit * (1 - discountRate));

		return newEntry;

	}

	public TicketEntry getEntry(String barCode) {

		TicketEntry entry = null;
		for (TicketEntry e : entries) {
			if (e.getBarCode() == barCode) {
				entry = e;
				break;
			}
		}
		return entry;

	}

	public void addEntry(String barCode, String productDescription, double pricePerUnit, double discountRate,
			int amount) {

		entries.add(new TicketEntryImpl(barCode, productDescription, pricePerUnit, discountRate, amount));

	}

	public Boolean removeAmountFromEntry(String barCode, int amountToRemove) {
		// remove amount from entry if amount<previous amount, deletes entry if
		// amount==previous amount, return false if amount>previous amount

		Boolean updated = false;
		Iterator<TicketEntry> iter = entries.iterator();
		while (iter.hasNext()) {
			TicketEntry entry = iter.next();
			if (entry.getBarCode() == barCode) { // product present in the saleTransaction
				int previousAmount = entry.getAmount();
				if (amountToRemove < previousAmount) {
					entry.setAmount(previousAmount - amountToRemove);
					updated = true;
					break;
				} else if (amountToRemove == previousAmount) {
					iter.remove();
					updated = true;
					break;
				}
				// else if (amountToRemove > previousAmount) updated=false;
				System.out.println("Found item to remove" + entry);
				break;
			}
		}
		// if product not present in the saleTransaction updated==false
		return updated;

	}

	public void setDiscountRateToProduct(String barCode, double discountRate) {

		for (TicketEntry entry : entries) {
			if (entry.getBarCode() == barCode) {
				entry.setDiscountRate(discountRate);
				System.out.println(entry);
				break;
			}
		}

	}

	public double computePrice() {

		double price = 0;
		for (TicketEntry entry : entries)
			price += entry.getPricePerUnit() * entry.getAmount() * (1 - entry.getDiscountRate());
		price = price * (1 - this.getDiscountRate());
		this.setDiscountRate(price);
		return price;

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

	public BalanceOperation getBalanceOperation() {

		return balanceOperation;

	}

	public void setBalanceOperation(BalanceOperation balanceOperation) {

		this.balanceOperation = balanceOperation;

	}

	@Override
	public String toString() {

		return "SaleTransactionImpl [ticketNumber=" + this.getTicketNumber() + ", money=" + this.price + " entries="
				+ entries + ", discountRate=" + discountRate + "]";

	}

}
