package backend.academy.linktracker.scrapper.grpc;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.grpc.generated.AddLinkRequest;
import backend.academy.linktracker.scrapper.grpc.generated.LinkResponse;
import backend.academy.linktracker.scrapper.grpc.generated.LinksRequest;
import backend.academy.linktracker.scrapper.grpc.generated.LinksServiceGrpc;
import backend.academy.linktracker.scrapper.grpc.generated.ListLinksResponse;
import backend.academy.linktracker.scrapper.grpc.generated.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.LinkService;
import io.grpc.stub.StreamObserver;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.transport", havingValue = "grpc")
public class GrpcLinksService extends LinksServiceGrpc.LinksServiceImplBase {
    private final LinkService linkService;

    @Override
    public void getLinks(LinksRequest request, StreamObserver<ListLinksResponse> responseObserver) {
        log.atInfo().addKeyValue("chat_id", request.getTgChatId()).log("gRPC: Получение списка ссылок");
        List<Link> links = linkService.findAllByChatId(request.getTgChatId());
        List<LinkResponse> responses = links.stream().map(this::toLinkResponse).toList();
        responseObserver.onNext(ListLinksResponse.newBuilder()
                .addAllLinks(responses)
                .setSize(responses.size())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void addLink(AddLinkRequest request, StreamObserver<LinkResponse> responseObserver) {
        log.atInfo()
                .addKeyValue("chat_id", request.getTgChatId())
                .addKeyValue("link_url", request.getLink())
                .log("gRPC: Добавление ссылки");
        Link link = linkService.add(request.getTgChatId(), URI.create(request.getLink()), request.getTagsList());
        responseObserver.onNext(toLinkResponse(link));
        responseObserver.onCompleted();
    }

    @Override
    public void removeLink(RemoveLinkRequest request, StreamObserver<LinkResponse> responseObserver) {
        log.atInfo()
                .addKeyValue("chat_id", request.getTgChatId())
                .addKeyValue("link_url", request.getLink())
                .log("gRPC: Удаление ссылки");
        Link link = linkService.remove(request.getTgChatId(), URI.create(request.getLink()));
        responseObserver.onNext(toLinkResponse(link));
        responseObserver.onCompleted();
    }

    private LinkResponse toLinkResponse(Link link) {
        return LinkResponse.newBuilder()
                .setId(link.getId())
                .setUrl(link.getUrl().toString())
                .addAllTags(link.getTags() != null ? link.getTags() : List.of())
                .build();
    }
}
