/* 
  This file is part of AvatarMod.
    
  AvatarMod is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  AvatarMod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with AvatarMod. If not, see <http://www.gnu.org/licenses/>.
*/

package com.crowsofwar.gorecore.util;

import static java.util.UUID.randomUUID;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.crowsofwar.gorecore.GoreCore;
import com.crowsofwar.gorecore.settings.GoreCoreModConfig;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;

/**
 * <p>
 * Contains utility methods for getting player's account UUIDs. Account UUIDs
 * are the UUIDs on Mojang, and entity UUIDs are the UUIDs gotten from
 * Entity#getUniqueID().
 * </p>
 * 
 * <p>
 * UUID results are stored in a cache file, and are loaded from the cache as
 * well.
 * </p>
 * 
 * @author CrowsOfWar
 */
public final class AccountUUIDs {
	
	/**
	 * The cache of usernames -> account IDs, which is also saved to a cache
	 * file. Never exceeds {@link GoreCoreModConfig#MAX_UUID_CACHE_SIZE maximum
	 * cache size}.
	 */
	private static final Map<String, AccountId> idCache;
	
	static {
		idCache = new HashMap<String, AccountId>();
	}
	
	/**
	 * Clears the UUID cache, then reads the UUID cache from the cache file
	 * located in different places for client/server. This is so that the big
	 * cache list does not have to be re-created every time Minecraft restarts.
	 * 
	 * @see #saveCache()
	 */
	public static void readCache() {
		BufferedReader br = null;
		try {
			
			long start = System.currentTimeMillis();
			GoreCore.LOGGER.info("Reading UUIDs from cache file");
			
			idCache.clear();
			
			File file = GoreCore.proxy.getUUIDCacheFile();
			if (!file.exists()) {
				file.createNewFile();
			}
			
			br = new BufferedReader(new FileReader(file));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) continue;
				if (!line.contains("=")) continue;
				
				String[] split = line.split("=");
				if (split.length != 2) continue;
				
				boolean temp = false;
				if (line.startsWith("%")) {
					temp = true;
					line = line.substring(1);
					GoreCore.LOGGER.warn("UUID cache for player " + split[0]
							+ " is temporary, connect to online to fix this");
				}
				
				try {
					idCache.put(split[0], new AccountId(UUID.fromString(split[1]), temp));
				} catch (IllegalArgumentException e) {
					GoreCore.LOGGER.warn("UUID cache contains invalidly formatted UUID for player " + split[0]
							+ ", skipping");
				}
				
			}
			
			GoreCore.LOGGER.info("Finished reading " + idCache.entrySet().size() + " player UUID(s)");
			
		} catch (Exception e) {
			FMLLog.severe("Error reading GoreCore player UUID cache from text file:");
			e.printStackTrace();
			FMLLog.severe("Please contact CrowsOfWar for help.");
		} finally {
			if (br != null) try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Saves the cache of UUIDs to a text file for reading later so that the
	 * UUID cache does not have to be re-built every time Minecraft restarts.
	 * 
	 * @see #readCache()
	 */
	public static void saveCache() {
		try {
			
			long start = System.currentTimeMillis();
			GoreCore.LOGGER.info("Saving UUIDs to cache file");
			
			File file = GoreCore.proxy.getUUIDCacheFile();
			if (!file.exists()) file.createNewFile();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			Iterator<Map.Entry<String, AccountId>> entries = idCache.entrySet().iterator();
			boolean next = entries.hasNext();
			
			String ln = System.getProperty("line.separator");
			bw.write("# This holds a cache of all the players' UUIDs determined by GoreCore" + ln);
			bw.write("# Please do not edit this file, or you may face strange problems like data deletion"
					+ ln);
			bw.write("# This is re-written when Minecraft closes, so any of your comments will not be saved!"
					+ (next ? ln + ln : ""));
			
			while (next) {
				Map.Entry<String, AccountId> current = entries.next();
				next = entries.hasNext();
				String username = current.getKey();
				AccountId account = current.getValue();
				bw.write((account.temporary ? "%" : "") + username + "=" + account.uuid + ln);
			}
			
			bw.close();
			
			GoreCore.LOGGER.info("GoreCore: Finished saving UUIDs. Time taken in seconds: %f.",
					(System.currentTimeMillis() - start) / 1000.0);
			
		} catch (Exception e) {
			GoreCore.LOGGER.error("Error saving GoreCore player UUID cache to text file", e);
			GoreCore.LOGGER.error("Please contact CrowsOfWar to fix it.");
		}
	}
	
	/**
	 * <p>
	 * Finds the player in the world whose account has the given UUID.
	 * </p>
	 * 
	 * <p>
	 * This is different from <code>world.func_152378_a(playerID)</code> in that
	 * the world's method uses the player's entity ID, while this method uses
	 * the player's account ID.
	 * 
	 * @param playerID
	 *            The UUID of the player to find
	 * @param world
	 *            The world to look for the player in
	 * @return
	 */
	public static EntityPlayer findEntityFromUUID(World world, UUID playerID) {
		for (int i = 0; i < world.playerEntities.size(); i++) {
			UUID accountId = getId(world.playerEntities.get(i).getName()).getUUID();
			if (accountId.equals(playerID)) {
				return world.playerEntities.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * <p>
	 * Gets the UUID of the player with the given username. If it exists in the
	 * cache, the UUID will be obtained via the cache; otherwise, a HTTP request
	 * will be made to obtain the UUID.
	 * </p>
	 * 
	 * <p>
	 * The UUID found can be extracted from the UUID-result via
	 * {@link AccountId#getUUID()} as long as an error has not occurred.
	 * </p>
	 * 
	 * @param username
	 *            The username to get the UUID for
	 * @return The UUID result of the getting
	 */
	public static AccountUUIDs.AccountId getId(String username) {
		if (idCache.containsKey(username)) {
			return idCache.get(username);
		} else {
			UUID found = requestId(username);
			return cacheResults(username, found == null ? new AccountId() : new AccountId(found));
		}
	}
	
	public static void tryFixId(String playerName) {
		AccountId id = getId(playerName);
		if (id.isTemporary()) {
			
		}
	}
	
	/**
	 * Caches the results for the given username. However, the cache will not be
	 * added on to if the cache is too large (if the size of the cache exceeds
	 * the maximum UUID cache size).
	 * 
	 * @param username
	 *            The username to store in the cache (key)
	 * @param id
	 *            The account ID to store in the cache (value)
	 * @return id parameter
	 */
	private static AccountId cacheResults(String username, AccountId id) {
		if (idCache.size() < GoreCore.config.MAX_UUID_CACHE_SIZE) {
			idCache.put(username, id);
		}
		return id;
	}
	
	/**
	 * Sends a request to Mojang's API and get the player's UUID. Returns null
	 * if any error occurred.
	 */
	private static UUID requestId(String username) {
		try {
			String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
			
			URL obj = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
			
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			
			int responseCode = connection.getResponseCode();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = br.readLine()) != null)
				response.append(line);
			br.close();
			
			// For normal webpages, it would be like
			// "<html><head>...</head><body><p>HAI</p></body></html>" or
			// something like that
			// for this it's a JSON
			String result = response.toString();
			
			if (responseCode == 204) {
				return null;
			}
			
			if (responseCode != 200) {
				GoreCore.LOGGER.warn("Attempted to get a UUID for player " + username
						+ ", but the response code was unexpected (" + responseCode + ")");
				return null;
			}
			
			String resultOfExtraction = result.replace("{", "");
			resultOfExtraction = resultOfExtraction.replace("}", "");
			resultOfExtraction = resultOfExtraction.substring(0, resultOfExtraction.indexOf(','));
			resultOfExtraction = resultOfExtraction.substring(resultOfExtraction.indexOf(':'),
					resultOfExtraction.length());
			resultOfExtraction = resultOfExtraction.replace("\"", "");
			resultOfExtraction = resultOfExtraction.replace(":", "");
			
			String uuidCleaned = resultOfExtraction.replaceAll("[^a-zA-Z0-9]", "");
			uuidCleaned = (uuidCleaned.substring(0, 8) + "-" + uuidCleaned.substring(8, 12) + "-"
					+ uuidCleaned.substring(12, 16) + "-" + uuidCleaned.substring(16, 20) + "-"
					+ uuidCleaned.substring(20, 32));
			
			UUID uuidResult = UUID.fromString(uuidCleaned);
			return uuidResult;
			
		} catch (Exception e) {
			GoreCore.LOGGER.error("Unexpected error getting UUID for " + username, e);
			return null;
		}
	}
	
	/**
	 * GetUUIDResult shows the result of getting UUIDs from player names through
	 * {@link AccountUUIDs#getId(String)}. It has a UUID for the result and a
	 * {@link Outcome} that describes what happened.
	 * 
	 * @author CrowsOfWar
	 */
	public static class AccountId {
		private final UUID uuid;
		private final boolean temporary;
		
		/**
		 * Creates a temporary ID using a randomly generated UUID
		 */
		public AccountId() {
			this.uuid = randomUUID();
			this.temporary = true;
		}
		
		/**
		 * Creates an account ID from a successful result
		 * 
		 * @param uuid
		 *            UUID obtained from Mojang API
		 */
		public AccountId(UUID uuid) {
			this.uuid = uuid;
			this.temporary = false;
		}
		
		/**
		 * Creates an account ID which might be temporary
		 */
		public AccountId(UUID uuid, boolean temporary) {
			this.uuid = uuid;
			this.temporary = temporary;
		}
		
		/**
		 * Gets the UUID of this UUID result. If the result isn't successful, it
		 * is null.
		 */
		public UUID getUUID() {
			return uuid;
		}
		
		/**
		 * Returns whether this ID is temporary. If an error occurred when
		 * accessing Mojang's API, a random UUID is generated and this will
		 * return true.
		 * 
		 * @return
		 */
		public boolean isTemporary() {
			return temporary;
		}
		
		@Override
		public String toString() {
			return "AccountId[uuid=" + uuid + ",temporary=" + temporary + "]";
		}
		
	}
	
}
