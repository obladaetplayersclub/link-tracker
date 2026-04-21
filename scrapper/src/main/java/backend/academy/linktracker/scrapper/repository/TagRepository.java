package backend.academy.linktracker.scrapper.repository;

import java.util.List;

public interface TagRepository {
    void add(String name);

    void remove(String name);

    List<String> findAll();

    boolean exists(String name);

    void addTagToLink(long linkId, String tagName);

    void removeTagFromLink(long linkId, String tagName);

    List<String> findTagsByLinkId(long linkId);
}
