package com.gmail.nossr50.util;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.config.AdvancedConfig;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.events.fake.FakeBlockBreakEvent;
import com.gmail.nossr50.events.fake.FakeBlockDamageEvent;
import com.gmail.nossr50.events.fake.FakePlayerAnimationEvent;
import com.gmail.nossr50.events.items.McMMOItemSpawnEvent;
import com.gmail.nossr50.mods.ModChecks;
import com.gmail.nossr50.party.PartyManager;

public final class Misc {
    private static Random random = new Random();
    public static int toolDurabilityLoss = Config.getInstance().getAbilityToolDamage();
    public static int abilityLengthIncreaseLevel = AdvancedConfig.getInstance().getAbilityLength();
    public static boolean isSpawnerXPEnabled = Config.getInstance().getExperienceGainsMobspawnersEnabled();
    public static final int PLAYER_RESPAWN_COOLDOWN_SECONDS = 5;
    public static final int TIME_CONVERSION_FACTOR = 1000;
    public static final double SKILL_MESSAGE_MAX_SENDING_DISTANCE = 10.0;
    //Sound Pitches & Volumes from CB
    public static final float ANVIL_USE_PITCH = 0.3F; // Not in CB directly, I went off the place sound values
    public static final float ANVIL_USE_VOLUME = 1.0F; // Not in CB directly, I went off the place sound values
    public static final float FIZZ_PITCH = 2.6F + (Misc.getRandom().nextFloat() - Misc.getRandom().nextFloat()) * 0.8F;
    public static final float FIZZ_VOLUME = 0.5F;
    public static final float POP_PITCH = ((getRandom().nextFloat() - getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F;
    public static final float POP_VOLUME = 0.2F;

    private Misc() {};

    public static boolean isFriendlyPet(Player attacker, Tameable pet) {
        if (pet.isTamed()) {
            AnimalTamer tamer = pet.getOwner();

            if (tamer instanceof Player) {
                Player owner = (Player) tamer;

                if (owner == attacker || PartyManager.inSameParty(attacker, owner)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isNPCEntity(Entity entity) {
        if (entity == null || entity.hasMetadata("NPC")) {
            return true;
        }

        return false;
    }

    public static boolean isNPCPlayer(Player player) {
        if (player == null || player.hasMetadata("NPC")) {
            return true;
        }

        return false;
    }

    /**
     * Checks to see if an entity is currently invincible.
     *
     * @param le The LivingEntity to check
     * @param event The event the entity is involved in
     * @return true if the entity is invincible, false otherwise
     */
    public static boolean isInvincible(LivingEntity le, EntityDamageEvent event) {

        /*
         * So apparently if you do more damage to a LivingEntity than its last damage int you bypass the invincibility.
         * So yeah, this is for that.
         */
        if (le.getNoDamageTicks() > le.getMaximumNoDamageTicks() / 2.0F && event.getDamage() <= le.getLastDamage()) {
            return true;
        }

        return false;
    }

    /**
     * Simulate a block break event.
     *
     * @param block The block to break
     * @param player The player breaking the block
     * @param shouldArmSwing true if an armswing event should be fired, false otherwise
     * @return true if the event wasn't cancelled, false otherwise
     */
    public static boolean blockBreakSimulate(Block block, Player player, Boolean shouldArmSwing) {

        //Support for NoCheat
        if (shouldArmSwing) {
            FakePlayerAnimationEvent armswing = new FakePlayerAnimationEvent(player);
            mcMMO.p.getServer().getPluginManager().callEvent(armswing);
        }

        PluginManager pluginManger = mcMMO.p.getServer().getPluginManager();

        FakeBlockDamageEvent damageEvent = new FakeBlockDamageEvent(player, block, player.getItemInHand(), true);
        pluginManger.callEvent(damageEvent);

        FakeBlockBreakEvent breakEvent = new FakeBlockBreakEvent(block, player);
        pluginManger.callEvent(breakEvent);

        if (!damageEvent.isCancelled() && !breakEvent.isCancelled()) {
            return true;
        }

        return false;
    }

    /**
     * Get the upgrade tier of the item in hand.
     *
     * @param inHand The item to check the tier of
     * @return the tier of the item
     */
    public static int getTier(ItemStack inHand) {
        int tier = 0;

        if (ItemChecks.isWoodTool(inHand)) {
            tier = 1;
        }
        else if (ItemChecks.isStoneTool(inHand)) {
            tier = 2;
        }
        else if (ItemChecks.isIronTool(inHand)) {
            tier = 3;
        }
        else if (ItemChecks.isGoldTool(inHand)) {
            tier = 1;
        }
        else if (ItemChecks.isDiamondTool(inHand)) {
            tier = 4;
        }
        else if (ModChecks.isCustomTool(inHand)) {
            tier = ModChecks.getToolFromItemStack(inHand).getTier();
        }

        return tier;
    }

    /**
     * Determine if two locations are near each other.
     *
     * @param first The first location
     * @param second The second location
     * @param maxDistance The max distance apart
     * @return true if the distance between <code>first</code> and <code>second</code> is less than <code>maxDistance</code>, false otherwise
     */
    public static boolean isNear(Location first, Location second, double maxDistance) {
        if (!first.getWorld().equals(second.getWorld())) {
            return false;
        }

        if (first.distanceSquared(second) < (maxDistance * maxDistance)) {
            return true;
        }

        return false;
    }

    /**
     * Drop items at a given location.
     *
     * @param location The location to drop the items at
     * @param is The items to drop
     * @param quantity The amount of items to drop
     */
    public static void dropItems(Location location, ItemStack is, int quantity) {
        for (int i = 0; i < quantity; i++) {
            dropItem(location, is);
        }
    }

    /**
     * Randomly drop an item at a given location.
     *
     * @param location The location to drop the items at
     * @param is The item to drop
     * @param chance The percentage chance for the item to drop
     */
    public static void randomDropItem(Location location, ItemStack is, int chance) {
        if (random.nextInt(100) < chance) {
            dropItem(location, is);
        }
    }

    /**
     * Randomly drop items at a given location.
     *
     * @param location The location to drop the items at
     * @param is The item to drop
     * @param chance The percentage chance for the item to drop
     * @param quantity The amount of items to drop
     */
    public static void randomDropItems(Location location, ItemStack is, int quantity) {
        int dropCount = random.nextInt(quantity + 1);

        if (dropCount > 0) {
            is.setAmount(dropCount);
            dropItem(location, is);
        }
    }

    /**
     * Drop an item at a given location.
     *
     * @param location The location to drop the item at
     * @param itemStack The item to drop
     */
    public static void dropItem(Location location, ItemStack itemStack) {

        if (itemStack.getType() == Material.AIR) {
            return;
        }

        // We can't get the item until we spawn it and we want to make it cancellable, so we have a custom event.
        McMMOItemSpawnEvent event = new McMMOItemSpawnEvent(location, itemStack);
        mcMMO.p.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        Item newItem = location.getWorld().dropItemNaturally(location, itemStack);

        ItemStack cloned = itemStack.clone();
        cloned.setAmount(newItem.getItemStack().getAmount());

        newItem.setItemStack(cloned);
    }

    /**
     * Get the max power level for a player.
     *
     * @return the maximum power level for a player
     */
    public static int getPowerLevelCap() {
        int levelCap = Config.getInstance().getPowerLevelCap();

        if (levelCap > 0) {
            return levelCap;
        }

        return Integer.MAX_VALUE;
    }

    public static Random getRandom() {
        return random;
    }
}
