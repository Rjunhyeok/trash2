package test.drhealthbar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class DRHealthBar extends JavaPlugin implements CommandExecutor, Listener {
    private HashMap<UUID, Integer> healthMap;

    @Override
    public void onEnable() {
        this.healthMap = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("체력관리").setExecutor(this);
        getServer().getScheduler().runTaskTimer(this, this::updateExpBar, 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, this::reduceHealth, 20L, 20L);
    }

    public void reduceHealth() {
        for (UUID uuid : healthMap.keySet()) {
            int currentHealth = healthMap.get(uuid);
            if (currentHealth > 0) {
                healthMap.put(uuid, currentHealth - 1);
                updateExpBar();
            }
        }
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        healthMap.put(player.getUniqueId(), 20);
    }

    public void updateExpBar() {
        for (UUID uuid : healthMap.keySet()) {
            Player player = getServer().getPlayer(uuid);
            if (player != null) {
                int currentHealth = healthMap.get(uuid);
                float percentage = (float) currentHealth / 20;
                player.setLevel(currentHealth);
                player.setExp(Math.min(1.0f, percentage));
            }
        }
    }



    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c이 명령은 플레이어만 실행할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.isOp()) {
            player.sendMessage("§c이 명령을 실행할 권한이 없습니다.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("체력관리")) {
            if (args.length < 1) {
                player.sendMessage("§e/체력관리 §f(증가/감소/설정/확인) §e(플레이어이름) (숫자)");
                return true;
            }

            String action = args[0];

            if (action.equalsIgnoreCase("증가")) {
                if (args.length != 3) {
                    player.sendMessage("§e/체력관리 증가 (플레이어이름) (숫자)");
                    return true;
                }

                Player targetPlayer = getServer().getPlayer(args[1]);
                if (targetPlayer != null) {
                    int amount = Integer.parseInt(args[2]);
                    int currentHealth = healthMap.get(targetPlayer.getUniqueId());
                    healthMap.put(targetPlayer.getUniqueId(), currentHealth + amount);
                    player.sendMessage(targetPlayer.getName() + "의 체력이 " + amount + "만큼 증가했습니다.");
                } else {
                    player.sendMessage("§c플레이어를 찾을 수 없습니다.");
                }
            } else if (action.equalsIgnoreCase("감소")) {
                if (args.length != 3) {
                    player.sendMessage("§e/체력관리 감소 (플레이어이름) (숫자)");
                    return true;
                }

                Player targetPlayer = getServer().getPlayer(args[1]);
                if (targetPlayer != null) {
                    int amount = Integer.parseInt(args[2]);
                    int currentHealth = healthMap.getOrDefault(targetPlayer.getUniqueId(), 20);
                    int newHealth = Math.max(0, currentHealth - amount);
                    healthMap.put(targetPlayer.getUniqueId(), newHealth);
                    player.sendMessage(targetPlayer.getName() + "의 체력이 " + amount + "만큼 감소했습니다.");
                } else {
                    player.sendMessage("§c플레이어를 찾을 수 없습니다.");
                }
            } else if (action.equalsIgnoreCase("설정")) {
                if (args.length != 3) {
                    player.sendMessage("§e/체력관리 설정 (플레이어이름) (숫자)");
                    return true;
                }

                Player targetPlayer = getServer().getPlayer(args[1]);
                if (targetPlayer != null) {
                    int amount = Integer.parseInt(args[2]);
                    healthMap.put(targetPlayer.getUniqueId(), amount);
                    player.sendMessage(targetPlayer.getName() + "의 체력이 " + amount + "(으)로 설정되었습니다.");
                } else {
                    player.sendMessage("§c플레이어를 찾을 수 없습니다.");
                }
            } else if (action.equalsIgnoreCase("확인")) {
                if (args.length != 2) {
                    player.sendMessage("§e/체력관리 확인 (플레이어이름)");
                    return true;
                }

                Player targetPlayer = getServer().getPlayer(args[1]);
                if (targetPlayer != null) {
                    int currentHealth = healthMap.getOrDefault(targetPlayer.getUniqueId(), 20);
                    player.sendMessage(targetPlayer.getName() + "의 현재 체력: " + currentHealth);
                } else {
                    player.sendMessage("§c플레이어를 찾을 수 없습니다.");
                }
            }
        }

        return true;
    }
}
