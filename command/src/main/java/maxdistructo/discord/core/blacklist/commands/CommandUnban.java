package maxdistructo.discord.core.blacklist.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import maxdistructo.discord.core.blacklist.Blacklist;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class CommandUnban extends Command {
    public CommandUnban() {
        this.help = "unban <@User> | Unbans a user";
        this.name = "unban";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        Member mentioned = getMentionedUser(event.getMessage());
        Blacklist.unbanUser(mentioned.getIdLong());
        event.reply("User: " + mentioned.getAsMention() + " has been unbanned.");
    }

    private Member getMentionedUser(Message message) {
        List<Member> mentionedList = message.getMentionedMembers();
        Member mentioned;
        if (!mentionedList.isEmpty()) {
            mentioned = mentionedList.get(0);
        } else {
            mentioned = null;
        }
        return mentioned;
    }
}
