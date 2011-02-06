/**
 * 
 */
package com.bukkit.Vandolis.CodeRedEconomy.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.bukkit.Vandolis.CodeRedEconomy.EconomyProperties;

/**
 * @author Vandolis
 */
public class Items {
	private int		ID;
	private int		itemID;
	private boolean	subtyped	= false;
	private int		subtype		= 0;
	private int		breakValue	= 0;
	private String	name		= "";
	private int		buyPrice	= 0;
	private int		sellPrice	= 0;
	
	/**
	 * @param Id
	 *            of the SQL row
	 */
	public Items(int Id) {
		try {
			Connection conn = DriverManager.getConnection(EconomyProperties.getDB());
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select * from Items where ID = " + Id + ";");
			
			while (rs.next()) {
				itemID = rs.getInt("ItemID");
				subtyped = rs.getBoolean("Subtyped");
				subtype = rs.getInt("Subtype");
				breakValue = rs.getInt("BreakValue");
				name = rs.getString("Name");
				buyPrice = rs.getInt("BuyPrice");
				sellPrice = rs.getInt("SellPrice");
			}
			
			rs.close();
			stat.close();
			conn.close();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Items(int itemID, String itemName, int buyPrice, int sellPrice, int breakValue) {
		this.itemID = itemID;
		name = itemName;
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		this.breakValue = breakValue;
		
		update();
	}
	
	/**
	 * 
	 */
	private void update() {
		try {
			PreparedStatement prep =
					EconomyProperties.getConn().prepareStatement(
							"insert into Items (ItemID, Name, BuyPrice, SellPrice, BreakValue) values (?, ?, ?, ?, ?);");
			
			prep.setInt(1, itemID);
			prep.setString(2, name);
			prep.setInt(3, buyPrice);
			prep.setInt(4, sellPrice);
			prep.setInt(5, breakValue);
			
			prep.execute();
			
			prep.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param itemName
	 * @return SQL id
	 */
	protected static int getId(String itemName) {
		try {
			Statement stat = EconomyProperties.getConn().createStatement();
			
			ResultSet rs = stat.executeQuery("select Items.ID from Items where Name = '" + itemName + "';");
			
			int id = rs.getInt("ID");
			
			rs.close();
			
			return id;
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	
	/**
	 * @param itemID
	 * @param subtype
	 * @return SQL ID
	 */
	protected static int getID(int itemID, boolean subtyped, int subtype) {
		try {
			Statement stat = EconomyProperties.getConn().createStatement();
			ResultSet rs;
			
			if (subtyped) {
				rs = stat.executeQuery("select Items.ID from Items where ItemID = " + itemID + " and Subtype = " + subtype + ";");
			}
			else {
				rs = stat.executeQuery("select Items.ID from Items where ItemID = " + itemID + ";");
			}
			
			int id = rs.getInt("ID");
			
			rs.close();
			stat.close();
			
			return id;
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	
	/**
	 * @return the iD
	 */
	protected int getID() {
		return ID;
	}
	
	/**
	 * @return the itemID
	 */
	protected int getItemID() {
		return itemID;
	}
	
	/**
	 * @return the subtyped
	 */
	protected boolean isSubtyped() {
		return subtyped;
	}
	
	/**
	 * @return the subtype
	 */
	protected int getSubtype() {
		return subtype;
	}
	
	/**
	 * @return the breakValue
	 */
	public int getBreakValue() {
		return breakValue;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the buyPrice
	 */
	public int getBuyPrice() {
		return buyPrice;
	}
	
	/**
	 * @param buyPrice
	 *            the buyPrice to set
	 */
	public void setBuyPrice(int buyPrice) {
		this.buyPrice = buyPrice;
	}
	
	/**
	 * @return the sellPrice
	 */
	public int getSellPrice() {
		return sellPrice;
	}
	
	/**
	 * @param sellPrice
	 *            the sellPrice to set
	 */
	public void setSellPrice(int sellPrice) {
		this.sellPrice = sellPrice;
	}
	
	/**
	 * Removes the Items from the table
	 */
	protected void remove() {
		try {
			Statement stat = EconomyProperties.getConn().createStatement();
			stat.executeUpdate("delete from Items where ID = " + ID + ";");
			
			stat.close();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param typeId
	 * @return SQL ID
	 */
	public static int getID(int typeId, short durability) {
		try {
			Statement stat = EconomyProperties.getConn().createStatement();
			ResultSet rs = stat.executeQuery("select * from Items where ItemID = " + typeId + ";");
			
			while (rs.next()) {
				if (rs.getBoolean("IsSubtyped")) {
					if (rs.getInt("Subtype") == durability) {
						return rs.getInt("ID");
					}
				}
				else {
					return rs.getInt("ID");
				}
			}
			
			rs.close();
			stat.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
