package backend.academy.linktracker.scrapper.parser;

public record GitHubParsedLink(String owner, String repo) implements ParsedLink {}
