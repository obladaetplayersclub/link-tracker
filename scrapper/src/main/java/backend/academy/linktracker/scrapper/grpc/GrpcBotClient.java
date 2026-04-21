package backend.academy.linktracker.scrapper.grpc;

import backend.academy.linktracker.scrapper.client.BotNotifier;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.grpc.generated.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.grpc.generated.LinkUpdateServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GrpcBotClient implements BotNotifier {
    private final LinkUpdateServiceGrpc.LinkUpdateServiceBlockingStub stub;

    @Override
    public void sendUpdate(LinkUpdate linkUpdate) {
        log.atInfo().addKeyValue("link_url", linkUpdate.url()).log("Отправка gRPC-обновления в бот");

        LinkUpdateRequest request = LinkUpdateRequest.newBuilder()
                .setId(linkUpdate.id())
                .setUrl(linkUpdate.url().toString())
                .setDescription(linkUpdate.description())
                .addAllTgChatIds(linkUpdate.tgChatIds())
                .build();

        stub.sendUpdate(request);
    }
}
