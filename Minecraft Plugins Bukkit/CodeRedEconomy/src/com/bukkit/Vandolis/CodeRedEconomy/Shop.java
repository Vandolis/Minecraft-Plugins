/*
 * Economy made for the Redstrype Minecraft Server. Copyright (C) 2010 Michael Robinette This program is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 */
package com.bukkit.Vandolis.CodeRedEconomy;

import org.bukkit.Server;

/**
 * Class that handles buy/sell of items.
 * 
 * @author Vandolis
 */
public class Shop extends EconEntity {
	private final String	regex			= DataManager.getShopRegex();
	private final String	regex2			= DataManager.getShop2Regex();
	private boolean			infItems		= false;
	private long			lastRestock		= 0;
	private boolean			canRestock		= true;
	private boolean			usersCanBuy		= true;
	private boolean			usersCanSell	= true;
	private boolean			hidden			= false;
	
	/**
	 * Loads a shop from a save string.
	 * 
	 * @param saveData
	 */
	public Shop(String saveData) {
		load(saveData);
		setShop(this);
	}
	
	/**
	 * Makes a shop with the given name, given infItems value, and given {@link Money} amount. Preferred constructor.
	 * 
	 * @param name
	 *            Name of the shop
	 * @param infItems
	 *            Have infinite stock of items or not
	 * @param amountMoney
	 *            Starting amount of money.
	 * @param canRestock
	 *            Let the shop restock
	 */
	public Shop(String name, boolean infItems, int amountMoney, boolean canRestock) {
		super(new Money(amountMoney));
		this.infItems = infItems;
		this.name = name;
		this.canRestock = canRestock;
		setShop(this);
		initialize();
	}
	
	/**
	 * @return the restock
	 */
	public boolean isRestock() {
		return canRestock;
	}
	
	/**
	 * @param restock
	 *            the restock to set
	 */
	public void setRestock(boolean restock) {
		canRestock = restock;
	}
	
	/**
	 * Function ran when a player tries to buy an item. Parses the command used to get the item name as well as the amount. If no amount is
	 * given the default value is 1.
	 * 
	 * @param user
	 * @param split
	 */
	public void buy(User user, String[] split) {
		String itemName = "";
		int amount = 1;
		
		/*
		 * Check the command length to make sure it is at least 2. [0] = /buy [1] = itemName
		 */
		if (split.length >= 2) {
			try {
				/*
				 * Loop through the split and grab the itemName as well as the amount.
				 */
				for (String iter : split) {
					/*
					 * Skip the first value as it is the /buy
					 */
					if (!iter.equalsIgnoreCase(split[0])) {
						/*
						 * Try and convert into the amount, if that fails it must be part of the name.
						 */
						try {
							amount = Integer.valueOf(iter);
						}
						catch (NumberFormatException e) {
							/*
							 * Not a number, add to name.
							 */
							itemName += iter + " ";
						}
					}
				}
				
				/*
				 * Trim the name for use and show debug info.
				 */
				itemName = itemName.trim();
				
				if (DataManager.getDebug()) {
					System.out.println("Item name is: " + itemName);
				}
				
				/*
				 * Check to make sure the amount is nonnegative as well as the itemName has a value.
				 */
				if ((amount > 0) && !itemName.equalsIgnoreCase("")) {
					if (DataManager.getDebug()) {
						System.out.println("Valid, processing.");
					}
					
					ShopItemStack stack = new ShopItemStack(ShopItem.getId(itemName), amount);
					
					Transaction.process(new Transaction(this, user, stack));
				}
				else {
					user.sendMessage("Please enter a valid amount and/or item name.");
				}
			}
			catch (Exception e2) {
				user.sendMessage("The correct use is /buy [item name] [amount]");
			}
		}
		else {
			user.sendMessage("The correct use is /buy [item name] [amount]");
		}
	}
	
	/**
	 * Stocks the shop with items.
	 */
	private void initialize() {
		restock();
		setShop(this);
	}
	
	/**
	 * Loads a shop from a save string.
	 * 
	 * @param loadData
	 */
	public void load(String loadData) {
		String sc[] = loadData.split(DataManager.getShopRegex()); // Split colon
		
		if (DataManager.getDebug()) {
			System.out.println("Loading a shop named " + sc[0]);
		}
		
		/*
		 * Safety check of length then load the data.
		 */
		if (sc.length >= 4) {
			name = sc[0];
			money.setAmount(Integer.valueOf(sc[1]));
			infItems = Boolean.valueOf(sc[2]);
			lastRestock = Long.valueOf(sc[3]);
			for (String iter : sc) {
				if (!iter.equalsIgnoreCase(sc[0]) && !iter.equalsIgnoreCase(sc[1]) && !iter.equalsIgnoreCase(sc[2])
						&& !iter.equalsIgnoreCase(sc[3])) {
					String[] ss = iter.split(regex2);
					if (ss.length == 2) {
						/*
						 * Old format
						 */
						availableItems.add(new ShopItemStack(Integer.valueOf(ss[0].trim()), Integer.valueOf(ss[1].trim())));
					}
					else {
						/*
						 * New format
						 */
						availableItems.add(new ShopItemStack(Integer.valueOf(ss[0].trim()), Integer.valueOf(ss[2].trim()), Integer
								.valueOf(ss[1].trim()), Integer.valueOf(ss[3].trim())));
					}
				}
			}
		}
		else {
			initialize();
		}
	}
	
	/**
	 * Tries to restock the {@link Shop}. If not forced, checks the last restock time against the current {@link Server} time.
	 * 
	 * @param force
	 */
	public void restock(boolean force) {
		/*
		 * Check the current time against the last restock time
		 */
		if (((DataManager.getServer().getTime() - lastRestock >= DataManager.getRestockTime()) || force) && canRestock) {
			System.out.println("Restocking " + name);
			lastRestock = DataManager.getServer().getTime();
			for (ShopItemStack iter : availableItems) {
				/*
				 * Checks the current value against the max value for the item. 
				 * Only restocks the item if the current amount is less than the max.
				 */
				if (iter.getAmountAvail() < iter.getMaxAvail()) {
					iter.setAmountAvail(iter.getMaxAvail());
				}
			}
			
			if (force) {
				boolean found = false;
				for (ShopItem iter : DataManager.getItemList()) {
					found = false;
					for (ShopItem current : availableItems) {
						if (iter.getItemId() == current.getItemId()) {
							found = true;
						}
					}
					
					if (!found) {
						availableItems.add(new ShopItemStack(iter.getItemId(), iter.getMaxAvail()));
					}
				}
			}
		}
	}
	
	/**
	 * Normal restock call, not forced.
	 */
	public void restock() {
		restock(false);
	}
	
	/**
	 * Function ran when a player tries to sell an item. Parses the command used to get the item name as well as the amount. If no amount is
	 * given the default value is 1.
	 * 
	 * @param user
	 * @param split
	 */
	public void sell(User user, String[] split) {
		String itemName = "";
		int amount = 1;
		
		/*
		 * Check the command length to make sure it is at least 2. [0] = /sell [1] = itemName
		 */
		if (split.length >= 2) {
			try {
				/*
				 * Loop through the split and grab the itemName as well as the amount.
				 */
				for (String iter : split) {
					/*
					 * Skip the first one as it is /sell
					 */
					if (!iter.equalsIgnoreCase(split[0])) {
						/*
						 * Try and convert it into the amount, if that fails it must be part of the name
						 */
						try {
							amount = Integer.valueOf(iter);
						}
						catch (NumberFormatException e) {
							/*
							 * Not a number, add to the name.
							 */
							itemName += iter + " ";
						}
					}
				}
				
				/*
				 * Trim the name for use and print debug info
				 */
				itemName = itemName.trim();
				if (DataManager.getDebug()) {
					System.out.println("Item name is: " + itemName);
				}
				
				/*
				 * Check to make sure the amount is nonnegative and the itemName is not empty.
				 */
				if ((amount > 0) && !itemName.equalsIgnoreCase("")) {
					/*
					 * Process a new transaction with the parsed data
					 */
					try {
						Transaction.process(new Transaction(user, this, new ShopItemStack(DataManager.getItemId(itemName), amount)));
					}
					catch (NullPointerException e1) {
						user.sendMessage(itemName + " cannot be sold.");
					}
				}
				else {
					user.sendMessage("Please enter a valid amount and/or item name.");
				}
			}
			catch (NumberFormatException e1) {
				user.sendMessage("The correct use is /sell [item name] [amount]");
			}
		}
		else {
			user.sendMessage("The correct use is /sell [item name] [amount]");
		}
	}
	
	/**
	 * Returns the save string to write to file. Format is ShopName:Shop Money:infAmount:lastRestock:idamount:id amount:id amount
	 * 
	 * @return
	 */
	public String getSaveString() {
		// Used for saving the shop, data needed is:
		// 
		String temp = name + regex + money.getAmount() + regex + infItems + regex + lastRestock;
		
		for (ShopItemStack iter : availableItems) {
			temp += regex + iter.getItemId() + regex2 + iter.getBuyPrice() + regex2 + iter.getSellPrice() + regex2 + iter.getAmountAvail();
		}
		
		return temp;
	}
	
	/**
	 * Returns if the {@link Shop} has all infinite items
	 * 
	 * @return
	 */
	public boolean getInfItems() {
		return infItems;
	}
	
	/**
	 * Returns the last time the {@link Shop} was restocked.
	 * 
	 * @return
	 */
	public long getLastRestock() {
		return lastRestock;
	}
	
	/**
	 * @return the canRestock
	 */
	protected boolean isCanRestock() {
		return canRestock;
	}
	
	/**
	 * @param canRestock
	 *            the canRestock to set
	 */
	protected void setCanRestock(boolean canRestock) {
		this.canRestock = canRestock;
	}
	
	/**
	 * @return the usersCanBuy
	 */
	protected boolean isUsersCanBuy() {
		return usersCanBuy;
	}
	
	/**
	 * @param usersCanBuy
	 *            the usersCanBuy to set
	 */
	protected void setUsersCanBuy(boolean usersCanBuy) {
		this.usersCanBuy = usersCanBuy;
	}
	
	/**
	 * @return the usersCanSell
	 */
	protected boolean isUsersCanSell() {
		return usersCanSell;
	}
	
	/**
	 * @param usersCanSell
	 *            the usersCanSell to set
	 */
	protected void setUsersCanSell(boolean usersCanSell) {
		this.usersCanSell = usersCanSell;
	}
	
	/**
	 * @return the showInLists
	 */
	protected boolean isHidden() {
		return hidden;
	}
	
	/**
	 * @param showInLists
	 *            the showInLists to set
	 */
	protected void setShowInLists(boolean showInLists) {
		hidden = showInLists;
	}
	
	/**
	 * @param infItems
	 *            the infItems to set
	 */
	protected void setInfItems(boolean infItems) {
		this.infItems = infItems;
	}
	
	/**
	 * @param lastRestock
	 *            the lastRestock to set
	 */
	protected void setLastRestock(long lastRestock) {
		this.lastRestock = lastRestock;
	}
	
	/**
	 * Returns the number of items in the inventory with a given name. Returns 0 if the item is not listed.
	 * 
	 * @param itemName
	 * @return
	 */
	public int getAvailableCount(String itemName) {
		for (ShopItemStack iter : availableItems) {
			if (iter.getName().equalsIgnoreCase(itemName)) {
				return iter.getAmountAvail();
			}
		}
		return 0;
	}
}
