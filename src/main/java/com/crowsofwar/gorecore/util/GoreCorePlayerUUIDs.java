package com.crowsofwar.gorecore.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.crowsofwar.gorecore.GoreCore;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;

/**
 * <p>
 * Contains utility methods for getting player's account UUIDs. Account UUIDs are the UUIDs on
 * Mojang, and entity UUIDs are the UUIDs gotten from Entity#getUniqueID().
 * </p>
 * 
 * <p>
 * UUID results are stored in a cache file, and are loaded from the cache as well.
 * </p>
 * 
 * @author CrowsOfWar
 */
public final class GoreCorePlayerUUIDs {
	
	/**
	 * A cache of UUIDs for quick use. It is saved via a UUID cache file. The UUID cache map size
	 * will never exceed the maximum UUID cache size.
	 */
	private static final Map<String, UUID> playerNameToUUID;
	
	static {
		playerNameToUUID = new HashMap<String, UUID>();
	}
	
	/**
	 * Clears the UUID cache, then reads the UUID cache from the cache file located in different
	 * places for client/server. This is so that the big cache list does not have to be re-created
	 * every time Minecraft restarts.
	 * 
	 * @see #saveUUIDCache()
	 */
	public static void addUUIDsToCacheFromCacheFile() {
		try {
			
			long start = System.currentTimeMillis();
			FMLLog.info("GoreCore> Reading UUIDs from cache file");
			
			playerNameToUUID.clear();
			
			File file = GoreCore.proxy.getUUIDCacheFile();
			if (!file.exists()) {
				file.createNewFile();
			}
			
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) continue;
				if (!line.contains("=")) continue;
				String[] split = line.split("=");
				if (split.length != 2) continue;
				
				// An IllegalArgumentException is thrown if split[1] is not a valid UUID string. It
				// self-validates!
				try {
					playerNameToUUID.put(split[0], UUID.fromString(split[1]));
				} catch (IllegalArgumentException e) {
					continue;
				}
				
			}
			
			FMLLog.info("GoreCore> Success! Read %1$d player UUID(s). Time taken in seconds: %2$f.", playerNameToUUID.entrySet().size(),
					(System.currentTimeMillis() - start) / 1000.0);
			
		} catch (Exception e) {
			FMLLog.severe("Error reading GoreCore player UUID cache from text file:");
			e.printStackTrace();
			FMLLog.severe("Please contact CrowsOfWar for help.");
		}
	}
	
	/**
	 * Saves the cache of UUIDs to a text file for reading later so that the UUID cache does not
	 * have to be re-built every time Minecraft restarts.
	 * 
	 * @see #addUUIDsToCacheFromCacheFile()
	 */
	public static void saveUUIDCache() {
		try {
			
			long start = System.currentTimeMillis();
			FMLLog.info("GoreCore> Saving UUIDs to cache file");
			
			File file = GoreCore.proxy.getUUIDCacheFile();
			if (!file.exists()) file.createNewFile();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			
			Iterator<Map.Entry<String, UUID>> entries = playerNameToUUID.entrySet().iterator();
			boolean next = entries.hasNext();
			
			bw.write("# This holds a cache of all the players' UUIDs determined by GoreCore\n");
			bw.write("# Please do not edit this file, or you may face strange problems like data deletion\n");
			bw.write("# This is re-written when Minecraft closes, so any of your comments will not be saved!" + (next ? "\n\n" : ""));
			
			while (next) {
				Map.Entry<String, UUID> current = entries.next();
				next = entries.hasNext();
				
				bw.write(current.getKey() + "=" + current.getValue() + (next ? "\n" : ""));
			}
			
			bw.close();
			
			FMLLog.info("GoreCore: Finished saving UUIDs. Time taken in seconds: %f.", (System.currentTimeMillis() - start) / 1000.0);
			
		} catch (Exception e) {
			FMLLog.severe("Error saving GoreCore player UUID cache to text file:");
			e.printStackTrace();
			FMLLog.severe("Please contact CrowsOfWar for help.");
		}
	}
	
	/**
	 * <p>
	 * Finds the player in the world whose account has the given UUID.
	 * </p>
	 * 
	 * <p>
	 * This is different from <code>world.func_152378_a(playerID)</code> in that the world's method
	 * uses the player's entity ID, while this method uses the player's account ID.
	 * 
	 * @param playerID
	 *            The UUID of the player to find
	 * @param world
	 *            The world to look for the player in
	 * @return
	 */
	public static EntityPlayer findPlayerInWorldFromUUID(World world, UUID playerID) {
		for (int i = 0; i < world.playerEntities.size(); i++) {
			if (getUUID(((EntityPlayer) world.playerEntities.get(i)).getCommandSenderName()).getUUID().equals(playerID)) {
				return (EntityPlayer) world.playerEntities.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * <p>
	 * Gets the UUID of the player with the given username. If it exists in the cache, the UUID will
	 * be obtained via the cache; otherwise, a HTTP request will be made to obtain the UUID.
	 * </p>
	 * 
	 * <p>
	 * The UUID found can be extracted from the UUID-result via {@link GetUUIDResult#getUUID()} as
	 * long as an error has not occurred.
	 * </p>
	 * 
	 * @param username
	 *            The username to get the UUID for
	 * @return The UUID result of the getting
	 */
	public static GoreCorePlayerUUIDs.GetUUIDResult getUUID(String username) {
		if (playerNameToUUID.containsKey(username)) {
			return new GetUUIDResult(playerNameToUUID.get(username), ResultOutcome.SUCCESS);
		} else {
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
				// "<html><head>...</head><body><p>HAI</p></body></html>" or something like that
				// for this it's a JSON
				String result = response.toString();
				
				if (responseCode == 204) {
					return new GetUUIDResult(null, ResultOutcome.USERNAME_DOES_NOT_EXIST);
				}
				
				if (responseCode != 200) {
					FMLLog.warning("GoreCore> Attempted to get a UUID for player " + username + ", but the response code was unexpected ("
							+ responseCode + ")");
					return new GetUUIDResult(null, ResultOutcome.BAD_HTTP_CODE);
				}
				
				String resultOfExtraction = result.replace("{", "");
				resultOfExtraction = resultOfExtraction.replace("}", "");
				resultOfExtraction = resultOfExtraction.substring(0, resultOfExtraction.indexOf(','));
				resultOfExtraction = resultOfExtraction.substring(resultOfExtraction.indexOf(':'), resultOfExtraction.length());
				resultOfExtraction = resultOfExtraction.replace("\"", "");
				resultOfExtraction = resultOfExtraction.replace(":", "");
				
				String uuidCleaned = resultOfExtraction.replaceAll("[^a-zA-Z0-9]", "");
				uuidCleaned = (uuidCleaned.substring(0, 8) + "-" + uuidCleaned.substring(8, 12) + "-" + uuidCleaned.substring(12, 16) + "-"
						+ uuidCleaned.substring(16, 20) + "-" + uuidCleaned.substring(20, 32));
				
				UUID uuidResult = UUID.fromString(uuidCleaned);
				
				cacheResults(username, uuidResult);
				return new GetUUIDResult(uuidResult, ResultOutcome.SUCCESS);
				
			} catch (Exception e) {
				FMLLog.severe("GoreCore> Error getting player UUID for username " + username);
				e.printStackTrace();
				return new GetUUIDResult(null, ResultOutcome.EXCEPTION_OCCURED);
			}
		}
	}
	
	/**
	 * <p>
	 * Gets the account UUID of the player with the given username. If it exists in the cache, the
	 * UUID will be obtained via the cache; otherwise, a HTTP request will be made to obtain the
	 * UUID.
	 * </p>
	 * 
	 * <p>
	 * The UUID found will be returned. If an error occurs, this will return null. A minimal amount
	 * of objects will be created.
	 * </p>
	 * 
	 * @param username
	 *            The username to get the UUID for
	 * @return The account UUID for the username, or null if an error occurred
	 */
	public static UUID getUUIDPerformance(String username) {
		if (playerNameToUUID.containsKey(username)) {
			return playerNameToUUID.get(username);
		} else {
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
				// "<html><head>...</head><body><p>HAI</p></body></html>" or something like that
				// for this it's a JSON
				String result = response.toString();
				
				if (responseCode == 204) return null;
				
				if (responseCode != 200) {
					FMLLog.warning("GoreCore> Attempted to get a UUID for player " + username + ", but the response code was unexpected ("
							+ responseCode + ")");
					return null;
				}
				
				String resultOfExtraction = result.replace("{", "");
				resultOfExtraction = resultOfExtraction.replace("}", "");
				resultOfExtraction = resultOfExtraction.substring(0, resultOfExtraction.indexOf(','));
				resultOfExtraction = resultOfExtraction.substring(resultOfExtraction.indexOf(':'), resultOfExtraction.length());
				resultOfExtraction = resultOfExtraction.replace("\"", "");
				resultOfExtraction = resultOfExtraction.replace(":", "");
				
				String uuidCleaned = resultOfExtraction.replaceAll("[^a-zA-Z0-9]", "");
				uuidCleaned = (uuidCleaned.substring(0, 8) + "-" + uuidCleaned.substring(8, 12) + "-" + uuidCleaned.substring(12, 16) + "-"
						+ uuidCleaned.substring(16, 20) + "-" + uuidCleaned.substring(20, 32));
				
				UUID uuidResult = UUID.fromString(uuidCleaned);
				
				cacheResults(username, uuidResult);
				return uuidResult;
				
			} catch (Exception e) {
				FMLLog.severe("GoreCore> Error getting player UUID for username " + username);
				e.printStackTrace();
				return null;
			}
		}
	}
	
	/**
	 * Caches the results for the given username. However, the cache will not be added on to if the
	 * cache is too large (if the size of the cache exceeds the maximum UUID cache size).
	 * 
	 * @param username
	 *            The username to store in the cache (key)
	 * @param uuid
	 *            The UUID to store in the cache (value)
	 */
	private static void cacheResults(String username, UUID uuid) {
		if (playerNameToUUID.size() < GoreCore.config.MAX_UUID_CACHE_SIZE) {
			playerNameToUUID.put(username, uuid);
		}
	}
	
	/**
	 * GetUUIDResult shows the result of getting UUIDs from player names through
	 * {@link GoreCorePlayerUUIDs#getUUID(String)}. It has a UUID for the result and a
	 * {@link ResultOutcome} that describes what happened.
	 * 
	 * @author CrowsOfWar
	 */
	public static class GetUUIDResult {
		private final UUID uuid;
		private final ResultOutcome outcome;
		
		public GetUUIDResult(UUID uuid, ResultOutcome outcome) {
			this.uuid = uuid;
			this.outcome = outcome;
		}
		
		/**
		 * Gets the UUID of this UUID result. If the result isn't successful, it is null.
		 */
		public UUID getUUID() {
			return uuid;
		}
		
		/**
		 * Gets the outcome of the result.
		 */
		public ResultOutcome getResult() {
			return outcome;
		}
		
		/**
		 * Returns whether the result of fetching the UUID is successful or not; that is, whether
		 * the UUID could be found correctly and did not have any errors.
		 */
		public boolean isResultSuccessful() {
			return outcome == ResultOutcome.SUCCESS && uuid != null;
		}
		
		/**
		 * Logs an error to the console if the UUID-get was unsuccessful.
		 */
		public void logError() {
			if (!isResultSuccessful()) {
				String text = "There's a bug with the error warning code! o_o";
				if (outcome == ResultOutcome.USERNAME_DOES_NOT_EXIST)
					text = "The player was not registered on Minecraft.net - are you using a cracked launcher?";
				if (outcome == ResultOutcome.EXCEPTION_OCCURED)
					text = "An unexpected error (specifically, an exception) occured while getting the player's UUID";
				if (outcome == ResultOutcome.BAD_HTTP_CODE) text = "Got an unexpected HTTP code";
				FMLLog.warning("GoreCore> Attempted to get a player's UUID but failed: " + text);
			}
		}
		
		@Override
		public String toString() {
			return "GetUUIDResult[uuid=" + uuid + ", outcome=" + outcome + "]";
		}
		
	}
	
	/**
	 * ResultOutcome enumerates different possibilities for results of getting UUIDs from usernames.
	 * Using this enum will allow for handling on errors/success based on what specific things
	 * happened.
	 * 
	 * @author CrowsOfWar
	 */
	public static enum ResultOutcome {
		/**
		 * No errors were encountered while getting the UUID.
		 */
		SUCCESS,
		
		/**
		 * The player is not registered in minecraft.net, so the username has no UUID.
		 */
		USERNAME_DOES_NOT_EXIST,
		
		/**
		 * The HTTP code has no handling for it (handled ones are 200s and 204s)
		 */
		BAD_HTTP_CODE,
		
		/**
		 * An exception occured while trying to get the result
		 */
		EXCEPTION_OCCURED;
	}
	
}
