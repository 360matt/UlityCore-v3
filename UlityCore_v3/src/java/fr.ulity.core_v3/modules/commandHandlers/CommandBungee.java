package fr.ulity.core_v3.modules.commandHandlers;

import fr.ulity.core_v3.modules.commandHandlers.bukkit.Status;
import fr.ulity.core_v3.modules.commandHandlers.bungee.ArgBungee;
import fr.ulity.core_v3.modules.datas.UserCooldown;
import fr.ulity.core_v3.modules.language.Lang;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CommandBungee extends Command {
    private final List<DivisionBungee> divisions = new ArrayList<>();

    public CommandSender sender;
    public Command cmd;
    public String[] args;
    public ArgBungee arg;
    public UserCooldown cooldown;
    public Status status = Status.SUCCESS;

    public CommandBungee(String name) {
        super(name);
    }

    public boolean execDivision (CommandSender sender, String[] args) {
        AtomicBoolean stat = new AtomicBoolean(false);

        divisions.forEach(x -> {
            int count = 0;
            for (String y : args) {
                if (x.requiredArgs.containsKey(count)) {
                    if (!x.requiredArgs.get(count).contains(y))
                        break;
                    count++;
                    if (args.length >= count) {
                        stat.set(true);
                        x.execute(sender, args);
                    }
                } else break;
            }
        });
        return stat.get();
    }

    public abstract void exec(CommandSender sender, String[] args);

    public void execute(CommandSender sender, String[] args) {
        this.sender = sender;
        this.cmd = this;
        this.args = args;
        this.arg = new ArgBungee(sender, this.cmd, args);
        this.cooldown = new UserCooldown(sender.getName(), getName());

        if (!execDivision(sender, args)) {
            exec(sender, args);
            operateStatus();
        }
    }

    public boolean isPlayer() {
        return this.sender instanceof ProxiedPlayer;
    }

    public ProxiedPlayer getPlayer() {
        return (ProxiedPlayer) this.sender;
    }

    public boolean requirePlayer() {
        if (isPlayer())
            return true;
        this.status = Status.PLAYER_ONLY;
        return false;
    }

    public boolean hasPermission(String permission) {
        return this.sender.hasPermission(permission);
    }

    public boolean requirePermission(String permission) {
        if (hasPermission(permission))
            return true;
        this.status = Status.NOPERM;
        return false;
    }

    private void operateStatus() {
        if (this.status.equals(Status.PLAYER_ONLY)) {
            Lang.prepare("global.player_only").sendPlayer(this.sender);
        } else if (this.status.equals(Status.NOPERM)) {
            Lang.prepare("global.no_perm").sendPlayer(this.sender);
        }
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
