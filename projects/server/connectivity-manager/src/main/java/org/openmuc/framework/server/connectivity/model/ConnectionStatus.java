package org.openmuc.framework.server.connectivity.model;

/**
 * Represents the status of a VPN connection.
 */
public enum ConnectionStatus {
    IDLE, // Connection configured but not active
    CONNECTING, // Connection is being established
    ESTABLISHED, // Connection is active and established
    FAILED, // Connection attempt failed
    UNKNOWN // Status could not be determined
}
