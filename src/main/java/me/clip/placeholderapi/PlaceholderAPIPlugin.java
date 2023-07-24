/*
 * This file is part of PlaceholderAPI
 *
 * PlaceholderAPI
 * Copyright (c) 2015 - 2021 PlaceholderAPI Team
 *
 * PlaceholderAPI free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlaceholderAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.clip.placeholderapi;

import me.clip.placeholderapi.expansion.Version;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import me.clip.placeholderapi.listeners.ServerLoadEventListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

/**
 * Yes I have a shit load of work to do...
 *
 * @author Ryan McCarthy
 */
public final class PlaceholderAPIPlugin extends JavaPlugin {

  @NotNull
  private static final Version VERSION;
  private static PlaceholderAPIPlugin instance;

  static {
    final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    boolean isSpigot;
    try {
      Class.forName("org.spigotmc.SpigotConfig");
      isSpigot = true;
    } catch (final ExceptionInInitializerError | ClassNotFoundException ignored) {
      isSpigot = false;
    }

    VERSION = new Version(version, isSpigot);
  }

  @NotNull
  private final LocalExpansionManager localExpansionManager = new LocalExpansionManager(this);

  /**
   * Gets the static instance of the main class for PlaceholderAPI. This class is not the actual API
   * class, this is the main class that extends JavaPlugin. For most API methods, use static methods
   * available from the class: {@link PlaceholderAPI}
   *
   * @return PlaceholderAPIPlugin instance
   */
  @NotNull
  public static PlaceholderAPIPlugin getInstance() {
    return instance;
  }

  /**
   * Get the configurable {@linkplain String} value that should be returned when a boolean is true
   *
   * @return string value of true
   */
  @NotNull
  public static String booleanTrue() {
    return "yes";
  }

  /**
   * Get the configurable {@linkplain String} value that should be returned when a boolean is false
   *
   * @return string value of false
   */
  @NotNull
  public static String booleanFalse() {
    return "no";
  }

  /**
   * Get the configurable {@linkplain SimpleDateFormat} object that is used to parse time for
   * generic time based placeholders
   *
   * @return date format
   */
  @NotNull
  public static SimpleDateFormat getDateFormat() {
    return new SimpleDateFormat("MM/dd/yy HH:mm:ss");
  }

  public static Version getServerVersion() {
    return VERSION;
  }

  @Override
  public void onLoad() {
    instance = this;

    saveDefaultConfig();
  }

  @Override
  public void onEnable() {
    setupExpansions();
  }

  @Override
  public void onDisable() {
    getLocalExpansionManager().kill();

    HandlerList.unregisterAll(this);

    Bukkit.getScheduler().cancelTasks(this);

    instance = null;
  }

  public void reloadConf(@NotNull final CommandSender sender) {
    getLocalExpansionManager().kill();

    reloadConfig();

    getLocalExpansionManager().load(sender);
  }

  @NotNull
  public LocalExpansionManager getLocalExpansionManager() {
    return localExpansionManager;
  }

  private void setupExpansions() {
    Bukkit.getPluginManager().registerEvents(getLocalExpansionManager(), this);

    try {
      Class.forName("org.bukkit.event.server.ServerLoadEvent");
      new ServerLoadEventListener(this);
    } catch (final ClassNotFoundException ignored) {
      Bukkit.getScheduler()
          .runTaskLater(this, () -> getLocalExpansionManager().load(Bukkit.getConsoleSender()), 1);
    }
  }

}
