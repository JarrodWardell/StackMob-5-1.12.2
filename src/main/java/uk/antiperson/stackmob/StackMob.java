package uk.antiperson.stackmob;

import org.bstats.bukkit.Metrics;
//import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import uk.antiperson.stackmob.commands.Commands;
import uk.antiperson.stackmob.config.EntityTranslation;
import uk.antiperson.stackmob.config.MainConfig;
import uk.antiperson.stackmob.entity.EntityManager;
import uk.antiperson.stackmob.entity.traits.TraitManager;
import uk.antiperson.stackmob.hook.HookManager;
import uk.antiperson.stackmob.listeners.*;
import uk.antiperson.stackmob.tasks.MergeTask;
import uk.antiperson.stackmob.tasks.TagTask;
import uk.antiperson.stackmob.tasks.SaveTask;
import uk.antiperson.stackmob.utils.ItemTools;
//import uk.antiperson.stackmob.utils.TemporaryCompat;
import uk.antiperson.stackmob.utils.Updater;
import uk.antiperson.stackmob.utils.Utilities;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

public class StackMob extends JavaPlugin {

    private final String stackKey = "stack-size";
    private final String toolKey = "stack-tool";
    private final String saveDirectory = "./plugins/" + this.getName() + "/stackmob.data";

    private MainConfig config;
    private EntityTranslation entityTranslation;
    private TraitManager traitManager;
    private HookManager hookManager;
    private EntityManager entityManager;
    private Updater updater;
    private ItemTools itemTools;

    @Override
    public void onLoad() {
        hookManager = new HookManager(this);
        try {
            hookManager.registerOnLoad();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            getLogger().log(Level.SEVERE, "There was a problem registering hooks. Features won't work.");
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        traitManager = new TraitManager(this);
        entityManager = EntityManager.loadData(saveDirectory, this);

        config = new MainConfig(this);
        entityTranslation = new EntityTranslation(this);
        updater = new Updater(this, 29999);
        itemTools = new ItemTools(this);
        getLogger().info("StackMob v" + getDescription().getVersion() + " by antiPerson and contributors.");
        getLogger().info("GitHub: " + Utilities.GITHUB + " Discord: " + Utilities.DISCORD);
        getLogger().info("Loading config files...");
        try {
            getMainConfig().load();
            getEntityTranslation().load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "There was a problem loading the configuration file.");
            e.printStackTrace();
        }
        getLogger().info("Registering hooks and trait checks...");
        try {
            getHookManager().registerHooks();
            getTraitManager().registerTraits();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        getLogger().info("Registering events, commands and tasks...");
        try {
            registerEvents();
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        PluginCommand command = getCommand("stackmob");
        Commands commands = new Commands(this);
        command.setExecutor(commands);
        command.setTabCompleter(commands);
        commands.registerSubCommands();
        int stackInterval = getMainConfig().getStackInterval();
        new MergeTask(this).runTaskTimer(this, 5, stackInterval);
        int tagInterval = getMainConfig().getTagNearbyInterval();
        new TagTask(this).runTaskTimer(this, 10, tagInterval);
        int saveInterval = getMainConfig().getSaveInterval();
        new SaveTask(this).runTaskTimer(this, 600, saveInterval);
        /*if (!Utilities.isNativeVersion() && getHookManager().getProtocolLibHook() == null) {
            getLogger().warning("You are not running the plugins native version and ProtocolLib could not be found (or has been disabled).");
            getLogger().warning("The display name visibility setting 'NEARBY' will not work unless this is fixed.");
        }*/
        getEntityManager().registerAllEntities();
        getUpdater().checkUpdate().whenComplete(((updateResult, throwable) -> {
            switch (updateResult.getResult()) {
                case NONE: getLogger().info("No update is currently available."); break;
                case ERROR: getLogger().info("There was an error while getting the latest update."); break;
                case AVAILABLE: getLogger().info("A new version is currently available. (" + updateResult.getNewVersion() + ")"); break;
            }
        }));
        /*if (!Utilities.isPaper()) {
            getLogger().warning("It has been detected that you are not using Paper (https://papermc.io).");
            getLogger().warning("StackMob makes use of Paper's API, which means you're missing out on features.");
        }*/
        new Metrics(this, 522);
        /*if (Utilities.isNativeVersion()) {
            if (Utilities.isPaper() && !Bukkit.getVersion().contains("d76f8e0")) {
                return;
            }
            getLogger().info("We are enabling temporary compatibility measures for 1.17.");
            getLogger().info("See https://github.com/Nathat23/StackMob-5/issues/223 for info.");
            TemporaryCompat temporaryCompat = new TemporaryCompat(this);
            temporaryCompat.init();
            temporaryCompat.runTaskTimer(this, 20, 100);
            getServer().getPluginManager().registerEvents(temporaryCompat, this);
        }*/
    }

    @Override
    public void onDisable() {
        getEntityManager().saveData(saveDirectory);
        getEntityManager().unregisterAllEntities();
    }

    private void registerEvents() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        //registerEvent(BucketListener.class);
        registerEvent(DeathListener.class);
        //registerEvent(TransformListener.class);
        registerEvent(BreedInteractListener.class);
        registerEvent(TagInteractListener.class);
        registerEvent(DyeListener.class);
        registerEvent(ShearListener.class);
        registerEvent(ExplosionListener.class);
        //registerEvent(DropListener.class);
        registerEvent(TameListener.class);
        registerEvent(SlimeListener.class);
        registerEvent(SpawnListener.class);
        registerEvent(TargetListener.class);
        registerEvent(PlayerListener.class);
        //registerEvent(LeashListener.class);
        registerEvent(EquipListener.class);
        /*if (Utilities.isPaper()) {
            registerEvent(RemoveListener.class);
            return;
        }*/
        registerEvent(ChunkListener.class);
    }

    private void registerEvent(Class<? extends Listener> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ListenerMetadata listenerMetadata = clazz.getAnnotation(ListenerMetadata.class);
        if (listenerMetadata != null) {
            if (!getMainConfig().isSet(listenerMetadata.config())) {
                return;
            }
            if (!getMainConfig().getBoolean(listenerMetadata.config())) {
                return;
            }
        }
        Listener listener = clazz.getDeclaredConstructor(StackMob.class).newInstance(this);
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public void downloadBridge() {
        getLogger().info("Installing StackMobBridge (utility to convert legacy mob stacks)...");
        File file = new File(getDataFolder().getParent(), "StackMobBridge.jar");
        String bridgeUrl = "http://aqua.api.spiget.org/v2/resources/45495/download";
        Utilities.downloadFile(file, bridgeUrl).whenComplete(((downloadResult, throwable) -> {
            if (downloadResult == Utilities.DownloadResult.ERROR) {
                getLogger().log(Level.SEVERE,"There was an issue while downloading StackMobBridge.");
                getLogger().log(Level.SEVERE, "This means that mob stacks will not be converted to the newer format.");
                return;
            }
            if (getServer().getPluginManager().getPlugin("StackMobBridge") != null) {
                return;
            }
            try {
                Plugin plugin = getPluginLoader().loadPlugin(file);
                getPluginLoader().enablePlugin(plugin);
            } catch (InvalidPluginException e) {
                e.printStackTrace();
            }
        }));
    }

    public EntityTranslation getEntityTranslation() {
        return entityTranslation;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public MainConfig getMainConfig() {
        return config;
    }

    public TraitManager getTraitManager() {
        return traitManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public Updater getUpdater() {
        return updater;
    }

    public String getStackKey() {
        return stackKey;
    }

    public String getToolKey() {
        return toolKey;
    }

    public String getSaveDirectory() {
        return saveDirectory;
    }

    public ItemTools getItemTools() {
        return itemTools;
    }
}
