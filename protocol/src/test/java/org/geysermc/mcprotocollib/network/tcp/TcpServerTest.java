package org.geysermc.mcprotocollib.network.tcp;

import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpServerTest {
    @Test
    void bindWithMinusOnePortSelectsOpenPort() {
        var connectionManager = new TcpConnectionManager();
        var server = new TcpServer("localhost", -1, MinecraftProtocol::new, connectionManager);
        assertEquals(0, server.getPort(), "getPort should return 0 before bind is called.");

        try {
            server.bind(true);

            assertTrue(server.isListening(), "Server should be listening.");
            assertTrue(server.getPort() > 0, "Server should bind to a valid positive port.");
        } finally {
            server.close(true);
            connectionManager.close();
        }
    }
}
