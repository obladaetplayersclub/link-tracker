package backend.academy.linktracker.scrapper.parser;

import java.time.OffsetDateTime;

public record LinkUpdateInfo(String title, String author, OffsetDateTime createdAt, String preview) {}
