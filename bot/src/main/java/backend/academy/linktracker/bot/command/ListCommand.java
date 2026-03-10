package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.dto.LinkResponse;
import backend.academy.linktracker.bot.client.dto.ListLinksResponse;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListCommand implements Command {
    private final ScrapperClient scrapperClient;

    @Override
    public String command() {
        return "/list";
    }

    @Override
    public String description() {
        return "Показать список отслеживаемых ссылок";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        String text = update.message().text().trim();

        ListLinksResponse response = scrapperClient.getLinks(chatId);

        if (response.links() == null || response.links().isEmpty()) {
            return new SendMessage(chatId, "Нет отслеживаемых ссылок");
        }

        String tag = null;
        if (text.length() > "/list".length()) {
            tag = text.substring("/list".length()).trim();
        }

        StringBuilder sb = new StringBuilder("Отслеживаемые ссылки:\n");
        for (LinkResponse link : response.links()) {
            if (tag != null
                    && !tag.isEmpty()
                    && (link.tags() == null || !link.tags().contains(tag))) {
                continue;
            }
            sb.append("• ").append(link.url());
            if (link.tags() != null && !link.tags().isEmpty()) {
                sb.append(" [").append(String.join(", ", link.tags())).append("]");
            }
            sb.append("\n");
        }

        String result = sb.toString().trim();
        if (result.equals("Отслеживаемые ссылки:")) {
            return new SendMessage(chatId, "Нет ссылок с тегом: " + tag);
        }

        return new SendMessage(chatId, result);
    }
}
