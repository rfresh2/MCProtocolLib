package org.geysermc.mcprotocollib.protocol;

/**
 * Returns the current chunk section count
 *
 * This data is only knowable by tracking dimension registry data sent to the client and updating on login/respawn packets
 *
 * There is no default provider for this data, i'm opting to implement it directly in ZenithProxy without any fallback, although
 * it's possible to implement in MCPL with some extra state tracking
 *
 * This isn't strictly required, as seen in upstream MCPL, but it allows us to avoid an extra memory copy when deserializing chunk data
 */
@FunctionalInterface
public interface ChunkSectionCountProvider {
    int getSectionCount();
}
