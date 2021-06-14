package com.tagsgroup.simulators.fix;

import java.util.Objects;
import java.util.Properties;

public class ConnectionDetails {
    private String serverField;
    private int portField;
    private String userNameField;
    private String passwordField;
    private String topicInField;
    private String topicOutField;
    private String durableName;


    public ConnectionDetails(String venue, Properties properties) {
        unmarshall(venue, properties);
    }

    public ConnectionDetails(String serverField, int portField, String userNameField, String passwordField, String topicInField, String topicOutField, String durableName) {
        this.serverField = serverField;
        this.portField = portField;
        this.userNameField = userNameField;
        this.passwordField = passwordField;
        this.topicInField = topicInField;
        this.topicOutField = topicOutField;
        this.durableName = durableName;
    }

    public String getServerPort() {
        return serverField + ":" + portField;
    }

    public String getServerField() {
        return serverField;
    }

    public int getPortField() {
        return portField;
    }

    public String getUserNameField() {
        return userNameField;
    }

    public void setServerField(String serverField) {
        this.serverField = serverField;
    }

    public void setPortField(int portField) {
        this.portField = portField;
    }

    public void setUserNameField(String userNameField) {
        this.userNameField = userNameField;
    }

    public void setPasswordField(String passwordField) {
        this.passwordField = passwordField;
    }

    public void setTopicInField(String topicInField) {
        this.topicInField = topicInField;
    }

    public void setTopicOutField(String topicOutField) {
        this.topicOutField = topicOutField;
    }

    public void setDurableName(String durableName) {
        this.durableName = durableName;
    }

    public String getPasswordField() {
        return passwordField;
    }

    public String getTopicInField() {
        return topicInField;
    }

    public String getTopicOutField() {
        return topicOutField;
    }

    public String getDurableName() {
        return durableName;
    }



    public void unmarshall(String venue, Properties properties) {
        serverField = properties.getProperty("connection.server", "<some server name>");
        portField = Integer.parseInt(properties.getProperty("connection.port", "12"));

        userNameField = properties.getProperty("connection.username", "<username>");
        passwordField = properties.getProperty("connection.password", "<password>");

        topicInField = properties.getProperty("connection.receive.topic", "<topic in>");
        topicOutField = properties.getProperty("connection.send.topic", "<topic out>");

        // we need some unique value for this instance.
        durableName = properties.getProperty("connection.durable.name", System.getProperty("user.name") + "." + venue + ".1"); // concept from EMS
    }


    public void marshall(Properties properties) {
        properties.setProperty("connection.server", serverField);
        properties.setProperty("connection.port", "" + portField);
        properties.setProperty("connection.username", userNameField);
        properties.setProperty("connection.password", passwordField);
        properties.setProperty("connection.receive.topic", topicInField);
        properties.setProperty("connection.send.topic", topicOutField);
        properties.setProperty("connection.durable.name", durableName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionDetails that = (ConnectionDetails) o;

        if (portField != that.portField) return false;
        if (serverField != null ? !serverField.equals(that.serverField) : that.serverField != null) return false;
        if (userNameField != null ? !userNameField.equals(that.userNameField) : that.userNameField != null)
            return false;
        if (passwordField != null ? !passwordField.equals(that.passwordField) : that.passwordField != null)
            return false;
        if (topicInField != null ? !topicInField.equals(that.topicInField) : that.topicInField != null) return false;
        if (topicOutField != null ? !topicOutField.equals(that.topicOutField) : that.topicOutField != null)
            return false;
        return durableName != null ? durableName.equals(that.durableName) : that.durableName == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverField, portField, userNameField, passwordField, topicInField, topicOutField, durableName);
    }

    @Override
    public String toString() {
        return serverField + "@" + portField + " IN:" + topicInField +  " OUT:" + topicOutField + " DUR:" + durableName;
    }
}
