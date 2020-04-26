package me.wolfyscript.customcrafting.gui.main_gui;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.TestCache;
import me.wolfyscript.customcrafting.data.cache.items.Items;
import me.wolfyscript.customcrafting.gui.ExtendedGuiWindow;
import me.wolfyscript.customcrafting.gui.Setting;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.custom_items.CustomItem;
import me.wolfyscript.utilities.api.custom_items.CustomItems;
import me.wolfyscript.utilities.api.inventory.GuiUpdateEvent;
import me.wolfyscript.utilities.api.inventory.InventoryAPI;
import me.wolfyscript.utilities.api.inventory.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.button.buttons.ActionButton;
import me.wolfyscript.utilities.api.utils.InventoryUtils;
import me.wolfyscript.utilities.api.utils.NamespacedKey;
import me.wolfyscript.utilities.api.utils.chat.ClickData;
import me.wolfyscript.utilities.api.utils.chat.ClickEvent;
import me.wolfyscript.utilities.api.utils.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class ItemEditor extends ExtendedGuiWindow {

    public ItemEditor(InventoryAPI inventoryAPI, CustomCrafting customCrafting) {
        super("item_editor", inventoryAPI, 45, customCrafting);
    }

    @Override
    public void onInit() {
        registerButton(new ActionButton("back", new ButtonState("none", "back", WolfyUtilities.getCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0="), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            guiHandler.openPreviousInv();
            if (!((TestCache) guiHandler.getCustomCache()).getSetting().equals(Setting.ITEMS)) {
                guiHandler.openCluster("recipe_creator");
            }
            return true;
        })));
        registerButton(new ActionButton("load_item", new ButtonState("load_item", Material.ITEM_FRAME, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            TestCache cache = (TestCache) guiHandler.getCustomCache();
            cache.getChatLists().setCurrentPageItems(1);
            sendItemListExpanded(player);
            guiHandler.setChatInputAction((guiHandler1, player1, s, args) -> {
                if (args.length > 1) {
                    Items items = ((TestCache) guiHandler.getCustomCache()).getItems();
                    CustomItem customItem = CustomItems.getCustomItem(args[0], args[1], false);
                    if (customItem == null) {
                        sendMessage(player1, "error");
                        return true;
                    }
                    ((TestCache) guiHandler1.getCustomCache()).getChatLists().setLastUsedItem(customItem.getId());

                    if (items.isRecipeItem()) {
                        cache.applyItem(customItem);
                        sendMessage(player1, "item_applied");
                        if (!cache.getSetting().equals(Setting.ITEMS)) {
                            guiHandler.openCluster("recipe_creator");
                        } else {
                            guiHandler.openPreviousInv();
                        }
                        return false;
                    }
                    if (InventoryUtils.hasInventorySpace(player1, customItem)) {
                        player1.getInventory().addItem(customItem.getItemStack());
                        api.sendPlayerMessage(player1, "$commands.give.success$", new String[]{"%PLAYER%", player1.getDisplayName()}, new String[]{"%ITEM%", customItem.getId()});
                    } else {
                        api.sendPlayerMessage(player1, "$commands.give.no_inv_space$");
                    }
                    return false;
                }
                sendMessage(player1, "no_name");
                return true;
            });
            return true;
        })));
        registerButton(new ActionButton("create_item", new ButtonState("create_item", Material.ITEM_FRAME, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            guiHandler.changeToInv("item_creator", "main_menu");
            return true;
        })));
        registerButton(new ActionButton("edit_item", new ButtonState("edit_item", Material.REDSTONE, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            Items items = ((TestCache) guiHandler.getCustomCache()).getItems();
            if (items.isRecipeItem()) {
                if (items.isSaved()) {
                    items.setItem(CustomItems.getCustomItem(items.getNamespacedKey()));
                }
                guiHandler.changeToInv("item_creator", "main_menu");
            } else {
                ((TestCache) guiHandler.getCustomCache()).getChatLists().setCurrentPageItems(1);
                sendItemListExpanded(player);
                guiHandler.setChatInputAction((guiHandler1, player1, s, args) -> {
                    if (args.length > 1) {
                        Items items1 = ((TestCache) guiHandler.getCustomCache()).getItems();
                        CustomItem customItem = CustomItems.getCustomItem(args[0], args[1], false);
                        if (customItem == null) {
                            sendMessage(player1, "error");
                            return true;
                        }
                        ((TestCache) guiHandler1.getCustomCache()).getChatLists().setLastUsedItem(customItem.getId());
                        items1.setItem(false, customItem);
                        sendMessage(player1, "item_editable");
                        Bukkit.getScheduler().runTask(api.getPlugin(), () -> guiHandler1.changeToInv("item_creator", "main_menu"));
                        return false;
                    }
                    sendMessage(player1, "no_name");
                    return true;
                });
                guiHandler.close();
            }
            return true;
        })));
        registerButton(new ActionButton("delete_item", new ButtonState("delete_item", Material.BARRIER, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            ((TestCache) guiHandler.getCustomCache()).getChatLists().setCurrentPageItems(1);
            sendItemListExpanded(player);
            guiHandler.setChatInputAction((guiHandler1, player1, s, args) -> {
                if (args.length > 1) {
                    CustomItem customItem = CustomItems.getCustomItem(new NamespacedKey(args[0], args[1]), false);
                    if (customItem == null) {
                        sendMessage(player1, "error");
                        return true;
                    }
                    ((TestCache) guiHandler1.getCustomCache()).getChatLists().setLastUsedItem(customItem.getId());
                    CustomItems.removeCustomItem(customItem);
                    System.gc();
                    if (CustomCrafting.hasDataBaseHandler()) {
                        CustomCrafting.getDataBaseHandler().removeItem(customItem.getConfig().getNamespacedKey());
                    } else {
                        customItem.getConfig().getConfigFile().deleteOnExit();
                    }
                    return false;
                }
                sendMessage(player1, "no_name");
                return true;
            });
            guiHandler.close();
            return true;
        })));
    }

    @EventHandler
    public void onUpdate(GuiUpdateEvent event) {
        if (event.verify(this)) {
            event.setButton(0, "back");
            TestCache cache = (TestCache) event.getGuiHandler().getCustomCache();
            if (cache.getItems().isRecipeItem()) {
                event.setButton(20, "load_item");
                event.setButton(22, "create_item");
                event.setButton(24, "edit_item");
            } else {
                event.setButton(20, "create_item");
                event.setButton(22, "edit_item");
                event.setButton(24, "delete_item");
                event.setButton(31, "load_item");
            }
        }
    }

    private void sendItemListExpanded(Player player) {
        TestCache cache = ((TestCache) api.getInventoryAPI().getGuiHandler(player).getCustomCache());
        for (int i = 0; i < 20; i++) {
            player.sendMessage(" ");
        }

        int currentPage = cache.getChatLists().getCurrentPageItems();
        int itemsPerPage = cache.getChatLists().getLastUsedItem().equals("") ? 16 : 14;
        int maxPages = ((CustomItems.getCustomItems().size() % itemsPerPage) > 0 ? 1 : 0) + CustomItems.getCustomItems().size() / itemsPerPage;

        api.sendActionMessage(player,
                new ClickData("[&3« Back&7]", null, new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cc")),
                new ClickData("                   &7&lItems            ", null),
                new ClickData("&7[&e&l«&7]", (wolfyUtilities, p) -> {
                    if (currentPage > 1) {
                        cache.getChatLists().setCurrentPageItems(cache.getChatLists().getCurrentPageItems() - 1);
                    }
                    sendItemListExpanded(p);
                }, true),
                new ClickData(" &e" + currentPage + "&7/&6" + maxPages + "", null),
                new ClickData(" &7[&e&l»&7]", (wolfyUtilities, p) -> {
                    if (currentPage < maxPages) {
                        cache.getChatLists().setCurrentPageItems(cache.getChatLists().getCurrentPageItems() + 1);
                    }
                    sendItemListExpanded(p);
                }, true));
        api.sendPlayerMessage(player, "&8-------------------------------------------------");

        for (int i = (currentPage - 1) * itemsPerPage; i < (currentPage - 1) * itemsPerPage + itemsPerPage; i++) {
            if (i < CustomItems.getCustomItems().size()) {
                CustomItem customItem = CustomItems.getCustomItems().get(i);
                api.sendActionMessage(player, new ClickData((i % 2 == 1 ? "§3" : "§7") + " - ", null), new ClickData(customItem.getId(), null, new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, customItem.getId().split(":")[0] + " " + customItem.getId().split(":")[1]), new HoverEvent(customItem)));
            } else {
                api.sendPlayerMessage(player, "");
            }
        }

        if (!cache.getChatLists().getLastUsedItem().equals("")) {
            api.sendPlayerMessage(player, "§ePreviously used:");
            CustomItem customItem = CustomItems.getCustomItem(cache.getChatLists().getLastUsedItem());
            if (customItem != null) {
                api.sendActionMessage(player, new ClickData("§b - ", null), new ClickData(customItem.getId(), null, new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, customItem.getId().split(":")[0] + " " + customItem.getId().split(":")[1]), new HoverEvent(customItem)));
            }
        }
        api.sendPlayerMessage(player, "&8-------------------------------------------------");
        sendMessage(player, "input");
    }
}
