package backend.academy.linktracker.bot.grpc;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.grpc.generated.LinkUpdateRequest;
import backend.academy.linktracker.bot.grpc.generated.LinkUpdateResponse;
import backend.academy.linktracker.bot.grpc.generated.LinkUpdateServiceGrpc;
import backend.academy.linktracker.bot.service.NotificationService;
import io.grpc.stub.StreamObserver;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.transport", havingValue = "grpc")
public class GrpcLinkUpdateService extends LinkUpdateServiceGrpc.LinkUpdateServiceImplBase {
    private final NotificationService notificationService;

    @Override
    public void sendUpdate(LinkUpdateRequest request, StreamObserver<LinkUpdateResponse> responseObserver) {
        log.atInfo()
                .addKeyValue("link_url", request.getUrl())
                .addKeyValue("chat_ids_count", request.getTgChatIdsList().size())
                .log("Получено gRPC-обновление от Scrapper");

        LinkUpdate linkUpdate = new LinkUpdate(
                request.getId(), URI.create(request.getUrl()), request.getDescription(), request.getTgChatIdsList());

        notificationService.sendUpdate(linkUpdate);

        responseObserver.onNext(LinkUpdateResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
