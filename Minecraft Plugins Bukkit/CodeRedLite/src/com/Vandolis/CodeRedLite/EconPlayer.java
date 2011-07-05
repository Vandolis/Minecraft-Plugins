/**
 *
 */
package com.Vandolis.CodeRedLite;

import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.entity.Player;

/**
 * @author Vandolis
 */
public class EconPlayer
{
	private int			sqlID				= 0;
	private int			balance				= 0;
	private int			lastShopTransaction	= 0;
	private Player		player				= null;
	private CodeRedLite	plugin				= null;
	
	/**
	 * Default ctor, makes an EconPlayer with the given player
	 * 
	 * @param player
	 */
	public EconPlayer(Player player, CodeRedLite instance)
	{
		this.player = player;
		plugin = instance;
	}
	
	/**
	 * Sets the
	 * 
	 * @param codeRed
	 */
	//	protected static void setPlugin(CodeRedLite codeRed)
	//	{
	//		plugin = codeRed;
	//	}
	
	/**
	 * @param int1
	 */
	public void setSQLID(int int1)
	{
		sqlID = int1;
	}
	
	/**
	 * @param int1
	 */
	public void setBalance(int int1)
	{
		balance = int1;
	}
	
	/**
	 * @param int1
	 */
	public void setLastShopTransaction(int int1)
	{
		lastShopTransaction = int1;
	}
	
	/**
	 * @param autoPayTime
	 */
	public void addMoney(int payAmount)
	{
		balance += payAmount;
	}
	
	public void removeMoney(int amount)
	{
		balance -= amount;
	}
	
	/**
	 * @return SQLID
	 */
	public int getSQLID()
	{
		return sqlID;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public void update()
	{
		try
		{
			plugin.getSQL().update(this);
		}
		catch (SQLException e)
		{
			plugin.getLog().log(
				Level.WARNING,
					"CodeRedLite could not save " + player.getName() + "\'s player data. SQL Error: "
						+ e.getErrorCode());
		}
	}
	
	public int getBalance()
	{
		return balance;
	}
	
	public int getLastShopTransaction()
	{
		return lastShopTransaction;
	}
	
	public void unload()
	{
		plugin.unloadPlayer(getPlayer());
	}
}
