/*
 *       ____ _  _ ____ ___ ____ _  _ ____ ____ ____ ____ ___ _ _  _ ____
 *       |    |  | [__   |  |  | |\/| |    |__/ |__| |___  |  | |\ | | __
 *       |___ |__| ___]  |  |__| |  | |___ |  \ |  | |     |  | | \| |__]
 *
 *       CustomCrafting Recipe creation and management tool for Minecraft
 *                      Copyright (C) 2021  WolfyScript
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.wolfyscript.customcrafting.handlers;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.CustomRecipe;
import me.wolfyscript.customcrafting.recipes.RecipeLoader;
import me.wolfyscript.customcrafting.recipes.RecipeType;
import me.wolfyscript.customcrafting.utils.ChatUtils;
import me.wolfyscript.customcrafting.utils.NamespacedKeyUtils;
import me.wolfyscript.lib.com.fasterxml.jackson.databind.InjectableValues;
import me.wolfyscript.utilities.api.inventory.custom_items.CustomItem;
import me.wolfyscript.utilities.util.NamespacedKey;
import me.wolfyscript.utilities.util.json.jackson.JacksonUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class LocalStorageLoader extends ResourceLoader {

    public static final File DATA_FOLDER = new File(CustomCrafting.inst().getDataFolder() + File.separator + "data");
    private static final String ITEMS_FOLDER = "items";
    private static final String RECIPES_FOLDER = "recipes";

    protected LocalStorageLoader(CustomCrafting customCrafting) {
        super(customCrafting, new NamespacedKey(customCrafting, "local_loader"));
    }

    @Override
    public void load() {
        /* CustomCrafting/data/<namespace>/
         *   recipes/<folder>/<recipe_name>
         *   items/<folder>/<item_name>
         */
        api.getConsole().info("- - - - [Local Storage] - - - -");
        api.getConsole().info("Searching for namespaces...");
        String[] dirs = DATA_FOLDER.list();
        if (dirs != null) {
            api.getConsole().info("Namespaces: [" + String.join(", ", dirs) + "]");
            api.getConsole().info(" - ");
            api.getConsole().info("Loading items...");
            for (String dir : dirs) {
                loadItemsInNamespace(dir);
            }
            api.getConsole().info("Loading recipes...");
            new NewDataLoader(dirs).load();
            //Loading old & legacy recipes
            //The recipes are only loaded if they are not already loaded in previous stages! So if a new version of a recipe exists, then the older ones are ignored.
            new OldDataLoader(dirs).load();
            new LegacyDataLoader(dirs).load();

            api.getConsole().info("Loaded " + customCrafting.getRegistries().getRecipes().values().size() + " recipes");
            api.getConsole().info("");
        }
    }

    /**
     * Gets the file at the specific path from the {@link NamespacedKey} and sub-folder.
     *
     * @param namespacedKey The NamespacedKey for the path.
     * @param typeFolder The sub-folder of the path. Like {@link #ITEMS_FOLDER} or {@link #RECIPES_FOLDER}.
     * @return The File at the specific path.
     */
    private File getFileAt(NamespacedKey namespacedKey, String typeFolder) {
        return new File(DataHandler.JSON_OBJ_PATH.formatted(NamespacedKeyUtils.getKeyRoot(namespacedKey), typeFolder, NamespacedKeyUtils.getRelativeKeyObjPath(namespacedKey)));
    }

    /**
     * Gets the NamespacedKey from the namespace and path.
     *
     * @param namespace The namespace in the data folder.
     * @param path The rest of the path.
     * @return The NamespacedKey from the namespace and path.
     */
    private NamespacedKey keyFromFile(String namespace, Path path) {
        String pathString = path.toString();
        return new NamespacedKey(customCrafting, namespace + "/" + pathString.substring(0, pathString.lastIndexOf(".")));
    }

    private void loadItemsInNamespace(String namespace) {
        var customItems = customCrafting.getApi().getRegistries().getCustomItems();
        readFiles(namespace, ITEMS_FOLDER, (relative, file, attrs) -> {
            var namespacedKey = keyFromFile(namespace, relative);
            if (isReplaceData() || !customItems.has(namespacedKey)) {
                try {
                    customItems.register(namespacedKey, objectMapper.readValue(file.toFile(), CustomItem.class));
                } catch (IOException e) {
                    customCrafting.getLogger().severe(String.format("Could not load item '%s':", namespacedKey));
                    e.printStackTrace();
                    customCrafting.getLogger().severe("----------------------");
                }
            }
            return FileVisitResult.CONTINUE;
        });
    }

    private void readFiles(String namespace, String subFolder, NamespaceFileVisitor.VisitFile<Path> visitFile) {
        var dataFile = new File(DATA_FOLDER, namespace + File.separator + subFolder);
        if (dataFile.exists()) {
            try {
                var root = dataFile.toPath();
                Files.walkFileTree(root, new NamespaceFileVisitor<>(root, visitFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean save(CustomRecipe<?> recipe) {
        File file = getFileAt(recipe.getNamespacedKey(), RECIPES_FOLDER);
        if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
            try {
                if (file.isFile() || file.createNewFile()) {
                    JacksonUtil.getObjectWriter(customCrafting.getConfigHandler().getConfig().isPrettyPrinting()).writeValue(file, recipe);
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean save(CustomItem item) {
        if (item.getNamespacedKey() != null) {
            var key = item.getNamespacedKey();
            if (key != null) {
                var file = getFileAt(key, ITEMS_FOLDER);
                if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
                    try {
                        if (file.exists() || file.createNewFile()) {
                            JacksonUtil.getObjectWriter(CustomCrafting.inst().getConfigHandler().getConfig().isPrettyPrinting()).writeValue(file, item);
                            return true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean delete(CustomRecipe<?> recipe) {
        File file = getFileAt(recipe.getNamespacedKey(), recipe.getRecipeType().getId());
        System.gc();
        if (file.delete()) {
            return true;
        } else {
            file.deleteOnExit();
        }
        return false;
    }

    @Override
    public boolean delete(CustomItem item) {
        System.gc();
        var key = item.getNamespacedKey();
        if (key != null) {
            var file = getFileAt(key, ITEMS_FOLDER);
            if (file.delete()) {
                return true;
            } else {
                file.deleteOnExit();
            }
        }
        return false;
    }

    private class NewDataLoader extends DataLoader {

        private NewDataLoader(String[] dirs) {
            super(dirs);
        }

        @Override
        protected void load() {
            for (String dir : dirs) {
                loadRecipesInNamespace(dir); //Load new recipe format files
            }
            api.getConsole().getLogger().info(String.format("[DEFAULT] Loaded %d recipes; skipped: %d error/s, %d already existing", loaded.size(), skippedError.size(), skippedAlreadyExisting.size()));
        }

        private void loadRecipesInNamespace(String namespace) {
            var injectableValues = new InjectableValues.Std();
            readFiles(namespace, RECIPES_FOLDER, (relative, file, attrs) -> {
                var namespacedKey = keyFromFile(namespace, relative);
                if (isReplaceData() || !customCrafting.getRegistries().getRecipes().has(namespacedKey)) {
                    try {
                        injectableValues.addValue("key", namespacedKey);
                        customCrafting.getRegistries().getRecipes().register(objectMapper.reader(injectableValues).readValue(file.toFile(), CustomRecipe.class));
                        loaded.add(namespacedKey);
                    } catch (IOException e) {
                        ChatUtils.sendRecipeItemLoadingError(namespacedKey.getNamespace(), namespacedKey.getKey(), "", e);
                        skippedError.add(namespacedKey);
                    }
                } else {
                    skippedAlreadyExisting.add(namespacedKey);
                }
                return FileVisitResult.CONTINUE;
            });
        }

    }

    private class LegacyDataLoader extends OldDataLoader {

        private LegacyDataLoader(String[] dirs) {
            super(dirs);
        }

        @Override
        protected void load() {
            for (String dir : this.dirs) {
                String[] typeFolders = getOldTypeFolders(dir);
                if (typeFolders != null && typeFolders.length > 0) {
                    loadAndRegisterOldOrLegacyRecipe(RecipeType.Container.CRAFTING, dir);
                    loadAndRegisterOldOrLegacyRecipe(RecipeType.Container.ELITE_CRAFTING, dir);
                }
            }
            api.getConsole().getLogger().info(String.format("[LEGACY] Loaded %d recipes; skipped: %d error/s, %d already existing", loaded.size(), skippedError.size(), skippedAlreadyExisting.size()));
        }
    }

    private class OldDataLoader extends DataLoader {

        private OldDataLoader(String[] dirs) {
            super(dirs);
        }

        protected void load() {
            for (String dir : this.dirs) {
                String[] typeFolders = getOldTypeFolders(dir);
                if (typeFolders != null && typeFolders.length > 0) {
                    for (RecipeType<? extends CustomRecipe<?>> type : RecipeType.values()) {
                        loadAndRegisterOldOrLegacyRecipe(type, dir);
                    }
                }
            }
            api.getConsole().getLogger().info(String.format("[OLD] Loaded %d recipes; skipped: %d error/s, %d already existing", loaded.size(), skippedError.size(), skippedAlreadyExisting.size()));
        }

        protected List<File> getOldOrLegacyFiles(String subFolder, String type) {
            var data = new File(DATA_FOLDER, subFolder + File.separator + type);
            if (!data.exists()) return new ArrayList<>();
            File[] files = data.listFiles(file -> file.isFile() && file.getName().endsWith(".json"));
            return files != null ? Arrays.stream(files).toList() : new ArrayList<>();
        }

        protected void loadAndRegisterOldOrLegacyRecipe(RecipeLoader<?> loader, String namespace) {
            if (loader instanceof RecipeType.Container<?> container && container.hasLegacy()) {
                //Loading legacy recipes
                List<File> legacyFiles = getOldOrLegacyFiles(namespace, container.getLegacyID());
                if (!legacyFiles.isEmpty()) { //If there are no legacy recipes we can skip it.
                    loadOldOrLegacyRecipeFiles(loader, legacyFiles, namespace);
                }
                return;
            }
            loadOldOrLegacyRecipeFiles(loader, getOldOrLegacyFiles(namespace, loader.getId()), namespace);
        }

        protected void loadOldOrLegacyRecipeFiles(RecipeLoader<?> loader, List<File> files, String namespace) {
            for (File file : files) {
                var name = file.getName();
                var namespacedKey = new NamespacedKey(customCrafting, namespace + "/" + name.substring(0, name.lastIndexOf(".")));
                if (isReplaceData() || !customCrafting.getRegistries().getRecipes().has(namespacedKey)) {
                    try {
                        customCrafting.getRegistries().getRecipes().register(loader.getInstance(namespacedKey, objectMapper.readTree(file)));
                        loaded.add(namespacedKey);
                    } catch (IOException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                        ChatUtils.sendRecipeItemLoadingError(namespacedKey.getNamespace(), namespacedKey.getKey(), loader.getId(), e);
                        skippedError.add(namespacedKey);
                    }
                } else {
                    skippedAlreadyExisting.add(namespacedKey);
                }
            }
        }

        protected String[] getOldTypeFolders(String namespace) {
            return  new File(DATA_FOLDER + "/" + namespace).list((dir1, name) -> !name.equals(ITEMS_FOLDER) && !name.equals(RECIPES_FOLDER));
        }

    }

    /**
     * Used to load data & cache the loaded, skipped errors & already existing keys.
     */
    private abstract class DataLoader {

        protected List<NamespacedKey> loaded;
        protected List<NamespacedKey> skippedError;
        protected List<NamespacedKey> skippedAlreadyExisting;
        protected final String[] dirs;

        private DataLoader(String[] dirs) {
            this.dirs = dirs;
            this.loaded = new LinkedList<>();
            this.skippedError = new LinkedList<>();
            this.skippedAlreadyExisting = new LinkedList<>();
        }

        protected abstract void load();

    }

    /**
     * A simple file visitor that that goes through the file tree of the specified namespace using the custom {@link VisitFile<T>} callback.
     *
     * @param <T> The type of the path.
     */
    private static class NamespaceFileVisitor<T extends Path> extends SimpleFileVisitor<T> {

        private final Path root;
        private final VisitFile<T> visitFile;

        private NamespaceFileVisitor(Path root, VisitFile<T> visitFile) {
            this.root = root;
            this.visitFile = visitFile;
        }

        @Override
        public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
            return visitFile.visit(root.relativize(file), file, attrs);
        }

        private interface VisitFile<T extends Path> {

            FileVisitResult visit(Path relative, T file, BasicFileAttributes attrs);

        }

    }

}
