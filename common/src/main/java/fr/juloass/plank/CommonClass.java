package fr.juloass.plank;

import com.google.gson.*;
import com.google.gson.stream.MalformedJsonException;
import com.mojang.datafixers.kinds.Const;
import fr.juloass.plank.platform.Services;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class CommonClass {

    private static void initializeItemGroupsFromLocalVersion() {
        Constants.LOG.info("Initializing item groups ...");
        // Step 1: Obtain local version JSON
        File localVersionFile = Services.PLATFORM.getLocalVersionFile();
        if (!localVersionFile.exists()) {
            Constants.LOG.error("Local version file does not exist. Cannot initialize items.");
            return; // Early exit if the file doesn't exist
        }

        try {
            String localVersionContent = FileUtils.readFileToString(localVersionFile, StandardCharsets.UTF_8);
            JsonObject localVersionJson = new Gson().fromJson(localVersionContent, JsonObject.class);

            // Step 2: Process items array
            JsonArray itemGroupsArray = localVersionJson.getAsJsonArray("creativeTabs");
            if (itemGroupsArray == null || itemGroupsArray.isEmpty()) {
                Constants.LOG.warn("No item groups found in local version JSON.");
                return; // Early exit if no items found
            }

            Constants.LOG.info("Initializing item groups from local version...");

            // Step 3: Register each item group
            for (JsonElement itemElement : itemGroupsArray) {
                JsonObject itemGroupInfo = itemElement.getAsJsonObject();

                String domain = null;
                String name = null;

                if(itemGroupInfo.has("domain")) domain = itemGroupInfo.get("domain").getAsString();
                if(itemGroupInfo.has("name")) name = itemGroupInfo.get("name").getAsString();

                if(domain == null || name == null) {
                    Constants.LOG.warn("Skipping Item Group because it is missing either domain, name or icon.");
                    return;
                }

                ItemGroupDefinition itemGroupDefinition = new ItemGroupDefinition.Builder()
                        .setDomain(domain)
                        .setName(name)
                        .build();

                Services.PLATFORM.createItemGroup(itemGroupDefinition);
            }

            Constants.LOG.info("Item Groups initialized successfully from local version.");

        }  catch (IOException e) {
            Constants.LOG.error("Error reading local version file: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            Constants.LOG.error("Error parsing local version JSON: " + e.getMessage());
        } catch (Exception e) {
            Constants.LOG.error("An unexpected error occurred while initializing item groups: {}", e.getMessage());
        }

    }

    public static void initializeItemsFromLocalVersion() {
        Constants.LOG.info("Initializing items ...");
        // Step 1: Obtain local version JSON
        File localVersionFile = Services.PLATFORM.getLocalVersionFile();
        if (!localVersionFile.exists()) {
            Constants.LOG.error("Local version file does not exist. Cannot initialize items.");
            return; // Early exit if the file doesn't exist
        }

        try {
            String localVersionContent = FileUtils.readFileToString(localVersionFile, StandardCharsets.UTF_8);
            JsonObject localVersionJson = new Gson().fromJson(localVersionContent, JsonObject.class);

            // Step 2: Process items array
            JsonArray itemsArray = localVersionJson.getAsJsonArray("items");
            if (itemsArray == null || itemsArray.size() == 0) {
                Constants.LOG.warn("No items found in local version JSON.");
                return; // Early exit if no items found
            }

            Constants.LOG.info("Initializing items from local version...");

            // Step 3: Create each item
            for (JsonElement itemElement : itemsArray) {
                JsonObject itemInfo = itemElement.getAsJsonObject();
                String domain = itemInfo.get("domain").getAsString();
                String name = itemInfo.get("name").getAsString();
                String itemGroup = "plank:base";
                if(itemInfo.has("creativeTab")) itemGroup = itemInfo.get("creativeTab").getAsString();
                boolean autoGenerate = !itemInfo.has("properties");

                ItemType type = ItemType.BASE; // Default type
                if (itemInfo.has("type")) {
                    String typeString = itemInfo.get("type").getAsString();
                    try {
                        type = new Gson().fromJson(typeString, ItemType.class);
                    } catch (JsonSyntaxException e) {
                        Constants.LOG.warn("Invalid item type '" + typeString + "' for item '" + name + "'. Defaulting to ItemType.BASE.");
                    }
                }

                // Create the ItemDefinition
                ItemDefinition itemDefinition = new ItemDefinition.Builder()
                        .setDomain(domain)
                        .setName(name)
                        .setAutoGenerate(autoGenerate)
                        .setItemGroup(itemGroup)
                        .setItemType(type)
                        .build();

                // Register or create the item using the service platform
                Services.PLATFORM.createItem(itemDefinition);
            }

            Constants.LOG.info("Items initialized successfully from local version.");

        } catch (IOException e) {
            Constants.LOG.error("Error reading local version file: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            Constants.LOG.error("Error parsing local version JSON: " + e.getMessage());
        } catch (Exception e) {
            Constants.LOG.error("An unexpected error occurred while initializing items: " + e.getMessage());
        }
    }

    public static void init() {
        Constants.LOG.info("Initializing Plank Mod...");

        // 1. Check if local version file exists
        File localVersionFile = Services.PLATFORM.getLocalVersionFile();
        String localVersion = "0.0.0"; // Default local version if the file doesn't exist
        JsonObject localVersionJson = null;

        try {
            // Load local version file as a JsonObject if it exists
            if (localVersionFile.exists()) {
                String localVersionContent = FileUtils.readFileToString(localVersionFile, StandardCharsets.UTF_8);
                localVersionJson = new Gson().fromJson(localVersionContent, JsonObject.class);
                localVersion = localVersionJson.get("version").getAsString();
                Constants.LOG.info("Local version: " + localVersion);
            } else {
                Constants.LOG.info("No local version file found, defaulting to version 0.0.0.");
            }

            // 2. Fetch the list of versions from the remote server
            JsonObject remoteVersionsJson = fetchRemoteVersions();

            // Extract the target version based on Constants.TARGET (e.g., "live", "beta")
            String targetVersion = remoteVersionsJson.get(Constants.TARGET_VERSION).getAsString();
            Constants.LOG.info("Target version from server: " + targetVersion);

            // 3. Compare local and target versions
            if (localVersion.equals(targetVersion)) {
                Constants.LOG.info("Local version is up-to-date. No update necessary.");
            } else {
                Constants.LOG.info("Local version is outdated. Updating to version " + targetVersion);
                processUpdate(targetVersion); // Call the update handler
            }

        } catch (IOException e) {
            if ("0.0.0".equals(localVersion)) {
                abortLaunch("Failed to reach the server and no local version available.");
            } else {
                Constants.LOG.error("Failed to fetch remote versions. Continuing with local version " + localVersion);
            }
        }

        // 4. Initialize Items
        initializeItemsFromLocalVersion();
        initializeItemGroupsFromLocalVersion();
        // 5. Initialize Blocks
        // 6. Initialize Books
        // 7. Initialize Mobs
    }

    /**
     * Fetch the remote versions.json file and parse it into a JsonObject.
     * @return JsonObject containing versions information.
     * @throws IOException If an error occurs during the fetch.
     */
    private static JsonObject fetchRemoteVersions() throws IOException {
        URL url = new URL(Constants.REMOTE_URL + "versions.json");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        Constants.LOG.info("Fetching remote versions at " + url.toString());
        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to connect to server. Response code: " + connection.getResponseCode());
        }

        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            String response = scanner.useDelimiter("\\A").next();
            try {
                return new Gson().fromJson(response, JsonObject.class);
            } catch(JsonSyntaxException e) {
                throw new IOException("Response is not valid JSON: " + e.getMessage());
            }
        }
    }

    /**
     * Process the update by fetching and applying the specified version's content.
     * @param targetVersion The version to update to.
     */
    private static void processUpdate(String targetVersion) {
        try {
            // This method will handle the actual downloading and processing of the target version's content
            Constants.LOG.info("Processing update for version " + targetVersion);

            // Fetch the target version.json
            JsonObject versionJson = fetchVersionJson(targetVersion);

            // TODO: Implement your logic to process the assets and content of the version
            processVersion(versionJson);

            // 1. Save the downloaded version.json to localVersionFile as the new local version
            updateLocalVersionFile(versionJson);

            Constants.LOG.info("Successfully updated to version " + targetVersion);

        } catch (IOException e) {
            Constants.LOG.error("Error during update to version " + targetVersion + ": ", e);
        }
    }

    private static void processVersion(JsonObject versionJson) throws IOException {

        String version = versionJson.get("version").getAsString();
        String author = versionJson.get("author").getAsString();
        long timestamp = versionJson.get("timestamp").getAsLong();
        JsonArray assets = versionJson.getAsJsonArray("assets");
        JsonArray items = versionJson.getAsJsonArray("items");

        Constants.LOG.info("Processing update " + version + " from author: " + author);
        Constants.LOG.info("Last updated : " + new java.text.SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss")
                .format(new java.util.Date(timestamp*1000)));

        File resourceFolder = Services.PLATFORM.getResourcesFolder(); // TODO : change to Services.PLATFORM.getResourcesFolder();
        File cacheFolder = Services.PLATFORM.getCacheFolder(); // TODO : change to service.platform.getCacheFolder

        resourceFolder.mkdirs();
        FileUtils.cleanDirectory(resourceFolder);

        // Process each asset
        for (JsonElement asset : assets.asList()) {
            JsonObject assetInfo = asset.getAsJsonObject();

            // Initialize variables for asset information
            String name = null;
            String path = null;
            String url = null;
            String md5 = null;
            long size = -1; // Default size if not present

            // Safely extract necessary fields
            try {
                if (assetInfo.has("name")) {
                    name = assetInfo.get("name").getAsString();
                } else {
                    Constants.LOG.error("Missing 'name' for asset. Skipping asset.");
                    continue; // Skip this asset
                }

                if (assetInfo.has("path")) {
                    path = assetInfo.get("path").getAsString();
                } else {
                    Constants.LOG.error("Missing 'path' for asset '" + name + "'. Skipping asset.");
                    continue; // Skip this asset
                }

                if (assetInfo.has("url")) {
                    url = assetInfo.get("url").getAsString();
                } else {
                    Constants.LOG.error("Missing 'url' for asset '" + name + "'. Skipping asset.");
                    continue; // Skip this asset
                }

                // Check for MD5
                if (assetInfo.has("md5")) {
                    md5 = assetInfo.get("md5").getAsString();
                } else {
                    Constants.LOG.warn("Missing 'md5' for asset '" + name + "'. Assigning default value.");
                    md5 = ""; // Assign default MD5 value
                }

                // Check for size
                if (assetInfo.has("size")) {
                    size = assetInfo.get("size").getAsLong();
                } else {
                    Constants.LOG.warn("Missing 'size' for asset '" + name + "'. Assigning default value of 0.");
                    size = -1; // Assign default size value
                }

                Constants.LOG.info("Processing asset: " + name);
                // Set the download path for the asset
                File downloadFile = new File(resourceFolder,path);

                // Check if md5 or size is default
                if (md5.isEmpty() || size == -1) {
                    // Download asset, compute md5 and cache it
                    downloadAsset(url, md5, size, downloadFile, true);
                    String computedMd5 = MD5Checksum.getMD5Checksum(downloadFile);
                    cacheAsset(downloadFile, computedMd5, cacheFolder);
                } else {
                    // Check cache for the asset
                    File cachedFile = new File(cacheFolder, md5);
                    if (cachedFile.exists()) {
                        // Asset already exists in cache, copy it to the resource folder
                        FileUtils.copyFile(cachedFile, downloadFile);
                        Constants.LOG.info("Copied asset '" + name + "' from cache.");
                    } else {
                        // Download asset since it does not exist in cache
                        downloadAsset(url, md5, size, downloadFile, true);
                        cacheAsset(downloadFile, md5, cacheFolder);
                    }
                }
            } catch (Exception e) {
                Constants.LOG.error("Error processing asset '" + name + "': " + e.getMessage());
                continue; // Skip this asset
            }

        }

    }

    /**
     * Caches a downloaded asset by copying it to the specified cache directory using its MD5 hash as the filename.
     *
     * @param assetFile The file object representing the downloaded asset to cache.
     * @param md5 The MD5 hash of the downloaded asset, used as the filename in the cache.
     * @param cacheFolder The folder where the asset should be cached.
     * @throws IOException If an error occurs during the caching process.
     */
    private static void cacheAsset(File assetFile, String md5, File cacheFolder) throws IOException {
        // Check if the cache folder exists, create it if not
        if (!cacheFolder.exists()) {
            if (!cacheFolder.mkdirs()) {
                throw new IOException("Failed to create cache folder: " + cacheFolder.getAbsolutePath());
            }
        }

        // Define the path for the cached file
        File cachedFile = new File(cacheFolder, md5);

        // Copy the asset file to the cache directory
        FileUtils.copyFile(assetFile, cachedFile);
        Constants.LOG.info("Cached asset '" + assetFile.getName() + "' as '" + cachedFile.getName() + "' in cache directory.");
    }


    private static void clearResourceFolder(File resourceFolder) {
        try {
            FileUtils.cleanDirectory(resourceFolder);
            Constants.LOG.info("Cleared resource folder: " + resourceFolder.getAbsolutePath());
        } catch (IOException e) {
            Constants.LOG.error("Failed to clear resource folder: " + e.getMessage());
        }
    }

    /**
     * Downloads an asset from the specified URL and validates it against the expected size and MD5 hash.
     *
     * @param url The URL of the asset to download.
     * @param md5 The expected MD5 hash of the downloaded file (can be empty if not provided).
     * @param size The expected size of the downloaded file in bytes (can be -1 if not provided).
     * @param downloadFile The file object representing the destination where the downloaded asset will be saved.
     * @param retry A boolean indicating whether to retry the download if validation fails.
     *               If true, the method will attempt to download the asset again after a validation failure.
     * @throws Exception If an error occurs during the download or validation process.
     */
    private static void downloadAsset(String url, String md5, long size, File downloadFile, boolean retry) throws Exception {
        // Implement the downloading logic here
        Constants.LOG.info("Downloading asset from: " + url);
        FileUtils.copyURLToFile(new URL(url), downloadFile);
        if(size == 0 || md5.equalsIgnoreCase("")) {
            Constants.LOG.info("Unsafely downloaded asset: " + downloadFile.getName());
            return;
        }
        if (!validateFile(downloadFile, md5, size)) {
            Constants.LOG.warn("Downloaded file validation failed for: " + downloadFile.getName());

            // If the validation fails and the retry flag is true, attempt to download the file again.
            if (retry) {
                Constants.LOG.info("Retrying download for: " + url);
                downloadAsset(url, md5, size, downloadFile, false); // Retry without further retries
            } else {
                FileUtils.delete(downloadFile);
                throw new IOException("File validation failed for: " + downloadFile.getName());
            }
        } else {
            Constants.LOG.info("Successfully downloaded and validated asset: " + downloadFile.getName());
        }

    }

    /**
     * Validates the downloaded file against its expected size and md5 hash.
     * @param file The file to validate.
     * @param expectedMd5 The expected MD5 hash of the file.
     * @param expectedSize The expected size of the file in bytes.
     * @return true if the file is valid; false otherwise.
     * @throws Exception if an error occurs during MD5checksum
     */
    private static boolean validateFile(File file, String expectedMd5, long expectedSize) throws Exception {
        // Check if the file exists
        if (!file.exists()) {
            Constants.LOG.error("File does not exist: " + file.getAbsolutePath());
            return false;
        }

        // Validate the size of the file
        if (expectedSize >= 0 && file.length() != expectedSize) {
            Constants.LOG.error("File size mismatch for: " + file.getName() +
                    " (expected: " + expectedSize + ", actual: " + file.length() + ")");
            return false;
        }

        // Validate the MD5 hash of the file
        if (!expectedMd5.isEmpty()) {
            String computedMd5 = MD5Checksum.getMD5Checksum(file);
            if (!computedMd5.equals(expectedMd5)) {
                Constants.LOG.error("MD5 mismatch for file: " + file.getName() +
                        " (expected: " + expectedMd5 + ", actual: " + computedMd5 + ")");
                return false;
            }
        }

        return true; // File is valid
    }

    /**
     * Fetches the version.json file for the specified target version.
     * @param targetVersion The version number to fetch.
     * @return JsonObject containing version information.
     * @throws IOException If an error occurs during the fetch.
     */
    private static JsonObject fetchVersionJson(String targetVersion) throws IOException {
        URL url = new URL(Constants.REMOTE_URL + "versions/" + targetVersion + ".json");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        Constants.LOG.info("Fetching remote version at " + url.toString());

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to connect to server. Response code: " + connection.getResponseCode());
        }

        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            String response = scanner.useDelimiter("\\A").next();
            return new Gson().fromJson(response, JsonObject.class);
        }
    }

    /**
     * Update the local version file with the new version information by saving the
     * downloaded target version.json.
     * @param versionJson The JsonObject of the new version to save locally.
     * @throws IOException If an error occurs during file writing.
     */
    private static void updateLocalVersionFile(JsonObject versionJson) throws IOException {
        File localVersionFile = new File("local_version.json");// Services.PLATFORM.getLocalVersionFile();

        // Write the downloaded target version.json content as the new local version file
        FileUtils.writeStringToFile(localVersionFile, versionJson.toString(), StandardCharsets.UTF_8);
        Constants.LOG.info("Updated local version file with downloaded version.");
    }

    /**
     * Abort the launch if critical failure happens (e.g., no local version and can't reach the remote).
     * @param message The error message to log before aborting.
     */
    private static void abortLaunch(String message) {
        Constants.LOG.error(message);
        throw new RuntimeException("Plank Mod cannot continue due to critical failure: " + message);
    }
}
