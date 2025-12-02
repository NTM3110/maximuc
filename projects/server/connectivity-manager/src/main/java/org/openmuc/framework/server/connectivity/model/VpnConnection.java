package org.openmuc.framework.server.connectivity.model;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

/**
 * Represents a VPN connection configuration.
 * Based on strongSwan connection format with metadata support.
 */
public class VpnConnection {

    private String name;
    @SerializedName("category")
    private String category; // "site-to-site", "road-warrior"
    @SerializedName("auth_method")
    private String authMethod; // "ikev2-psk", "ikev2-cert"
    @SerializedName("ike_version")
    private String ikeVersion; // "ikev1", "ikev2", "any"
    @SerializedName("server_address")
    private String serverAddress;
    @SerializedName("remote_address")
    private String remoteAddress;
    @SerializedName("local_identity")
    private String localIdentity;
    private String peerIdentity;
    @SerializedName("remote_identity")
    private String remoteIdentity;
    @SerializedName("pre_shared_key")
    private String preSharedKey;
    @SerializedName("local_traffic_selector")
    private String localTrafficSelector;
    @SerializedName("remote_traffic_selector")
    private String remoteTrafficSelector;
    private String startAction; // "none", "trap", "start"
    @SerializedName("server_certificate_name")
    private String serverCertificateName;
    @SerializedName("ca_certificate_name")
    private String caCertificateName;
    private boolean activeInitiator;
    private boolean autoCASelect;
    private boolean useServerValue;
    private String status; // IDLE, CONNECTING, ESTABLISHED, FAILED

    // Default constructor
    public VpnConnection() {
        this.status = ConnectionStatus.IDLE;
        this.ikeVersion = "any";
        this.startAction = "none";
        this.activeInitiator = false;
        this.autoCASelect = false;
        this.useServerValue = false;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getIkeVersion() {
        return ikeVersion;
    }

    public void setIkeVersion(String ikeVersion) {
        this.ikeVersion = ikeVersion;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getLocalIdentity() {
        return localIdentity;
    }

    public void setLocalIdentity(String localIdentity) {
        this.localIdentity = localIdentity;
    }

    public String getPeerIdentity() {
        return peerIdentity;
    }

    public void setPeerIdentity(String peerIdentity) {
        this.peerIdentity = peerIdentity;
    }

    public String getRemoteIdentity() {
        return remoteIdentity;
    }

    public void setRemoteIdentity(String remoteIdentity) {
        this.remoteIdentity = remoteIdentity;
    }

    public String getPreSharedKey() {
        return preSharedKey;
    }

    public void setPreSharedKey(String preSharedKey) {
        this.preSharedKey = preSharedKey;
    }

    public String getLocalTrafficSelector() {
        return localTrafficSelector;
    }

    public void setLocalTrafficSelector(String localTrafficSelector) {
        this.localTrafficSelector = localTrafficSelector;
    }

    public String getRemoteTrafficSelector() {
        return remoteTrafficSelector;
    }

    public void setRemoteTrafficSelector(String remoteTrafficSelector) {
        this.remoteTrafficSelector = remoteTrafficSelector;
    }

    public String getStartAction() {
        return startAction;
    }

    public void setStartAction(String startAction) {
        this.startAction = startAction;
    }

    public String getServerCertificateName() {
        return serverCertificateName;
    }

    public void setServerCertificateName(String serverCertificateName) {
        this.serverCertificateName = serverCertificateName;
    }

    public String getCaCertificateName() {
        return caCertificateName;
    }

    public void setCaCertificateName(String caCertificateName) {
        this.caCertificateName = caCertificateName;
    }

    public boolean isActiveInitiator() {
        return activeInitiator;
    }

    public void setActiveInitiator(boolean activeInitiator) {
        this.activeInitiator = activeInitiator;
    }

    public boolean isAutoCASelect() {
        return autoCASelect;
    }

    public void setAutoCASelect(boolean autoCASelect) {
        this.autoCASelect = autoCASelect;
    }

    public boolean isUseServerValue() {
        return useServerValue;
    }

    public void setUseServerValue(boolean useServerValue) {
        this.useServerValue = useServerValue;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VpnConnection that = (VpnConnection) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "VpnConnection{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", authMethod='" + authMethod + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", status=" + status +
                '}';
    }
}
