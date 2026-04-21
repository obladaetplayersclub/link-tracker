package backend.academy.linktracker.scrapper.parser;

public sealed interface ParsedLink permits GitHubParsedLink, StackOverflowParsedLink {}
