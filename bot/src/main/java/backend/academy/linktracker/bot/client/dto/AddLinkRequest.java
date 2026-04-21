package backend.academy.linktracker.bot.client.dto;

import java.net.URI;
import java.util.List;

public record AddLinkRequest(URI link, List<String> tags) {}
